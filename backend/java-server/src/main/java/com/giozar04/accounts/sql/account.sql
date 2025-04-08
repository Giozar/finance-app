-- Tabla de cuentas
CREATE TABLE accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bank_client_id BIGINT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    current_balance DECIMAL(14, 2) DEFAULT 0.00,
    account_number VARCHAR(50),
    clabe VARCHAR(50),
    credit_limit DECIMAL(14, 2),
    cutoff_day INT,
    payment_day INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Clave foránea (si aplica)
    CONSTRAINT fk_accounts_bank_client FOREIGN KEY (bank_client_id) REFERENCES bank_clients(id) ON DELETE SET NULL
);

-- Índices
CREATE INDEX idx_accounts_bank_client_id ON accounts(bank_client_id);
CREATE INDEX idx_accounts_type ON accounts(type);
