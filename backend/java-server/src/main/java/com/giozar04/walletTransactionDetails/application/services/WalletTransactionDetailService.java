package com.giozar04.walletTransactionDetails.application.services;

import java.util.List;

import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;
import com.giozar04.walletTransactionDetails.domain.interfaces.WalletTransactionDetailRepositoryInterface;

public class WalletTransactionDetailService implements WalletTransactionDetailRepositoryInterface {

    private final WalletTransactionDetailRepositoryInterface repository;

    public WalletTransactionDetailService(WalletTransactionDetailRepositoryInterface repository) {
        this.repository = repository;
    }

    @Override
    public WalletTransactionDetail createDetail(WalletTransactionDetail detail) {
        return repository.createDetail(detail);
    }

    @Override
    public WalletTransactionDetail getDetailById(long id) {
        return repository.getDetailById(id);
    }

    @Override
    public WalletTransactionDetail updateDetailById(long id, WalletTransactionDetail detail) {
        return repository.updateDetailById(id, detail);
    }

    @Override
    public void deleteDetailById(long id) {
        repository.deleteDetailById(id);
    }

    @Override
    public List<WalletTransactionDetail> getAllDetails() {
        return repository.getAllDetails();
    }

    @Override
    public List<WalletTransactionDetail> getDetailsByTransactionId(long transactionId) {
        return repository.getDetailsByTransactionId(transactionId);
    }
}
