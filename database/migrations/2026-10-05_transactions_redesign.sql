-- ======================================================
-- MIGRACIÓN 2026-10-05: REDISEÑO DE TRANSACTIONS (reglas estructurales)
-- ======================================================
-- Requisito: la BD ya tiene aplicadas, en orden:
--   1. 2026-10-03_reallocation.sql
--   2. 2026-10-03_schema_consistency.sql
--   3. 2026-10-03_transaction_integrity.sql
--   4. 2026-10-04_account_reconciliation.sql
--
-- Cambios (no tocan saldos ni datos; la vista v_account_reconciliation y
-- sp_reconcile_account NO cambian):
--   1. transactions: CHECK chk_tx_wallet_expense (WALLET solo con EXPENSE).
--   2. transactions: índices idx_tx_category (category_id) e idx_tx_parent
--      (parent_transaction_id). Sustituyen a los índices implícitos de fk_tx_category y
--      fk_tx_parent (MySQL los elimina solo al existir uno equivalente).
--   3. categories: chk_category_type admite 'REALLOCATION'
--      (INCOME, EXPENSE, REALLOCATION, BOTH).
--   4. Procedimientos:
--        sp_validate_wallet_detail (7, CAMBIA DE FIRMA: p_transaction_id, p_source_type,
--          p_wallet_account_id, p_card_id): padre WALLET, cuenta de tipo WALLET y, en
--          LINKED_CARD, card_id obligatorio y vinculado a la wallet (wallet_card_links).
--        sp_validate_transaction_parties (9, nuevo): origen / destino / entidad externa
--          según operation_type.
--        sp_validate_card_detail (10, nuevo): padre CARD y tarjeta de la cuenta origen.
--   5. Triggers:
--        5.3 tr_before_wallet_detail_insert_val  (recreado: nueva firma)
--        5.4 tr_before_wallet_detail_update_val  (recreado: nueva firma; ahora también valida
--            si cambia transaction_id o wallet_account_id)
--        8.2 tr_before_transaction_insert_parties_check (nuevo, FOLLOWS 8)
--        8.3 tr_before_transaction_update_parties_check (nuevo, FOLLOWS 8.1; valida siempre)
--        10   tr_before_card_detail_insert_val (nuevo, BEFORE INSERT card_transaction_details)
--        10.1 tr_before_card_detail_update_val (nuevo, BEFORE UPDATE card_transaction_details)
--
-- Uso: SOLO para bases de datos con datos existentes (no borra datos).
--   mysql -u root -p < database/migrations/2026-10-05_transactions_redesign.sql
-- Para instalaciones nuevas basta con ejecutar database/schemas.sql.
-- Requiere MySQL 8.0.19+ (DROP CHECK en el paso 3).
--
-- Recomendación: respaldar antes (mysqldump finanzas > respaldo.sql), ejecutar con la
-- aplicación detenida y revisar ANTES el diagnóstico del paso 0.
-- Ejecutar UNA sola vez: no es idempotente (el ADD CONSTRAINT del paso 1 y los CREATE INDEX
-- del paso 2 fallan en una segunda ejecución y el cliente mysql se detiene ahí). El paso 1
-- va primero a propósito: si hay datos que lo violan, la migración se corta sin haber
-- aplicado nada.
-- ======================================================

