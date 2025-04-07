-- Tabla de clientes bancarios (bank_clients)
CREATE TABLE bank_clients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    client_number VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Restricción de clave foránea (suponiendo que la tabla 'users' existe)
    CONSTRAINT fk_bank_clients_user FOREIGN KEY (user_id) REFERENCES users(id),
    -- Restringe que un mismo usuario registre el mismo banco y número de cliente más de una vez.
    UNIQUE KEY unique_client_per_bank (user_id, bank_name, client_number)
);

-- Índices adicionales útiles
CREATE INDEX idx_bank_clients_user_id ON bank_clients(user_id);
CREATE INDEX idx_bank_clients_client_number ON bank_clients(client_number);


-- Si la tabla existe y no se ha creado la llave única
-- ALTER TABLE bank_clients
-- ADD UNIQUE KEY unique_client_per_bank (user_id, bank_name, client_number);
