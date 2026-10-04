-- ======================================================
-- FINANZAS - SCHEMA COMPLETO
-- ======================================================
-- NOTA: Ejecuta este archivo desde MySQL CLI con:
--   mysql -u root -p < schemas.sql
-- O desde MySQL Workbench ejecutando el script completo.

-- Permisos para crear triggers (necesario en algunos entornos)
SET GLOBAL log_bin_trust_function_creators = 1;

DROP DATABASE IF EXISTS finanzas;

CREATE DATABASE IF NOT EXISTS finanzas;

USE finanzas;

-- ======================================================
-- ESTRUCTURA DE USUARIOS Y BANCOS
-- ======================================================

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
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users (email);

-- ======================================================
-- 2. BANK_CLIENTS
-- ======================================================
CREATE TABLE IF NOT EXISTS bank_clients (
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
-- 3.1. ACCOUNTS (Tabla base)
-- ======================================================
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
        -- type: 'CASH', 'DEBIT', 'CREDIT', 'WALLET', 'BENEFIT', 'SAVINGS', 'INVESTMENT' (shared AccountTypes)
    type VARCHAR(20) NOT NULL,
    current_balance DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_acc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_account_type
        CHECK (type IN ('CASH', 'DEBIT', 'CREDIT', 'WALLET', 'BENEFIT', 'SAVINGS', 'INVESTMENT'))
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
    -- si es true, se puede retirar dinero de la cuenta 
    -- si es false, solo se puede depositar dinero en la cuenta
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
    credit_used DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    cutoff_day INT NOT NULL,
    payment_deadline_day INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_credit_acc_base FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_credit_bank_client FOREIGN KEY (bank_client_id) REFERENCES bank_clients(id) ON DELETE RESTRICT,
    CONSTRAINT chk_cutoff_day CHECK (cutoff_day BETWEEN 1 AND 31),
    CONSTRAINT chk_payment_day CHECK (payment_deadline_day BETWEEN 1 AND 31),
    CONSTRAINT chk_credit_limit CHECK (credit_limit >= 0),
    CONSTRAINT chk_credit_used CHECK (credit_used >= 0),
    CONSTRAINT chk_credit_used_limit CHECK (credit_used <= credit_limit)
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
CREATE INDEX idx_savings_last_calc ON savings_details (last_yield_calculation);


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
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',   -- 'ACTIVE', 'MATURED', 'CANCELLED'

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
        ),
    CONSTRAINT chk_investment_status
        CHECK (status IN ('ACTIVE', 'MATURED', 'CANCELLED'))
);

-- Índices operativos: listar por cuenta y procesar vencimientos
CREATE INDEX idx_investment_account ON investment_details (account_id);
CREATE INDEX idx_investment_status_maturity ON investment_details (status, maturity_date);
CREATE INDEX idx_investment_instrument ON investment_details (instrument_type, term_days);
CREATE INDEX idx_investment_opened_at ON investment_details (opened_at);
CREATE INDEX idx_investment_matured_at ON investment_details (matured_at);

-- ======================================================
-- 3.6. ACCOUNT_CASHBACK_SETTINGS (Configuración de Cashback por Cuenta)
-- ======================================================
-- Tabla donde se configura si una cuenta (wallet) tiene activo el cashback y su tasa.
-- default_cashback_rate se guarda como fracción: 0.020000 = 2%

CREATE TABLE IF NOT EXISTS account_cashback_settings (
    account_id BIGINT PRIMARY KEY,
    default_cashback_rate DECIMAL(9, 6) NULL,
    cashback_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cashback_account
        FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT chk_cashback_rate
        CHECK (
            default_cashback_rate IS NULL
            OR (default_cashback_rate >= 0 AND default_cashback_rate <= 1)
        )
);

-- ======================================================
-- 4. CARDS
-- ======================================================

-- ======================================================
-- 4.1. CARDS (Tarjetas físicas o digitales de una cuenta)
-- ======================================================
CREATE TABLE IF NOT EXISTS cards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
        -- card_type: 'PHYSICAL', 'DIGITAL' (shared CardTypes)
    card_type VARCHAR(20) NOT NULL,
    card_number VARCHAR(4) NOT NULL,
    expiration_date DATE NOT NULL,
        -- status: 'ACTIVE', 'BLOCKED', 'EXPIRED'
        -- Usamos VARCHAR + CHECK en lugar de ENUM para mayor flexibilidad
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cards_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE,
    CONSTRAINT chk_card_type CHECK (card_type IN ('PHYSICAL', 'DIGITAL')),
    CONSTRAINT chk_card_status CHECK (status IN ('ACTIVE', 'BLOCKED', 'EXPIRED'))
);