-- ======================================================
-- 0. DIAGNÓSTICO PREVIO (ejecutar a mano y corregir antes de migrar)
-- ======================================================
-- A) BLOQUEANTE: transacciones WALLET que no son EXPENSE (el paso 1 falla si existen).
--
-- SELECT id, user_id, operation_type, payment_method, status, amount
--   FROM transactions
--   WHERE payment_method = 'WALLET' AND operation_type <> 'EXPENSE';
--
--    Corrección: UPDATE transactions SET operation_type = 'EXPENSE' WHERE id = ...;
--    (si además hay que ajustar origen/destino/entidad, hacerlo en el mismo UPDATE). En una
--    transacción WALLET el cambio de operation_type no mueve saldos (los procedimientos de
--    efecto ignoran WALLET; el efecto lo dan sus wallet_transaction_details).
--
-- B) NO bloqueantes, pero tras la migración el trigger 8.3 valida SIEMPRE en UPDATE: estas
--    filas no se podrán editar ni cancelar hasta que el mismo UPDATE las corrija (sí se
--    pueden borrar). Las reglas son: INCOME -> destino y entidad, sin origen; EXPENSE ->
--    origen y entidad, sin destino; REALLOCATION -> sin entidad.
--
-- -- B1) INCOME sin destino, sin entidad o con origen
-- SELECT id, user_id, payment_method, status, source_account_id, destination_account_id, external_entity_id
--   FROM transactions
--   WHERE operation_type = 'INCOME'
--     AND (destination_account_id IS NULL OR external_entity_id IS NULL OR source_account_id IS NOT NULL);
--
-- -- B2) EXPENSE sin origen, sin entidad o con destino
-- SELECT id, user_id, payment_method, status, source_account_id, destination_account_id, external_entity_id
--   FROM transactions
--   WHERE operation_type = 'EXPENSE'
--     AND (source_account_id IS NULL OR external_entity_id IS NULL OR destination_account_id IS NOT NULL);
--
-- -- B3) REALLOCATION con entidad externa
-- SELECT id, user_id, payment_method, status, source_account_id, destination_account_id, external_entity_id
--   FROM transactions
--   WHERE operation_type = 'REALLOCATION' AND external_entity_id IS NOT NULL;
--
--    Correcciones sin efecto en saldos (los procedimientos ignoran esas columnas para ese
--    tipo de operación):
--      INCOME con origen:         UPDATE transactions SET source_account_id = NULL WHERE id = ...;
--      EXPENSE con destino:       UPDATE transactions SET destination_account_id = NULL WHERE id = ...;
--      REALLOCATION con entidad:  UPDATE transactions SET external_entity_id = NULL WHERE id = ...;
--    Sin entidad: asignar una entidad del mismo usuario (p. ej. crear una "Sin especificar").
--    Sin cuenta origen/destino (normalmente por una cuenta borrada, ON DELETE SET NULL):
--    revisión manual; asignar una cuenta SÍ mueve saldos (trigger 3).
--
-- C) Detalles de tarjeta inválidos (padre no CARD o tarjeta de otra cuenta). No bloquean;
--    el trigger 10.1 solo valida si cambia transaction_id o card_id.
--
-- SELECT ctd.id, ctd.transaction_id, ctd.card_id, c.account_id AS card_account_id,
--        t.payment_method, t.source_account_id
--   FROM card_transaction_details ctd
--   INNER JOIN transactions t ON t.id = ctd.transaction_id
--   INNER JOIN cards c ON c.id = ctd.card_id
--   WHERE t.payment_method <> 'CARD'
--      OR NOT (c.account_id <=> t.source_account_id);
--
-- D) Detalles de wallet inválidos (padre no WALLET, cuenta que no es WALLET, LINKED_CARD sin
--    tarjeta o con tarjeta no vinculada a la wallet). No bloquean; el trigger 5.4 solo valida
--    si cambia transaction_id, source_type, wallet_account_id o card_id.
--
-- SELECT w.id, w.transaction_id, t.payment_method, w.source_type, w.wallet_account_id,
--        a.type AS wallet_account_type, w.card_id
--   FROM wallet_transaction_details w
--   INNER JOIN transactions t ON t.id = w.transaction_id
--   INNER JOIN accounts a ON a.id = w.wallet_account_id
--   LEFT JOIN wallet_card_links l ON l.account_id = w.wallet_account_id AND l.card_id = w.card_id
--   WHERE t.payment_method <> 'WALLET'
--      OR a.type <> 'WALLET'
--      OR (w.source_type = 'LINKED_CARD' AND (w.card_id IS NULL OR l.card_id IS NULL));
--
--    Ojo: borrar o cambiar un detalle de wallet de un padre COMPLETED mueve saldos
--    (triggers 5.1 / 5.2).
-- ======================================================

USE finanzas;

SET GLOBAL log_bin_trust_function_creators = 1;

-- ======================================================
-- 1. CHECK chk_tx_wallet_expense (primero: si los datos lo violan, no se aplica nada)
-- ======================================================
ALTER TABLE transactions
    ADD CONSTRAINT chk_tx_wallet_expense CHECK (payment_method <> 'WALLET' OR operation_type = 'EXPENSE');

-- ======================================================
-- 2. Índices de transactions
-- ======================================================
CREATE INDEX idx_tx_category ON transactions (category_id);
CREATE INDEX idx_tx_parent ON transactions (parent_transaction_id);

