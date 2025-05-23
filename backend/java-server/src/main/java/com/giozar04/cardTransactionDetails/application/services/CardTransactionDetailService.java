package com.giozar04.cardTransactionDetails.application.services;

import java.util.List;

import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;
import com.giozar04.cardTransactionDetails.domain.interfaces.CardTransactionDetailRepositoryInterface;

public class CardTransactionDetailService implements CardTransactionDetailRepositoryInterface {

    private final CardTransactionDetailRepositoryInterface repository;

    public CardTransactionDetailService(CardTransactionDetailRepositoryInterface repository) {
        this.repository = repository;
    }

    @Override
    public CardTransactionDetail createDetail(CardTransactionDetail detail) {
        return repository.createDetail(detail);
    }

    @Override
    public CardTransactionDetail getDetailById(long id) {
        return repository.getDetailById(id);
    }

    @Override
    public CardTransactionDetail updateDetailById(long id, CardTransactionDetail detail) {
        return repository.updateDetailById(id, detail);
    }

    @Override
    public void deleteDetailById(long id) {
        repository.deleteDetailById(id);
    }

    @Override
    public List<CardTransactionDetail> getAllDetails() {
        return repository.getAllDetails();
    }

    @Override
    public List<CardTransactionDetail> getDetailsByTransactionId(long transactionId) {
        return repository.getDetailsByTransactionId(transactionId);
    }
}
