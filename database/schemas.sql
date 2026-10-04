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
        -- opening_balance: saldo con el que se creó la cuenta. Lo fija el trigger 9
        -- (= current_balance al insertar); no se edita después. Base de v_account_reconciliation.
    opening_balance DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
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
        -- opening_credit_used: deuda con la que se registró el crédito. La fija el trigger 9.1
        -- (= credit_used al insertar); no se edita después. Base de v_account_reconciliation.
    opening_credit_used DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
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
    CONSTRAINT chk_credit_used_limit CHECK (credit_used <= credit_limit),
    CONSTRAINT chk_opening_credit_used CHECK (opening_credit_used >= 0)
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
        -- type: 'INCOME', 'EXPENSE', 'REALLOCATION' (reubicación entre cuentas propias), 'BOTH'
        -- (shared CategoryTypes). No se cruza con transactions.operation_type en BD.
        -- OPCIÓN 1: VARCHAR + CHECK (Flexibilidad)
        -- Es un texto con una regla "pegada" que imita al ENUM.
    type VARCHAR(20) NOT NULL,
        -- OPCIÓN 2: ENUM (Rigidez/Optimización)
        -- type ENUM('INCOME', 'EXPENSE', 'REALLOCATION', 'BOTH') NOT NULL,
    icon VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
        -- Tu restricción UNIQUE (user_id, name): 
        -- Evita que tengas dos "Comida", pero permite que OTRO usuario tenga la suya.
    CONSTRAINT unique_category_per_user UNIQUE (user_id, name),
        -- LA REGLA "TIPO ENUM":
        -- Obliga a que el VARCHAR solo acepte estas 4 palabras.
    CONSTRAINT chk_category_type CHECK (type IN ('INCOME', 'EXPENSE', 'REALLOCATION', 'BOTH'))
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
        -- status: 'PENDING', 'COMPLETED', 'FAILED', 'CANCELLED'. Solo COMPLETED afecta saldos
        -- (triggers 2, 3, 4 y 4.1, y los de wallet_transaction_details 5, 5.1 y 5.2)
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
        -- Partes por tipo de operación (sp_validate_transaction_parties, triggers 8.2 y 8.3;
        -- en REALLOCATION, origen/destino los valida sp_validate_transaction_reallocation):
        --   INCOME       -> destination_account_id y external_entity_id obligatorios; source_account_id NULL
        --   EXPENSE      -> source_account_id y external_entity_id obligatorios; destination_account_id NULL
        --   REALLOCATION -> source_account_id y destination_account_id (distintos); external_entity_id NULL
        -- En WALLET (solo EXPENSE), source_account_id es la cuenta que financia el pago: la
        -- wallet (WALLET_BALANCE) o la cuenta de la tarjeta vinculada (LINKED_CARD). En CARD, es
        -- la cuenta de la tarjeta de card_transaction_details.
        -- Son NULL-ables por las reglas anteriores y por ON DELETE SET NULL.
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
    CONSTRAINT chk_tx_internal_reallocation CHECK (payment_method <> 'INTERNAL' OR operation_type = 'REALLOCATION'),
        -- WALLET (pago con wallet) solo tiene sentido en un gasto
    CONSTRAINT chk_tx_wallet_expense CHECK (payment_method <> 'WALLET' OR operation_type = 'EXPENSE'),
    CONSTRAINT chk_tx_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED'))
);

-- idx_tx_category e idx_tx_parent sustituyen a los índices implícitos que InnoDB crea para
-- fk_tx_category y fk_tx_parent (MySQL los elimina al existir uno equivalente).
CREATE INDEX idx_tx_user_id ON transactions (user_id);
CREATE INDEX idx_tx_date ON transactions (date);
CREATE INDEX idx_tx_type ON transactions (operation_type);
CREATE INDEX idx_tx_method ON transactions (payment_method);
CREATE INDEX idx_tx_source_account ON transactions (source_account_id);
CREATE INDEX idx_tx_destination_account ON transactions (destination_account_id);
CREATE INDEX idx_tx_entity ON transactions (external_entity_id);
CREATE INDEX idx_tx_category ON transactions (category_id);
CREATE INDEX idx_tx_parent ON transactions (parent_transaction_id);

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
-- Detalle de una transacción con payment_method = 'CARD'. Solo registra información
-- (MSI, plástico usado): el efecto en saldos lo aplica la transacción sobre source_account_id.
-- Triggers 10 y 10.1 (sp_validate_card_detail): la transacción padre debe ser CARD y la
-- tarjeta debe pertenecer a su source_account_id. El backend inserta primero la transacción
-- y después el detalle (misma transacción SQL).
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
-- Detalle de una transacción con payment_method = 'WALLET' (solo EXPENSE). Su efecto en
-- saldos lo aplican los triggers 5, 5.1 y 5.2 (la transacción WALLET no tiene efecto propio).
-- Triggers 5.3 y 5.4 (sp_validate_wallet_detail): padre WALLET, wallet_account_id de tipo
-- WALLET y, en LINKED_CARD, card_id obligatorio y vinculado a la wallet (wallet_card_links).
-- El backend inserta primero la transacción y después el detalle (misma transacción SQL).
CREATE TABLE IF NOT EXISTS wallet_transaction_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL,
    source_type VARCHAR(20) NOT NULL, -- 'WALLET_BALANCE', 'LINKED_CARD' (shared WalletTransactionSourceType)
    wallet_account_id BIGINT NOT NULL, -- Referencia a accounts (type='WALLET', triggers 5.3 y 5.4)
    card_id BIGINT NULL,               -- Obligatorio si source_type = 'LINKED_CARD' (triggers 5.3 y 5.4, no CHECK: ver ahí)
    amount DECIMAL(12, 2) NOT NULL,
    cashback_rate DECIMAL(9, 6) NULL,  -- Fracción 0-1: 0.020000 = 2% (igual que default_cashback_rate). Solo informativo: no afecta saldos
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
-- PROCEDIMIENTOS ALMACENADOS (efecto en saldos y validaciones)
-- ======================================================
-- Centralizan la lógica para que los triggers no la dupliquen:
--   - sp_apply_transaction_effect / sp_revert_transaction_effect -> triggers 2, 3 y 4 (transactions)
--   - sp_wallet_detail_effect -> triggers 5, 5.1 y 5.2 (wallet_transaction_details)
--   - sp_wallet_details_effect_for_transaction -> triggers 3 y 4.1 (todos los detalles de wallet de una transacción)
--   - sp_validate_transaction_amount -> triggers 1 y 1.1 (monto)
--   - sp_validate_transaction_reallocation -> triggers 8 y 8.1 (reubicaciones)
--   - sp_validate_wallet_detail -> triggers 5.3 y 5.4 (padre WALLET, cuenta WALLET, tarjeta vinculada)
--   - sp_reconcile_account (PROCEDIMIENTO 8) -> definido al final, en RECONCILIACIÓN DE CUENTAS
--     (usa la vista v_account_reconciliation)
--   - sp_validate_transaction_parties (PROCEDIMIENTO 9) -> triggers 8.2 y 8.3 (origen, destino
--     y entidad externa según operation_type)
--   - sp_validate_card_detail (PROCEDIMIENTO 10) -> triggers 10 y 10.1 (padre CARD, tarjeta de
--     la cuenta origen)
--
-- Regla de estado: solo las transacciones con status = 'COMPLETED' afectan saldos.
-- Los procedimientos de efecto no consultan el estado; lo filtran los triggers que los llaman.

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

