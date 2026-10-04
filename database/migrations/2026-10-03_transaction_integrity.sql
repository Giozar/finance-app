-- ======================================================
-- MIGRACIÓN 2026-10-03: INTEGRIDAD DE TRANSACCIONES
-- ======================================================
-- Requisito: la BD ya tiene aplicadas, en orden:
--   1. 2026-10-03_reallocation.sql
--   2. 2026-10-03_schema_consistency.sql
--
-- Cambios:
--   1. transactions.status: normalizado a MAYÚSCULAS + CHECK chk_tx_status
--      ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED'). Solo COMPLETED afecta saldos.
--   2. Procedimientos nuevos:
--        sp_wallet_details_effect_for_transaction (aplica/revierte todos los detalles de wallet de una transacción)
--        sp_validate_transaction_amount           (ABS y bloqueo de monto 0; INOUT sobre NEW.amount)
--        sp_validate_transaction_reallocation     (reglas de REALLOCATION, antes dentro del trigger 8)
--        sp_validate_wallet_detail                (LINKED_CARD exige card_id)
--      sp_apply_transaction_effect, sp_revert_transaction_effect y sp_wallet_detail_effect sin cambios
--      de lógica (se recrean igual).
--   3. Triggers de transactions:
--        1   tr_before_transaction_insert_val       -> ahora llama a sp_validate_transaction_amount
--        1.1 tr_before_transaction_update_val       (NUEVO, BEFORE UPDATE)
--        2, 3 y 4                                   -> solo aplican/revierten si status = 'COMPLETED';
--                                                      el 3 además reacciona a cambios de status y
--                                                      aplica/revierte los detalles de wallet
--        4.1 tr_before_transaction_delete           (NUEVO, BEFORE DELETE: revierte los detalles de wallet
--                                                      que el ON DELETE CASCADE borrará sin disparar el 5.2)
--        8   tr_before_transaction_reallocation_check -> ahora llama a sp_validate_transaction_reallocation
--        8.1 tr_before_transaction_update_reallocation_check (NUEVO, BEFORE UPDATE, FOLLOWS 1.1)
--   4. Triggers de wallet_transaction_details:
--        5, 5.1 y 5.2 -> solo si la transacción padre está COMPLETED (5.1 también reacciona a
--                        cambios de transaction_id)
--        5.3 tr_before_wallet_detail_insert_val     (NUEVO, BEFORE INSERT)
--        5.4 tr_before_wallet_detail_update_val     (NUEVO, BEFORE UPDATE)
--   5. Transición de datos (paso 8): las transacciones existentes que NO están COMPLETED
--      tenían su efecto aplicado (los triggers anteriores ignoraban status). Se revierte ese
--      efecto para que los saldos cumplan la regla nueva.
--   6. Reporte de diagnóstico de saldos CREDIT (paso 9, solo SELECT).
--
-- Uso: SOLO para bases de datos con datos existentes (no borra datos).
--   mysql -u root -p < database/migrations/2026-10-03_transaction_integrity.sql
-- Para instalaciones nuevas basta con ejecutar database/schemas.sql.
-- Requiere MySQL 8.0.16+ (CHECK aplicados).
--
-- Recomendación: respaldar antes (mysqldump finanzas > respaldo.sql).
-- Ejecutar UNA sola vez: el paso 8 no es idempotente. Si se vuelve a ejecutar, el
-- ADD CONSTRAINT del paso 3 falla (el constraint ya existe) y el cliente mysql se detiene
-- antes de llegar al paso 8.
-- Nota: los triggers afectados se eliminan ANTES del UPDATE de status para que la
-- normalización no altere saldos; se recrean después.
-- ======================================================

