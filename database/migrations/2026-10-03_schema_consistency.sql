-- ======================================================
-- MIGRACIÓN 2026-10-03: CONSISTENCIA DEL ESQUEMA
-- ======================================================
-- Requisito: la BD ya tiene aplicada 2026-10-03_reallocation.sql.
--
-- Cambios:
--   1. Valores controlados en MAYÚSCULAS + CHECK:
--        accounts.type                 -> chk_account_type
--        cards.card_type / cards.status -> chk_card_type / chk_card_status
--        external_entities.type        -> chk_external_entity_type
--        investment_details.status     -> chk_investment_status
--        wallet_transaction_details.source_type -> chk_wallet_source_type (sin EXTERNAL_TRANSFER)
--        (categories.type ya tenía chk_category_type; solo se normaliza)
--   2. wallet_transaction_details.cashback_percentage DECIMAL(5,2) (0-100)
--        -> cashback_rate DECIMAL(9,6) (fracción 0-1), CHECK chk_wallet_cashback_rate,
--        índice idx_wallet_cashback -> idx_wallet_cashback_rate.
--   3. transactions: CHECK chk_tx_internal_reallocation (INTERNAL solo con REALLOCATION).
--   4. Procedimientos sp_apply_transaction_effect, sp_revert_transaction_effect y
--      sp_wallet_detail_effect. Triggers 2, 3 y 4 los usan (ahora manejan CREDIT/credit_used).
--   5. Trigger 5 respeta tarjetas de crédito; nuevos triggers 5.1 (update) y 5.2 (delete)
--      sobre wallet_transaction_details.
--   6. Trigger 8 con FOLLOWS tr_before_transaction_insert_val y mensaje nuevo.
--
-- Uso: SOLO para bases de datos con datos existentes (no borra datos).
--   mysql -u root -p < database/migrations/2026-10-03_schema_consistency.sql
-- Para instalaciones nuevas basta con ejecutar database/schemas.sql.
-- Requiere MySQL 8.0.19+ (ALTER TABLE ... DROP CHECK y CHECK aplicados).
--
-- Recomendación: respaldar antes (mysqldump finanzas > respaldo.sql).
-- Nota: los triggers afectados se eliminan ANTES de los UPDATE para que la
-- normalización de datos no altere saldos; se recrean al final.
-- Nota: esta migración NO recalcula saldos. Si antes se editaron/borraron transacciones
-- de cuentas CREDIT, los triggers viejos pudieron dejar current_balance/credit_used
-- desajustados; revisarlos a mano.
-- ======================================================

-- ======================================================
-- 0. DIAGNÓSTICO PREVIO (ejecutar a mano y corregir antes de migrar)
-- ======================================================
-- Si alguna de estas consultas devuelve filas, el ADD CONSTRAINT correspondiente fallará.
-- (Se comparan en MAYÚSCULAS porque el paso 2 normaliza.)
--
-- SELECT id, type FROM accounts
--   WHERE UPPER(type) NOT IN ('CASH', 'DEBIT', 'CREDIT', 'WALLET', 'BENEFIT', 'SAVINGS', 'INVESTMENT');
-- SELECT id, card_type, status FROM cards
--   WHERE UPPER(card_type) NOT IN ('PHYSICAL', 'DIGITAL')
--      OR UPPER(status) NOT IN ('ACTIVE', 'BLOCKED', 'EXPIRED');
-- SELECT id, type FROM categories
--   WHERE UPPER(type) NOT IN ('INCOME', 'EXPENSE', 'BOTH');
-- SELECT id, type FROM external_entities
--   WHERE UPPER(type) NOT IN ('PERSON', 'SERVICE', 'STORE');
-- SELECT id, status FROM investment_details
--   WHERE UPPER(status) NOT IN ('ACTIVE', 'MATURED', 'CANCELLED');
-- SELECT id, source_type FROM wallet_transaction_details
--   WHERE UPPER(source_type) NOT IN ('WALLET_BALANCE', 'LINKED_CARD');   -- p. ej. 'EXTERNAL_TRANSFER'
-- SELECT id, cashback_percentage FROM wallet_transaction_details
--   WHERE cashback_percentage < 0 OR cashback_percentage > 100;
-- SELECT id, operation_type, payment_method FROM transactions
--   WHERE payment_method = 'INTERNAL' AND operation_type <> 'REALLOCATION';
-- ======================================================

