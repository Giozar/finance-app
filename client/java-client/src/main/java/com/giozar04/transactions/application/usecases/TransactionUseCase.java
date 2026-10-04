package com.giozar04.transactions.application.usecases;

import java.util.List;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.application.ports.input.TransactionOperations;
import com.giozar04.transactions.application.ports.output.TransactionGateway;

public final class TransactionUseCase implements TransactionOperations {
    private final TransactionGateway gateway;

    public TransactionUseCase(TransactionGateway gateway) {
        this.gateway = java.util.Objects.requireNonNull(gateway);
    }

    @Override
    public Transaction createTransaction(Transaction transaction) throws ClientOperationException {
        return gateway.createTransaction(transaction);
    }

    @Override
    public Transaction updateTransactionById(Long transactionId, Transaction transaction) throws ClientOperationException {
        return gateway.updateTransactionById(transactionId, transaction);
    }

    @Override
    public void deleteTransactionById(Long transactionId) throws ClientOperationException {
        gateway.deleteTransactionById(transactionId);
    }

    @Override
    public Transaction getTransactionById(Long transactionId) throws ClientOperationException {
        return gateway.getTransactionById(transactionId);
    }

    @Override
    public List<Transaction> getAllTransactions() throws ClientOperationException {
        return gateway.getAllTransactions();
    }

    @Override
    public List<Transaction> getTransactionsByUserId(long userId) throws ClientOperationException {
        return gateway.getTransactionsByUserId(userId);
    }
}
