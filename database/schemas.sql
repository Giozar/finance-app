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
-- ======================================================
-- 3.1. ACCOUNTS
-- ======================================================
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL, -- 'CASH', 'BANK', 'CREDIT', 'WALLET', 'BENEFIT'
    current_balance DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_acc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_acc_user_type ON accounts (user_id, type);


-- ======================================================
-- 3.2. BANK_DETAILS (Extensión Bancaria y Vales)
-- ======================================================
CREATE TABLE IF NOT EXISTS bank_details (
    account_id BIGINT PRIMARY KEY,
    bank_client_id BIGINT NULL,
    clabe VARCHAR(18) NULL,
    account_number VARCHAR(20) NULL,
    can_transfer_out BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bank_acc_base FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_bank_client FOREIGN KEY (bank_client_id) REFERENCES bank_clients(id) ON DELETE SET NULL
);

-- Índices de búsqueda operativa
CREATE INDEX idx_bank_det_client ON bank_details (bank_client_id);
CREATE INDEX idx_bank_det_clabe ON bank_details (clabe);

-- ======================================================
-- 3.3. CREDIT_DETAILS (Extensión de Crédito)
-- ======================================================
CREATE TABLE IF NOT EXISTS credit_details (
    account_id BIGINT PRIMARY KEY,
    bank_client_id BIGINT NOT NULL,
    credit_limit DECIMAL(12, 2) NOT NULL,
    cutoff_day INT NOT NULL,
    payment_deadline_day INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_credit_acc_base FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_credit_bank_client FOREIGN KEY (bank_client_id) REFERENCES bank_clients(id) ON DELETE RESTRICT,
    CONSTRAINT chk_cutoff_day CHECK (cutoff_day BETWEEN 1 AND 31),
    CONSTRAINT chk_payment_day CHECK (payment_deadline_day BETWEEN 1 AND 31),
    CONSTRAINT chk_credit_limit CHECK (credit_limit >= 0)
);
-- Índices de gestión de deuda
CREATE INDEX idx_credit_det_client ON credit_details (bank_client_id);


-- ======================================================
-- 3.4. SAVINGS_DETAILS (Extensión de Rendimientos)
-- ======================================================
-- annual_yield se guarda como fracción:
--   0.150000 = 15% anual
-- yield_cap_amount:
--   NULL = sin límite
--   >= 0 = monto máximo que genera rendimiento

CREATE TABLE IF NOT EXISTS savings_details (
    account_id BIGINT PRIMARY KEY,
    annual_yield DECIMAL(9, 6) NOT NULL,
    yield_cap_amount DECIMAL(12, 2) NULL, -- Monto máximo para generar rendimientos
    last_yield_calculation DATE NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_savings_acc_base
        FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,

    CONSTRAINT chk_savings_yield
        CHECK (annual_yield >= 0 AND annual_yield <= 1),

    CONSTRAINT chk_savings_cap
        CHECK (yield_cap_amount IS NULL OR yield_cap_amount >= 0)
);

-- Índice para localizar cuentas pendientes de cálculo
CREATE INDEX idx_savings_last_calc 
    ON savings_details (last_yield_calculation);


-- ======================================================
-- 3.5. INVESTMENT_DETAILS (Posiciones de inversión a plazo)
-- ======================================================
-- Representa cada inversión/posición dentro de una cuenta contenedora (accounts).
-- Ejemplo: una cuenta "CETESDirecto" (accounts) puede tener muchas inversiones (investment_details).

