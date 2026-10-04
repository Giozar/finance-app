package com.giozar04.transactions.application.ports.output;

import java.util.List;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.transactions.domain.entities.Transaction;

public interface TransactionGateway {
    Transaction createTransaction(Transaction transaction) throws ClientOperationException;
    Transaction updateTransactionById(Long transactionId, Transaction transaction) throws ClientOperationException;
    void deleteTransactionById(Long transactionId) throws ClientOperationException;
    Transaction getTransactionById(Long transactionId) throws ClientOperationException;
    List<Transaction> getAllTransactions() throws ClientOperationException;
    List<Transaction> getTransactionsByUserId(long userId) throws ClientOperationException;
}