-- ======================================================
-- PROCEDIMIENTO 4: Aplicar (p_sign = 1) o revertir (p_sign = -1) todos los detalles de wallet de una transacción
-- ======================================================
-- Recorre wallet_transaction_details de p_transaction_id y llama a sp_wallet_detail_effect
-- por cada fila. Si la transacción no tiene detalles, no hace nada.
-- Lo usan:
--   - Trigger 3: cuando la transacción pasa de COMPLETED a otro estado (-1) o al revés (1).
--   - Trigger 4.1: antes de borrar una transacción COMPLETED (-1), porque el borrado en
--     cascada de sus detalles NO dispara el trigger 5.2.
DROP PROCEDURE IF EXISTS sp_wallet_details_effect_for_transaction //

CREATE PROCEDURE sp_wallet_details_effect_for_transaction(
    IN p_transaction_id BIGINT,
    IN p_sign TINYINT
)
BEGIN
    DECLARE v_done BOOLEAN DEFAULT FALSE;
    DECLARE v_source_type VARCHAR(20) DEFAULT NULL;
    DECLARE v_wallet_account_id BIGINT DEFAULT NULL;
    DECLARE v_card_id BIGINT DEFAULT NULL;
    DECLARE v_amount DECIMAL(12, 2) DEFAULT 0.00;

    DECLARE cur_details CURSOR FOR
        SELECT source_type, wallet_account_id, card_id, amount
        FROM wallet_transaction_details
        WHERE transaction_id = p_transaction_id;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = TRUE;

    OPEN cur_details;

    details_loop: LOOP
        FETCH cur_details INTO v_source_type, v_wallet_account_id, v_card_id, v_amount;
        IF v_done THEN
            LEAVE details_loop;
        END IF;

        CALL sp_wallet_detail_effect(v_source_type, v_wallet_account_id, v_card_id, v_amount, p_sign);

        -- Un SELECT ... INTO sin filas dentro del procedimiento llamado también activa el
        -- handler NOT FOUND; se reinicia la bandera para no cortar el recorrido antes de tiempo.
        SET v_done = FALSE;
    END LOOP;

    CLOSE cur_details;
END //

-- ======================================================
-- PROCEDIMIENTO 5: Validar y normalizar el monto de una transacción
-- ======================================================
-- INOUT: los triggers BEFORE pasan NEW.amount y el procedimiento lo modifica.
--   - Monto negativo -> se convierte a positivo (ABS)
--   - Monto cero     -> error (no tiene sentido contable)
DROP PROCEDURE IF EXISTS sp_validate_transaction_amount //

CREATE PROCEDURE sp_validate_transaction_amount(
    INOUT p_amount DECIMAL(12, 2)
)
BEGIN
    -- 1. Si el monto es negativo, lo pasamos a positivo (ABS)
    IF p_amount < 0 THEN
        SET p_amount = ABS(p_amount);
    END IF;

    -- 2. Bloqueamos montos en cero (no tienen sentido contable)
    IF p_amount = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: El monto de la transacción debe ser mayor a cero.';
    END IF;
END //

-- ======================================================
-- PROCEDIMIENTO 6: Validar una reubicación (REALLOCATION)
-- ======================================================
-- Si p_operation_type = 'REALLOCATION':
--   - exige origen y destino, y que sean distintos
--   - exige que la cuenta origen exista
--   - bloquea cuentas origen BENEFIT o con bank_details.can_transfer_out = FALSE
-- Para otros tipos de operación no hace nada.
DROP PROCEDURE IF EXISTS sp_validate_transaction_reallocation //

