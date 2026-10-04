package com.giozar04.transactions.application.ports.output;

import java.util.List;

import com.giozar04.transactions.domain.entities.Transaction;

/**
 * Repositorio del agregado Transaction (transacción + detalle de tarjeta/wallet + tagIds).
 */
public interface TransactionRepository {
    Transaction createTransaction(Transaction tx);
    Transaction getTransactionById(long id);
    Transaction updateTransactionById(long id, Transaction tx);
    void deleteTransactionById(long id);
    List<Transaction> getAllTransactions();
    List<Transaction> getTransactionsByUserId(long userId);
}