-- ======================================================
-- 3. categories.type admite REALLOCATION
-- ======================================================
-- Dos sentencias separadas: el CHECK se elimina y se vuelve a crear con el mismo nombre.
ALTER TABLE categories DROP CHECK chk_category_type;

ALTER TABLE categories
    ADD CONSTRAINT chk_category_type CHECK (type IN ('INCOME', 'EXPENSE', 'REALLOCATION', 'BOTH'));

-- ======================================================
-- 4. Eliminar los triggers que llaman a sp_validate_wallet_detail (cambia de firma)
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_wallet_detail_insert_val;
DROP TRIGGER IF EXISTS tr_before_wallet_detail_update_val;

-- ======================================================
-- 5. Procedimientos 7, 9 y 10 (copiados de database/schemas.sql)
-- ======================================================
DELIMITER //

-- ======================================================
-- PROCEDIMIENTO 7: Validar un detalle de wallet
-- ======================================================
--   - la transacción padre debe existir y tener payment_method = 'WALLET'
--   - wallet_account_id debe ser una cuenta de tipo 'WALLET'
--   - source_type = 'LINKED_CARD' exige card_id (sin tarjeta no hay cuenta a la que cargar)
--     y que la tarjeta esté vinculada a esa wallet (wallet_card_links)
-- Se valida con trigger y no con CHECK porque MySQL prohíbe CHECK sobre columnas
-- usadas en una FK con acción referencial ON DELETE SET NULL (fk_wallet_card sobre card_id),
-- y porque las reglas consultan otras tablas.
-- El padre ya existe al insertar el detalle: el backend inserta primero la transacción.
DROP PROCEDURE IF EXISTS sp_validate_wallet_detail //

CREATE PROCEDURE sp_validate_wallet_detail(
    IN p_transaction_id BIGINT,
    IN p_source_type VARCHAR(20),
    IN p_wallet_account_id BIGINT,
    IN p_card_id BIGINT
)
BEGIN
    DECLARE v_payment_method VARCHAR(20) DEFAULT NULL;
    DECLARE v_wallet_type VARCHAR(20) DEFAULT NULL;

    -- 1. Transacción padre: debe existir y ser un pago con WALLET
    SELECT payment_method INTO v_payment_method
    FROM transactions WHERE id = p_transaction_id;

    IF v_payment_method IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: La transacción del detalle de wallet no existe.';
    END IF;

    IF v_payment_method <> 'WALLET' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Solo una transacción con método de pago WALLET puede tener detalle de wallet.';
    END IF;

    -- 2. La cuenta del detalle debe ser una wallet
    SELECT type INTO v_wallet_type
    FROM accounts WHERE id = p_wallet_account_id;

    IF NOT (v_wallet_type <=> 'WALLET') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: La cuenta del detalle de wallet debe ser una cuenta de tipo WALLET.';
    END IF;

    -- 3. Tarjeta vinculada: obligatoria y enlazada a esa wallet
    IF p_source_type = 'LINKED_CARD' THEN
        IF p_card_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Un detalle de wallet con origen LINKED_CARD requiere card_id.';
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM wallet_card_links
            WHERE account_id = p_wallet_account_id AND card_id = p_card_id
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: La tarjeta no está vinculada a la wallet del detalle.';
        END IF;
    END IF;
END //

-- ======================================================
-- PROCEDIMIENTO 9: Validar las partes de una transacción (cuentas y entidad externa)
-- ======================================================
-- Reglas estructurales según operation_type (no consulta tablas):
--   INCOME       -> destino y entidad externa obligatorios; sin origen
--   EXPENSE      -> origen y entidad externa obligatorios; sin destino
--   REALLOCATION -> sin entidad externa (origen y destino los valida
--                   sp_validate_transaction_reallocation)
-- (El PROCEDIMIENTO 8, sp_reconcile_account, está al final del archivo.)
DROP PROCEDURE IF EXISTS sp_validate_transaction_parties //