CREATE PROCEDURE sp_validate_transaction_reallocation(
    IN p_operation_type VARCHAR(20),
    IN p_source_account_id BIGINT,
    IN p_destination_account_id BIGINT
)
BEGIN
    DECLARE v_acc_type VARCHAR(20) DEFAULT NULL;
    DECLARE v_can_transfer BOOLEAN DEFAULT TRUE;

    IF p_operation_type = 'REALLOCATION' THEN

        -- Validaciones mínimas de integridad para reubicaciones
        IF p_source_account_id IS NULL OR p_destination_account_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Reubicación requiere source_account_id y destination_account_id.';
        END IF;

        IF p_source_account_id = p_destination_account_id THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Origen y destino no pueden ser iguales.';
        END IF;

        -- Obtener tipo de cuenta origen y bandera can_transfer_out (si existe)
        SELECT a.type, COALESCE(bd.can_transfer_out, TRUE)
        INTO v_acc_type, v_can_transfer
        FROM accounts a
        LEFT JOIN bank_details bd ON a.id = bd.account_id
        WHERE a.id = p_source_account_id;

        -- Si la cuenta origen no existe, el SELECT anterior no devuelve fila (v_acc_type queda NULL).
        -- Esta validación fuerza un error claro.
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

-- ======================================================
-- PROCEDIMIENTO 7: Validar un detalle de wallet
-- ======================================================
--   - la transacción padre debe existir y tener payment_method = 'WALLET'
--   - wallet_account_id debe ser una cuenta de tipo 'WALLET'
--   - source_type = 'LINKED_CARD' exige card_id (sin tarjeta no hay cuenta a la que cargar)
--     y que la tarjeta esté vinculada a esa wallet (wallet_card_links)
-- Se valida con trigger y no con CHECK porque MySQL prohíbe CHECK sobre columnas
-- usadas en una FK con acción referencial ON DELETE SET NULL (fk_wallet_card sobre card_id),
-- y porque las reglas consultan otras tablas.
-- El padre ya existe al insertar el detalle: el backend inserta primero la transacción.
DROP PROCEDURE IF EXISTS sp_validate_wallet_detail //

CREATE PROCEDURE sp_validate_wallet_detail(
    IN p_transaction_id BIGINT,
    IN p_source_type VARCHAR(20),
    IN p_wallet_account_id BIGINT,
    IN p_card_id BIGINT
)
BEGIN
    DECLARE v_payment_method VARCHAR(20) DEFAULT NULL;
    DECLARE v_wallet_type VARCHAR(20) DEFAULT NULL;

    -- 1. Transacción padre: debe existir y ser un pago con WALLET
    SELECT payment_method INTO v_payment_method
    FROM transactions WHERE id = p_transaction_id;

    IF v_payment_method IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: La transacción del detalle de wallet no existe.';
    END IF;

    IF v_payment_method <> 'WALLET' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Solo una transacción con método de pago WALLET puede tener detalle de wallet.';
    END IF;

    -- 2. La cuenta del detalle debe ser una wallet
    SELECT type INTO v_wallet_type
    FROM accounts WHERE id = p_wallet_account_id;

    IF NOT (v_wallet_type <=> 'WALLET') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: La cuenta del detalle de wallet debe ser una cuenta de tipo WALLET.';
    END IF;

    -- 3. Tarjeta vinculada: obligatoria y enlazada a esa wallet
    IF p_source_type = 'LINKED_CARD' THEN
        IF p_card_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Un detalle de wallet con origen LINKED_CARD requiere card_id.';
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM wallet_card_links
            WHERE account_id = p_wallet_account_id AND card_id = p_card_id
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: La tarjeta no está vinculada a la wallet del detalle.';
        END IF;
    END IF;
END //

-- ======================================================
-- PROCEDIMIENTO 9: Validar las partes de una transacción (cuentas y entidad externa)
-- ======================================================
-- Reglas estructurales según operation_type (no consulta tablas):
--   INCOME       -> destino y entidad externa obligatorios; sin origen
--   EXPENSE      -> origen y entidad externa obligatorios; sin destino
--   REALLOCATION -> sin entidad externa (origen y destino los valida
--                   sp_validate_transaction_reallocation)
-- (El PROCEDIMIENTO 8, sp_reconcile_account, está al final del archivo.)
DROP PROCEDURE IF EXISTS sp_validate_transaction_parties //

CREATE PROCEDURE sp_validate_transaction_parties(
    IN p_operation_type VARCHAR(20),
    IN p_source_account_id BIGINT,
    IN p_destination_account_id BIGINT,
    IN p_external_entity_id BIGINT
)
BEGIN
    IF p_operation_type = 'INCOME' THEN
        IF p_destination_account_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Un ingreso requiere una cuenta destino.';
        END IF;

        IF p_external_entity_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Un ingreso requiere una entidad externa (quién paga).';
        END IF;

        IF p_source_account_id IS NOT NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Un ingreso no puede tener cuenta origen.';
        END IF;

    ELSEIF p_operation_type = 'EXPENSE' THEN
        IF p_source_account_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Un gasto requiere una cuenta origen.';
        END IF;

        IF p_external_entity_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Un gasto requiere una entidad externa (a quién se paga).';
        END IF;

        IF p_destination_account_id IS NOT NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Un gasto no puede tener cuenta destino.';
        END IF;

    ELSEIF p_operation_type = 'REALLOCATION' THEN
        IF p_external_entity_id IS NOT NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Una reubicación entre cuentas propias no puede tener entidad externa.';
        END IF;
    END IF;
END //

-- ======================================================
-- PROCEDIMIENTO 10: Validar un detalle de tarjeta
-- ======================================================
--   - la transacción padre debe existir y tener payment_method = 'CARD'
--   - la tarjeta debe existir y pertenecer a la cuenta origen de la transacción
--     (cards.account_id = transactions.source_account_id)
-- El padre ya existe al insertar el detalle: el backend inserta primero la transacción
-- (con source_account_id fijado) y después el detalle, en la misma transacción SQL.
DROP PROCEDURE IF EXISTS sp_validate_card_detail //