CREATE INDEX idx_cards_account_id ON cards (account_id);
CREATE INDEX idx_cards_card_type ON cards (card_type);

-- ======================================================
-- 4.2. WALLET_CARD_LINKS (Relación Muchos a Muchos wallet <-> tarjeta)
-- ======================================================
CREATE TABLE IF NOT EXISTS wallet_card_links (
    account_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id, card_id),
    CONSTRAINT fk_wallet_link_acc
        FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_wallet_link_card
        FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
);

CREATE INDEX idx_wallet_link_card ON wallet_card_links (card_id);

-- ======================================================
-- CATÁLOGOS Y ENTIDADES EXTERNAS
-- ======================================================

-- ======================================================
-- 5. EXTERNAL_ENTITIES
-- ======================================================
CREATE TABLE IF NOT EXISTS external_entities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
        -- type: 'PERSON', 'SERVICE', 'STORE' (shared ExternalEntityTypes)
    type VARCHAR(20) NOT NULL,
    contact VARCHAR(200),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_entities_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
        -- Evita duplicados para el mismo usuario, pero permite que dos usuarios
        -- distintos tengan su propia "TIENDA PEPE" sin chocar.
    UNIQUE KEY unique_entity_per_user (user_id, name),
    CONSTRAINT chk_external_entity_type CHECK (type IN ('PERSON', 'SERVICE', 'STORE'))
);

CREATE INDEX idx_external_entities_user ON external_entities (user_id);
CREATE INDEX idx_external_entities_type ON external_entities (type);
CREATE INDEX idx_external_entities_name ON external_entities (name);

-- ======================================================
-- 6. CATEGORIES
-- ======================================================
CREATE TABLE IF NOT EXISTS categories (
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
CREATE TABLE IF NOT EXISTS tags (
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

CREATE INDEX idx_tags_user_id ON tags (user_id);
CREATE INDEX idx_tags_name ON tags (name);
CREATE INDEX idx_tags_color ON tags (color);

-- ======================================================
-- MOVIMIENTOS Y TRANSACCIONES
-- ======================================================

-- ======================================================
-- 8. TRANSACTIONS
-- ======================================================
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    parent_transaction_id BIGINT NULL,
        -- operation_type: 'INCOME', 'EXPENSE', 'REALLOCATION' (reubicación entre cuentas propias)
    operation_type VARCHAR(20) NOT NULL,
        -- payment_method: 'CASH', 'CARD', 'WIRE_TRANSFER', 'INTERNAL', 'QR', 'CODI', 'WALLET'
    payment_method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED', -- 'PENDING', 'COMPLETED', 'FAILED', 'CANCELLED'
    source_account_id BIGINT NULL,
    destination_account_id BIGINT NULL,
    external_entity_id BIGINT NULL,
    category_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    concept VARCHAR(100) NOT NULL,
    description TEXT NULL,
    receipt_url VARCHAR(255) NULL,
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
    CONSTRAINT fk_tx_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT chk_tx_operation_type CHECK (operation_type IN ('INCOME', 'EXPENSE', 'REALLOCATION')),
    CONSTRAINT chk_tx_payment_method CHECK (payment_method IN ('CASH', 'CARD', 'WIRE_TRANSFER', 'INTERNAL', 'QR', 'CODI', 'WALLET')),
        -- INTERNAL (movimiento entre cuentas propias) solo tiene sentido en una REALLOCATION
    CONSTRAINT chk_tx_internal_reallocation CHECK (payment_method <> 'INTERNAL' OR operation_type = 'REALLOCATION')
);

CREATE INDEX idx_tx_user_id ON transactions (user_id);
CREATE INDEX idx_tx_date ON transactions (date);
CREATE INDEX idx_tx_type ON transactions (operation_type);
CREATE INDEX idx_tx_method ON transactions (payment_method);
CREATE INDEX idx_tx_source_account ON transactions (source_account_id);
CREATE INDEX idx_tx_destination_account ON transactions (destination_account_id);
CREATE INDEX idx_tx_entity ON transactions (external_entity_id);

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
-- 10. CARD_TRANSACTION_DETAILS
-- ======================================================
CREATE TABLE IF NOT EXISTS card_transaction_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    
    -- Monto específico cargado a la tarjeta (útil en pagos mixtos)
    amount DECIMAL(12, 2) NOT NULL,
    
    -- MSI: Si es NULL, es pago en una sola exhibición
    installment_months INT NULL,
    interest_free BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_tx FOREIGN KEY (transaction_id) REFERENCES transactions (id) ON DELETE CASCADE,
    CONSTRAINT fk_card_detail FOREIGN KEY (card_id) REFERENCES cards (id) ON DELETE CASCADE,
    
    -- Validaciones de integridad
    CONSTRAINT chk_card_amount CHECK (amount > 0),
    CONSTRAINT chk_installments CHECK (installment_months IS NULL OR installment_months > 0)
);

