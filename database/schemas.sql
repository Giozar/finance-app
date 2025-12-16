USE finanzas;

-- ======================================================
-- 1. USERS
-- ======================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    global_balance DECIMAL(14, 2) DEFAULT 0.00,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- ======================================================
-- 2. BANK_CLIENTS
-- ======================================================

CREATE TABLE IF NOT EXISTS bank_clients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    client_number VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_bank_clients_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    UNIQUE KEY unique_client_per_bank (user_id, bank_name, client_number)
);

CREATE INDEX idx_bank_clients_user_id ON bank_clients(user_id);
CREATE INDEX idx_bank_clients_client_number ON bank_clients(client_number);

-- ======================================================
-- 3. ACCOUNTS
-- ======================================================

CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bank_client_id BIGINT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    current_balance DECIMAL(14, 2) DEFAULT 0.00,
    account_number VARCHAR(50),
    clabe VARCHAR(50),
    credit_limit DECIMAL(14, 2),
    cutoff_day INT,
    payment_day INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_accounts_bank_client
        FOREIGN KEY (bank_client_id)
        REFERENCES bank_clients(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_accounts_bank_client_id ON accounts(bank_client_id);
CREATE INDEX idx_accounts_type ON accounts(type);

-- ======================================================
-- 4. EXTERNAL_ENTITIES
-- ======================================================

CREATE TABLE IF NOT EXISTS external_entities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    contact VARCHAR(200),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_external_entities_type ON external_entities(type);
CREATE INDEX idx_external_entities_name ON external_entities(name);

-- ======================================================
-- 5. CARDS
-- ======================================================

CREATE TABLE IF NOT EXISTS cards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    card_type VARCHAR(20) NOT NULL,
    card_number VARCHAR(4) NOT NULL,
    expiration_date DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_cards_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_cards_account_id ON cards(account_id);
CREATE INDEX idx_cards_card_type ON cards(card_type);

-- ======================================================
-- 6. WALLET_CARD_LINKS
-- ======================================================

CREATE TABLE IF NOT EXISTS wallet_card_links (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wallet_account_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_wallet_card_wallet
        FOREIGN KEY (wallet_account_id)
        REFERENCES accounts(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_wallet_card_card
        FOREIGN KEY (card_id)
        REFERENCES cards(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_wallet_card_wallet ON wallet_card_links(wallet_account_id);
CREATE INDEX idx_wallet_card_card ON wallet_card_links(card_id);

-- ======================================================
-- 7. CATEGORIES
-- ======================================================

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    icon VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_categories_type ON categories(type);
CREATE INDEX idx_categories_name ON categories(name);

-- ======================================================
-- 8. TAGS
-- ======================================================

CREATE TABLE IF NOT EXISTS tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_tags_name ON tags(name);
CREATE INDEX idx_tags_color ON tags(color);

-- ======================================================
-- 9. TRANSACTIONS
-- ======================================================

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operation_type VARCHAR(10) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    source_account_id BIGINT NULL,
    destination_account_id BIGINT NULL,
    external_entity_id BIGINT NULL,
    amount DECIMAL(12,2) NOT NULL,
    concept VARCHAR(100) NOT NULL,
    category VARCHAR(100) NOT NULL,
    description TEXT NULL,
    comments TEXT NULL,
    date DATETIME NOT NULL,
    timezone VARCHAR(50) NOT NULL,
    tags TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tx_source_account
        FOREIGN KEY (source_account_id)
        REFERENCES accounts(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_tx_destination_account
        FOREIGN KEY (destination_account_id)
        REFERENCES accounts(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_tx_entity
        FOREIGN KEY (external_entity_id)
        REFERENCES external_entities(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_tx_type ON transactions(operation_type);
CREATE INDEX idx_tx_method ON transactions(payment_method);
CREATE INDEX idx_tx_source_account ON transactions(source_account_id);
CREATE INDEX idx_tx_destination_account ON transactions(destination_account_id);
CREATE INDEX idx_tx_entity ON transactions(external_entity_id);

-- ======================================================
-- 10. CARD_TRANSACTION_DETAILS
-- ======================================================

CREATE TABLE IF NOT EXISTS card_transaction_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    installment_months INT NULL,
    interest_free BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_card_tx
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_card_detail
        FOREIGN KEY (card_id)
        REFERENCES cards(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_card_tx ON card_transaction_details(transaction_id);
CREATE INDEX idx_card_detail_card ON card_transaction_details(card_id);

-- ======================================================
-- 11. WALLET_TRANSACTION_DETAILS
-- ======================================================

CREATE TABLE IF NOT EXISTS wallet_transaction_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    wallet_account_id BIGINT NOT NULL,
    card_id BIGINT NULL,
    amount DECIMAL(12,2) NOT NULL,
    cashback_percentage DECIMAL(5,2) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_wallet_tx
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_wallet_account
        FOREIGN KEY (wallet_account_id)
        REFERENCES accounts(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_wallet_card
        FOREIGN KEY (card_id)
        REFERENCES cards(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_wallet_transaction
    ON wallet_transaction_details(transaction_id);

CREATE INDEX idx_wallet_payment_wallet
    ON wallet_transaction_details(wallet_account_id);

CREATE INDEX idx_wallet_payment_card
    ON wallet_transaction_details(card_id);