-- ======================================================
-- 0. DIAGNÓSTICO PREVIO (ejecutar a mano y corregir antes de migrar)
-- ======================================================
-- Si esta consulta devuelve filas, el ADD CONSTRAINT chk_tx_status fallará
-- (se compara en MAYÚSCULAS porque el paso 2 normaliza):
--
-- SELECT id, status FROM transactions
--   WHERE UPPER(TRIM(status)) NOT IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED');
--
-- Transacciones cuyo efecto revertirá el paso 8 (no COMPLETED):
--
-- SELECT id, user_id, status, operation_type, payment_method,
--        source_account_id, destination_account_id, amount
--   FROM transactions
--   WHERE UPPER(TRIM(status)) <> 'COMPLETED';
--
-- Detalles LINKED_CARD sin tarjeta (no los bloquea la migración; los triggers 5.3/5.4
-- solo validan inserciones y cambios de source_type/card_id):
--
-- SELECT id, transaction_id, wallet_account_id, amount FROM wallet_transaction_details
--   WHERE source_type = 'LINKED_CARD' AND card_id IS NULL;
-- ======================================================

USE finanzas;

SET GLOBAL log_bin_trust_function_creators = 1;

-- Los UPDATE masivos no filtran por clave (Workbench los bloquea en modo seguro)
SET SQL_SAFE_UPDATES = 0;

-- ======================================================
-- 1. Eliminar triggers y procedimientos afectados
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_transaction_reallocation_check;
DROP TRIGGER IF EXISTS tr_before_transaction_update_reallocation_check;
DROP TRIGGER IF EXISTS tr_before_transaction_insert_val;
DROP TRIGGER IF EXISTS tr_before_transaction_update_val;
DROP TRIGGER IF EXISTS tr_after_transaction_insert_master;
DROP TRIGGER IF EXISTS tr_after_transaction_update;
DROP TRIGGER IF EXISTS tr_after_transaction_delete;
DROP TRIGGER IF EXISTS tr_before_transaction_delete;
DROP TRIGGER IF EXISTS tr_after_wallet_detail_insert;
DROP TRIGGER IF EXISTS tr_after_wallet_detail_update;
DROP TRIGGER IF EXISTS tr_after_wallet_detail_delete;
DROP TRIGGER IF EXISTS tr_before_wallet_detail_insert_val;
DROP TRIGGER IF EXISTS tr_before_wallet_detail_update_val;
DROP PROCEDURE IF EXISTS sp_apply_transaction_effect;
DROP PROCEDURE IF EXISTS sp_revert_transaction_effect;
DROP PROCEDURE IF EXISTS sp_wallet_detail_effect;
DROP PROCEDURE IF EXISTS sp_wallet_details_effect_for_transaction;
DROP PROCEDURE IF EXISTS sp_validate_transaction_amount;
DROP PROCEDURE IF EXISTS sp_validate_transaction_reallocation;
DROP PROCEDURE IF EXISTS sp_validate_wallet_detail;

-- ======================================================
-- 2. Normalizar transactions.status a MAYÚSCULAS
-- ======================================================
UPDATE transactions SET status = UPPER(TRIM(status));

-- ======================================================
-- 3. CHECK de transactions.status
-- ======================================================
ALTER TABLE transactions
    ADD CONSTRAINT chk_tx_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED'));

-- ======================================================
-- 4. Procedimientos almacenados (copiados de database/schemas.sql)
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

-- ======================================================
-- PROCEDIMIENTO 4: Aplicar (p_sign = 1) o revertir (p_sign = -1) todos los detalles de wallet de una transacción
-- ======================================================
-- Recorre wallet_transaction_details de p_transaction_id y llama a sp_wallet_detail_effect
-- por cada fila. Si la transacción no tiene detalles, no hace nada.
-- Lo usan:
--   - Trigger 3: cuando la transacción pasa de COMPLETED a otro estado (-1) o al revés (1).
--   - Trigger 4.1: antes de borrar una transacción COMPLETED (-1), porque el borrado en
--     cascada de sus detalles NO dispara el trigger 5.2.
DROP PROCEDURE IF EXISTS sp_wallet_details_effect_for_transaction //

