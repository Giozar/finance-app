package com.giozar04.walletTransactionDetails.domain.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

import com.giozar04.walletTransactionDetails.domain.enums.WalletTransactionSourceType;

public class WalletTransactionDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long transactionId;
    private WalletTransactionSourceType sourceType;
    private long walletAccountId;
    private Long cardId;
    private BigDecimal amount;
    private BigDecimal cashbackPercentage;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public WalletTransactionDetail() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTransactionId() { return transactionId; }
    public void setTransactionId(long transactionId) { this.transactionId = transactionId; }

    public WalletTransactionSourceType getSourceType() { return sourceType; }
    public void setSourceType(WalletTransactionSourceType sourceType) { this.sourceType = sourceType; }

    public long getWalletAccountId() { return walletAccountId; }
    public void setWalletAccountId(long walletAccountId) { this.walletAccountId = walletAccountId; }

    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getCashbackPercentage() { return cashbackPercentage; }
    public void setCashbackPercentage(BigDecimal cashbackPercentage) { this.cashbackPercentage = cashbackPercentage; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
}