USE finanzas;

SET GLOBAL log_bin_trust_function_creators = 1;

-- Los UPDATE masivos no filtran por clave (Workbench los bloquea en modo seguro)
SET SQL_SAFE_UPDATES = 0;

-- ======================================================
-- 1. Eliminar triggers afectados (y procedimientos, si existieran)
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_transaction_insert_master;
DROP TRIGGER IF EXISTS tr_after_transaction_update;
DROP TRIGGER IF EXISTS tr_after_transaction_delete;
DROP TRIGGER IF EXISTS tr_after_wallet_detail_insert;
DROP TRIGGER IF EXISTS tr_after_wallet_detail_update;
DROP TRIGGER IF EXISTS tr_after_wallet_detail_delete;
DROP TRIGGER IF EXISTS tr_before_transaction_reallocation_check;
DROP PROCEDURE IF EXISTS sp_apply_transaction_effect;
DROP PROCEDURE IF EXISTS sp_revert_transaction_effect;
DROP PROCEDURE IF EXISTS sp_wallet_detail_effect;

-- ======================================================
-- 2. Normalizar valores controlados a MAYÚSCULAS
-- ======================================================
-- (tr_sync_global_balance se dispara con el UPDATE de accounts, pero no actúa
--  porque current_balance no cambia)
UPDATE accounts SET type = UPPER(type);
UPDATE cards SET card_type = UPPER(card_type), status = UPPER(status);
UPDATE categories SET type = UPPER(type);
UPDATE external_entities SET type = UPPER(type);
UPDATE investment_details SET status = UPPER(status);
UPDATE wallet_transaction_details SET source_type = UPPER(source_type);

-- ======================================================
-- 3. wallet_transaction_details: cashback_percentage (0-100) -> cashback_rate (0-1)
-- ======================================================
-- 3.1 El CHECK viejo usa la columna: MySQL no permite renombrarla mientras exista
ALTER TABLE wallet_transaction_details DROP CHECK chk_cashback;

-- 3.2 Renombrar y ampliar precisión
ALTER TABLE wallet_transaction_details
    CHANGE cashback_percentage cashback_rate DECIMAL(9, 6) NULL;

-- 3.3 Convertir porcentaje a fracción (2.00 -> 0.020000)
UPDATE wallet_transaction_details SET cashback_rate = cashback_rate / 100
WHERE cashback_rate IS NOT NULL;

-- 3.4 Renombrar índice
DROP INDEX idx_wallet_cashback ON wallet_transaction_details;
CREATE INDEX idx_wallet_cashback_rate ON wallet_transaction_details (cashback_rate);

-- ======================================================
-- 4. Constraints de valores controlados
-- ======================================================
-- Si alguno falla, revisar las consultas del paso 0.
ALTER TABLE accounts
    ADD CONSTRAINT chk_account_type
        CHECK (type IN ('CASH', 'DEBIT', 'CREDIT', 'WALLET', 'BENEFIT', 'SAVINGS', 'INVESTMENT'));

ALTER TABLE cards
    ADD CONSTRAINT chk_card_type CHECK (card_type IN ('PHYSICAL', 'DIGITAL')),
    ADD CONSTRAINT chk_card_status CHECK (status IN ('ACTIVE', 'BLOCKED', 'EXPIRED'));

ALTER TABLE external_entities
    ADD CONSTRAINT chk_external_entity_type CHECK (type IN ('PERSON', 'SERVICE', 'STORE'));

ALTER TABLE investment_details
    ADD CONSTRAINT chk_investment_status CHECK (status IN ('ACTIVE', 'MATURED', 'CANCELLED'));

