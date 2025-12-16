package com.giozar04.transactions.domain.interfaces;

import java.util.List;

import com.giozar04.transactions.domain.entities.Transaction;

public interface TransactionRepositoryInterface {
    Transaction createTransaction(Transaction tx);
    Transaction getTransactionById(long id);
    Transaction updateTransactionById(long id, Transaction tx);
    void deleteTransactionById(long id);
    List<Transaction> getAllTransactions();
}
