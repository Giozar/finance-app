CREATE TABLE wallet_transaction_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL,
    source_type VARCHAR(20) NOT NULL,                -- 'WALLET_BALANCE' o 'LINKED_CARD'
    wallet_account_id BIGINT NOT NULL,
    card_id BIGINT NULL,
    amount DECIMAL(12,2) NOT NULL,
    cashback_rate DECIMAL(9,6) NULL,                 -- fracción 0-1 (0.05 = 5%)
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_wallet_tx FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    CONSTRAINT fk_wallet_account FOREIGN KEY (wallet_account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_wallet_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE SET NULL,

    CONSTRAINT chk_wallet_amount CHECK (amount > 0),
    CONSTRAINT chk_wallet_source_type CHECK (source_type IN ('WALLET_BALANCE', 'LINKED_CARD')),
    CONSTRAINT chk_wallet_cashback_rate CHECK (cashback_rate IS NULL OR (cashback_rate >= 0 AND cashback_rate <= 1))
);

CREATE INDEX idx_wallet_transaction ON wallet_transaction_details(transaction_id);
CREATE INDEX idx_wallet_payment_wallet ON wallet_transaction_details(wallet_account_id);
CREATE INDEX idx_wallet_payment_card ON wallet_transaction_details(card_id);
