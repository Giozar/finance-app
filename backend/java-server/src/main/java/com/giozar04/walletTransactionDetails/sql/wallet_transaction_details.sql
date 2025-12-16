CREATE TABLE wallet_transaction_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    wallet_account_id BIGINT NOT NULL,
    card_id BIGINT NULL,
    amount DECIMAL(12,2) NOT NULL,
    cashback_percentage DECIMAL(5,2) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_wallet_tx FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    CONSTRAINT fk_wallet_account FOREIGN KEY (wallet_account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_wallet_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE SET NULL
);

CREATE INDEX idx_wallet_transaction ON wallet_transaction_details(transaction_id);
CREATE INDEX idx_wallet_payment_wallet ON wallet_transaction_details(wallet_account_id);
CREATE INDEX idx_wallet_payment_card ON wallet_transaction_details(card_id);
