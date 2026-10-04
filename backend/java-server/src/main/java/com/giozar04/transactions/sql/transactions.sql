-- Documentación: la fuente de verdad es database/schemas.sql (sección 8. TRANSACTIONS).
-- Triggers y procedimientos (efecto en saldos, partes, reubicaciones, detalles) viven solo allí.
-- El backend replica sus reglas con mensajes claros (transactions/application/validation).
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    parent_transaction_id BIGINT NULL,
        -- operation_type: 'INCOME', 'EXPENSE', 'REALLOCATION' (reubicación entre cuentas propias)
    operation_type VARCHAR(20) NOT NULL,
        -- payment_method: 'CASH', 'CARD', 'WIRE_TRANSFER', 'INTERNAL', 'QR', 'CODI', 'WALLET'
    payment_method VARCHAR(20) NOT NULL,
        -- status: 'PENDING', 'COMPLETED', 'FAILED', 'CANCELLED'. Solo COMPLETED afecta saldos
        -- (triggers 2, 3, 4 y 4.1, y los de wallet_transaction_details 5, 5.1 y 5.2)
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
        -- Partes por tipo de operación (sp_validate_transaction_parties, triggers 8.2 y 8.3;
        -- en REALLOCATION, origen/destino los valida sp_validate_transaction_reallocation):
        --   INCOME       -> destination_account_id y external_entity_id obligatorios; source_account_id NULL
        --   EXPENSE      -> source_account_id y external_entity_id obligatorios; destination_account_id NULL
        --   REALLOCATION -> source_account_id y destination_account_id (distintos); external_entity_id NULL
        -- En WALLET (solo EXPENSE), source_account_id es la cuenta que financia el pago: la
        -- wallet (WALLET_BALANCE) o la cuenta de la tarjeta vinculada (LINKED_CARD). En CARD, es
        -- la cuenta de la tarjeta de card_transaction_details.
        -- Son NULL-ables por las reglas anteriores y por ON DELETE SET NULL.
    source_account_id BIGINT NULL,
    destination_account_id BIGINT NULL,
    external_entity_id BIGINT NULL,
    category_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    concept VARCHAR(100) NOT NULL,
    description TEXT NULL,
    receipt_url VARCHAR(255) NULL,
    comments TEXT NULL,
    date DATETIME NOT NULL,
    timezone VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tx_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_tx_parent FOREIGN KEY (parent_transaction_id) REFERENCES transactions (id) ON DELETE SET NULL,
    CONSTRAINT fk_tx_source_account FOREIGN KEY (source_account_id) REFERENCES accounts (id) ON DELETE SET NULL,
    CONSTRAINT fk_tx_destination_account FOREIGN KEY (destination_account_id) REFERENCES accounts (id) ON DELETE SET NULL,
    CONSTRAINT fk_tx_entity FOREIGN KEY (external_entity_id) REFERENCES external_entities (id) ON DELETE SET NULL,
    CONSTRAINT fk_tx_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT chk_tx_operation_type CHECK (operation_type IN ('INCOME', 'EXPENSE', 'REALLOCATION')),
    CONSTRAINT chk_tx_payment_method CHECK (payment_method IN ('CASH', 'CARD', 'WIRE_TRANSFER', 'INTERNAL', 'QR', 'CODI', 'WALLET')),
        -- INTERNAL (movimiento entre cuentas propias) solo tiene sentido en una REALLOCATION
    CONSTRAINT chk_tx_internal_reallocation CHECK (payment_method <> 'INTERNAL' OR operation_type = 'REALLOCATION'),
        -- WALLET (pago con wallet) solo tiene sentido en un gasto
    CONSTRAINT chk_tx_wallet_expense CHECK (payment_method <> 'WALLET' OR operation_type = 'EXPENSE'),
    CONSTRAINT chk_tx_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED'))
);

-- idx_tx_category e idx_tx_parent sustituyen a los índices implícitos que InnoDB crea para
-- fk_tx_category y fk_tx_parent (MySQL los elimina al existir uno equivalente).
CREATE INDEX idx_tx_user_id ON transactions (user_id);
CREATE INDEX idx_tx_date ON transactions (date);
CREATE INDEX idx_tx_type ON transactions (operation_type);
CREATE INDEX idx_tx_method ON transactions (payment_method);
CREATE INDEX idx_tx_source_account ON transactions (source_account_id);
CREATE INDEX idx_tx_destination_account ON transactions (destination_account_id);
CREATE INDEX idx_tx_entity ON transactions (external_entity_id);
CREATE INDEX idx_tx_category ON transactions (category_id);
CREATE INDEX idx_tx_parent ON transactions (parent_transaction_id);
