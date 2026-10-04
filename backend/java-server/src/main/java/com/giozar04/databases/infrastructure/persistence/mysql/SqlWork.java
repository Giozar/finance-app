package com.giozar04.databases.infrastructure.persistence.mysql;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Trabajo SQL que se ejecuta dentro de una unidad de trabajo (ver TransactionalExecutor).
 * Recibe la conexión dedicada: no debe hacer commit, rollback ni cerrarla.
 *
 * @param <T> tipo del resultado
 */
@FunctionalInterface
public interface SqlWork<T> {
    T execute(Connection connection) throws SQLException;
}
