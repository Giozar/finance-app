package com.giozar04.cardTransactionDetails.application.ports.output;

import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;

public interface CardTransactionDetailGateway {
    CardTransactionDetail getDetailByTransactionId(Long transactionId) throws ClientOperationException;
}
