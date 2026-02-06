DROP DATABASE IF EXISTS finanzas;

CREATE DATABASE IF NOT EXISTS finanzas;

USE finanzas;

-- ======================================================
-- ESTRUCTURA DE USUARIOS Y BANCOS
-- ======================================================
-- ======================================================
-- 1. USERS
-- ======================================================
CREATE TABLE IF NOT EXISTS
    users (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        name VARCHAR(100) NOT NULL,
        email VARCHAR(150) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        global_balance DECIMAL(14, 2) DEFAULT 0.00,
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

CREATE INDEX idx_users_email ON users (email);

-- ======================================================
-- 2. BANK_CLIENTS
-- ======================================================
CREATE TABLE IF NOT EXISTS
    bank_clients (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        user_id BIGINT NOT NULL,
        bank_name VARCHAR(100) NOT NULL,
        client_number VARCHAR(50) NOT NULL,
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        CONSTRAINT fk_bank_clients_user
        -- Si borras al usuario, se borra su relación con el banco
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
        UNIQUE KEY unique_client_per_bank (user_id, bank_name, client_number)
    );

CREATE INDEX idx_bank_clients_user_id ON bank_clients (user_id);

CREATE INDEX idx_bank_clients_client_number ON bank_clients (client_number);

-- ======================================================
-- 3. ACCOUNTS
-- ======================================================
CREATE TABLE IF NOT EXISTS
    accounts (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        user_id BIGINT NULL,
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
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
        CONSTRAINT fk_accounts_bank_client FOREIGN KEY (bank_client_id) REFERENCES bank_clients (id) ON DELETE SET NULL,
        -- Validar relación con usuario directamente, o es de un banco, nunca ambos.
        CONSTRAINT chk_account_owner CHECK (
            (
                user_id IS NOT NULL
                AND bank_client_id IS NULL
            )
            OR (
                user_id IS NULL
                AND bank_client_id IS NOT NULL
            )
        )
    );

CREATE INDEX idx_accounts_user_id ON accounts (user_id);

CREATE INDEX idx_accounts_bank_client_id ON accounts (bank_client_id);

CREATE INDEX idx_accounts_type ON accounts (type);

-- ======================================================
-- CATÁLOGOS Y ENTIDADES EXTERNAS
-- ======================================================
-- ======================================================
-- 4. EXTERNAL_ENTITIES
-- ======================================================
CREATE TABLE IF NOT EXISTS
    external_entities (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        user_id BIGINT NOT NULL, -- Propiedad privada
        name VARCHAR(100) NOT NULL,
        type VARCHAR(20) NOT NULL, -- 'store', 'service', 'person'
        contact VARCHAR(200),
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        CONSTRAINT fk_entities_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
        -- Evita duplicados para el mismo usuario, pero permite que dos usuarios
        -- distintos tengan su propia "TIENDA PEPE" sin chocar.
        UNIQUE KEY unique_entity_per_user (user_id, name)
    );

CREATE INDEX idx_external_entities_user ON external_entities (user_id);

CREATE INDEX idx_external_entities_type ON external_entities (type);

CREATE INDEX idx_external_entities_name ON external_entities (name);

-- ======================================================
-- 5. CARDS
-- ======================================================
CREATE TABLE IF NOT EXISTS cards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    card_type VARCHAR(20) NOT NULL,
    card_number VARCHAR(4) NOT NULL,
    expiration_date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_cards_account 
        FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE INDEX idx_cards_account_id ON cards (account_id);
CREATE INDEX idx_cards_card_type ON cards (card_type);

-- Validar que la tarjeta se pueda vincular a una cuenta bancaria
DELIMITER //

DROP TRIGGER IF EXISTS tr_before_card_insert //
CREATE TRIGGER tr_before_card_insert
BEFORE INSERT ON cards
FOR EACH ROW
BEGIN
    DECLARE v_bank_id BIGINT;

    -- Buscamos si la cuenta seleccionada tiene un vínculo bancario
    SELECT bank_client_id INTO v_bank_id 
    FROM accounts 
    WHERE id = NEW.account_id;

    -- Si bank_client_id es NULL, significa que es Efectivo/Personal y bloqueamos
    IF v_bank_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Solo se pueden vincular tarjetas a cuentas bancarias.';
    END IF;
END //

DELIMITER ;

-- ======================================================
-- 6. CATEGORIES
-- ======================================================
CREATE TABLE IF NOT EXISTS
    categories (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        name VARCHAR(100) NOT NULL,
        type VARCHAR(20) NOT NULL,
        icon VARCHAR(100) NOT NULL,
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

CREATE INDEX idx_categories_type ON categories (type);

CREATE INDEX idx_categories_name ON categories (name);

-- ======================================================
-- 7. TAGS
-- ======================================================
CREATE TABLE IF NOT EXISTS
    tags (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        name VARCHAR(100) NOT NULL UNIQUE,
        color VARCHAR(20) NOT NULL,
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

CREATE INDEX idx_tags_name ON tags (name);

CREATE INDEX idx_tags_color ON tags (color);

-- ======================================================
-- MOVIMIENTOS Y TRANSACCIONES
-- ======================================================
-- ======================================================
-- 8. TRANSACTIONS
-- ======================================================
CREATE TABLE IF NOT EXISTS
    transactions (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        parent_transaction_id BIGINT NULL,
        operation_type VARCHAR(10) NOT NULL,
        payment_method VARCHAR(20) NOT NULL,
        source_account_id BIGINT NULL,
        destination_account_id BIGINT NULL,
        external_entity_id BIGINT NULL,
        category_id BIGINT NOT NULL,
        amount DECIMAL(12, 2) NOT NULL,
        concept VARCHAR(100) NOT NULL,
        description TEXT NULL,
        comments TEXT NULL,
        date DATETIME NOT NULL,
        timezone VARCHAR(50) NOT NULL,
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        CONSTRAINT fk_tx_parent FOREIGN KEY (parent_transaction_id) REFERENCES transactions (id) ON DELETE SET NULL,
        CONSTRAINT fk_tx_source_account FOREIGN KEY (source_account_id) REFERENCES accounts (id) ON DELETE SET NULL,
        CONSTRAINT fk_tx_destination_account FOREIGN KEY (destination_account_id) REFERENCES accounts (id) ON DELETE SET NULL,
        CONSTRAINT fk_tx_entity FOREIGN KEY (external_entity_id) REFERENCES external_entities (id) ON DELETE SET NULL,
        CONSTRAINT fk_tx_category FOREIGN KEY (category_id) REFERENCES categories (id)
    );

CREATE INDEX idx_tx_type ON transactions (operation_type);

CREATE INDEX idx_tx_method ON transactions (payment_method);

CREATE INDEX idx_tx_source_account ON transactions (source_account_id);

CREATE INDEX idx_tx_destination_account ON transactions (destination_account_id);

CREATE INDEX idx_tx_entity ON transactions (external_entity_id);

-- ======================================================
-- 9. TRANSACTION_TAGS
-- ======================================================
CREATE TABLE IF NOT EXISTS
    transaction_tags (
        transaction_id BIGINT NOT NULL,
        tag_id BIGINT NOT NULL,
        PRIMARY KEY (transaction_id, tag_id),
        CONSTRAINT fk_tt_transaction FOREIGN KEY (transaction_id) REFERENCES transactions (id) ON DELETE CASCADE,
        CONSTRAINT fk_tt_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
    );

-- ======================================================
-- DETALLES ESPECÍFICOS DE PAGO
-- ======================================================
-- ======================================================
-- 10. WALLET_CARD_LINKS
-- ======================================================
CREATE TABLE IF NOT EXISTS
    wallet_card_links (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        wallet_account_id BIGINT NOT NULL,
        card_id BIGINT NOT NULL,
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        CONSTRAINT fk_wallet_card_wallet FOREIGN KEY (wallet_account_id) REFERENCES accounts (id) ON DELETE CASCADE,
        CONSTRAINT fk_wallet_card_card FOREIGN KEY (card_id) REFERENCES cards (id) ON DELETE CASCADE
    );

CREATE INDEX idx_wallet_card_wallet ON wallet_card_links (wallet_account_id);

CREATE INDEX idx_wallet_card_card ON wallet_card_links (card_id);

-- ======================================================
-- 11. CARD_TRANSACTION_DETAILS
-- ======================================================
CREATE TABLE IF NOT EXISTS
    card_transaction_details (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        transaction_id BIGINT NOT NULL,
        card_id BIGINT NOT NULL,
        amount DECIMAL(12, 2) NOT NULL,
        installment_months INT NULL,
        interest_free BOOLEAN NOT NULL DEFAULT FALSE,
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        CONSTRAINT fk_card_tx FOREIGN KEY (transaction_id) REFERENCES transactions (id) ON DELETE CASCADE,
        CONSTRAINT fk_card_detail FOREIGN KEY (card_id) REFERENCES cards (id) ON DELETE CASCADE
    );

CREATE INDEX idx_card_tx ON card_transaction_details (transaction_id);

CREATE INDEX idx_card_detail_card ON card_transaction_details (card_id);

-- ======================================================
-- 12. WALLET_TRANSACTION_DETAILS
-- ======================================================
CREATE TABLE IF NOT EXISTS
    wallet_transaction_details (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        transaction_id BIGINT NOT NULL,
        source_type VARCHAR(20) NOT NULL,
        wallet_account_id BIGINT NOT NULL,
        card_id BIGINT NULL,
        amount DECIMAL(12, 2) NOT NULL,
        cashback_percentage DECIMAL(5, 2) NULL,
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        CONSTRAINT fk_wallet_tx FOREIGN KEY (transaction_id) REFERENCES transactions (id) ON DELETE CASCADE,
        CONSTRAINT fk_wallet_account FOREIGN KEY (wallet_account_id) REFERENCES accounts (id) ON DELETE CASCADE,
        CONSTRAINT fk_wallet_card FOREIGN KEY (card_id) REFERENCES cards (id) ON DELETE SET NULL
    );

CREATE INDEX idx_wallet_transaction ON wallet_transaction_details (transaction_id);

CREATE INDEX idx_wallet_payment_wallet ON wallet_transaction_details (wallet_account_id);

CREATE INDEX idx_wallet_payment_card ON wallet_transaction_details (card_id);

-- ======================================================
-- AUTOMATIZACIÓN DE SALDOS (TRIGGERS)
-- ======================================================
-- Aseguramos permisos para crear triggers
SET
    GLOBAL log_bin_trust_function_creators = 1;

DELIMITER / /
CREATE TRIGGER tr_after_transaction_insert AFTER
INSERT
    ON transactions FOR EACH ROW BEGIN
    -- Los pagos tipo WALLET se ignoran aquí para evitar duplicidad de cargos (se manejan en su propio trigger)
    IF NEW.payment_method <> 'WALLET' THEN
    -- Manejo de Egresos
    IF NEW.operation_type = 'EXPENSE'
    AND NEW.source_account_id IS NOT NULL THEN
UPDATE accounts
SET
    current_balance = current_balance - NEW.amount
WHERE
    id = NEW.source_account_id;

END IF;

-- Manejo de Ingresos
IF NEW.operation_type = 'INCOME'
AND NEW.destination_account_id IS NOT NULL THEN
UPDATE accounts
SET
    current_balance = current_balance + NEW.amount
WHERE
    id = NEW.destination_account_id;

END IF;

-- Manejo de Transferencias
IF NEW.operation_type = 'TRANSFER' THEN
UPDATE accounts
SET
    current_balance = current_balance - NEW.amount
WHERE
    id = NEW.source_account_id;

UPDATE accounts
SET
    current_balance = current_balance + NEW.amount
WHERE
    id = NEW.destination_account_id;

END IF;

END IF;

END / /
CREATE TRIGGER tr_after_wallet_detail_insert AFTER
INSERT
    ON wallet_transaction_details FOR EACH ROW BEGIN
    -- Caso Saldo Wallet
    IF NEW.source_type = 'wallet_balance' THEN
UPDATE accounts
SET
    current_balance = current_balance - NEW.amount
WHERE
    id = NEW.wallet_account_id;

END IF;

-- Caso Tarjeta Vinculada
IF NEW.source_type = 'linked_card'
AND NEW.card_id IS NOT NULL THEN
UPDATE accounts a
INNER JOIN cards c ON a.id = c.account_id
SET
    a.current_balance = a.current_balance - NEW.amount
WHERE
    c.id = NEW.card_id;

END IF;

END / / DELIMITER;

-- ======================================================
-- AUTOMATIZACIÓN DE SALDOS (TRANSACCIONES Y WALLET)
-- ======================================================
SET
    GLOBAL log_bin_trust_function_creators = 1;

DELIMITER / /
-- ======================================================
-- 1. TRIGGER PARA ACTUALIZAR SALDOS AL INSERTAR
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_transaction_insert / /
CREATE TRIGGER tr_after_transaction_insert AFTER
INSERT
    ON transactions FOR EACH ROW BEGIN IF NEW.payment_method <> 'WALLET' THEN
    -- GASTO
    IF NEW.operation_type = 'EXPENSE'
    AND NEW.source_account_id IS NOT NULL THEN
UPDATE accounts
SET
    current_balance = current_balance - NEW.amount
WHERE
    id = NEW.source_account_id;

-- INGRESO
ELSEIF NEW.operation_type = 'INCOME'
AND NEW.destination_account_id IS NOT NULL THEN
UPDATE accounts
SET
    current_balance = current_balance + NEW.amount
WHERE
    id = NEW.destination_account_id;

-- TRANSFERENCIA
ELSEIF NEW.operation_type = 'TRANSFER'
AND NEW.source_account_id IS NOT NULL
AND NEW.destination_account_id IS NOT NULL THEN
UPDATE accounts
SET
    current_balance = current_balance - NEW.amount
WHERE
    id = NEW.source_account_id;

UPDATE accounts
SET
    current_balance = current_balance + NEW.amount
WHERE
    id = NEW.destination_account_id;

END IF;

END IF;

END / /
-- ======================================================
-- 2. TRIGGER PARA CORREGIR SALDOS AL ACTUALIZAR
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_transaction_update / /
CREATE TRIGGER tr_after_transaction_update AFTER
UPDATE ON transactions FOR EACH ROW BEGIN
-- A. REVERTIR valores antiguos
IF OLD.payment_method <> 'WALLET' THEN IF OLD.operation_type = 'EXPENSE' THEN
UPDATE accounts
SET
    current_balance = current_balance + OLD.amount
WHERE
    id = OLD.source_account_id;

ELSEIF OLD.operation_type = 'INCOME' THEN
UPDATE accounts
SET
    current_balance = current_balance - OLD.amount
WHERE
    id = OLD.destination_account_id;

ELSEIF OLD.operation_type = 'TRANSFER' THEN
UPDATE accounts
SET
    current_balance = current_balance + OLD.amount
WHERE
    id = OLD.source_account_id;

UPDATE accounts
SET
    current_balance = current_balance - OLD.amount
WHERE
    id = OLD.destination_account_id;

END IF;

END IF;

-- B. APLICAR valores nuevos
IF NEW.payment_method <> 'WALLET' THEN IF NEW.operation_type = 'EXPENSE' THEN
UPDATE accounts
SET
    current_balance = current_balance - NEW.amount
WHERE
    id = NEW.source_account_id;

ELSEIF NEW.operation_type = 'INCOME' THEN
UPDATE accounts
SET
    current_balance = current_balance + NEW.amount
WHERE
    id = NEW.destination_account_id;

ELSEIF NEW.operation_type = 'TRANSFER' THEN
UPDATE accounts
SET
    current_balance = current_balance - NEW.amount
WHERE
    id = NEW.source_account_id;

UPDATE accounts
SET
    current_balance = current_balance + NEW.amount
WHERE
    id = NEW.destination_account_id;

END IF;

END IF;

END / /
-- ======================================================
-- 3. TRIGGER PARA RESTITUIR SALDOS AL ELIMINAR
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_transaction_delete / /
CREATE TRIGGER tr_after_transaction_delete AFTER DELETE ON transactions FOR EACH ROW BEGIN IF OLD.payment_method <> 'WALLET' THEN IF OLD.operation_type = 'EXPENSE' THEN
UPDATE accounts
SET
    current_balance = current_balance + OLD.amount
WHERE
    id = OLD.source_account_id;

ELSEIF OLD.operation_type = 'INCOME' THEN
UPDATE accounts
SET
    current_balance = current_balance - OLD.amount
WHERE
    id = OLD.destination_account_id;

ELSEIF OLD.operation_type = 'TRANSFER' THEN
UPDATE accounts
SET
    current_balance = current_balance + OLD.amount
WHERE
    id = OLD.source_account_id;

UPDATE accounts
SET
    current_balance = current_balance - OLD.amount
WHERE
    id = OLD.destination_account_id;

END IF;

END IF;

END / /
-- ======================================================
-- 4. TRIGGER MAESTRO: SINCRONIZACIÓN DE GLOBAL_BALANCE (FASE 0)
-- ======================================================
-- Este trigger asegura que users.global_balance sea la suma de todas sus cuentas
DROP TRIGGER IF EXISTS tr_sync_global_balance / /
CREATE TRIGGER tr_sync_global_balance AFTER
UPDATE ON accounts FOR EACH ROW BEGIN
-- Solo actuamos si el saldo actual cambió para evitar bucles infinitos
IF OLD.current_balance <> NEW.current_balance THEN
UPDATE users
SET
    global_balance = (
        SELECT
            SUM(current_balance)
        FROM
            accounts
        WHERE
            user_id = NEW.user_id
            OR bank_client_id IN (
                SELECT
                    id
                FROM
                    bank_clients
                WHERE
                    user_id = NEW.user_id
            )
    )
WHERE
    id = NEW.user_id
    OR id = (
        SELECT
            user_id
        FROM
            bank_clients
        WHERE
            id = NEW.bank_client_id
    );

END IF;

END / / DELIMITER;