-- Índices para reportes de MSI y consumo por plástico
CREATE INDEX idx_card_tx ON card_transaction_details (transaction_id);
CREATE INDEX idx_card_detail_card ON card_transaction_details (card_id);
CREATE INDEX idx_card_msi ON card_transaction_details (interest_free, installment_months);

-- ======================================================
-- 11. WALLET_TRANSACTION_DETAILS
-- ======================================================
CREATE TABLE IF NOT EXISTS wallet_transaction_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL,
    source_type VARCHAR(20) NOT NULL, -- 'WALLET_BALANCE', 'LINKED_CARD' (shared WalletTransactionSourceType)
    wallet_account_id BIGINT NOT NULL, -- Referencia a accounts (type='WALLET')
    card_id BIGINT NULL,               -- Solo si source_type = 'LINKED_CARD'
    amount DECIMAL(12, 2) NOT NULL,
    cashback_rate DECIMAL(9, 6) NULL,  -- Fracción 0-1: 0.020000 = 2% (igual que default_cashback_rate)
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_tx FOREIGN KEY (transaction_id) REFERENCES transactions (id) ON DELETE CASCADE,
    CONSTRAINT fk_wallet_account FOREIGN KEY (wallet_account_id) REFERENCES accounts (id) ON DELETE CASCADE,
    CONSTRAINT fk_wallet_card FOREIGN KEY (card_id) REFERENCES cards (id) ON DELETE SET NULL,
    
    -- Validaciones
    CONSTRAINT chk_wallet_amount CHECK (amount > 0),
    CONSTRAINT chk_wallet_source_type CHECK (source_type IN ('WALLET_BALANCE', 'LINKED_CARD')),
    CONSTRAINT chk_wallet_cashback_rate CHECK (cashback_rate IS NULL OR (cashback_rate >= 0 AND cashback_rate <= 1))
);

-- Índices para analítica de Cashback y uso de Wallet
CREATE INDEX idx_wallet_transaction ON wallet_transaction_details (transaction_id);
CREATE INDEX idx_wallet_payment_wallet ON wallet_transaction_details (wallet_account_id);
CREATE INDEX idx_wallet_payment_card ON wallet_transaction_details (card_id);
CREATE INDEX idx_wallet_cashback_rate ON wallet_transaction_details (cashback_rate);

-- ======================================================
-- PROCEDIMIENTOS ALMACENADOS (efecto en saldos)
-- ======================================================
-- Centralizan la lógica de saldos para que los triggers no la dupliquen:
--   - sp_apply_transaction_effect / sp_revert_transaction_effect -> triggers 2, 3 y 4 (transactions)
--   - sp_wallet_detail_effect -> triggers 5, 5.1 y 5.2 (wallet_transaction_details)

DELIMITER //

-- ======================================================
-- PROCEDIMIENTO 1: Aplicar el efecto de una transacción en los saldos
-- ======================================================
-- Origen (EXPENSE / REALLOCATION):
--   CREDIT -> credit_used + monto (aumenta la deuda)
--   otro   -> current_balance - monto
-- Destino (INCOME / REALLOCATION):
--   CREDIT -> pago de tarjeta: reduce credit_used; el sobrepago va a current_balance
--   otro   -> current_balance + monto
-- WALLET se ignora: lo gestionan los triggers de wallet_transaction_details.
DROP PROCEDURE IF EXISTS sp_apply_transaction_effect //