CREATE PROCEDURE sp_validate_card_detail(
    IN p_transaction_id BIGINT,
    IN p_card_id BIGINT
)
BEGIN
    DECLARE v_payment_method VARCHAR(20) DEFAULT NULL;
    DECLARE v_source_account_id BIGINT DEFAULT NULL;
    DECLARE v_card_account_id BIGINT DEFAULT NULL;

    -- 1. Transacción padre: debe existir y ser un pago con tarjeta
    SELECT payment_method, source_account_id INTO v_payment_method, v_source_account_id
    FROM transactions WHERE id = p_transaction_id;

    IF v_payment_method IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: La transacción del detalle de tarjeta no existe.';
    END IF;

    IF v_payment_method <> 'CARD' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Solo una transacción con método de pago CARD puede tener detalle de tarjeta.';
    END IF;

    -- 2. La tarjeta debe ser de la cuenta origen
    SELECT account_id INTO v_card_account_id
    FROM cards WHERE id = p_card_id;

    IF v_card_account_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: La tarjeta del detalle no existe.';
    END IF;

    IF NOT (v_card_account_id <=> v_source_account_id) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: La tarjeta debe pertenecer a la cuenta origen de la transacción.';
    END IF;
END //

DELIMITER ;

-- ======================================================
-- AUTOMATIZACIÓN DE SALDOS (TRIGGERS)
-- ======================================================

-- ======================================================
-- TRIGGERS DE TRANSACTIONS (1 a 4.1)
-- ======================================================
-- Regla de estado: solo las transacciones con status = 'COMPLETED' afectan saldos
-- (los suyos y los de sus wallet_transaction_details).
--
-- Orden por evento sobre transactions:
--   BEFORE INSERT: 1 -> 8 -> 8.2 (FOLLOWS)
--   BEFORE UPDATE: 1.1 -> 8.1 -> 8.3 (FOLLOWS)
--   BEFORE DELETE: 4.1
--   AFTER INSERT: 2 / AFTER UPDATE: 3 / AFTER DELETE: 4
--
-- Nota: las acciones en cascada de las FKs (p. ej. ON DELETE SET NULL de
-- source_account_id / destination_account_id al borrar una cuenta) NO disparan
-- triggers en MySQL, así que no pasan por las validaciones de UPDATE ni por el trigger 3.
DELIMITER //

-- ======================================================
-- TRIGGER 1: Validación de monto antes de insertar transacción
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_transaction_insert_val //

CREATE TRIGGER tr_before_transaction_insert_val
BEFORE INSERT ON transactions
FOR EACH ROW
BEGIN
    -- ABS de montos negativos y bloqueo de monto 0 (modifica NEW.amount vía INOUT)
    CALL sp_validate_transaction_amount(NEW.amount);
END //

-- ======================================================
-- TRIGGER 1.1: Validación de monto antes de actualizar transacción
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_transaction_update_val //

CREATE TRIGGER tr_before_transaction_update_val
BEFORE UPDATE ON transactions
FOR EACH ROW
BEGIN
    -- Misma regla que en INSERT: un UPDATE no puede dejar amount <= 0
    CALL sp_validate_transaction_amount(NEW.amount);
END //

-- ======================================================
-- TRIGGER 2: Aplicar saldos después de insertar transacción
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_transaction_insert_master //

CREATE TRIGGER tr_after_transaction_insert_master
AFTER INSERT ON transactions
FOR EACH ROW
BEGIN
    -- Solo las transacciones COMPLETED afectan saldos.
    -- (Una transacción recién insertada aún no tiene detalles de wallet.)
    IF NEW.status = 'COMPLETED' THEN
        -- Aplica el efecto del NEW (ignora WALLET dentro del procedimiento)
        CALL sp_apply_transaction_effect(
            NEW.operation_type, NEW.payment_method,
            NEW.source_account_id, NEW.destination_account_id, NEW.amount
        );
    END IF;
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
        AND OLD.amount <=> NEW.amount
        AND OLD.status <=> NEW.status) THEN

        -- A. REVERTIR valores antiguos (solo si estaban aplicados)
        IF OLD.status = 'COMPLETED' THEN
            CALL sp_revert_transaction_effect(
                OLD.operation_type, OLD.payment_method,
                OLD.source_account_id, OLD.destination_account_id, OLD.amount
            );
        END IF;

        -- B. APLICAR valores nuevos (solo si quedan COMPLETED)
        IF NEW.status = 'COMPLETED' THEN
            CALL sp_apply_transaction_effect(
                NEW.operation_type, NEW.payment_method,
                NEW.source_account_id, NEW.destination_account_id, NEW.amount
            );
        END IF;
    END IF;

    -- C. Detalles de wallet: su efecto depende solo del estado de la transacción padre
    --    (igual que en los triggers 5, 5.1 y 5.2). Si no tiene detalles, no hace nada.
    IF OLD.status = 'COMPLETED' AND NEW.status <> 'COMPLETED' THEN
        CALL sp_wallet_details_effect_for_transaction(NEW.id, -1);
    ELSEIF OLD.status <> 'COMPLETED' AND NEW.status = 'COMPLETED' THEN
        CALL sp_wallet_details_effect_for_transaction(NEW.id, 1);
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
    -- Solo se revierte lo que estaba aplicado
    IF OLD.status = 'COMPLETED' THEN
        CALL sp_revert_transaction_effect(
            OLD.operation_type, OLD.payment_method,
            OLD.source_account_id, OLD.destination_account_id, OLD.amount
        );
    END IF;