CREATE PROCEDURE sp_wallet_details_effect_for_transaction(
    IN p_transaction_id BIGINT,
    IN p_sign TINYINT
)
BEGIN
    DECLARE v_done BOOLEAN DEFAULT FALSE;
    DECLARE v_source_type VARCHAR(20) DEFAULT NULL;
    DECLARE v_wallet_account_id BIGINT DEFAULT NULL;
    DECLARE v_card_id BIGINT DEFAULT NULL;
    DECLARE v_amount DECIMAL(12, 2) DEFAULT 0.00;

    DECLARE cur_details CURSOR FOR
        SELECT source_type, wallet_account_id, card_id, amount
        FROM wallet_transaction_details
        WHERE transaction_id = p_transaction_id;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = TRUE;

    OPEN cur_details;

    details_loop: LOOP
        FETCH cur_details INTO v_source_type, v_wallet_account_id, v_card_id, v_amount;
        IF v_done THEN
            LEAVE details_loop;
        END IF;

        CALL sp_wallet_detail_effect(v_source_type, v_wallet_account_id, v_card_id, v_amount, p_sign);

        -- Un SELECT ... INTO sin filas dentro del procedimiento llamado también activa el
        -- handler NOT FOUND; se reinicia la bandera para no cortar el recorrido antes de tiempo.
        SET v_done = FALSE;
    END LOOP;

    CLOSE cur_details;
END //

-- ======================================================
-- PROCEDIMIENTO 5: Validar y normalizar el monto de una transacción
-- ======================================================
-- INOUT: los triggers BEFORE pasan NEW.amount y el procedimiento lo modifica.
--   - Monto negativo -> se convierte a positivo (ABS)
--   - Monto cero     -> error (no tiene sentido contable)
DROP PROCEDURE IF EXISTS sp_validate_transaction_amount //

CREATE PROCEDURE sp_validate_transaction_amount(
    INOUT p_amount DECIMAL(12, 2)
)
BEGIN
    -- 1. Si el monto es negativo, lo pasamos a positivo (ABS)
    IF p_amount < 0 THEN
        SET p_amount = ABS(p_amount);
    END IF;

    -- 2. Bloqueamos montos en cero (no tienen sentido contable)
    IF p_amount = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: El monto de la transacción debe ser mayor a cero.';
    END IF;
END //

-- ======================================================
-- PROCEDIMIENTO 6: Validar una reubicación (REALLOCATION)
-- ======================================================
-- Si p_operation_type = 'REALLOCATION':
--   - exige origen y destino, y que sean distintos
--   - exige que la cuenta origen exista
--   - bloquea cuentas origen BENEFIT o con bank_details.can_transfer_out = FALSE
-- Para otros tipos de operación no hace nada.
DROP PROCEDURE IF EXISTS sp_validate_transaction_reallocation //

CREATE PROCEDURE sp_validate_transaction_reallocation(
    IN p_operation_type VARCHAR(20),
    IN p_source_account_id BIGINT,
    IN p_destination_account_id BIGINT
)
BEGIN
    DECLARE v_acc_type VARCHAR(20) DEFAULT NULL;
    DECLARE v_can_transfer BOOLEAN DEFAULT TRUE;

    IF p_operation_type = 'REALLOCATION' THEN

        -- Validaciones mínimas de integridad para reubicaciones
        IF p_source_account_id IS NULL OR p_destination_account_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Reubicación requiere source_account_id y destination_account_id.';
        END IF;

        IF p_source_account_id = p_destination_account_id THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Origen y destino no pueden ser iguales.';
        END IF;

        -- Obtener tipo de cuenta origen y bandera can_transfer_out (si existe)
        SELECT a.type, COALESCE(bd.can_transfer_out, TRUE)
        INTO v_acc_type, v_can_transfer
        FROM accounts a
        LEFT JOIN bank_details bd ON a.id = bd.account_id
        WHERE a.id = p_source_account_id;

        -- Si la cuenta origen no existe, el SELECT anterior no devuelve fila (v_acc_type queda NULL).
        -- Esta validación fuerza un error claro.
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

-- ======================================================
-- PROCEDIMIENTO 7: Validar un detalle de wallet
-- ======================================================
-- source_type = 'LINKED_CARD' exige card_id (sin tarjeta no hay cuenta a la que cargar).
-- Se valida con trigger y no con CHECK porque MySQL prohíbe CHECK sobre columnas
-- usadas en una FK con acción referencial ON DELETE SET NULL (fk_wallet_card sobre card_id).
DROP PROCEDURE IF EXISTS sp_validate_wallet_detail //

CREATE PROCEDURE sp_validate_wallet_detail(
    IN p_source_type VARCHAR(20),
    IN p_card_id BIGINT
)
BEGIN
    IF p_source_type = 'LINKED_CARD' AND p_card_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Un detalle de wallet con origen LINKED_CARD requiere card_id.';
    END IF;
