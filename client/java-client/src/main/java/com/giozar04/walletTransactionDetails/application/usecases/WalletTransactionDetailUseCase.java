package com.giozar04.walletTransactionDetails.application.usecases;

import java.util.List;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;
import com.giozar04.walletTransactionDetails.application.ports.input.WalletTransactionDetailOperations;
import com.giozar04.walletTransactionDetails.application.ports.output.WalletTransactionDetailGateway;

public final class WalletTransactionDetailUseCase implements WalletTransactionDetailOperations {
    private final WalletTransactionDetailGateway gateway;

    public WalletTransactionDetailUseCase(WalletTransactionDetailGateway gateway) {
        this.gateway = java.util.Objects.requireNonNull(gateway);
    }

    @Override
    public List<WalletTransactionDetail> getDetailsByTransactionId(Long transactionId) throws ClientOperationException {
        return gateway.getDetailsByTransactionId(transactionId);
    }
}
