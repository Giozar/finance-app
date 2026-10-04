package com.giozar04.cardTransactionDetails.application.usecases;

import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.cardTransactionDetails.application.ports.input.CardTransactionDetailOperations;
import com.giozar04.cardTransactionDetails.application.ports.output.CardTransactionDetailGateway;

public final class CardTransactionDetailUseCase implements CardTransactionDetailOperations {
    private final CardTransactionDetailGateway gateway;

    public CardTransactionDetailUseCase(CardTransactionDetailGateway gateway) {
        this.gateway = java.util.Objects.requireNonNull(gateway);
    }

    @Override
    public CardTransactionDetail getDetailByTransactionId(Long transactionId) throws ClientOperationException {
        return gateway.getDetailByTransactionId(transactionId);
    }
}