END //

DELIMITER ;

-- ======================================================
-- 5. Triggers de transactions 1 a 4.1 (copiados de database/schemas.sql)
-- ======================================================
-- ======================================================
-- TRIGGERS DE TRANSACTIONS (1 a 4.1)
-- ======================================================
-- Regla de estado: solo las transacciones con status = 'COMPLETED' afectan saldos
-- (los suyos y los de sus wallet_transaction_details).
--
-- Orden por evento sobre transactions:
--   BEFORE INSERT: 1 -> 8 (FOLLOWS)
--   BEFORE UPDATE: 1.1 -> 8.1 (FOLLOWS)
--   BEFORE DELETE: 4.1
--   AFTER INSERT: 2 / AFTER UPDATE: 3 / AFTER DELETE: 4
--
-- Nota: las acciones en cascada de las FKs (p. ej. ON DELETE SET NULL de
-- source_account_id / destination_account_id al borrar una cuenta) NO disparan
-- triggers en MySQL, así que no pasan por las validaciones de UPDATE ni por el trigger 3.
DELIMITER //

-- ======================================================
-- TRIGGER 1: Validación de monto antes de insertar transacción
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_transaction_insert_val //

CREATE TRIGGER tr_before_transaction_insert_val
BEFORE INSERT ON transactions
FOR EACH ROW
BEGIN
    -- ABS de montos negativos y bloqueo de monto 0 (modifica NEW.amount vía INOUT)
    CALL sp_validate_transaction_amount(NEW.amount);
END //

-- ======================================================
-- TRIGGER 1.1: Validación de monto antes de actualizar transacción
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_transaction_update_val //

CREATE TRIGGER tr_before_transaction_update_val
BEFORE UPDATE ON transactions
FOR EACH ROW
BEGIN
    -- Misma regla que en INSERT: un UPDATE no puede dejar amount <= 0
    CALL sp_validate_transaction_amount(NEW.amount);
END //

-- ======================================================
-- TRIGGER 2: Aplicar saldos después de insertar transacción
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_transaction_insert_master //

CREATE TRIGGER tr_after_transaction_insert_master
AFTER INSERT ON transactions
FOR EACH ROW
BEGIN
    -- Solo las transacciones COMPLETED afectan saldos.
    -- (Una transacción recién insertada aún no tiene detalles de wallet.)
    IF NEW.status = 'COMPLETED' THEN
        -- Aplica el efecto del NEW (ignora WALLET dentro del procedimiento)
        CALL sp_apply_transaction_effect(
            NEW.operation_type, NEW.payment_method,
            NEW.source_account_id, NEW.destination_account_id, NEW.amount
        );
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
    -- Solo se recalcula si cambió algún dato que afecta saldos
    -- (<=> compara también NULLs). Así una edición de concepto o tags no
    -- redistribuye saldo a favor / deuda de una tarjeta de crédito.
    IF NOT (OLD.operation_type <=> NEW.operation_type
        AND OLD.payment_method <=> NEW.payment_method
        AND OLD.source_account_id <=> NEW.source_account_id
        AND OLD.destination_account_id <=> NEW.destination_account_id
        AND OLD.amount <=> NEW.amount
        AND OLD.status <=> NEW.status) THEN

        -- A. REVERTIR valores antiguos (solo si estaban aplicados)
        IF OLD.status = 'COMPLETED' THEN
            CALL sp_revert_transaction_effect(
                OLD.operation_type, OLD.payment_method,
                OLD.source_account_id, OLD.destination_account_id, OLD.amount
            );
        END IF;

        -- B. APLICAR valores nuevos (solo si quedan COMPLETED)
        IF NEW.status = 'COMPLETED' THEN
            CALL sp_apply_transaction_effect(
                NEW.operation_type, NEW.payment_method,
                NEW.source_account_id, NEW.destination_account_id, NEW.amount
            );
        END IF;
    END IF;

    -- C. Detalles de wallet: su efecto depende solo del estado de la transacción padre
    --    (igual que en los triggers 5, 5.1 y 5.2). Si no tiene detalles, no hace nada.
    IF OLD.status = 'COMPLETED' AND NEW.status <> 'COMPLETED' THEN
        CALL sp_wallet_details_effect_for_transaction(NEW.id, -1);
    ELSEIF OLD.status <> 'COMPLETED' AND NEW.status = 'COMPLETED' THEN
        CALL sp_wallet_details_effect_for_transaction(NEW.id, 1);
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
    -- Solo se revierte lo que estaba aplicado
    IF OLD.status = 'COMPLETED' THEN
        CALL sp_revert_transaction_effect(
            OLD.operation_type, OLD.payment_method,
            OLD.source_account_id, OLD.destination_account_id, OLD.amount
        );
    END IF;
