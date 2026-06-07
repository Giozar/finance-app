package com.giozar04.accountCashbackSettings.domain.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class AccountCashbackSetting implements Serializable {
    private static final long serialVersionUID = 1L;

    private long accountId;
    private boolean cashbackEnabled;
    private BigDecimal defaultCashbackRate; // fracción, ej. 0.020000 = 2%
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public AccountCashbackSetting() {}

    public long getAccountId() { return accountId; }
    public void setAccountId(long accountId) { this.accountId = accountId; }

    public boolean isCashbackEnabled() { return cashbackEnabled; }
    public void setCashbackEnabled(boolean cashbackEnabled) { this.cashbackEnabled = cashbackEnabled; }

    public BigDecimal getDefaultCashbackRate() { return defaultCashbackRate; }
    public void setDefaultCashbackRate(BigDecimal defaultCashbackRate) { this.defaultCashbackRate = defaultCashbackRate; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "AccountCashbackSetting{accountId=" + accountId
                + ", cashbackEnabled=" + cashbackEnabled
                + ", defaultCashbackRate=" + defaultCashbackRate + "}";
    }
}
