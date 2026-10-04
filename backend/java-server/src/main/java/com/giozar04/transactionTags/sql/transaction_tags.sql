-- Documentación: la fuente de verdad es database/schemas.sql (sección 9. TRANSACTION_TAGS).
-- La escribe TransactionTagRepositoryMySQL dentro de la unidad de trabajo de transactions.
CREATE TABLE IF NOT EXISTS transaction_tags (
    transaction_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (transaction_id, tag_id),
    CONSTRAINT fk_tt_transaction
        FOREIGN KEY (transaction_id) REFERENCES transactions (id) ON DELETE CASCADE,
    CONSTRAINT fk_tt_tag
        FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
);

CREATE INDEX idx_tt_tag_id ON transaction_tags (tag_id);