CREATE TABLE IF NOT EXISTS investment_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- Cuenta contenedora (ej. "CETESDirecto")
    account_id BIGINT NOT NULL,

    -- Tipo de instrumento (ej. CETES, BONDDIA)
    instrument_type VARCHAR(20) NOT NULL,

    -- Plazo en días (ej. 28, 91, 182). Para instrumentos sin plazo fijo (ej. BONDDIA), puede ser NULL.
    term_days INT NULL,

    -- Capital fijo invertido en esta posición (no cambia durante el plazo)
    principal_amount DECIMAL(12, 2) NOT NULL,

    -- Tasa anual fija para esta posición (fracción: 0.105000 = 10.5% anual)
    annual_yield DECIMAL(9, 6) NOT NULL,

    -- Base de cálculo de días (por defecto 360 para instrumentos tipo CETES; si no la necesitas, puedes fijarla en dominio)
    day_count_basis SMALLINT NOT NULL DEFAULT 360,

    -- Fechas del plazo (planificadas)
    start_date DATE NOT NULL,
    maturity_date DATE NOT NULL,

    -- Control de ciclo de vida (fechas reales de procesamiento)
    opened_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    matured_at DATETIME NULL,
    cancelled_at DATETIME NULL,

    -- Estado de la inversión
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',   -- ACTIVE | MATURED | CANCELLED

    -- Reinversión automática al vencimiento (si aplica)
    auto_reinvest BOOLEAN NOT NULL DEFAULT FALSE,
    reinvest_term_days INT NULL,
    reinvest_annual_yield DECIMAL(9, 6) NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_investment_acc_base
        FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,

    CONSTRAINT chk_investment_principal
        CHECK (principal_amount > 0),

    CONSTRAINT chk_investment_yield
        CHECK (annual_yield >= 0 AND annual_yield <= 1),

    CONSTRAINT chk_investment_basis
        CHECK (day_count_basis IN (360, 365)),

    CONSTRAINT chk_investment_dates
        CHECK (maturity_date > start_date),

    CONSTRAINT chk_investment_term_days
        CHECK (term_days IS NULL OR term_days > 0),

    CONSTRAINT chk_investment_reinvest_term
        CHECK (reinvest_term_days IS NULL OR reinvest_term_days > 0),

    CONSTRAINT chk_investment_reinvest_yield
        CHECK (
            reinvest_annual_yield IS NULL
            OR (reinvest_annual_yield >= 0 AND reinvest_annual_yield <= 1)
        )
);

-- Índices operativos: listar por cuenta y procesar vencimientos
CREATE INDEX idx_investment_account
    ON investment_details (account_id);

CREATE INDEX idx_investment_status_maturity
    ON investment_details (status, maturity_date);

CREATE INDEX idx_investment_instrument
    ON investment_details (instrument_type, term_days);

CREATE INDEX idx_investment_opened_at
    ON investment_details (opened_at);

CREATE INDEX idx_investment_matured_at
    ON investment_details (matured_at);