CREATE PROCEDURE sp_validate_transaction_parties(
    IN p_operation_type VARCHAR(20),
    IN p_source_account_id BIGINT,
    IN p_destination_account_id BIGINT,
    IN p_external_entity_id BIGINT
)
BEGIN
    IF p_operation_type = 'INCOME' THEN
        IF p_destination_account_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Un ingreso requiere una cuenta destino.';
        END IF;

        IF p_external_entity_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Un ingreso requiere una entidad externa (quién paga).';
        END IF;

        IF p_source_account_id IS NOT NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Un ingreso no puede tener cuenta origen.';
        END IF;

    ELSEIF p_operation_type = 'EXPENSE' THEN
        IF p_source_account_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Un gasto requiere una cuenta origen.';
        END IF;

        IF p_external_entity_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Un gasto requiere una entidad externa (a quién se paga).';
        END IF;

        IF p_destination_account_id IS NOT NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Un gasto no puede tener cuenta destino.';
        END IF;

    ELSEIF p_operation_type = 'REALLOCATION' THEN
        IF p_external_entity_id IS NOT NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Una reubicación entre cuentas propias no puede tener entidad externa.';
        END IF;
    END IF;
END //

-- ======================================================
-- PROCEDIMIENTO 10: Validar un detalle de tarjeta
-- ======================================================
--   - la transacción padre debe existir y tener payment_method = 'CARD'
--   - la tarjeta debe existir y pertenecer a la cuenta origen de la transacción
--     (cards.account_id = transactions.source_account_id)
-- El padre ya existe al insertar el detalle: el backend inserta primero la transacción
-- (con source_account_id fijado) y después el detalle, en la misma transacción SQL.
DROP PROCEDURE IF EXISTS sp_validate_card_detail //

CREATE PROCEDURE sp_validate_card_detail(
    IN p_transaction_id BIGINT,
    IN p_card_id BIGINT
)
BEGIN
    DECLARE v_payment_method VARCHAR(20) DEFAULT NULL;
    DECLARE v_source_account_id BIGINT DEFAULT NULL;
    DECLARE v_card_account_id BIGINT DEFAULT NULL;

    -- 1. Transacción padre: debe existir y ser un pago con tarjeta
    SELECT payment_method, source_account_id INTO v_payment_method, v_source_account_id
    FROM transactions WHERE id = p_transaction_id;

    IF v_payment_method IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: La transacción del detalle de tarjeta no existe.';
    END IF;

    IF v_payment_method <> 'CARD' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Solo una transacción con método de pago CARD puede tener detalle de tarjeta.';
    END IF;

    -- 2. La tarjeta debe ser de la cuenta origen
    SELECT account_id INTO v_card_account_id
    FROM cards WHERE id = p_card_id;

    IF v_card_account_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: La tarjeta del detalle no existe.';
    END IF;

    IF NOT (v_card_account_id <=> v_source_account_id) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: La tarjeta debe pertenecer a la cuenta origen de la transacción.';
    END IF;
END //

DELIMITER ;

-- ======================================================
-- 6. Triggers 5.3 y 5.4 (copiados de database/schemas.sql)
-- ======================================================
DELIMITER //

-- ======================================================
-- TRIGGER 5.3: Validar detalle de wallet antes de insertar
-- ======================================================
-- Padre WALLET, cuenta de tipo WALLET y, en LINKED_CARD, card_id obligatorio y vinculado
-- a la wallet (sp_validate_wallet_detail). Es trigger y no CHECK porque MySQL prohíbe
-- CHECK sobre card_id (FK con ON DELETE SET NULL, fk_wallet_card) y porque consulta
-- otras tablas.
DROP TRIGGER IF EXISTS tr_before_wallet_detail_insert_val //

CREATE TRIGGER tr_before_wallet_detail_insert_val
BEFORE INSERT ON wallet_transaction_details
FOR EACH ROW
BEGIN
    CALL sp_validate_wallet_detail(
        NEW.transaction_id, NEW.source_type, NEW.wallet_account_id, NEW.card_id
    );
END //

-- ======================================================
-- TRIGGER 5.4: Validar detalle de wallet antes de actualizar
-- ======================================================
-- Solo valida si cambió alguna columna que participa en las reglas (transaction_id,
-- source_type, wallet_account_id o card_id). Así un detalle histórico que quedó
-- LINKED_CARD con card_id NULL (tarjeta borrada) o cuya tarjeta se desvinculó después
-- se puede seguir editando (monto, cashback) sin error.
DROP TRIGGER IF EXISTS tr_before_wallet_detail_update_val //