CREATE PROCEDURE sp_apply_transaction_effect(
    IN p_operation_type VARCHAR(20),
    IN p_payment_method VARCHAR(20),
    IN p_source_account_id BIGINT,
    IN p_destination_account_id BIGINT,
    IN p_amount DECIMAL(12, 2)
)
proc: BEGIN
    DECLARE v_source_type VARCHAR(20) DEFAULT NULL;
    DECLARE v_dest_type VARCHAR(20) DEFAULT NULL;
    DECLARE v_credit_used DECIMAL(12, 2) DEFAULT 0.00;
    DECLARE v_overpayment DECIMAL(12, 2) DEFAULT 0.00;

    -- WALLET tiene su propia lógica (triggers 5, 5.1 y 5.2)
    IF p_payment_method = 'WALLET' THEN
        LEAVE proc;
    END IF;

    -- ==========================================
    -- CUENTA ORIGEN (sale dinero)
    -- ==========================================
    IF p_source_account_id IS NOT NULL AND p_operation_type IN ('EXPENSE', 'REALLOCATION') THEN
        SELECT type INTO v_source_type FROM accounts WHERE id = p_source_account_id;

        IF v_source_type = 'CREDIT' THEN
            -- Tarjeta de crédito: aumenta la deuda
            UPDATE credit_details SET credit_used = credit_used + p_amount
            WHERE account_id = p_source_account_id;
        ELSE
            -- Débito, efectivo, ahorro, etc.: resta el dinero
            UPDATE accounts SET current_balance = current_balance - p_amount
            WHERE id = p_source_account_id;
        END IF;
    END IF;

    -- ==========================================
    -- CUENTA DESTINO (entra dinero)
    -- ==========================================
    IF p_destination_account_id IS NOT NULL AND p_operation_type IN ('INCOME', 'REALLOCATION') THEN
        SELECT type INTO v_dest_type FROM accounts WHERE id = p_destination_account_id;

        IF v_dest_type = 'CREDIT' THEN
            -- Pago a la tarjeta de crédito (si no hay fila en credit_details, v_credit_used queda en 0)
            SELECT credit_used INTO v_credit_used
            FROM credit_details WHERE account_id = p_destination_account_id;

            IF p_amount <= v_credit_used THEN
                -- Cubre la deuda parcial o totalmente
                UPDATE credit_details SET credit_used = credit_used - p_amount
                WHERE account_id = p_destination_account_id;
            ELSE
                -- Pagó de más: la deuda queda en 0 y el excedente es saldo a favor
                SET v_overpayment = p_amount - v_credit_used;
                UPDATE credit_details SET credit_used = 0
                WHERE account_id = p_destination_account_id;
                UPDATE accounts SET current_balance = current_balance + v_overpayment
                WHERE id = p_destination_account_id;
            END IF;
        ELSE
            -- Depósito normal (débito, efectivo, ahorro, etc.)
            UPDATE accounts SET current_balance = current_balance + p_amount
            WHERE id = p_destination_account_id;
        END IF;
    END IF;
END //

-- ======================================================
-- PROCEDIMIENTO 2: Revertir el efecto de una transacción en los saldos
-- ======================================================
-- Inverso de sp_apply_transaction_effect:
-- Origen (EXPENSE / REALLOCATION):
--   CREDIT -> credit_used - monto
--   otro   -> current_balance + monto
-- Destino (INCOME / REALLOCATION):
--   CREDIT -> primero se descuenta del current_balance positivo (saldo a favor, hasta el monto)
--             y el resto vuelve a sumarse a credit_used
--   otro   -> current_balance - monto
DROP PROCEDURE IF EXISTS sp_revert_transaction_effect //