-- ======================================================
-- 3.6. WALLET_CARD_LINKS (Relación Muchos a Muchos)
-- ======================================================
CREATE TABLE IF NOT EXISTS wallet_card_links (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wallet_account_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_link_acc FOREIGN KEY (wallet_account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_wallet_link_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
    UNIQUE (wallet_account_id, card_id)
);

-- Índice inverso para wallets vinculadas a una tarjeta
CREATE INDEX idx_wallet_link_card ON wallet_card_links (card_id);


-- ======================================================
-- 3.7. TRIGGER: VALIDACIÓN DE TRANSFERENCIAS
-- ======================================================
DELIMITER //

DROP TRIGGER IF EXISTS tr_before_transaction_transfer_check //

CREATE TRIGGER tr_before_transaction_transfer_check
BEFORE INSERT ON transactions
FOR EACH ROW
BEGIN
    DECLARE v_acc_type VARCHAR(20);
    DECLARE v_can_transfer BOOLEAN;

    IF NEW.operation_type = 'TRANSFER' THEN

        -- Validaciones mínimas de integridad para transferencias
        IF NEW.source_account_id IS NULL OR NEW.destination_account_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Transferencia requiere source_account_id y destination_account_id.';
        END IF;

        IF NEW.source_account_id = NEW.destination_account_id THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Origen y destino no pueden ser iguales.';
        END IF;

        -- Obtener tipo de cuenta origen y bandera can_transfer_out (si existe)
        SELECT a.type, COALESCE(bd.can_transfer_out, TRUE)
        INTO v_acc_type, v_can_transfer
        FROM accounts a
        LEFT JOIN bank_details bd ON a.id = bd.account_id
        WHERE a.id = NEW.source_account_id;

        -- Si la cuenta origen no existe, el SELECT anterior no devuelve fila y MySQL lanza error genérico.
        -- Esta validación adicional fuerza un error claro.
        IF v_acc_type IS NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: La cuenta origen no existe.';
        END IF;

        -- Bloqueo por tipo BENEFIT o flag deshabilitado
        IF v_acc_type = 'BENEFIT' OR v_can_transfer = FALSE THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Restricción: Esta cuenta no permite transferencias salientes.';
        END IF;

    END IF;
END //

DELIMITER ;

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
CREATE TABLE IF NOT EXISTS
    cards (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        account_id BIGINT NOT NULL,
        name VARCHAR(100) NOT NULL,
        card_type VARCHAR(20) NOT NULL,
        card_number VARCHAR(4) NOT NULL,
        expiration_date DATE NOT NULL,
        -- status ENUM('ACTIVE', 'BLOCKED', 'EXPIRED') DEFAULT 'ACTIVE',
        -- Usamos VARCHAR en lugar de ENUM para mayor flexibilidad
        status VARCHAR(20) DEFAULT 'ACTIVE',
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        CONSTRAINT fk_cards_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE
    );

CREATE INDEX idx_cards_account_id ON cards (account_id);

CREATE INDEX idx_cards_card_type ON cards (card_type);

-- Validar que la tarjeta se pueda vincular a una cuenta bancaria
DELIMITER / /
DROP TRIGGER IF EXISTS tr_before_card_insert / /
CREATE TRIGGER tr_before_card_insert BEFORE
INSERT
    ON cards FOR EACH ROW BEGIN DECLARE v_bank_id BIGINT;

-- Buscamos si la cuenta seleccionada tiene un vínculo bancario
SELECT
    bank_client_id INTO v_bank_id
FROM
    accounts
WHERE
    id = NEW.account_id;

-- Si bank_client_id es NULL, significa que es Efectivo/Personal y bloqueamos
IF v_bank_id IS NULL THEN SIGNAL SQLSTATE '45000'
SET
    MESSAGE_TEXT = 'Error: Solo se pueden vincular tarjetas a cuentas bancarias.';

END IF;

END / / DELIMITER;

-- ======================================================
-- 6. CATEGORIES
-- ======================================================
CREATE TABLE IF NOT EXISTS
    categories (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        user_id BIGINT NOT NULL,
        name VARCHAR(100) NOT NULL,
        -- OPCIÓN 1: VARCHAR + CHECK (Flexibilidad)
        -- Es un texto con una regla "pegada" que imita al ENUM.
        type VARCHAR(20) NOT NULL,
        -- OPCIÓN 2: ENUM (Rigidez/Optimización)
        -- type ENUM('INCOME', 'EXPENSE', 'BOTH') NOT NULL,
        icon VARCHAR(100) NOT NULL,
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
        -- Tu restricción UNIQUE (user_id, name): 
        -- Evita que tengas dos "Comida", pero permite que OTRO usuario tenga la suya.
        CONSTRAINT unique_category_per_user UNIQUE (user_id, name),
        -- LA REGLA "TIPO ENUM":
        -- Obliga a que el VARCHAR solo acepte estas 3 palabras.
        CONSTRAINT chk_category_type CHECK (type IN ('INCOME', 'EXPENSE', 'BOTH'))
    );

CREATE INDEX idx_categories_user_id ON categories (user_id);

CREATE INDEX idx_categories_type ON categories (type);

CREATE INDEX idx_categories_name ON categories (name);

-- ======================================================
-- 7. TAGS
-- ======================================================
CREATE TABLE IF NOT EXISTS
    tags (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        user_id BIGINT NOT NULL,
        name VARCHAR(100) NOT NULL,
        color VARCHAR(20) NOT NULL,
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        CONSTRAINT fk_tags_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
        -- Llave única: El usuario 1 no puede repetir "#Cena", 
        -- pero el usuario 2 sí puede tener su propio "#Cena".
        CONSTRAINT unique_tag_per_user UNIQUE (user_id, name)
    );

-- REINSTALANDO Y AJUSTANDO TUS ÍNDICES:
CREATE INDEX idx_tags_user_id ON tags (user_id);

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
        user_id BIGINT NOT NULL,
        parent_transaction_id BIGINT NULL,
        -- operation_type ENUM('INCOME', 'EXPENSE', 'TRANSFER') NOT NULL,
        operation_type VARCHAR(10) NOT NULL,
        -- payment_method ENUM('CARD', 'CASH', 'TRANSFER', 'QR', 'CODI', 'WALLET') NOT NULL,
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
        CONSTRAINT fk_tx_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
        CONSTRAINT fk_tx_parent FOREIGN KEY (parent_transaction_id) REFERENCES transactions (id) ON DELETE SET NULL,
        CONSTRAINT fk_tx_source_account FOREIGN KEY (source_account_id) REFERENCES accounts (id) ON DELETE SET NULL,
        CONSTRAINT fk_tx_destination_account FOREIGN KEY (destination_account_id) REFERENCES accounts (id) ON DELETE SET NULL,
        CONSTRAINT fk_tx_entity FOREIGN KEY (external_entity_id) REFERENCES external_entities (id) ON DELETE SET NULL,
        CONSTRAINT fk_tx_category FOREIGN KEY (category_id) REFERENCES categories (id)
    );

