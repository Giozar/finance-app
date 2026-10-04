-- ======================================================
-- MIGRACIÓN 2026-10-04: RECONCILIACIÓN DE CUENTAS
-- ======================================================
-- Requisito: la BD ya tiene aplicadas, en orden:
--   1. 2026-10-03_reallocation.sql
--   2. 2026-10-03_schema_consistency.sql
--   3. 2026-10-03_transaction_integrity.sql
--
-- Cambios:
--   1. Columnas nuevas:
--        accounts.opening_balance           DECIMAL(12, 2) NOT NULL DEFAULT 0.00
--        credit_details.opening_credit_used DECIMAL(12, 2) NOT NULL DEFAULT 0.00
--          + CHECK chk_opening_credit_used (>= 0)
--   2. Triggers nuevos (fijan la apertura al insertar; backend/client no los envían):
--        9   tr_before_account_insert_opening        (BEFORE INSERT accounts)
--        9.1 tr_before_credit_details_insert_opening (BEFORE INSERT credit_details)
--   3. Vista nueva v_account_reconciliation (una fila por cuenta: opening_net, total_inflows,
--      total_outflows, expected_net, actual_net, difference).
--   4. Procedimiento nuevo sp_reconcile_account(p_account_id): ajusta la cuenta para que
--      difference = 0.
--   5. Línea base (paso 4) para las cuentas existentes.
--
-- IMPORTANTE (paso 4): el saldo inicial real de las cuentas existentes NO se conoce. La
-- migración ACEPTA EL ESTADO ACTUAL COMO CORRECTO: calcula los valores de apertura para que
-- la diferencia de cada cuenta sea 0 hoy:
--     opening_net = actual_net - total_inflows + total_outflows
--   no CREDIT (o CREDIT sin fila en credit_details): opening_balance = opening_net
--   CREDIT con credit_details: opening_net >= 0 -> opening_balance = opening_net, opening_credit_used = 0
--                              opening_net <  0 -> opening_balance = 0, opening_credit_used = -opening_net
-- Así, cualquier descuadre ya existente queda absorbido en la apertura y solo se detectarán
-- los que aparezcan a partir de ahora. Si conoces el saldo inicial real de una cuenta, puedes
-- corregirlo a mano, p. ej.:
--     UPDATE accounts SET opening_balance = 1500.00 WHERE id = 42;
--     UPDATE credit_details SET opening_credit_used = 0.00 WHERE account_id = 42;
-- y luego revisar v_account_reconciliation (y, si procede, CALL sp_reconcile_account(42)).
--
-- Uso: SOLO para bases de datos con datos existentes (no borra datos).
--   mysql -u root -p < database/migrations/2026-10-04_account_reconciliation.sql
-- Para instalaciones nuevas basta con ejecutar database/schemas.sql.
-- Requiere MySQL 8.0.16+ (CHECK aplicados).
--
-- Recomendación: respaldar antes (mysqldump finanzas > respaldo.sql) y ejecutar con la
-- aplicación detenida (si se registran transacciones durante el paso 4, la línea base
-- puede quedar desfasada).
-- Ejecutar UNA sola vez: no es idempotente (el ADD COLUMN del paso 1 falla si las columnas
-- ya existen y el cliente mysql se detiene ahí).
-- Nota: el paso 4 solo toca opening_balance / opening_credit_used (current_balance no cambia,
-- así que el trigger 6 no se dispara) y conserva updated_at.
-- ======================================================

-- ======================================================
-- 0. DIAGNÓSTICO PREVIO (opcional, ejecutar a mano)
-- ======================================================
-- Cuentas CREDIT sin fila en credit_details: sus cargos (como origen o tarjeta vinculada de
-- wallet) nunca afectaron saldos y la vista los excluye igual que los procedimientos; su
-- apertura se guarda solo en opening_balance (puede quedar negativa):
--
-- SELECT a.id, a.user_id, a.name, a.current_balance
--   FROM accounts a LEFT JOIN credit_details cd ON cd.account_id = a.id
--   WHERE a.type = 'CREDIT' AND cd.account_id IS NULL;
-- ======================================================

