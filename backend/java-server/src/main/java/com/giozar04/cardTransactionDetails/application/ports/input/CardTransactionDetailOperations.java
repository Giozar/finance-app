package com.giozar04.cardTransactionDetails.application.ports.input;

import java.util.List;

import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;

public interface CardTransactionDetailOperations {
    CardTransactionDetail createDetail(CardTransactionDetail detail);
    CardTransactionDetail getDetailById(long id);
    CardTransactionDetail updateDetailById(long id, CardTransactionDetail detail);
    void deleteDetailById(long id);
    List<CardTransactionDetail> getAllDetails();
    List<CardTransactionDetail> getDetailsByTransactionId(long transactionId);
}