END //

-- ======================================================
-- TRIGGER 4.1: Restituir saldos de los detalles de wallet antes de eliminar la transacción
-- ======================================================
-- Al borrar una transacción, sus wallet_transaction_details se eliminan por
-- ON DELETE CASCADE (fk_wallet_tx). MySQL NO dispara triggers en acciones en cascada,
-- así que el trigger 5.2 no se ejecuta: este trigger revierte su efecto aquí.
-- No hay doble reversión: el 5.2 solo corre en un DELETE directo sobre
-- wallet_transaction_details, nunca en la cascada.
-- Si el DELETE falla después (p. ej. por otra FK), InnoDB deshace también esta reversión.
DROP TRIGGER IF EXISTS tr_before_transaction_delete //

CREATE TRIGGER tr_before_transaction_delete
BEFORE DELETE ON transactions
FOR EACH ROW
BEGIN
    IF OLD.status = 'COMPLETED' THEN
        CALL sp_wallet_details_effect_for_transaction(OLD.id, -1);
    END IF;
END //

DELIMITER ;

-- ======================================================
-- TRIGGERS DE WALLET_TRANSACTION_DETAILS (5 a 5.4)
-- ======================================================
-- Los efectos de un detalle solo se aplican si su transacción padre está COMPLETED.
--
-- Regla de cascadas (MySQL NO dispara triggers en acciones referenciales):
--   - Borrar la transacción padre (fk_wallet_tx CASCADE): lo revierte el trigger 4.1.
--   - Borrar una tarjeta (fk_wallet_card SET NULL): el movimiento histórico ya ocurrió y
--     NO se revierte; el detalle queda LINKED_CARD con card_id NULL. El trigger 5.1 no
--     reacciona (la cascada no lo dispara) y, a partir de ahí, ese detalle ya no afecta
--     saldos aunque se edite o borre (sp_wallet_detail_effect ignora card_id NULL).
--   - Borrar la cuenta wallet (fk_wallet_account CASCADE): tampoco se revierte.
DELIMITER //

-- ======================================================
-- TRIGGER 5: Aplicar saldos al insertar detalle de wallet
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_wallet_detail_insert //

CREATE TRIGGER tr_after_wallet_detail_insert
AFTER INSERT ON wallet_transaction_details
FOR EACH ROW
BEGIN
    DECLARE v_status VARCHAR(20) DEFAULT NULL;

    SELECT status INTO v_status FROM transactions WHERE id = NEW.transaction_id;

    IF v_status = 'COMPLETED' THEN
        CALL sp_wallet_detail_effect(NEW.source_type, NEW.wallet_account_id, NEW.card_id, NEW.amount, 1);
    END IF;
END //

-- ======================================================
-- TRIGGER 5.1: Corregir saldos al actualizar detalle de wallet
-- ======================================================
DROP TRIGGER IF EXISTS tr_after_wallet_detail_update //

CREATE TRIGGER tr_after_wallet_detail_update
AFTER UPDATE ON wallet_transaction_details
FOR EACH ROW
BEGIN
    DECLARE v_old_status VARCHAR(20) DEFAULT NULL;
    DECLARE v_new_status VARCHAR(20) DEFAULT NULL;

    -- Solo si cambió algún dato que afecta saldos (incluido mover el detalle a otra transacción)
    IF NOT (OLD.transaction_id <=> NEW.transaction_id
        AND OLD.source_type <=> NEW.source_type
        AND OLD.wallet_account_id <=> NEW.wallet_account_id
        AND OLD.card_id <=> NEW.card_id
        AND OLD.amount <=> NEW.amount) THEN

        SELECT status INTO v_old_status FROM transactions WHERE id = OLD.transaction_id;
        SELECT status INTO v_new_status FROM transactions WHERE id = NEW.transaction_id;

        -- A. REVERTIR valores antiguos (solo si estaban aplicados)
        IF v_old_status = 'COMPLETED' THEN
            CALL sp_wallet_detail_effect(OLD.source_type, OLD.wallet_account_id, OLD.card_id, OLD.amount, -1);
        END IF;

        -- B. APLICAR valores nuevos (solo si la transacción padre está COMPLETED)
        IF v_new_status = 'COMPLETED' THEN
            CALL sp_wallet_detail_effect(NEW.source_type, NEW.wallet_account_id, NEW.card_id, NEW.amount, 1);
        END IF;
    END IF;
END //

-- ======================================================
-- TRIGGER 5.2: Restituir saldos al eliminar detalle de wallet
-- ======================================================
-- Solo se dispara en un DELETE directo; el borrado en cascada desde transactions
-- lo cubre el trigger 4.1.
DROP TRIGGER IF EXISTS tr_after_wallet_detail_delete //

CREATE TRIGGER tr_after_wallet_detail_delete
AFTER DELETE ON wallet_transaction_details
FOR EACH ROW
BEGIN
    DECLARE v_status VARCHAR(20) DEFAULT NULL;

    SELECT status INTO v_status FROM transactions WHERE id = OLD.transaction_id;

    IF v_status = 'COMPLETED' THEN
        CALL sp_wallet_detail_effect(OLD.source_type, OLD.wallet_account_id, OLD.card_id, OLD.amount, -1);
    END IF;
END //

-- ======================================================
-- TRIGGER 5.3: Validar detalle de wallet antes de insertar
-- ======================================================
-- Padre WALLET, cuenta de tipo WALLET y, en LINKED_CARD, card_id obligatorio y vinculado
-- a la wallet (sp_validate_wallet_detail). Es trigger y no CHECK porque MySQL prohíbe
-- CHECK sobre card_id (FK con ON DELETE SET NULL, fk_wallet_card) y porque consulta
-- otras tablas.
DROP TRIGGER IF EXISTS tr_before_wallet_detail_insert_val //