USE finanzas;

SET GLOBAL log_bin_trust_function_creators = 1;

-- Los UPDATE masivos no filtran por clave (Workbench los bloquea en modo seguro)
SET SQL_SAFE_UPDATES = 0;

-- ======================================================
-- 1. COLUMNAS DE APERTURA
-- ======================================================
ALTER TABLE accounts
    ADD COLUMN opening_balance DECIMAL(12, 2) NOT NULL DEFAULT 0.00 AFTER current_balance;

ALTER TABLE credit_details
    ADD COLUMN opening_credit_used DECIMAL(12, 2) NOT NULL DEFAULT 0.00 AFTER credit_used,
    ADD CONSTRAINT chk_opening_credit_used CHECK (opening_credit_used >= 0);

-- ======================================================
-- 2. TRIGGERS DE APERTURA (copiados de schemas.sql)
-- ======================================================
-- ======================================================
-- TRIGGERS DE APERTURA (9 y 9.1)
-- ======================================================
-- Únicos BEFORE INSERT sobre accounts y credit_details (no requieren FOLLOWS).
DELIMITER //

-- ======================================================
-- TRIGGER 9: Fijar el saldo de apertura de la cuenta
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_account_insert_opening //

CREATE TRIGGER tr_before_account_insert_opening
BEFORE INSERT ON accounts
FOR EACH ROW
BEGIN
    SET NEW.opening_balance = NEW.current_balance;
END //

-- ======================================================
-- TRIGGER 9.1: Fijar la deuda de apertura del crédito
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_credit_details_insert_opening //

CREATE TRIGGER tr_before_credit_details_insert_opening
BEFORE INSERT ON credit_details
FOR EACH ROW
BEGIN
    SET NEW.opening_credit_used = NEW.credit_used;
END //

DELIMITER ;

-- ======================================================
-- 3. VISTA (copiada de schemas.sql; se crea antes de la línea base porque el paso 4 la usa)
-- ======================================================
-- ======================================================
-- VISTA: v_account_reconciliation (una fila por cuenta)
-- ======================================================
-- Replica las reglas de sp_apply_transaction_effect y sp_wallet_detail_effect:
--   Entradas (total_inflows): transacciones COMPLETED, payment_method <> 'WALLET',
--     destination_account_id = cuenta y operation_type IN ('INCOME', 'REALLOCATION').
--     (En destino CREDIT el pago reduce credit_used y el sobrepago va a current_balance:
--     la posición neta sube el monto completo en ambos casos.)
--   Salidas (total_outflows):
--     a) transacciones COMPLETED, payment_method <> 'WALLET', source_account_id = cuenta
--        y operation_type IN ('EXPENSE', 'REALLOCATION');
--     b) detalles de wallet WALLET_BALANCE (padre COMPLETED) -> salida de wallet_account_id;
--     c) detalles de wallet LINKED_CARD con card_id no nulo (padre COMPLETED) -> salida de
--        cards.account_id.
--     Los detalles de wallet cuentan sin importar el payment_method del padre (igual que
--     los triggers 5, 5.1, 5.2, 3 y 4.1).
--   Excepción (igual que los procedimientos): en una cuenta CREDIT SIN fila en
--     credit_details, los procedimientos actualizan credit_details (0 filas) y no tienen
--     efecto, así que a) y c) no cuentan para ella. b) sí cuenta (actúa en current_balance).
-- expected_net = opening_net + total_inflows - total_outflows
-- difference   = actual_net - expected_net (0 = cuenta cuadrada)
-- Usa subconsultas escalares correlacionadas (no joins que multipliquen filas). Todos los
-- importes se exponen como DECIMAL(14, 2).
CREATE OR REPLACE VIEW v_account_reconciliation AS
SELECT
    r.account_id,
    r.user_id,
    r.account_name,
    r.account_type,
    r.opening_net,
    r.total_inflows,
    r.total_outflows,
    CAST(r.opening_net + r.total_inflows - r.total_outflows AS DECIMAL(14, 2)) AS expected_net,
    r.actual_net,
    CAST(r.actual_net - (r.opening_net + r.total_inflows - r.total_outflows) AS DECIMAL(14, 2)) AS difference