ALTER TABLE wallet_transaction_details
    ADD CONSTRAINT chk_wallet_source_type CHECK (source_type IN ('WALLET_BALANCE', 'LINKED_CARD')),
    ADD CONSTRAINT chk_wallet_cashback_rate
        CHECK (cashback_rate IS NULL OR (cashback_rate >= 0 AND cashback_rate <= 1));

ALTER TABLE transactions
    ADD CONSTRAINT chk_tx_internal_reallocation
        CHECK (payment_method <> 'INTERNAL' OR operation_type = 'REALLOCATION');

SET SQL_SAFE_UPDATES = 1;

-- ======================================================
-- 5. Procedimientos almacenados (copiados de database/schemas.sql)
-- ======================================================
DELIMITER //

-- ======================================================
-- PROCEDIMIENTO 1: Aplicar el efecto de una transacción en los saldos
-- ======================================================
-- Origen (EXPENSE / REALLOCATION):
--   CREDIT -> credit_used + monto (aumenta la deuda)
--   otro   -> current_balance - monto
-- Destino (INCOME / REALLOCATION):
--   CREDIT -> pago de tarjeta: reduce credit_used; el sobrepago va a current_balance
--   otro   -> current_balance + monto
-- WALLET se ignora: lo gestionan los triggers de wallet_transaction_details.
DROP PROCEDURE IF EXISTS sp_apply_transaction_effect //

CREATE PROCEDURE sp_apply_transaction_effect(
    IN p_operation_type VARCHAR(20),
    IN p_payment_method VARCHAR(20),
    IN p_source_account_id BIGINT,
    IN p_destination_account_id BIGINT,
    IN p_amount DECIMAL(12, 2)
)
proc: BEGIN
    DECLARE v_source_type VARCHAR(20) DEFAULT NULL;
    DECLARE v_dest_type VARCHAR(20) DEFAULT NULL;
    DECLARE v_credit_used DECIMAL(12, 2) DEFAULT 0.00;
    DECLARE v_overpayment DECIMAL(12, 2) DEFAULT 0.00;

    -- WALLET tiene su propia lógica (triggers 5, 5.1 y 5.2)
    IF p_payment_method = 'WALLET' THEN
        LEAVE proc;
    END IF;

    -- ==========================================
    -- CUENTA ORIGEN (sale dinero)
    -- ==========================================
    IF p_source_account_id IS NOT NULL AND p_operation_type IN ('EXPENSE', 'REALLOCATION') THEN
        SELECT type INTO v_source_type FROM accounts WHERE id = p_source_account_id;

        IF v_source_type = 'CREDIT' THEN
            -- Tarjeta de crédito: aumenta la deuda
            UPDATE credit_details SET credit_used = credit_used + p_amount
            WHERE account_id = p_source_account_id;
        ELSE
            -- Débito, efectivo, ahorro, etc.: resta el dinero
            UPDATE accounts SET current_balance = current_balance - p_amount
            WHERE id = p_source_account_id;
        END IF;
    END IF;

    -- ==========================================
    -- CUENTA DESTINO (entra dinero)
    -- ==========================================
    IF p_destination_account_id IS NOT NULL AND p_operation_type IN ('INCOME', 'REALLOCATION') THEN
        SELECT type INTO v_dest_type FROM accounts WHERE id = p_destination_account_id;

        IF v_dest_type = 'CREDIT' THEN
            -- Pago a la tarjeta de crédito (si no hay fila en credit_details, v_credit_used queda en 0)
            SELECT credit_used INTO v_credit_used
            FROM credit_details WHERE account_id = p_destination_account_id;

            IF p_amount <= v_credit_used THEN
                -- Cubre la deuda parcial o totalmente
                UPDATE credit_details SET credit_used = credit_used - p_amount
                WHERE account_id = p_destination_account_id;
            ELSE
                -- Pagó de más: la deuda queda en 0 y el excedente es saldo a favor
                SET v_overpayment = p_amount - v_credit_used;
                UPDATE credit_details SET credit_used = 0
                WHERE account_id = p_destination_account_id;
                UPDATE accounts SET current_balance = current_balance + v_overpayment
                WHERE id = p_destination_account_id;
            END IF;
        ELSE
            -- Depósito normal (débito, efectivo, ahorro, etc.)
            UPDATE accounts SET current_balance = current_balance + p_amount
            WHERE id = p_destination_account_id;
        END IF;
    END IF;
