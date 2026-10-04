-- Tabla de cuentas
-- Documentación: la fuente de verdad del esquema es database/schemas.sql.
-- Reconciliación (migración 2026-10-04_account_reconciliation.sql):
--   accounts.opening_balance y credit_details.opening_credit_used son de SOLO LECTURA para el backend:
--   los fijan los triggers tr_before_account_insert_opening / tr_before_credit_details_insert_opening
--   al crear (= current_balance / credit_used iniciales). No se envían en INSERT ni en UPDATE.
CREATE TABLE accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bank_client_id BIGINT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,                       -- 'CASH', 'DEBIT', 'CREDIT', 'WALLET', 'BENEFIT', 'SAVINGS' o 'INVESTMENT'
    current_balance DECIMAL(14, 2) DEFAULT 0.00,
    opening_balance DECIMAL(12, 2) NOT NULL DEFAULT 0.00,  -- saldo de apertura (lo fija el trigger 9 al crear)
    account_number VARCHAR(50),
    clabe VARCHAR(50),
    credit_limit DECIMAL(14, 2),
    cutoff_day INT,
    payment_day INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Clave foránea (si aplica)
    CONSTRAINT fk_accounts_bank_client FOREIGN KEY (bank_client_id) REFERENCES bank_clients(id) ON DELETE SET NULL,
    CONSTRAINT chk_account_type CHECK (type IN ('CASH', 'DEBIT', 'CREDIT', 'WALLET', 'BENEFIT', 'SAVINGS', 'INVESTMENT'))
);

-- Índices
CREATE INDEX idx_accounts_bank_client_id ON accounts(bank_client_id);
CREATE INDEX idx_accounts_type ON accounts(type);