CREATE PROCEDURE sp_revert_transaction_effect(
    IN p_operation_type VARCHAR(20),
    IN p_payment_method VARCHAR(20),
    IN p_source_account_id BIGINT,
    IN p_destination_account_id BIGINT,
    IN p_amount DECIMAL(12, 2)
)
proc: BEGIN
    DECLARE v_source_type VARCHAR(20) DEFAULT NULL;
    DECLARE v_dest_type VARCHAR(20) DEFAULT NULL;
    DECLARE v_balance DECIMAL(12, 2) DEFAULT 0.00;
    DECLARE v_from_balance DECIMAL(12, 2) DEFAULT 0.00;

    -- WALLET tiene su propia lógica (triggers 5, 5.1 y 5.2)
    IF p_payment_method = 'WALLET' THEN
        LEAVE proc;
    END IF;

    -- ==========================================
    -- CUENTA ORIGEN (se devuelve el dinero)
    -- ==========================================
    IF p_source_account_id IS NOT NULL AND p_operation_type IN ('EXPENSE', 'REALLOCATION') THEN
        SELECT type INTO v_source_type FROM accounts WHERE id = p_source_account_id;

        IF v_source_type = 'CREDIT' THEN
            -- Se elimina el cargo: baja la deuda
            UPDATE credit_details SET credit_used = credit_used - p_amount
            WHERE account_id = p_source_account_id;
        ELSE
            UPDATE accounts SET current_balance = current_balance + p_amount
            WHERE id = p_source_account_id;
        END IF;
    END IF;

    -- ==========================================
    -- CUENTA DESTINO (se retira el dinero)
    -- ==========================================
    IF p_destination_account_id IS NOT NULL AND p_operation_type IN ('INCOME', 'REALLOCATION') THEN
        SELECT type INTO v_dest_type FROM accounts WHERE id = p_destination_account_id;

        IF v_dest_type = 'CREDIT' THEN
            -- Se deshace un pago a la tarjeta:
            -- 1) se quita del saldo a favor (current_balance positivo), hasta el monto
            SELECT current_balance INTO v_balance FROM accounts WHERE id = p_destination_account_id;
            SET v_from_balance = LEAST(GREATEST(v_balance, 0), p_amount);

            IF v_from_balance > 0 THEN
                UPDATE accounts SET current_balance = current_balance - v_from_balance
                WHERE id = p_destination_account_id;
            END IF;

            -- 2) el resto vuelve a ser deuda
            IF p_amount - v_from_balance > 0 THEN
                UPDATE credit_details SET credit_used = credit_used + (p_amount - v_from_balance)
                WHERE account_id = p_destination_account_id;
            END IF;
        ELSE
            UPDATE accounts SET current_balance = current_balance - p_amount
            WHERE id = p_destination_account_id;
        END IF;
    END IF;
END //

-- ======================================================
-- PROCEDIMIENTO 3: Aplicar (p_sign = 1) o revertir (p_sign = -1) un detalle de wallet
-- ======================================================
-- WALLET_BALANCE -> current_balance de la wallet - (signo * monto)
-- LINKED_CARD    -> según la cuenta de la tarjeta:
--                   CREDIT -> credit_used + (signo * monto)
--                   otro   -> current_balance - (signo * monto)
DROP PROCEDURE IF EXISTS sp_wallet_detail_effect //

CREATE PROCEDURE sp_wallet_detail_effect(
    IN p_source_type VARCHAR(20),
    IN p_wallet_account_id BIGINT,
    IN p_card_id BIGINT,
    IN p_amount DECIMAL(12, 2),
    IN p_sign TINYINT
)
BEGIN
    DECLARE v_card_account_id BIGINT DEFAULT NULL;
    DECLARE v_card_account_type VARCHAR(20) DEFAULT NULL;

    IF p_source_type = 'WALLET_BALANCE' THEN
        -- Caso saldo de la wallet
        UPDATE accounts SET current_balance = current_balance - (p_sign * p_amount)
        WHERE id = p_wallet_account_id;

    ELSEIF p_source_type = 'LINKED_CARD' AND p_card_id IS NOT NULL THEN
        -- Caso tarjeta vinculada: se afecta la cuenta dueña de la tarjeta
        SELECT a.id, a.type INTO v_card_account_id, v_card_account_type
        FROM cards c
        INNER JOIN accounts a ON a.id = c.account_id
        WHERE c.id = p_card_id;

        IF v_card_account_type = 'CREDIT' THEN
            UPDATE credit_details SET credit_used = credit_used + (p_sign * p_amount)
            WHERE account_id = v_card_account_id;
        ELSEIF v_card_account_id IS NOT NULL THEN
            UPDATE accounts SET current_balance = current_balance - (p_sign * p_amount)
            WHERE id = v_card_account_id;
        END IF;
    END IF;
END //

DELIMITER ;

-- ======================================================
-- AUTOMATIZACIÓN DE SALDOS (TRIGGERS)
-- ======================================================

-- ======================================================
-- TRIGGERS DE TRANSACTIONS (1 a 4)
-- ======================================================
DELIMITER //

-- ======================================================
-- TRIGGER 1: Validación de monto antes de insertar transacción
-- ======================================================
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