CREATE TRIGGER tr_before_wallet_detail_update_val
BEFORE UPDATE ON wallet_transaction_details
FOR EACH ROW
BEGIN
    IF NOT (OLD.transaction_id <=> NEW.transaction_id
        AND OLD.source_type <=> NEW.source_type
        AND OLD.wallet_account_id <=> NEW.wallet_account_id
        AND OLD.card_id <=> NEW.card_id) THEN

        CALL sp_validate_wallet_detail(
            NEW.transaction_id, NEW.source_type, NEW.wallet_account_id, NEW.card_id
        );
    END IF;
END //

DELIMITER ;

-- ======================================================
-- 7. Triggers 8.2 y 8.3 (copiados de database/schemas.sql)
-- ======================================================
-- Orden resultante de los BEFORE sobre transactions:
--   BEFORE INSERT: 1 (monto) -> 8 (reubicación) -> 8.2 (partes)
--   BEFORE UPDATE: 1.1 (monto) -> 8.1 (reubicación) -> 8.3 (partes)
DELIMITER //

-- ======================================================
-- TRIGGER 8.2: Validación de partes (origen, destino, entidad) al insertar
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_transaction_insert_parties_check //

CREATE TRIGGER tr_before_transaction_insert_parties_check
BEFORE INSERT ON transactions
FOR EACH ROW
FOLLOWS tr_before_transaction_reallocation_check
BEGIN
    CALL sp_validate_transaction_parties(
        NEW.operation_type, NEW.source_account_id,
        NEW.destination_account_id, NEW.external_entity_id
    );
END //

-- ======================================================
-- TRIGGER 8.3: Validación de partes (origen, destino, entidad) al actualizar
-- ======================================================
-- Valida SIEMPRE (las reglas son estructurales, no dependen del estado de otras tablas).
-- Consecuencia: una fila que quedó inválida por un ON DELETE SET NULL (cuenta o entidad
-- borrada; las cascadas no disparan triggers) no se puede editar ni cancelar hasta que el
-- mismo UPDATE complete los datos que faltan. Borrarla sí se puede.
DROP TRIGGER IF EXISTS tr_before_transaction_update_parties_check //

CREATE TRIGGER tr_before_transaction_update_parties_check
BEFORE UPDATE ON transactions
FOR EACH ROW
FOLLOWS tr_before_transaction_update_reallocation_check
BEGIN
    CALL sp_validate_transaction_parties(
        NEW.operation_type, NEW.source_account_id,
        NEW.destination_account_id, NEW.external_entity_id
    );
END //

DELIMITER ;

-- ======================================================
-- 8. Triggers 10 y 10.1 (copiados de database/schemas.sql)
-- ======================================================
-- ======================================================
-- TRIGGERS DE CARD_TRANSACTION_DETAILS (10 y 10.1)
-- ======================================================
-- Únicos BEFORE INSERT / BEFORE UPDATE sobre card_transaction_details (sin FOLLOWS).
-- El detalle de tarjeta no tiene efecto en saldos (lo aplica la transacción padre sobre
-- source_account_id); estos triggers solo validan su coherencia con el padre.
DELIMITER //

-- ======================================================
-- TRIGGER 10: Validar detalle de tarjeta antes de insertar
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_card_detail_insert_val //

CREATE TRIGGER tr_before_card_detail_insert_val
BEFORE INSERT ON card_transaction_details
FOR EACH ROW
BEGIN
    CALL sp_validate_card_detail(NEW.transaction_id, NEW.card_id);
END //

-- ======================================================
-- TRIGGER 10.1: Validar detalle de tarjeta antes de actualizar
-- ======================================================
-- Solo valida si cambió transaction_id o card_id: editar MSI o monto de un detalle
-- histórico no falla aunque la tarjeta se haya movido de cuenta después.
DROP TRIGGER IF EXISTS tr_before_card_detail_update_val //

CREATE TRIGGER tr_before_card_detail_update_val
BEFORE UPDATE ON card_transaction_details
FOR EACH ROW
BEGIN
    IF NOT (OLD.transaction_id <=> NEW.transaction_id AND OLD.card_id <=> NEW.card_id) THEN
        CALL sp_validate_card_detail(NEW.transaction_id, NEW.card_id);
    END IF;
END //

DELIMITER ;