END //

-- ======================================================
-- PROCEDIMIENTO 2: Revertir el efecto de una transacción en los saldos
-- ======================================================
-- Inverso de sp_apply_transaction_effect:
-- Origen (EXPENSE / REALLOCATION):
--   CREDIT -> credit_used - monto
--   otro   -> current_balance + monto
-- Destino (INCOME / REALLOCATION):
--   CREDIT -> primero se descuenta del current_balance positivo (saldo a favor, hasta el monto)
--             y el resto vuelve a sumarse a credit_used
--   otro   -> current_balance - monto
DROP PROCEDURE IF EXISTS sp_revert_transaction_effect //

CREATE PROCEDURE sp_revert_transaction_effect(
    IN p_operation_type VARCHAR(20),
    IN p_payment_method VARCHAR(20),
    IN p_source_account_id BIGINT,
    IN p_destination_account_id BIGINT,
    IN p_amount DECIMAL(12, 2)
)
proc: BEGIN
    DECLARE v_source_type VARCHAR(20) DEFAULT NULL;
    DECLARE v_dest_type VARCHAR(20) DEFAULT NULL;
    DECLARE v_balance DECIMAL(12, 2) DEFAULT 0.00;
    DECLARE v_from_balance DECIMAL(12, 2) DEFAULT 0.00;

    -- WALLET tiene su propia lógica (triggers 5, 5.1 y 5.2)
    IF p_payment_method = 'WALLET' THEN
        LEAVE proc;
    END IF;

    -- ==========================================
    -- CUENTA ORIGEN (se devuelve el dinero)
    -- ==========================================
    IF p_source_account_id IS NOT NULL AND p_operation_type IN ('EXPENSE', 'REALLOCATION') THEN
        SELECT type INTO v_source_type FROM accounts WHERE id = p_source_account_id;

        IF v_source_type = 'CREDIT' THEN
            -- Se elimina el cargo: baja la deuda
            UPDATE credit_details SET credit_used = credit_used - p_amount
            WHERE account_id = p_source_account_id;
        ELSE
            UPDATE accounts SET current_balance = current_balance + p_amount
            WHERE id = p_source_account_id;
        END IF;
    END IF;

    -- ==========================================
    -- CUENTA DESTINO (se retira el dinero)
    -- ==========================================
    IF p_destination_account_id IS NOT NULL AND p_operation_type IN ('INCOME', 'REALLOCATION') THEN
        SELECT type INTO v_dest_type FROM accounts WHERE id = p_destination_account_id;

        IF v_dest_type = 'CREDIT' THEN
            -- Se deshace un pago a la tarjeta:
            -- 1) se quita del saldo a favor (current_balance positivo), hasta el monto
            SELECT current_balance INTO v_balance FROM accounts WHERE id = p_destination_account_id;
            SET v_from_balance = LEAST(GREATEST(v_balance, 0), p_amount);

            IF v_from_balance > 0 THEN
                UPDATE accounts SET current_balance = current_balance - v_from_balance
                WHERE id = p_destination_account_id;
            END IF;

            -- 2) el resto vuelve a ser deuda
            IF p_amount - v_from_balance > 0 THEN
                UPDATE credit_details SET credit_used = credit_used + (p_amount - v_from_balance)
                WHERE account_id = p_destination_account_id;
            END IF;
        ELSE
            UPDATE accounts SET current_balance = current_balance - p_amount
            WHERE id = p_destination_account_id;
        END IF;
    END IF;
END //

-- ======================================================
-- PROCEDIMIENTO 3: Aplicar (p_sign = 1) o revertir (p_sign = -1) un detalle de wallet
-- ======================================================
-- WALLET_BALANCE -> current_balance de la wallet - (signo * monto)
-- LINKED_CARD    -> según la cuenta de la tarjeta:
--                   CREDIT -> credit_used + (signo * monto)
--                   otro   -> current_balance - (signo * monto)
DROP PROCEDURE IF EXISTS sp_wallet_detail_effect //

