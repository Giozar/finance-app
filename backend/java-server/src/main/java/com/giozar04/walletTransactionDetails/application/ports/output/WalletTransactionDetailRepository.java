package com.giozar04.walletTransactionDetails.application.ports.output;

import java.util.List;

import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;

public interface WalletTransactionDetailRepository {
    WalletTransactionDetail createDetail(WalletTransactionDetail detail);
    WalletTransactionDetail getDetailById(long id);
    WalletTransactionDetail updateDetailById(long id, WalletTransactionDetail detail);
    void deleteDetailById(long id);
    List<WalletTransactionDetail> getAllDetails();
    List<WalletTransactionDetail> getDetailsByTransactionId(long transactionId);
}
