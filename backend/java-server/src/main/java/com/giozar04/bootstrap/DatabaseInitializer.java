package com.giozar04.bootstrap;

import com.giozar04.configs.DatabaseConfig;
import com.giozar04.databases.infrastructure.persistence.mysql.DatabaseConnectionInterface;
import com.giozar04.databases.infrastructure.persistence.mysql.DatabaseConnectionMySQL;
import com.giozar04.logging.infrastructure.ConsoleLogger;

public class DatabaseInitializer {
    private final DatabaseConfig dbConfig;
    private final ConsoleLogger logger;

    public DatabaseInitializer(DatabaseConfig dbConfig, ConsoleLogger logger) {
        this.dbConfig = dbConfig;
        this.logger = logger;
    }

    public DatabaseConnectionInterface initialize() {
        DatabaseConnectionInterface connection = DatabaseConnectionMySQL.getInstance(
                dbConfig.getHost(),
                dbConfig.getPort(),
                dbConfig.getName(),
                dbConfig.getUsername(),
                dbConfig.getPassword()
        );
        logger.info("Conexión a la base de datos establecida exitosamente.");
        return connection;
    }
}