CREATE TRIGGER tr_before_wallet_detail_insert_val
BEFORE INSERT ON wallet_transaction_details
FOR EACH ROW
BEGIN
    CALL sp_validate_wallet_detail(
        NEW.transaction_id, NEW.source_type, NEW.wallet_account_id, NEW.card_id
    );
END //

-- ======================================================
-- TRIGGER 5.4: Validar detalle de wallet antes de actualizar
-- ======================================================
-- Solo valida si cambió alguna columna que participa en las reglas (transaction_id,
-- source_type, wallet_account_id o card_id). Así un detalle histórico que quedó
-- LINKED_CARD con card_id NULL (tarjeta borrada) o cuya tarjeta se desvinculó después
-- se puede seguir editando (monto, cashback) sin error.
DROP TRIGGER IF EXISTS tr_before_wallet_detail_update_val //

CREATE TRIGGER tr_before_wallet_detail_update_val
BEFORE UPDATE ON wallet_transaction_details
FOR EACH ROW
BEGIN
    IF NOT (OLD.transaction_id <=> NEW.transaction_id
        AND OLD.source_type <=> NEW.source_type
        AND OLD.wallet_account_id <=> NEW.wallet_account_id
        AND OLD.card_id <=> NEW.card_id) THEN

        CALL sp_validate_wallet_detail(
            NEW.transaction_id, NEW.source_type, NEW.wallet_account_id, NEW.card_id
        );
    END IF;
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
-- TRIGGERS DE TRANSACTIONS: validación estructural (8 a 8.3)
-- ======================================================
-- 8 / 8.1: reubicaciones (sp_validate_transaction_reallocation).
-- 8.2 / 8.3: partes según operation_type (sp_validate_transaction_parties).
-- FOLLOWS fija el orden de los BEFORE sobre transactions:
--   BEFORE INSERT: 1 (monto) -> 8 (reubicación) -> 8.2 (partes)
--   BEFORE UPDATE: 1.1 (monto) -> 8.1 (reubicación) -> 8.3 (partes)
DELIMITER //

-- ======================================================
-- TRIGGER 8: Validación de reubicaciones al insertar
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_transaction_reallocation_check //

CREATE TRIGGER tr_before_transaction_reallocation_check
BEFORE INSERT ON transactions
FOR EACH ROW
FOLLOWS tr_before_transaction_insert_val
BEGIN
    CALL sp_validate_transaction_reallocation(
        NEW.operation_type, NEW.source_account_id, NEW.destination_account_id
    );
END //

-- ======================================================
-- TRIGGER 8.1: Validación de reubicaciones al actualizar
-- ======================================================
-- Solo valida si cambió el tipo de operación, el origen o el destino, o si la
-- transacción pasa a COMPLETED (en ese momento mueve dinero). Así, editar el
-- concepto o cancelar una reubicación vieja no falla aunque la cuenta origen
-- haya cambiado después (p. ej. can_transfer_out = FALSE).
DROP TRIGGER IF EXISTS tr_before_transaction_update_reallocation_check //

CREATE TRIGGER tr_before_transaction_update_reallocation_check
BEFORE UPDATE ON transactions
FOR EACH ROW
FOLLOWS tr_before_transaction_update_val
BEGIN
    IF (NOT (OLD.operation_type <=> NEW.operation_type
            AND OLD.source_account_id <=> NEW.source_account_id
            AND OLD.destination_account_id <=> NEW.destination_account_id))
        OR (OLD.status <> 'COMPLETED' AND NEW.status = 'COMPLETED') THEN

        CALL sp_validate_transaction_reallocation(
            NEW.operation_type, NEW.source_account_id, NEW.destination_account_id
        );
    END IF;
END //

-- ======================================================
-- TRIGGER 8.2: Validación de partes (origen, destino, entidad) al insertar
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_transaction_insert_parties_check //

CREATE TRIGGER tr_before_transaction_insert_parties_check
BEFORE INSERT ON transactions
FOR EACH ROW
FOLLOWS tr_before_transaction_reallocation_check
BEGIN
    CALL sp_validate_transaction_parties(
        NEW.operation_type, NEW.source_account_id,
        NEW.destination_account_id, NEW.external_entity_id
    );
END //

-- ======================================================
-- TRIGGER 8.3: Validación de partes (origen, destino, entidad) al actualizar
-- ======================================================
-- Valida SIEMPRE (las reglas son estructurales, no dependen del estado de otras tablas).
-- Consecuencia: una fila que quedó inválida por un ON DELETE SET NULL (cuenta o entidad
-- borrada; las cascadas no disparan triggers) no se puede editar ni cancelar hasta que el
-- mismo UPDATE complete los datos que faltan. Borrarla sí se puede.
DROP TRIGGER IF EXISTS tr_before_transaction_update_parties_check //

CREATE TRIGGER tr_before_transaction_update_parties_check
BEFORE UPDATE ON transactions
FOR EACH ROW
FOLLOWS tr_before_transaction_update_reallocation_check
BEGIN
    CALL sp_validate_transaction_parties(
        NEW.operation_type, NEW.source_account_id,
        NEW.destination_account_id, NEW.external_entity_id
    );
END //

DELIMITER ;

