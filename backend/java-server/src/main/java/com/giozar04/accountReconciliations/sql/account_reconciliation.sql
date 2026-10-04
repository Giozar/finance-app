-- ======================================================
-- Reconciliación de cuentas (solo documentación)
-- ======================================================
-- Fuente de verdad: database/schemas.sql (instalación nueva) y
-- database/migrations/2026-10-04_account_reconciliation.sql (BD existentes).
-- Esta feature NO tiene tabla propia: lee la vista v_account_reconciliation y escribe
-- únicamente a través del procedimiento sp_reconcile_account.
--
-- Columnas de apertura (solo lectura para el backend; las fijan triggers al crear):
--   accounts.opening_balance           DECIMAL(12, 2) NOT NULL DEFAULT 0.00
--   credit_details.opening_credit_used DECIMAL(12, 2) NOT NULL DEFAULT 0.00 (CHECK >= 0)

-- ------------------------------------------------------
-- VISTA: v_account_reconciliation (una fila por cuenta)
-- ------------------------------------------------------
-- account_id     BIGINT
-- user_id        BIGINT
-- account_name   VARCHAR
-- account_type   VARCHAR  ('CASH', 'DEBIT', 'CREDIT', 'WALLET', 'BENEFIT', 'SAVINGS', 'INVESTMENT')
-- opening_net    DECIMAL(14, 2)  opening_balance - COALESCE(opening_credit_used, 0)
-- total_inflows  DECIMAL(14, 2)  transacciones COMPLETED (no WALLET) INCOME/REALLOCATION hacia la cuenta
-- total_outflows DECIMAL(14, 2)  transacciones COMPLETED (no WALLET) EXPENSE/REALLOCATION desde la cuenta
--                                + detalles de wallet WALLET_BALANCE / LINKED_CARD
-- expected_net   DECIMAL(14, 2)  opening_net + total_inflows - total_outflows
-- actual_net     DECIMAL(14, 2)  current_balance - COALESCE(credit_used, 0)
-- difference     DECIMAL(14, 2)  actual_net - expected_net (0 = cuenta cuadrada)
--
-- Consultas usadas por AccountReconciliationRepositoryMySQL:
SELECT account_id, user_id, account_name, account_type,
       opening_net, total_inflows, total_outflows, expected_net, actual_net, difference
FROM v_account_reconciliation;
-- ... WHERE user_id = ?    (por usuario)
-- ... WHERE account_id = ? (por cuenta)

-- ------------------------------------------------------
-- PROCEDIMIENTO: sp_reconcile_account(IN p_account_id BIGINT)
-- ------------------------------------------------------
-- Ajusta la cuenta para que difference = 0:
--   no CREDIT -> current_balance = expected_net
--   CREDIT    -> expected_net >= 0: credit_used = 0, current_balance = expected_net
--                expected_net <  0: credit_used = -expected_net, current_balance = 0
-- Lanza SIGNAL SQLSTATE '45000' (mensaje en español) si la cuenta no existe, si una cuenta
-- CREDIT no tiene credit_details o si la deuda supera credit_limit.
-- No abre transacción: el backend hace commit tras la llamada (rollback si falla).
-- Llamada desde Java (CallableStatement):
--   {CALL sp_reconcile_account(?)}
CALL sp_reconcile_account(1);
