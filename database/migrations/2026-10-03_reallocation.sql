-- ======================================================
-- MIGRACIÓN 2026-10-03: TRANSFER -> REALLOCATION / WIRE_TRANSFER
-- ======================================================
-- Contexto: 'TRANSFER' era ambiguo. Nuevo vocabulario (MAYÚSCULAS):
--   operation_type: INCOME | EXPENSE | REALLOCATION (reubicación entre cuentas propias)
--   payment_method: CASH | CARD | WIRE_TRANSFER | INTERNAL | QR | CODI | WALLET
--
-- Uso: SOLO para bases de datos con datos existentes (no borra datos).
--   mysql -u root -p < database/migrations/2026-10-03_reallocation.sql
-- Para instalaciones nuevas basta con ejecutar database/schemas.sql.
--
-- Recomendación: respaldar antes (mysqldump finanzas > respaldo.sql).
-- Nota: los UPDATE de este script dispararían tr_after_transaction_update y alterarían
-- saldos (el OLD 'TRANSFER' y el NEW 'REALLOCATION' no se tratarían igual). Por eso los
-- triggers de transactions se eliminan ANTES de los UPDATE y se recrean al final.
-- ======================================================

USE finanzas;

SET GLOBAL log_bin_trust_function_creators = 1;

-- ======================================================
-- 1. Eliminar triggers afectados (incluido el nombre viejo del trigger 8)
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_transaction_insert_master;
DROP TRIGGER IF EXISTS tr_after_transaction_update;
DROP TRIGGER IF EXISTS tr_after_transaction_delete;
DROP TRIGGER IF EXISTS tr_before_transaction_transfer_check;
DROP TRIGGER IF EXISTS tr_before_transaction_reallocation_check;

-- ======================================================
-- 2. Ampliar operation_type ('REALLOCATION' tiene 12 caracteres)
-- ======================================================
ALTER TABLE transactions MODIFY operation_type VARCHAR(20) NOT NULL;

-- ======================================================
-- 3. Normalizar datos existentes
-- ======================================================
-- 3.1 Pasar a MAYÚSCULAS
UPDATE transactions SET operation_type = UPPER(operation_type), payment_method = UPPER(payment_method);

-- 3.2 Sustituir el valor ambiguo 'TRANSFER'
UPDATE transactions SET operation_type = 'REALLOCATION' WHERE operation_type = 'TRANSFER';
UPDATE transactions SET payment_method = 'WIRE_TRANSFER' WHERE payment_method = 'TRANSFER';

-- ======================================================
-- 4. Constraints de valores controlados
-- ======================================================
-- Si falla, hay filas con valores fuera del vocabulario: revisarlas con
--   SELECT id, operation_type, payment_method FROM transactions
--   WHERE operation_type NOT IN ('INCOME', 'EXPENSE', 'REALLOCATION')
--      OR payment_method NOT IN ('CASH', 'CARD', 'WIRE_TRANSFER', 'INTERNAL', 'QR', 'CODI', 'WALLET');
ALTER TABLE transactions
    ADD CONSTRAINT chk_tx_operation_type CHECK (operation_type IN ('INCOME', 'EXPENSE', 'REALLOCATION'));

ALTER TABLE transactions
    ADD CONSTRAINT chk_tx_payment_method CHECK (payment_method IN ('CASH', 'CARD', 'WIRE_TRANSFER', 'INTERNAL', 'QR', 'CODI', 'WALLET'));

-- ======================================================
-- 5. Recrear triggers 2, 3, 4 y 8 (copiados de database/schemas.sql)
-- ======================================================
DELIMITER //

-- ======================================================
-- TRIGGER 2: Actualización de saldos después de insertar transacción
-- ======================================================

DROP TRIGGER IF EXISTS tr_after_transaction_insert_master //

