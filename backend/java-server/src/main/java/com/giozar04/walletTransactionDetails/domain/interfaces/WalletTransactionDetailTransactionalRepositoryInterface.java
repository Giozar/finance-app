package com.giozar04.walletTransactionDetails.domain.interfaces;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;

/**
 * Operaciones sobre wallet_transaction_details que participan en una unidad de trabajo externa
 * (TransactionalExecutor). Reciben la conexión: no hacen commit, rollback ni la cierran.
 */
public interface WalletTransactionDetailTransactionalRepositoryInterface {
    WalletTransactionDetail insert(Connection conn, WalletTransactionDetail detail) throws SQLException;
    int deleteByTransactionId(Connection conn, long transactionId) throws SQLException;
    List<WalletTransactionDetail> findByTransactionId(Connection conn, long transactionId) throws SQLException;
}
