package com.giozar04.walletCardLinks.domain.entities;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class WalletCardLink implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long walletAccountId;
    private long cardId;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public WalletCardLink() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getWalletAccountId() { return walletAccountId; }
    public void setWalletAccountId(long walletAccountId) { this.walletAccountId = walletAccountId; }

    public long getCardId() { return cardId; }
    public void setCardId(long cardId) { this.cardId = cardId; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
}
