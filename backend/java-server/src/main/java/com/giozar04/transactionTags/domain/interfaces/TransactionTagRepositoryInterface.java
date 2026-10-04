package com.giozar04.transactionTags.domain.interfaces;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Relación N:M transacción-etiqueta (transaction_tags). No tiene entidad ni CRUD propio:
 * solo se usa dentro de la unidad de trabajo de transactions (TransactionalExecutor).
 * Reciben la conexión: no hacen commit, rollback ni la cierran.
 */
public interface TransactionTagRepositoryInterface {
    void replaceTags(Connection conn, long transactionId, List<Long> tagIds) throws SQLException;
    List<Long> findTagIds(Connection conn, long transactionId) throws SQLException;
}
