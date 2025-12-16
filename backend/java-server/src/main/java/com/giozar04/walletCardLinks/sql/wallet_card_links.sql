CREATE TABLE wallet_card_links (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wallet_account_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_wallet_card_wallet FOREIGN KEY (wallet_account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_wallet_card_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
);

CREATE INDEX idx_wallet_card_wallet ON wallet_card_links(wallet_account_id);
CREATE INDEX idx_wallet_card_card ON wallet_card_links(card_id);