FROM (
    SELECT
        a.id AS account_id,
        a.user_id AS user_id,
        a.name AS account_name,
        a.type AS account_type,
        CAST(a.opening_balance - COALESCE(cd.opening_credit_used, 0) AS DECIMAL(14, 2)) AS opening_net,
        CAST(a.current_balance - COALESCE(cd.credit_used, 0) AS DECIMAL(14, 2)) AS actual_net,
        CAST(COALESCE((
            SELECT SUM(t.amount)
            FROM transactions t
            WHERE t.destination_account_id = a.id
              AND t.status = 'COMPLETED'
              AND t.payment_method <> 'WALLET'
              AND t.operation_type IN ('INCOME', 'REALLOCATION')
        ), 0) AS DECIMAL(14, 2)) AS total_inflows,
        CAST(
            -- a) Transacciones con la cuenta como origen
            (CASE WHEN a.type = 'CREDIT' AND cd.account_id IS NULL THEN 0 ELSE COALESCE((
                SELECT SUM(t.amount)
                FROM transactions t
                WHERE t.source_account_id = a.id
                  AND t.status = 'COMPLETED'
                  AND t.payment_method <> 'WALLET'
                  AND t.operation_type IN ('EXPENSE', 'REALLOCATION')
            ), 0) END)
            -- b) Pagos con saldo de la wallet
            + COALESCE((
                SELECT SUM(w.amount)
                FROM wallet_transaction_details w
                INNER JOIN transactions t ON t.id = w.transaction_id
                WHERE w.wallet_account_id = a.id
                  AND w.source_type = 'WALLET_BALANCE'
                  AND t.status = 'COMPLETED'
            ), 0)
            -- c) Pagos de wallet con una tarjeta de esta cuenta
            + (CASE WHEN a.type = 'CREDIT' AND cd.account_id IS NULL THEN 0 ELSE COALESCE((
                SELECT SUM(w.amount)
                FROM wallet_transaction_details w
                INNER JOIN cards c ON c.id = w.card_id
                INNER JOIN transactions t ON t.id = w.transaction_id
                WHERE c.account_id = a.id
                  AND w.source_type = 'LINKED_CARD'
                  AND t.status = 'COMPLETED'
            ), 0) END)
        AS DECIMAL(14, 2)) AS total_outflows
    FROM accounts a
    LEFT JOIN credit_details cd ON cd.account_id = a.id
) r;

-- ======================================================
-- 4. LÍNEA BASE: el estado actual pasa a ser la apertura
-- ======================================================
-- opening_net requerido = actual_net - total_inflows + total_outflows (no depende de los
-- valores de apertura actuales). Se materializa en una tabla temporal para no actualizar
-- accounts / credit_details mientras se leen a través de la vista.
DROP TEMPORARY TABLE IF EXISTS tmp_reconciliation_baseline;

CREATE TEMPORARY TABLE tmp_reconciliation_baseline AS
SELECT
    v.account_id,
    v.account_type,
    CAST(v.actual_net - v.total_inflows + v.total_outflows AS DECIMAL(14, 2)) AS opening_net,
    (cd.account_id IS NOT NULL) AS has_credit_details
FROM v_account_reconciliation v
LEFT JOIN credit_details cd ON cd.account_id = v.account_id;

START TRANSACTION;

-- updated_at = updated_at evita que ON UPDATE CURRENT_TIMESTAMP marque todas las cuentas
UPDATE accounts a
INNER JOIN tmp_reconciliation_baseline b ON b.account_id = a.id
SET a.opening_balance = CASE
        WHEN b.account_type = 'CREDIT' AND b.has_credit_details AND b.opening_net < 0 THEN 0.00
        ELSE b.opening_net
    END,
    a.updated_at = a.updated_at;

