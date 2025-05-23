package com.giozar04.cardTransactionDetails.domain.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class CardTransactionDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long transactionId;
    private long cardId;
    private BigDecimal amount;
    private Integer installmentMonths; // null = contado
    private boolean interestFree;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public CardTransactionDetail() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTransactionId() { return transactionId; }
    public void setTransactionId(long transactionId) { this.transactionId = transactionId; }

    public long getCardId() { return cardId; }
    public void setCardId(long cardId) { this.cardId = cardId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Integer getInstallmentMonths() { return installmentMonths; }
    public void setInstallmentMonths(Integer installmentMonths) { this.installmentMonths = installmentMonths; }

    public boolean isInterestFree() { return interestFree; }
    public void setInterestFree(boolean interestFree) { this.interestFree = interestFree; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
}
