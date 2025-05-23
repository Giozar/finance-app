CREATE TABLE card_transaction_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,

    -- Nuevos campos para compras a plazos
    installment_months INT NULL,             -- Núm. de meses; NULL = contado
    interest_free BOOLEAN NOT NULL DEFAULT FALSE,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                 ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_card_tx FOREIGN KEY (transaction_id)
        REFERENCES transactions(id) ON DELETE CASCADE,
    CONSTRAINT fk_card_detail FOREIGN KEY (card_id)
        REFERENCES cards(id) ON DELETE CASCADE
);

CREATE INDEX idx_card_tx            ON card_transaction_details(transaction_id);
CREATE INDEX idx_card_detail_card   ON card_transaction_details(card_id);