END //

-- ======================================================
-- TRIGGER 4.1: Restituir saldos de los detalles de wallet antes de eliminar la transacción
-- ======================================================
-- Al borrar una transacción, sus wallet_transaction_details se eliminan por
-- ON DELETE CASCADE (fk_wallet_tx). MySQL NO dispara triggers en acciones en cascada,
-- así que el trigger 5.2 no se ejecuta: este trigger revierte su efecto aquí.
-- No hay doble reversión: el 5.2 solo corre en un DELETE directo sobre
-- wallet_transaction_details, nunca en la cascada.
-- Si el DELETE falla después (p. ej. por otra FK), InnoDB deshace también esta reversión.
DROP TRIGGER IF EXISTS tr_before_transaction_delete //

CREATE TRIGGER tr_before_transaction_delete
BEFORE DELETE ON transactions
FOR EACH ROW
BEGIN
    IF OLD.status = 'COMPLETED' THEN
        CALL sp_wallet_details_effect_for_transaction(OLD.id, -1);
    END IF;
END //

DELIMITER ;

-- ======================================================
-- 6. Triggers de wallet_transaction_details 5 a 5.4 (copiados de database/schemas.sql)
-- ======================================================
-- ======================================================
-- TRIGGERS DE WALLET_TRANSACTION_DETAILS (5 a 5.4)
-- ======================================================
-- Los efectos de un detalle solo se aplican si su transacción padre está COMPLETED.
--
-- Regla de cascadas (MySQL NO dispara triggers en acciones referenciales):
--   - Borrar la transacción padre (fk_wallet_tx CASCADE): lo revierte el trigger 4.1.
--   - Borrar una tarjeta (fk_wallet_card SET NULL): el movimiento histórico ya ocurrió y
--     NO se revierte; el detalle queda LINKED_CARD con card_id NULL. El trigger 5.1 no
--     reacciona (la cascada no lo dispara) y, a partir de ahí, ese detalle ya no afecta
--     saldos aunque se edite o borre (sp_wallet_detail_effect ignora card_id NULL).
--   - Borrar la cuenta wallet (fk_wallet_account CASCADE): tampoco se revierte.
DELIMITER //

-- ======================================================
-- TRIGGER 5: Aplicar saldos al insertar detalle de wallet
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_wallet_detail_insert //

CREATE TRIGGER tr_after_wallet_detail_insert
AFTER INSERT ON wallet_transaction_details
FOR EACH ROW
BEGIN
    DECLARE v_status VARCHAR(20) DEFAULT NULL;

    SELECT status INTO v_status FROM transactions WHERE id = NEW.transaction_id;

    IF v_status = 'COMPLETED' THEN
        CALL sp_wallet_detail_effect(NEW.source_type, NEW.wallet_account_id, NEW.card_id, NEW.amount, 1);
    END IF;
END //

-- ======================================================
-- TRIGGER 5.1: Corregir saldos al actualizar detalle de wallet
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_wallet_detail_update //

