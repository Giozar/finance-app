package com.giozar04.cardTransactionDetails.domain.interfaces;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;

/**
 * Operaciones sobre card_transaction_details que participan en una unidad de trabajo externa
 * (TransactionalExecutor). Reciben la conexión: no hacen commit, rollback ni la cierran.
 */
public interface CardTransactionDetailTransactionalRepositoryInterface {
    CardTransactionDetail insert(Connection conn, CardTransactionDetail detail) throws SQLException;
    int deleteByTransactionId(Connection conn, long transactionId) throws SQLException;
    List<CardTransactionDetail> findByTransactionId(Connection conn, long transactionId) throws SQLException;
}