CREATE PROCEDURE sp_wallet_detail_effect(
    IN p_source_type VARCHAR(20),
    IN p_wallet_account_id BIGINT,
    IN p_card_id BIGINT,
    IN p_amount DECIMAL(12, 2),
    IN p_sign TINYINT
)
BEGIN
    DECLARE v_card_account_id BIGINT DEFAULT NULL;
    DECLARE v_card_account_type VARCHAR(20) DEFAULT NULL;

    IF p_source_type = 'WALLET_BALANCE' THEN
        -- Caso saldo de la wallet
        UPDATE accounts SET current_balance = current_balance - (p_sign * p_amount)
        WHERE id = p_wallet_account_id;

    ELSEIF p_source_type = 'LINKED_CARD' AND p_card_id IS NOT NULL THEN
        -- Caso tarjeta vinculada: se afecta la cuenta dueña de la tarjeta
        SELECT a.id, a.type INTO v_card_account_id, v_card_account_type
        FROM cards c
        INNER JOIN accounts a ON a.id = c.account_id
        WHERE c.id = p_card_id;

        IF v_card_account_type = 'CREDIT' THEN
            UPDATE credit_details SET credit_used = credit_used + (p_sign * p_amount)
            WHERE account_id = v_card_account_id;
        ELSEIF v_card_account_id IS NOT NULL THEN
            UPDATE accounts SET current_balance = current_balance - (p_sign * p_amount)
            WHERE id = v_card_account_id;
        END IF;
    END IF;
END //

DELIMITER ;

-- ======================================================
-- 6. Recrear triggers 2, 3 y 4 (copiados de database/schemas.sql)
-- ======================================================
DELIMITER //

-- ======================================================
-- TRIGGER 2: Aplicar saldos después de insertar transacción
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_transaction_insert_master //

CREATE TRIGGER tr_after_transaction_insert_master
AFTER INSERT ON transactions
FOR EACH ROW
BEGIN
    -- Aplica el efecto del NEW (ignora WALLET dentro del procedimiento)
    CALL sp_apply_transaction_effect(
        NEW.operation_type, NEW.payment_method,
        NEW.source_account_id, NEW.destination_account_id, NEW.amount
    );
END //

-- ======================================================
-- TRIGGER 3: Corregir saldos al actualizar transacción
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_transaction_update //

CREATE TRIGGER tr_after_transaction_update
AFTER UPDATE ON transactions
FOR EACH ROW
BEGIN
    -- Solo se recalcula si cambió algún dato que afecta saldos
    -- (<=> compara también NULLs). Así una edición de concepto o tags no
    -- redistribuye saldo a favor / deuda de una tarjeta de crédito.
    IF NOT (OLD.operation_type <=> NEW.operation_type
        AND OLD.payment_method <=> NEW.payment_method
        AND OLD.source_account_id <=> NEW.source_account_id
        AND OLD.destination_account_id <=> NEW.destination_account_id
        AND OLD.amount <=> NEW.amount) THEN

        -- A. REVERTIR valores antiguos
        CALL sp_revert_transaction_effect(
            OLD.operation_type, OLD.payment_method,
            OLD.source_account_id, OLD.destination_account_id, OLD.amount
        );

        -- B. APLICAR valores nuevos
        CALL sp_apply_transaction_effect(
            NEW.operation_type, NEW.payment_method,
            NEW.source_account_id, NEW.destination_account_id, NEW.amount
        );
    END IF;
END //

-- ======================================================
-- TRIGGER 4: Restituir saldos al eliminar transacción
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_transaction_delete //

CREATE TRIGGER tr_after_transaction_delete
AFTER DELETE ON transactions
FOR EACH ROW
BEGIN
    CALL sp_revert_transaction_effect(
        OLD.operation_type, OLD.payment_method,
        OLD.source_account_id, OLD.destination_account_id, OLD.amount
    );
END //

DELIMITER ;