CREATE TRIGGER tr_after_wallet_detail_update
AFTER UPDATE ON wallet_transaction_details
FOR EACH ROW
BEGIN
    DECLARE v_old_status VARCHAR(20) DEFAULT NULL;
    DECLARE v_new_status VARCHAR(20) DEFAULT NULL;

    -- Solo si cambió algún dato que afecta saldos (incluido mover el detalle a otra transacción)
    IF NOT (OLD.transaction_id <=> NEW.transaction_id
        AND OLD.source_type <=> NEW.source_type
        AND OLD.wallet_account_id <=> NEW.wallet_account_id
        AND OLD.card_id <=> NEW.card_id
        AND OLD.amount <=> NEW.amount) THEN

        SELECT status INTO v_old_status FROM transactions WHERE id = OLD.transaction_id;
        SELECT status INTO v_new_status FROM transactions WHERE id = NEW.transaction_id;

        -- A. REVERTIR valores antiguos (solo si estaban aplicados)
        IF v_old_status = 'COMPLETED' THEN
            CALL sp_wallet_detail_effect(OLD.source_type, OLD.wallet_account_id, OLD.card_id, OLD.amount, -1);
        END IF;

        -- B. APLICAR valores nuevos (solo si la transacción padre está COMPLETED)
        IF v_new_status = 'COMPLETED' THEN
            CALL sp_wallet_detail_effect(NEW.source_type, NEW.wallet_account_id, NEW.card_id, NEW.amount, 1);
        END IF;
    END IF;
END //

-- ======================================================
-- TRIGGER 5.2: Restituir saldos al eliminar detalle de wallet
-- ======================================================
-- Solo se dispara en un DELETE directo; el borrado en cascada desde transactions
-- lo cubre el trigger 4.1.
DROP TRIGGER IF EXISTS tr_after_wallet_detail_delete //

CREATE TRIGGER tr_after_wallet_detail_delete
AFTER DELETE ON wallet_transaction_details
FOR EACH ROW
BEGIN
    DECLARE v_status VARCHAR(20) DEFAULT NULL;

    SELECT status INTO v_status FROM transactions WHERE id = OLD.transaction_id;

    IF v_status = 'COMPLETED' THEN
        CALL sp_wallet_detail_effect(OLD.source_type, OLD.wallet_account_id, OLD.card_id, OLD.amount, -1);
    END IF;
END //

-- ======================================================
-- TRIGGER 5.3: Validar detalle de wallet antes de insertar
-- ======================================================
-- LINKED_CARD exige card_id. Es trigger y no CHECK porque MySQL prohíbe CHECK sobre
-- card_id: la columna está en una FK con ON DELETE SET NULL (fk_wallet_card).
DROP TRIGGER IF EXISTS tr_before_wallet_detail_insert_val //

CREATE TRIGGER tr_before_wallet_detail_insert_val
BEFORE INSERT ON wallet_transaction_details
FOR EACH ROW
BEGIN
    CALL sp_validate_wallet_detail(NEW.source_type, NEW.card_id);
END //

-- ======================================================
-- TRIGGER 5.4: Validar detalle de wallet antes de actualizar
-- ======================================================
-- Solo valida si cambió source_type o card_id: un detalle histórico que quedó
-- LINKED_CARD con card_id NULL (tarjeta borrada) se puede seguir editando
-- (monto, cashback) sin error.
DROP TRIGGER IF EXISTS tr_before_wallet_detail_update_val //

CREATE TRIGGER tr_before_wallet_detail_update_val
BEFORE UPDATE ON wallet_transaction_details
FOR EACH ROW
BEGIN
    IF NOT (OLD.source_type <=> NEW.source_type AND OLD.card_id <=> NEW.card_id) THEN
        CALL sp_validate_wallet_detail(NEW.source_type, NEW.card_id);
    END IF;
END //

DELIMITER ;

-- ======================================================
-- 7. Triggers 8 y 8.1 (copiados de database/schemas.sql)
-- ======================================================
-- ======================================================
-- TRIGGERS DE TRANSACTIONS: validación de reubicaciones (8 y 8.1)
-- ======================================================
-- Son los segundos BEFORE INSERT / BEFORE UPDATE sobre transactions: FOLLOWS fija
-- que se ejecuten después de los triggers 1 / 1.1 (que normalizan el monto).
DELIMITER //

-- ======================================================
-- TRIGGER 8: Validación de reubicaciones al insertar
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_transaction_reallocation_check //

CREATE TRIGGER tr_before_transaction_reallocation_check
BEFORE INSERT ON transactions
FOR EACH ROW
FOLLOWS tr_before_transaction_insert_val
BEGIN
    CALL sp_validate_transaction_reallocation(
        NEW.operation_type, NEW.source_account_id, NEW.destination_account_id
    );
END //