-- ======================================================
-- TRIGGER 2: Aplicar saldos después de insertar transacción
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_transaction_insert_master //

CREATE TRIGGER tr_after_transaction_insert_master
AFTER INSERT ON transactions
FOR EACH ROW
BEGIN
    -- Aplica el efecto del NEW (ignora WALLET dentro del procedimiento)
    CALL sp_apply_transaction_effect(
        NEW.operation_type, NEW.payment_method,
        NEW.source_account_id, NEW.destination_account_id, NEW.amount
    );
END //

-- ======================================================
-- TRIGGER 3: Corregir saldos al actualizar transacción
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_transaction_update //

CREATE TRIGGER tr_after_transaction_update
AFTER UPDATE ON transactions
FOR EACH ROW
BEGIN
    -- Solo se recalcula si cambió algún dato que afecta saldos
    -- (<=> compara también NULLs). Así una edición de concepto o tags no
    -- redistribuye saldo a favor / deuda de una tarjeta de crédito.
    IF NOT (OLD.operation_type <=> NEW.operation_type
        AND OLD.payment_method <=> NEW.payment_method
        AND OLD.source_account_id <=> NEW.source_account_id
        AND OLD.destination_account_id <=> NEW.destination_account_id
        AND OLD.amount <=> NEW.amount) THEN

        -- A. REVERTIR valores antiguos
        CALL sp_revert_transaction_effect(
            OLD.operation_type, OLD.payment_method,
            OLD.source_account_id, OLD.destination_account_id, OLD.amount
        );

        -- B. APLICAR valores nuevos
        CALL sp_apply_transaction_effect(
            NEW.operation_type, NEW.payment_method,
            NEW.source_account_id, NEW.destination_account_id, NEW.amount
        );
    END IF;
END //

-- ======================================================
-- TRIGGER 4: Restituir saldos al eliminar transacción
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_transaction_delete //

CREATE TRIGGER tr_after_transaction_delete
AFTER DELETE ON transactions
FOR EACH ROW
BEGIN
    CALL sp_revert_transaction_effect(
        OLD.operation_type, OLD.payment_method,
        OLD.source_account_id, OLD.destination_account_id, OLD.amount
    );
END //

DELIMITER ;

-- ======================================================
-- TRIGGERS DE WALLET_TRANSACTION_DETAILS (5, 5.1 y 5.2)
-- ======================================================
DELIMITER //

-- ======================================================
-- TRIGGER 5: Aplicar saldos al insertar detalle de wallet
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_wallet_detail_insert //

CREATE TRIGGER tr_after_wallet_detail_insert
AFTER INSERT ON wallet_transaction_details
FOR EACH ROW
BEGIN
    CALL sp_wallet_detail_effect(NEW.source_type, NEW.wallet_account_id, NEW.card_id, NEW.amount, 1);
END //

-- ======================================================
-- TRIGGER 5.1: Corregir saldos al actualizar detalle de wallet
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_wallet_detail_update //

CREATE TRIGGER tr_after_wallet_detail_update
AFTER UPDATE ON wallet_transaction_details
FOR EACH ROW
BEGIN
    -- Solo si cambió algún dato que afecta saldos
    IF NOT (OLD.source_type <=> NEW.source_type
        AND OLD.wallet_account_id <=> NEW.wallet_account_id
        AND OLD.card_id <=> NEW.card_id
        AND OLD.amount <=> NEW.amount) THEN

        -- A. REVERTIR valores antiguos
        CALL sp_wallet_detail_effect(OLD.source_type, OLD.wallet_account_id, OLD.card_id, OLD.amount, -1);

        -- B. APLICAR valores nuevos
        CALL sp_wallet_detail_effect(NEW.source_type, NEW.wallet_account_id, NEW.card_id, NEW.amount, 1);
    END IF;
END //

-- ======================================================
-- TRIGGER 5.2: Restituir saldos al eliminar detalle de wallet
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_wallet_detail_delete //

CREATE TRIGGER tr_after_wallet_detail_delete
AFTER DELETE ON wallet_transaction_details
FOR EACH ROW
BEGIN
    CALL sp_wallet_detail_effect(OLD.source_type, OLD.wallet_account_id, OLD.card_id, OLD.amount, -1);
END //

DELIMITER ;