UPDATE credit_details cd
INNER JOIN tmp_reconciliation_baseline b ON b.account_id = cd.account_id
SET cd.opening_credit_used = CASE
        WHEN b.account_type = 'CREDIT' AND b.opening_net < 0 THEN -b.opening_net
        ELSE 0.00
    END,
    cd.updated_at = cd.updated_at;

COMMIT;

DROP TEMPORARY TABLE IF EXISTS tmp_reconciliation_baseline;

-- ======================================================
-- 5. PROCEDIMIENTO DE RECONCILIACIÓN (copiado de schemas.sql)
-- ======================================================
-- ======================================================
-- PROCEDIMIENTO 8: Reconciliar una cuenta (difference = 0)
-- ======================================================
-- Lee expected_net de v_account_reconciliation y ajusta la cuenta:
--   no CREDIT -> current_balance = expected_net (se calcula como current_balance - difference,
--                que es lo mismo y además cuadra si hubiera una fila anómala en credit_details)
--   CREDIT    -> expected_net >= 0: credit_used = 0, current_balance = expected_net
--                expected_net <  0: credit_used = -expected_net, current_balance = 0
--                (misma regla de sobrepago que sp_apply_transaction_effect).
--                SIGNAL si la deuda supera credit_limit o si no hay fila en credit_details.
-- SIGNAL si la cuenta no existe. users.global_balance lo actualiza el trigger 6.
-- No abre transacción: si se quiere atomicidad con otras operaciones, envolver la llamada.
DELIMITER //

DROP PROCEDURE IF EXISTS sp_reconcile_account //

CREATE PROCEDURE sp_reconcile_account(
    IN p_account_id BIGINT
)
BEGIN
    DECLARE v_account_type VARCHAR(20) DEFAULT NULL;
    DECLARE v_expected_net DECIMAL(14, 2) DEFAULT 0.00;
    DECLARE v_difference DECIMAL(14, 2) DEFAULT 0.00;
    DECLARE v_has_credit_details BOOLEAN DEFAULT FALSE;
    DECLARE v_credit_limit DECIMAL(12, 2) DEFAULT 0.00;

    SELECT type INTO v_account_type FROM accounts WHERE id = p_account_id;

    IF v_account_type IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: La cuenta a reconciliar no existe.';
    END IF;

    SELECT expected_net, difference INTO v_expected_net, v_difference
    FROM v_account_reconciliation
    WHERE account_id = p_account_id;

    IF v_account_type <> 'CREDIT' THEN
        UPDATE accounts SET current_balance = current_balance - v_difference
        WHERE id = p_account_id;
    ELSE
        SELECT TRUE, credit_limit INTO v_has_credit_details, v_credit_limit
        FROM credit_details WHERE account_id = p_account_id;

        IF v_expected_net >= 0 THEN
            -- Sin deuda: la posición positiva es saldo a favor
            IF v_has_credit_details THEN
                UPDATE credit_details SET credit_used = 0
                WHERE account_id = p_account_id;
            END IF;
            UPDATE accounts SET current_balance = v_expected_net
            WHERE id = p_account_id;
        ELSE
            -- Con deuda: va a credit_used y el saldo a favor queda en 0
            IF NOT v_has_credit_details THEN
                SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Error: La cuenta de crédito no tiene detalle de crédito; no se puede registrar la deuda.';
            END IF;

            IF -v_expected_net > v_credit_limit THEN
                SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Error: La deuda calculada supera el límite de crédito; no se puede reconciliar la cuenta.';
            END IF;

            UPDATE credit_details SET credit_used = -v_expected_net
            WHERE account_id = p_account_id;
            UPDATE accounts SET current_balance = 0
            WHERE id = p_account_id;
        END IF;
    END IF;
END //

DELIMITER ;

-- ======================================================
-- 6. VERIFICACIÓN FINAL
-- ======================================================
-- Debe devolver 0 filas: tras la línea base todas las cuentas cuadran.
SELECT account_id, user_id, account_name, account_type,
       opening_net, total_inflows, total_outflows, expected_net, actual_net, difference
FROM v_account_reconciliation
WHERE difference <> 0;

SET SQL_SAFE_UPDATES = 1;
