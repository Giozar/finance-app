package com.giozar04.databases.application.services;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.databases.domain.interfaces.SqlWork;
import com.giozar04.logging.CustomLogger;

/**
 * Unidad de trabajo para operaciones que escriben en varias tablas.
 * Abre una conexión dedicada (createConnection), ejecuta el trabajo, hace commit y la cierra.
 * Ante cualquier excepción hace rollback y la relanza sin envolver.
 * Al no usar la conexión compartida, es seguro entre hilos.
 */
public class TransactionalExecutor {

    private final DatabaseConnectionInterface databaseConnection;
    private final CustomLogger logger = CustomLogger.getInstance();

    public TransactionalExecutor(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, "La conexión a base de datos no puede ser nula");
    }

    public <T> T inTransaction(SqlWork<T> work) throws SQLException {
        Objects.requireNonNull(work, "El trabajo SQL no puede ser nulo");

        try (Connection conn = databaseConnection.createConnection()) {
            try {
                T result = work.execute(conn);
                conn.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                rollback(conn);
                throw e;
            }
        }
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
            logger.info("Rollback de la unidad de trabajo ejecutado");
        } catch (SQLException e) {
            logger.error("Error al hacer rollback de la unidad de trabajo", e);
        }
    }
}