-- ======================================================
-- TRIGGERS DE ACCOUNTS: sincronización de global_balance (6, 6.1 y 6.2)
-- ======================================================
DELIMITER //

-- ======================================================
-- TRIGGER 6: Sincronización de global_balance en users
-- ======================================================
DROP TRIGGER IF EXISTS tr_sync_global_balance //

CREATE TRIGGER tr_sync_global_balance
AFTER UPDATE ON accounts
FOR EACH ROW
BEGIN
    -- Solo actuamos si el saldo cambió para evitar bucles infinitos
    IF OLD.current_balance <> NEW.current_balance THEN
        UPDATE users
        SET global_balance = (
            SELECT COALESCE(SUM(current_balance), 0.00)
            FROM accounts
            WHERE user_id = NEW.user_id
        )
        WHERE id = NEW.user_id;
    END IF;
END //

-- ======================================================
-- TRIGGER 6.1: Sincronización al CREAR una cuenta (INSERT)
-- ======================================================
DROP TRIGGER IF EXISTS tr_sync_global_balance_insert //

CREATE TRIGGER tr_sync_global_balance_insert
AFTER INSERT ON accounts
FOR EACH ROW
BEGIN
    -- Solo actualizamos si la cuenta se crea con un saldo inicial diferente de cero
    IF NEW.current_balance <> 0 THEN
        UPDATE users
        SET global_balance = (
            SELECT COALESCE(SUM(current_balance), 0.00)
            FROM accounts
            WHERE user_id = NEW.user_id
        )
        WHERE id = NEW.user_id;
    END IF;
END //

-- ======================================================
-- TRIGGER 6.2: Sincronización al ELIMINAR una cuenta (DELETE)
-- ======================================================
DROP TRIGGER IF EXISTS tr_sync_global_balance_delete //

CREATE TRIGGER tr_sync_global_balance_delete
AFTER DELETE ON accounts
FOR EACH ROW
BEGIN
    -- Solo actualizamos si la cuenta eliminada tenía dinero
    IF OLD.current_balance <> 0 THEN
        UPDATE users
        SET global_balance = (
            SELECT COALESCE(SUM(current_balance), 0.00)
            FROM accounts
            WHERE user_id = OLD.user_id
        )
        WHERE id = OLD.user_id;
    END IF;
END //

DELIMITER ;

-- ======================================================
-- TRIGGER DE CARDS (7)
-- ======================================================
DELIMITER //

-- ======================================================
-- TRIGGER 7: Validar que la tarjeta se vincule a cuenta bancaria
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_card_insert //

CREATE TRIGGER tr_before_card_insert
BEFORE INSERT ON cards
FOR EACH ROW
BEGIN
    DECLARE v_bank_id BIGINT;

    -- Buscamos si la cuenta tiene vínculo bancario (en bank_details o credit_details)
    SELECT COALESCE(
        (SELECT bank_client_id FROM bank_details WHERE account_id = NEW.account_id),
        (SELECT bank_client_id FROM credit_details WHERE account_id = NEW.account_id)
    ) INTO v_bank_id;

    -- Si es NULL, no es una cuenta bancaria y bloqueamos
    IF v_bank_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Solo se pueden vincular tarjetas a cuentas bancarias.';
    END IF;
END //

DELIMITER ;

-- ======================================================
-- TRIGGER DE TRANSACTIONS: validación de reubicaciones (8)
-- ======================================================
-- Es el segundo BEFORE INSERT sobre transactions: FOLLOWS fija que se ejecute
-- después del trigger 1 (que normaliza el monto).
DELIMITER //

-- ======================================================
-- TRIGGER 8: Validación de reubicaciones
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_transaction_reallocation_check //

CREATE TRIGGER tr_before_transaction_reallocation_check
BEFORE INSERT ON transactions
FOR EACH ROW
FOLLOWS tr_before_transaction_insert_val
BEGIN
    DECLARE v_acc_type VARCHAR(20);
    DECLARE v_can_transfer BOOLEAN;

    IF NEW.operation_type = 'REALLOCATION' THEN

        -- Validaciones mínimas de integridad para reubicaciones
        IF NEW.source_account_id IS NULL OR NEW.destination_account_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Reubicación requiere source_account_id y destination_account_id.';
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
            SET MESSAGE_TEXT = 'Restricción: Esta cuenta no permite salidas de dinero (reubicaciones).';
        END IF;

    END IF;
END //

DELIMITER ;