-- Índices para optimización de consultas y reportes
CREATE INDEX idx_tx_user_id ON transactions (user_id);

CREATE INDEX idx_tx_date ON transactions (date);

CREATE INDEX idx_tx_type ON transactions (operation_type);

CREATE INDEX idx_tx_method ON transactions (payment_method);

CREATE INDEX idx_tx_source_account ON transactions (source_account_id);

CREATE INDEX idx_tx_destination_account ON transactions (destination_account_id);

CREATE INDEX idx_tx_entity ON transactions (external_entity_id);

-- ======================================================
-- Trigger de Validación (Autocorrección de Monto) 
-- ======================================================

DELIMITER //

DROP TRIGGER IF EXISTS tr_before_transaction_insert_val //

CREATE TRIGGER tr_before_transaction_insert_val
BEFORE INSERT ON transactions
FOR EACH ROW
BEGIN
    -- 1. Si el monto es negativo, lo pasamos a positivo (ABS)
    IF NEW.amount < 0 THEN
        SET NEW.amount = ABS(NEW.amount);
    END IF;

    -- 2. Bloqueamos montos en cero (no tienen sentido contable)
    IF NEW.amount = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: El monto de la transacción debe ser mayor a cero.';
    END IF;
END //

DELIMITER ;

-- ======================================================
-- 9. TRANSACTION_TAGS
-- ======================================================
CREATE TABLE IF NOT EXISTS transaction_tags (
    transaction_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    
    -- Llave primaria compuesta: asegura unicidad y rapidez de búsqueda por transacción
    PRIMARY KEY (transaction_id, tag_id),
    
    CONSTRAINT fk_tt_transaction 
        FOREIGN KEY (transaction_id) REFERENCES transactions (id) ON DELETE CASCADE,
    
    CONSTRAINT fk_tt_tag 
        FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
);

-- Índice para optimizar búsquedas inversas (Estadísticas por Tag)
CREATE INDEX idx_tt_tag_id ON transaction_tags (tag_id);

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