-- ======================================================
-- TRIGGERS DE CARD_TRANSACTION_DETAILS (10 y 10.1)
-- ======================================================
-- Únicos BEFORE INSERT / BEFORE UPDATE sobre card_transaction_details (sin FOLLOWS).
-- El detalle de tarjeta no tiene efecto en saldos (lo aplica la transacción padre sobre
-- source_account_id); estos triggers solo validan su coherencia con el padre.
DELIMITER //

-- ======================================================
-- TRIGGER 10: Validar detalle de tarjeta antes de insertar
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_card_detail_insert_val //

CREATE TRIGGER tr_before_card_detail_insert_val
BEFORE INSERT ON card_transaction_details
FOR EACH ROW
BEGIN
    CALL sp_validate_card_detail(NEW.transaction_id, NEW.card_id);
END //

-- ======================================================
-- TRIGGER 10.1: Validar detalle de tarjeta antes de actualizar
-- ======================================================
-- Solo valida si cambió transaction_id o card_id: editar MSI o monto de un detalle
-- histórico no falla aunque la tarjeta se haya movido de cuenta después.
DROP TRIGGER IF EXISTS tr_before_card_detail_update_val //

CREATE TRIGGER tr_before_card_detail_update_val
BEFORE UPDATE ON card_transaction_details
FOR EACH ROW
BEGIN
    IF NOT (OLD.transaction_id <=> NEW.transaction_id AND OLD.card_id <=> NEW.card_id) THEN
        CALL sp_validate_card_detail(NEW.transaction_id, NEW.card_id);
    END IF;
END //

DELIMITER ;

-- ======================================================
-- RECONCILIACIÓN DE CUENTAS
-- ======================================================
-- Compara el saldo guardado de cada cuenta con el que se deduce de su saldo de
-- apertura + el historial de transacciones COMPLETED, y permite ajustarlo.
--
-- Posición neta de una cuenta = current_balance - COALESCE(credit_details.credit_used, 0)
-- (en CREDIT el sobrepago vive en current_balance y la deuda en credit_used).
--
-- Valores de apertura (accounts.opening_balance, credit_details.opening_credit_used):
-- los fijan los triggers 9 y 9.1 al insertar y NO se editan después (backend y
-- client no los envían). Solo la migración 2026-10-04_account_reconciliation.sql
-- los calculó para las cuentas existentes; si se conoce el saldo inicial real, se
-- puede corregir con un UPDATE manual.

-- ======================================================
-- TRIGGERS DE APERTURA (9 y 9.1)
-- ======================================================
-- Únicos BEFORE INSERT sobre accounts y credit_details (no requieren FOLLOWS).
DELIMITER //

-- ======================================================
-- TRIGGER 9: Fijar el saldo de apertura de la cuenta
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_account_insert_opening //

CREATE TRIGGER tr_before_account_insert_opening
BEFORE INSERT ON accounts
FOR EACH ROW
BEGIN
    SET NEW.opening_balance = NEW.current_balance;
END //

-- ======================================================
-- TRIGGER 9.1: Fijar la deuda de apertura del crédito
-- ======================================================
DROP TRIGGER IF EXISTS tr_before_credit_details_insert_opening //

CREATE TRIGGER tr_before_credit_details_insert_opening
BEFORE INSERT ON credit_details
FOR EACH ROW
BEGIN
    SET NEW.opening_credit_used = NEW.credit_used;
END //

DELIMITER ;

-- ======================================================
-- VISTA: v_account_reconciliation (una fila por cuenta)
-- ======================================================
-- Replica las reglas de sp_apply_transaction_effect y sp_wallet_detail_effect:
--   Entradas (total_inflows): transacciones COMPLETED, payment_method <> 'WALLET',
--     destination_account_id = cuenta y operation_type IN ('INCOME', 'REALLOCATION').
--     (En destino CREDIT el pago reduce credit_used y el sobrepago va a current_balance:
--     la posición neta sube el monto completo en ambos casos.)
--   Salidas (total_outflows):
--     a) transacciones COMPLETED, payment_method <> 'WALLET', source_account_id = cuenta
--        y operation_type IN ('EXPENSE', 'REALLOCATION');
--     b) detalles de wallet WALLET_BALANCE (padre COMPLETED) -> salida de wallet_account_id;
--     c) detalles de wallet LINKED_CARD con card_id no nulo (padre COMPLETED) -> salida de
--        cards.account_id.
--     Los detalles de wallet cuentan sin importar el payment_method del padre (igual que
--     los triggers 5, 5.1, 5.2, 3 y 4.1).
--     En una transacción WALLET, source_account_id (cuenta que financia: la wallet o la
--     cuenta de la tarjeta vinculada) NO cuenta en a) porque se excluye WALLET: la salida
--     está solo en b) o c), sin doble conteo. En CARD, la salida está solo en a): los
--     card_transaction_details no se suman.
--   Excepción (igual que los procedimientos): en una cuenta CREDIT SIN fila en
--     credit_details, los procedimientos actualizan credit_details (0 filas) y no tienen
--     efecto, así que a) y c) no cuentan para ella. b) sí cuenta (actúa en current_balance).
-- expected_net = opening_net + total_inflows - total_outflows
-- difference   = actual_net - expected_net (0 = cuenta cuadrada)
-- Usa subconsultas escalares correlacionadas (no joins que multipliquen filas). Todos los
-- importes se exponen como DECIMAL(14, 2).
CREATE OR REPLACE VIEW v_account_reconciliation AS
SELECT
    r.account_id,
    r.user_id,
    r.account_name,
    r.account_type,
    r.opening_net,
    r.total_inflows,
    r.total_outflows,
    CAST(r.opening_net + r.total_inflows - r.total_outflows AS DECIMAL(14, 2)) AS expected_net,
    r.actual_net,
    CAST(r.actual_net - (r.opening_net + r.total_inflows - r.total_outflows) AS DECIMAL(14, 2)) AS difference
