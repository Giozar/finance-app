CREATE TABLE IF NOT EXISTS account_cashback_settings (
    account_id          BIGINT          PRIMARY KEY,
    cashback_enabled    BOOLEAN         NOT NULL DEFAULT FALSE,
    default_cashback_rate DECIMAL(9, 6) NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_cashback_account
        FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,

    CONSTRAINT chk_cashback_rate
        CHECK (
            default_cashback_rate IS NULL
            OR (default_cashback_rate >= 0 AND default_cashback_rate <= 1)
        )
);