-- ======================================================
-- TRIGGER 8.1: Validación de reubicaciones al actualizar
-- ======================================================
-- Solo valida si cambió el tipo de operación, el origen o el destino, o si la
-- transacción pasa a COMPLETED (en ese momento mueve dinero). Así, editar el
-- concepto o cancelar una reubicación vieja no falla aunque la cuenta origen
-- haya cambiado después (p. ej. can_transfer_out = FALSE).
DROP TRIGGER IF EXISTS tr_before_transaction_update_reallocation_check //

CREATE TRIGGER tr_before_transaction_update_reallocation_check
BEFORE UPDATE ON transactions
FOR EACH ROW
FOLLOWS tr_before_transaction_update_val
BEGIN
    IF (NOT (OLD.operation_type <=> NEW.operation_type
            AND OLD.source_account_id <=> NEW.source_account_id
            AND OLD.destination_account_id <=> NEW.destination_account_id))
        OR (OLD.status <> 'COMPLETED' AND NEW.status = 'COMPLETED') THEN

        CALL sp_validate_transaction_reallocation(
            NEW.operation_type, NEW.source_account_id, NEW.destination_account_id
        );
    END IF;
END //

DELIMITER ;

-- ======================================================
-- 8. Transición: revertir el efecto de las transacciones existentes que NO están COMPLETED
-- ======================================================
-- Los triggers anteriores aplicaban el efecto de toda transacción sin importar su status.
-- Con la regla nueva, una PENDING/FAILED/CANCELLED no debe afectar saldos; si se dejara su
-- efecto aplicado, al pasarla a COMPLETED se aplicaría dos veces y al borrarla no se revertiría.
-- Se revierte aquí su efecto propio y el de sus detalles de wallet, con los mismos
-- procedimientos que usan los triggers.
-- Se ejecuta dentro de una transacción: si algún UPDATE falla (p. ej. chk_credit_used por un
-- saldo ya descuadrado), el cliente mysql se detiene y la transacción no se confirma.
-- En ese caso los pasos 1-7 ya quedaron aplicados: corregir el dato y ejecutar a mano
-- solo este paso (y el 9).
DROP PROCEDURE IF EXISTS sp_tmp_revert_non_completed_transactions;

DELIMITER //

CREATE PROCEDURE sp_tmp_revert_non_completed_transactions()
BEGIN
    DECLARE v_done BOOLEAN DEFAULT FALSE;
    DECLARE v_id BIGINT DEFAULT NULL;
    DECLARE v_operation_type VARCHAR(20) DEFAULT NULL;
    DECLARE v_payment_method VARCHAR(20) DEFAULT NULL;
    DECLARE v_source_account_id BIGINT DEFAULT NULL;
    DECLARE v_destination_account_id BIGINT DEFAULT NULL;
    DECLARE v_amount DECIMAL(12, 2) DEFAULT 0.00;

    DECLARE cur_tx CURSOR FOR
        SELECT id, operation_type, payment_method, source_account_id, destination_account_id, amount
        FROM transactions
        WHERE status <> 'COMPLETED'
        ORDER BY id;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = TRUE;

    OPEN cur_tx;

    tx_loop: LOOP
        FETCH cur_tx INTO v_id, v_operation_type, v_payment_method,
            v_source_account_id, v_destination_account_id, v_amount;
        IF v_done THEN
            LEAVE tx_loop;
        END IF;

        -- Efecto propio (ignora WALLET dentro del procedimiento)
        CALL sp_revert_transaction_effect(
            v_operation_type, v_payment_method,
            v_source_account_id, v_destination_account_id, v_amount
        );

        -- Efecto de sus detalles de wallet
        CALL sp_wallet_details_effect_for_transaction(v_id, -1);

        -- Un NOT FOUND dentro de los procedimientos llamados también activa el handler
        SET v_done = FALSE;
    END LOOP;

    CLOSE cur_tx;
END //

DELIMITER ;

START TRANSACTION;
CALL sp_tmp_revert_non_completed_transactions();
COMMIT;

DROP PROCEDURE IF EXISTS sp_tmp_revert_non_completed_transactions;

