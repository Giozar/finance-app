CREATE TABLE transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    operation_type VARCHAR(20) NOT NULL,              -- 'INCOME', 'EXPENSE' o 'REALLOCATION'
    payment_method VARCHAR(20) NOT NULL,              -- 'CASH', 'CARD', 'WIRE_TRANSFER', 'INTERNAL', 'QR', 'CODI', 'WALLET' (INTERNAL solo con REALLOCATION)

    source_account_id BIGINT NULL,
    destination_account_id BIGINT NULL,
    external_entity_id BIGINT NULL,

    amount DECIMAL(12,2) NOT NULL,
    concept VARCHAR(100) NOT NULL,
    category VARCHAR(100) NOT NULL,                   -- FK o denormalizado
    description TEXT NULL,
    comments TEXT NULL,
    date DATETIME NOT NULL,
    timezone VARCHAR(50) NOT NULL,

    tags TEXT NULL,                                   -- CSV o lista serializada

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tx_source_account FOREIGN KEY (source_account_id) REFERENCES accounts(id) ON DELETE SET NULL,
    CONSTRAINT fk_tx_destination_account FOREIGN KEY (destination_account_id) REFERENCES accounts(id) ON DELETE SET NULL,
    CONSTRAINT fk_tx_entity FOREIGN KEY (external_entity_id) REFERENCES external_entities(id) ON DELETE SET NULL
);

CREATE INDEX idx_tx_type ON transactions(operation_type);
CREATE INDEX idx_tx_method ON transactions(payment_method);
CREATE INDEX idx_tx_source_account ON transactions(source_account_id);
CREATE INDEX idx_tx_destination_account ON transactions(destination_account_id);
CREATE INDEX idx_tx_entity ON transactions(external_entity_id);
