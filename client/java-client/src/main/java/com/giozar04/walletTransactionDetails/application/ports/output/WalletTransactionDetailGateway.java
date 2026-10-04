package com.giozar04.walletTransactionDetails.application.ports.output;

import java.util.List;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;

public interface WalletTransactionDetailGateway {
    List<WalletTransactionDetail> getDetailsByTransactionId(Long transactionId) throws ClientOperationException;
}
