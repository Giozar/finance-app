package com.giozar04.cardTransactionDetails.application.ports.output;

import java.util.List;

import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;

public interface CardTransactionDetailRepository {
    CardTransactionDetail createDetail(CardTransactionDetail detail);
    CardTransactionDetail getDetailById(long id);
    CardTransactionDetail updateDetailById(long id, CardTransactionDetail detail);
    void deleteDetailById(long id);
    List<CardTransactionDetail> getAllDetails();
    List<CardTransactionDetail> getDetailsByTransactionId(long transactionId);
}
