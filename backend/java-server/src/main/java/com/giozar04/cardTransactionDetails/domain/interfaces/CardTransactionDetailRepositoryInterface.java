package com.giozar04.cardTransactionDetails.domain.interfaces;

import java.util.List;

import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;

public interface CardTransactionDetailRepositoryInterface {
    CardTransactionDetail createDetail(CardTransactionDetail detail);
    CardTransactionDetail getDetailById(long id);
    CardTransactionDetail updateDetailById(long id, CardTransactionDetail detail);
    void deleteDetailById(long id);
    List<CardTransactionDetail> getAllDetails();
    List<CardTransactionDetail> getDetailsByTransactionId(long transactionId);
}
