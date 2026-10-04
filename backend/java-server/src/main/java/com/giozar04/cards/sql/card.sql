CREATE TABLE cards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    card_type VARCHAR(20) NOT NULL,                  -- 'PHYSICAL' o 'DIGITAL'
    card_number VARCHAR(4) NOT NULL,
    expiration_date DATETIME NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',             -- 'ACTIVE', 'BLOCKED' o 'EXPIRED'
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_cards_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT chk_card_type CHECK (card_type IN ('PHYSICAL', 'DIGITAL')),
    CONSTRAINT chk_card_status CHECK (status IN ('ACTIVE', 'BLOCKED', 'EXPIRED'))
);

CREATE INDEX idx_cards_account_id ON cards(account_id);
CREATE INDEX idx_cards_card_type ON cards(card_type);