FROM (
    SELECT
        a.id AS account_id,
        a.user_id AS user_id,
        a.name AS account_name,
        a.type AS account_type,
        CAST(a.opening_balance - COALESCE(cd.opening_credit_used, 0) AS DECIMAL(14, 2)) AS opening_net,
        CAST(a.current_balance - COALESCE(cd.credit_used, 0) AS DECIMAL(14, 2)) AS actual_net,
        CAST(COALESCE((
            SELECT SUM(t.amount)
            FROM transactions t
            WHERE t.destination_account_id = a.id
              AND t.status = 'COMPLETED'
              AND t.payment_method <> 'WALLET'
              AND t.operation_type IN ('INCOME', 'REALLOCATION')
        ), 0) AS DECIMAL(14, 2)) AS total_inflows,
        CAST(
            -- a) Transacciones con la cuenta como origen
            (CASE WHEN a.type = 'CREDIT' AND cd.account_id IS NULL THEN 0 ELSE COALESCE((
                SELECT SUM(t.amount)
                FROM transactions t
                WHERE t.source_account_id = a.id
                  AND t.status = 'COMPLETED'
                  AND t.payment_method <> 'WALLET'
                  AND t.operation_type IN ('EXPENSE', 'REALLOCATION')
            ), 0) END)
            -- b) Pagos con saldo de la wallet
            + COALESCE((
                SELECT SUM(w.amount)
                FROM wallet_transaction_details w
                INNER JOIN transactions t ON t.id = w.transaction_id
                WHERE w.wallet_account_id = a.id
                  AND w.source_type = 'WALLET_BALANCE'
                  AND t.status = 'COMPLETED'
            ), 0)
            -- c) Pagos de wallet con una tarjeta de esta cuenta
            + (CASE WHEN a.type = 'CREDIT' AND cd.account_id IS NULL THEN 0 ELSE COALESCE((
                SELECT SUM(w.amount)
                FROM wallet_transaction_details w
                INNER JOIN cards c ON c.id = w.card_id
                INNER JOIN transactions t ON t.id = w.transaction_id
                WHERE c.account_id = a.id
                  AND w.source_type = 'LINKED_CARD'
                  AND t.status = 'COMPLETED'
            ), 0) END)
        AS DECIMAL(14, 2)) AS total_outflows
    FROM accounts a
    LEFT JOIN credit_details cd ON cd.account_id = a.id
) r;

-- ======================================================
-- PROCEDIMIENTO 8: Reconciliar una cuenta (difference = 0)
-- ======================================================
-- Lee expected_net de v_account_reconciliation y ajusta la cuenta:
--   no CREDIT -> current_balance = expected_net (se calcula como current_balance - difference,
--                que es lo mismo y además cuadra si hubiera una fila anómala en credit_details)
--   CREDIT    -> expected_net >= 0: credit_used = 0, current_balance = expected_net
--                expected_net <  0: credit_used = -expected_net, current_balance = 0
--                (misma regla de sobrepago que sp_apply_transaction_effect).
--                SIGNAL si la deuda supera credit_limit o si no hay fila en credit_details.
-- SIGNAL si la cuenta no existe. users.global_balance lo actualiza el trigger 6.
-- No abre transacción: si se quiere atomicidad con otras operaciones, envolver la llamada.
DELIMITER //

DROP PROCEDURE IF EXISTS sp_reconcile_account //

CREATE PROCEDURE sp_reconcile_account(
    IN p_account_id BIGINT
)
BEGIN
    DECLARE v_account_type VARCHAR(20) DEFAULT NULL;
    DECLARE v_expected_net DECIMAL(14, 2) DEFAULT 0.00;
    DECLARE v_difference DECIMAL(14, 2) DEFAULT 0.00;
    DECLARE v_has_credit_details BOOLEAN DEFAULT FALSE;
    DECLARE v_credit_limit DECIMAL(12, 2) DEFAULT 0.00;

    SELECT type INTO v_account_type FROM accounts WHERE id = p_account_id;

    IF v_account_type IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: La cuenta a reconciliar no existe.';
    END IF;

    SELECT expected_net, difference INTO v_expected_net, v_difference
    FROM v_account_reconciliation
    WHERE account_id = p_account_id;

    IF v_account_type <> 'CREDIT' THEN
        UPDATE accounts SET current_balance = current_balance - v_difference
        WHERE id = p_account_id;
    ELSE
        SELECT TRUE, credit_limit INTO v_has_credit_details, v_credit_limit
        FROM credit_details WHERE account_id = p_account_id;

        IF v_expected_net >= 0 THEN
            -- Sin deuda: la posición positiva es saldo a favor
            IF v_has_credit_details THEN
                UPDATE credit_details SET credit_used = 0
                WHERE account_id = p_account_id;
            END IF;
            UPDATE accounts SET current_balance = v_expected_net
            WHERE id = p_account_id;
        ELSE
            -- Con deuda: va a credit_used y el saldo a favor queda en 0
            IF NOT v_has_credit_details THEN
                SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Error: La cuenta de crédito no tiene detalle de crédito; no se puede registrar la deuda.';
            END IF;

            IF -v_expected_net > v_credit_limit THEN
                SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Error: La deuda calculada supera el límite de crédito; no se puede reconciliar la cuenta.';
            END IF;

            UPDATE credit_details SET credit_used = -v_expected_net
            WHERE account_id = p_account_id;
            UPDATE accounts SET current_balance = 0
            WHERE id = p_account_id;
        END IF;
    END IF;
END //

DELIMITER ;
