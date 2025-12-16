package com.giozar04.transactions.application.services;

import java.util.List;

import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.interfaces.TransactionRepositoryInterface;

public class TransactionService implements TransactionRepositoryInterface {

    private final TransactionRepositoryInterface repository;

    public TransactionService(TransactionRepositoryInterface repository) {
        this.repository = repository;
    }

    @Override
    public Transaction createTransaction(Transaction tx) {
        return repository.createTransaction(tx);
    }

    @Override
    public Transaction getTransactionById(long id) {
        return repository.getTransactionById(id);
    }

    @Override
    public Transaction updateTransactionById(long id, Transaction tx) {
        return repository.updateTransactionById(id, tx);
    }

    @Override
    public void deleteTransactionById(long id) {
        repository.deleteTransactionById(id);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return repository.getAllTransactions();
    }
}