CREATE TRIGGER tr_after_transaction_insert_master
AFTER INSERT ON transactions
FOR EACH ROW
BEGIN
    DECLARE v_source_type VARCHAR(20) DEFAULT NULL;
    DECLARE v_dest_type VARCHAR(20) DEFAULT NULL;
    DECLARE v_credit_used DECIMAL(12,2);
    DECLARE v_payment_amount DECIMAL(12,2);
    DECLARE v_overpayment DECIMAL(12,2);

    -- 1. Obtener tipos de cuenta
    IF NEW.source_account_id IS NOT NULL THEN
        SELECT type INTO v_source_type FROM accounts WHERE id = NEW.source_account_id;
    END IF;
    
    IF NEW.destination_account_id IS NOT NULL THEN
        SELECT type INTO v_dest_type FROM accounts WHERE id = NEW.destination_account_id;
    END IF;

    -- 2. Ignoramos Wallet porque tiene su propio Trigger (Trigger 5)
    IF NEW.payment_method <> 'WALLET' THEN

        -- ==========================================
        -- LOGICA PARA LA CUENTA ORIGEN (Sale dinero)
        -- ==========================================
        IF NEW.source_account_id IS NOT NULL AND NEW.operation_type IN ('EXPENSE', 'REALLOCATION') THEN
            IF v_source_type = 'CREDIT' THEN
                -- Es tarjeta de crédito: aumenta la deuda
                UPDATE credit_details SET credit_used = credit_used + NEW.amount 
                WHERE account_id = NEW.source_account_id;
            ELSE
                -- Es débito/efectivo: resta el dinero
                UPDATE accounts SET current_balance = current_balance - NEW.amount 
                WHERE id = NEW.source_account_id;
            END IF;
        END IF;

        -- ==========================================
        -- LOGICA PARA LA CUENTA DESTINO (Entra dinero)
        -- ==========================================
        IF NEW.destination_account_id IS NOT NULL AND NEW.operation_type IN ('INCOME', 'REALLOCATION') THEN
            IF v_dest_type = 'CREDIT' THEN
                -- Es un PAGO a la tarjeta de crédito
                SELECT credit_used INTO v_credit_used FROM credit_details WHERE account_id = NEW.destination_account_id;
                SET v_payment_amount = NEW.amount;
                
                IF v_payment_amount <= v_credit_used THEN
                    -- Cubre la deuda parcial o totalmente
                    UPDATE credit_details SET credit_used = credit_used - v_payment_amount WHERE account_id = NEW.destination_account_id;
                ELSE
                    -- Pagó de más (Saldo a favor)
                    SET v_overpayment = v_payment_amount - v_credit_used;
                    UPDATE credit_details SET credit_used = 0 WHERE account_id = NEW.destination_account_id;
                    UPDATE accounts SET current_balance = current_balance + v_overpayment WHERE id = NEW.destination_account_id;
                END IF;
            ELSE
                -- Es depósito normal (débito, cash, ahorro)
                UPDATE accounts SET current_balance = current_balance + NEW.amount 
                WHERE id = NEW.destination_account_id;
            END IF;
        END IF;

    END IF;
END //

-- ======================================================
-- TRIGGER 3: Corregir saldos al actualizar transacción
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_transaction_update //

CREATE TRIGGER tr_after_transaction_update
AFTER UPDATE ON transactions
FOR EACH ROW
BEGIN
    -- A. REVERTIR valores antiguos
    IF OLD.payment_method <> 'WALLET' THEN
        IF OLD.operation_type = 'EXPENSE' THEN
            UPDATE accounts
            SET current_balance = current_balance + OLD.amount
            WHERE id = OLD.source_account_id;

        ELSEIF OLD.operation_type = 'INCOME' THEN
            UPDATE accounts
            SET current_balance = current_balance - OLD.amount
            WHERE id = OLD.destination_account_id;

        ELSEIF OLD.operation_type = 'REALLOCATION' THEN
            UPDATE accounts
            SET current_balance = current_balance + OLD.amount
            WHERE id = OLD.source_account_id;

            UPDATE accounts
            SET current_balance = current_balance - OLD.amount
            WHERE id = OLD.destination_account_id;
        END IF;
    END IF;

    -- B. APLICAR valores nuevos
    IF NEW.payment_method <> 'WALLET' THEN
        IF NEW.operation_type = 'EXPENSE' THEN
            UPDATE accounts
            SET current_balance = current_balance - NEW.amount
            WHERE id = NEW.source_account_id;

        ELSEIF NEW.operation_type = 'INCOME' THEN
            UPDATE accounts
            SET current_balance = current_balance + NEW.amount
            WHERE id = NEW.destination_account_id;

        ELSEIF NEW.operation_type = 'REALLOCATION' THEN
            UPDATE accounts
            SET current_balance = current_balance - NEW.amount
            WHERE id = NEW.source_account_id;

            UPDATE accounts
            SET current_balance = current_balance + NEW.amount
            WHERE id = NEW.destination_account_id;
        END IF;
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
    IF OLD.payment_method <> 'WALLET' THEN
        IF OLD.operation_type = 'EXPENSE' THEN
            UPDATE accounts
            SET current_balance = current_balance + OLD.amount
            WHERE id = OLD.source_account_id;

        ELSEIF OLD.operation_type = 'INCOME' THEN
            UPDATE accounts
            SET current_balance = current_balance - OLD.amount
            WHERE id = OLD.destination_account_id;

        ELSEIF OLD.operation_type = 'REALLOCATION' THEN
            UPDATE accounts
            SET current_balance = current_balance + OLD.amount
            WHERE id = OLD.source_account_id;

            UPDATE accounts
            SET current_balance = current_balance - OLD.amount
            WHERE id = OLD.destination_account_id;
        END IF;
    END IF;
END //

-- ======================================================
-- TRIGGER 8: Validación de reubicaciones
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_transaction_reallocation_check //

CREATE TRIGGER tr_before_transaction_reallocation_check
BEFORE INSERT ON transactions
FOR EACH ROW
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
            SET MESSAGE_TEXT = 'Restricción: Esta cuenta no permite transferencias salientes.';
        END IF;

    END IF;
END //

DELIMITER ;
