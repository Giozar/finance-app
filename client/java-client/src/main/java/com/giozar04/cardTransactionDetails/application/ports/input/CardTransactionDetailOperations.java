package com.giozar04.cardTransactionDetails.application.ports.input;

import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;

public interface CardTransactionDetailOperations {
    CardTransactionDetail getDetailByTransactionId(Long transactionId) throws ClientOperationException;
}