-- ======================================================
-- 7. Triggers 5, 5.1 y 5.2 de wallet_transaction_details (copiados de database/schemas.sql)
-- ======================================================
DELIMITER //

-- ======================================================
-- TRIGGER 5: Aplicar saldos al insertar detalle de wallet
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_wallet_detail_insert //

CREATE TRIGGER tr_after_wallet_detail_insert
AFTER INSERT ON wallet_transaction_details
FOR EACH ROW
BEGIN
    CALL sp_wallet_detail_effect(NEW.source_type, NEW.wallet_account_id, NEW.card_id, NEW.amount, 1);
END //

-- ======================================================
-- TRIGGER 5.1: Corregir saldos al actualizar detalle de wallet
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_wallet_detail_update //

CREATE TRIGGER tr_after_wallet_detail_update
AFTER UPDATE ON wallet_transaction_details
FOR EACH ROW
BEGIN
    -- Solo si cambió algún dato que afecta saldos
    IF NOT (OLD.source_type <=> NEW.source_type
        AND OLD.wallet_account_id <=> NEW.wallet_account_id
        AND OLD.card_id <=> NEW.card_id
        AND OLD.amount <=> NEW.amount) THEN

        -- A. REVERTIR valores antiguos
        CALL sp_wallet_detail_effect(OLD.source_type, OLD.wallet_account_id, OLD.card_id, OLD.amount, -1);

        -- B. APLICAR valores nuevos
        CALL sp_wallet_detail_effect(NEW.source_type, NEW.wallet_account_id, NEW.card_id, NEW.amount, 1);
    END IF;
END //

-- ======================================================
-- TRIGGER 5.2: Restituir saldos al eliminar detalle de wallet
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_wallet_detail_delete //

CREATE TRIGGER tr_after_wallet_detail_delete
AFTER DELETE ON wallet_transaction_details
FOR EACH ROW
BEGIN
    CALL sp_wallet_detail_effect(OLD.source_type, OLD.wallet_account_id, OLD.card_id, OLD.amount, -1);
END //

DELIMITER ;

-- ======================================================
-- 8. Recrear trigger 8 (copiado de database/schemas.sql)
-- ======================================================
-- Es el segundo BEFORE INSERT sobre transactions: FOLLOWS fija que se ejecute
-- después del trigger 1 (que normaliza el monto).
DELIMITER //

-- ======================================================
-- TRIGGER 8: Validación de reubicaciones
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_transaction_reallocation_check //

CREATE TRIGGER tr_before_transaction_reallocation_check
BEFORE INSERT ON transactions
FOR EACH ROW
FOLLOWS tr_before_transaction_insert_val
BEGIN
    DECLARE v_acc_type VARCHAR(20);
    DECLARE v_can_transfer BOOLEAN;

    IF NEW.operation_type = 'REALLOCATION' THEN

        -- Validaciones mínimas de integridad para reubicaciones
        IF NEW.source_account_id IS NULL OR NEW.destination_account_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Reubicación requiere source_account_id y destination_account_id.';
        END IF;

        IF NEW.source_account_id = NEW.destination_account_id THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Origen y destino no pueden ser iguales.';
        END IF;

        -- Obtener tipo de cuenta origen y bandera can_transfer_out (si existe)
        SELECT a.type, COALESCE(bd.can_transfer_out, TRUE)
        INTO v_acc_type, v_can_transfer
        FROM accounts a
        LEFT JOIN bank_details bd ON a.id = bd.account_id
        WHERE a.id = NEW.source_account_id;

        -- Si la cuenta origen no existe, el SELECT anterior no devuelve fila y MySQL lanza error genérico.
        -- Esta validación adicional fuerza un error claro.
        IF v_acc_type IS NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: La cuenta origen no existe.';
        END IF;

        -- Bloqueo por tipo BENEFIT o flag deshabilitado
        IF v_acc_type = 'BENEFIT' OR v_can_transfer = FALSE THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Restricción: Esta cuenta no permite salidas de dinero (reubicaciones).';
        END IF;

    END IF;
END //

DELIMITER ;