-- ======================================================
-- 9. DIAGNÓSTICO DE SALDOS CREDIT (solo reporte, sin UPDATE)
-- ======================================================
-- Limitación: los saldos ya descuadrados NO se pueden recalcular automáticamente porque
-- el esquema no guarda el saldo inicial de cada cuenta (no existe opening_balance):
-- current_balance y credit_used son acumulados y no se sabe con qué valor nació la cuenta.
--
-- Este reporte compara, por cada cuenta CREDIT, la posición actual
--   (credit_used - current_balance)   [deuda menos saldo a favor]
-- contra la suma de efectos de las transacciones COMPLETED según la lógica de los procedimientos:
--   cargos_tx     = EXPENSE / REALLOCATION con origen en la cuenta (payment_method <> 'WALLET')
--   cargos_wallet = detalles LINKED_CARD con tarjeta de la cuenta (transacción padre COMPLETED)
--   pagos         = INCOME / REALLOCATION con destino en la cuenta (payment_method <> 'WALLET';
--                   reducen credit_used y el sobrepago va a current_balance)
-- diferencia = posición actual - (cargos_tx + cargos_wallet - pagos)
-- Si los triggers siempre funcionaron bien, diferencia es la posición inicial de la cuenta
-- (deuda con la que se dio de alta). Una diferencia inesperada indica un descuadre a revisar
-- a mano. Cargos de tarjetas ya borradas (card_id NULL) no se pueden atribuir y aparecen
-- dentro de la diferencia.
SELECT
    a.id AS account_id,
    a.user_id,
    a.name,
    cd.credit_used,
    cd.credit_limit,
    a.current_balance,
    (cd.credit_used - a.current_balance) AS posicion_actual,
    COALESCE(ch.total, 0.00) AS cargos_tx,
    COALESCE(wc.total, 0.00) AS cargos_wallet,
    COALESCE(pg.total, 0.00) AS pagos,
    (COALESCE(ch.total, 0.00) + COALESCE(wc.total, 0.00) - COALESCE(pg.total, 0.00)) AS neto_transacciones,
    (cd.credit_used - a.current_balance)
        - (COALESCE(ch.total, 0.00) + COALESCE(wc.total, 0.00) - COALESCE(pg.total, 0.00)) AS diferencia
FROM accounts a
INNER JOIN credit_details cd ON cd.account_id = a.id
LEFT JOIN (
    SELECT source_account_id AS account_id, SUM(amount) AS total
    FROM transactions
    WHERE status = 'COMPLETED'
      AND payment_method <> 'WALLET'
      AND operation_type IN ('EXPENSE', 'REALLOCATION')
      AND source_account_id IS NOT NULL
    GROUP BY source_account_id
) ch ON ch.account_id = a.id
LEFT JOIN (
    SELECT c.account_id, SUM(w.amount) AS total
    FROM wallet_transaction_details w
    INNER JOIN transactions t ON t.id = w.transaction_id
    INNER JOIN cards c ON c.id = w.card_id
    WHERE t.status = 'COMPLETED'
      AND w.source_type = 'LINKED_CARD'
    GROUP BY c.account_id
) wc ON wc.account_id = a.id
LEFT JOIN (
    SELECT destination_account_id AS account_id, SUM(amount) AS total
    FROM transactions
    WHERE status = 'COMPLETED'
      AND payment_method <> 'WALLET'
      AND operation_type IN ('INCOME', 'REALLOCATION')
      AND destination_account_id IS NOT NULL
    GROUP BY destination_account_id
) pg ON pg.account_id = a.id
WHERE a.type = 'CREDIT'
ORDER BY ABS(
    (cd.credit_used - a.current_balance)
        - (COALESCE(ch.total, 0.00) + COALESCE(wc.total, 0.00) - COALESCE(pg.total, 0.00))
) DESC;

-- Cuentas CREDIT con valores fuera de rango (las reversiones futuras pueden fallar por
-- chk_credit_used / chk_credit_used_limit):
SELECT a.id AS account_id, a.name, cd.credit_used, cd.credit_limit, a.current_balance
FROM accounts a
INNER JOIN credit_details cd ON cd.account_id = a.id
WHERE a.type = 'CREDIT'
  AND (cd.credit_used < 0 OR cd.credit_used > cd.credit_limit OR a.current_balance < 0);
