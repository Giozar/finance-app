-- Tabla de cuentas (versión compatible con MySQL)
CREATE TABLE accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    bank_client_id BIGINT NULL,
    name VARCHAR(100) NOT NULL,
    type ENUM('debit', 'credit', 'savings', 'cash') NOT NULL,
    current_balance DECIMAL(14, 2) DEFAULT 0.00,
    bank_name VARCHAR(100),
    account_number VARCHAR(50),
    clabe VARCHAR(50),
    credit_limit DECIMAL(14, 2),
    cutoff_day INT,
    payment_day INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Restricciones de claves foráneas
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_accounts_bank_client FOREIGN KEY (bank_client_id) REFERENCES bank_clients(id) ON DELETE SET NULL
);

-- Índices útiles
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_accounts_bank_client_id ON accounts(bank_client_id);
CREATE INDEX idx_accounts_type ON accounts(type);
