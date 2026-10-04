package com.giozar04.accountReconciliations.domain.entities;

import java.io.Serializable;
import java.math.BigDecimal;

import com.giozar04.accounts.domain.enums.AccountTypes;

// Fila de la vista v_account_reconciliation (solo lectura)
public class AccountReconciliation implements Serializable {
    private static final long serialVersionUID = 1L;

    private long accountId;
    private long userId;
    private String accountName;
    private AccountTypes accountType;
    private BigDecimal openingNet;
    private BigDecimal totalInflows;
    private BigDecimal totalOutflows;
    private BigDecimal expectedNet;
    private BigDecimal actualNet;
    private BigDecimal difference; // actualNet - expectedNet; 0 = cuadrada

    public AccountReconciliation() {}

    // Getters y setters
    public long getAccountId() { return accountId; }
    public void setAccountId(long accountId) { this.accountId = accountId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public AccountTypes getAccountType() { return accountType; }
    public void setAccountType(AccountTypes accountType) { this.accountType = accountType; }

    public BigDecimal getOpeningNet() { return openingNet; }
    public void setOpeningNet(BigDecimal openingNet) { this.openingNet = openingNet; }

    public BigDecimal getTotalInflows() { return totalInflows; }
    public void setTotalInflows(BigDecimal totalInflows) { this.totalInflows = totalInflows; }

    public BigDecimal getTotalOutflows() { return totalOutflows; }
    public void setTotalOutflows(BigDecimal totalOutflows) { this.totalOutflows = totalOutflows; }

    public BigDecimal getExpectedNet() { return expectedNet; }
    public void setExpectedNet(BigDecimal expectedNet) { this.expectedNet = expectedNet; }

    public BigDecimal getActualNet() { return actualNet; }
    public void setActualNet(BigDecimal actualNet) { this.actualNet = actualNet; }

    public BigDecimal getDifference() { return difference; }
    public void setDifference(BigDecimal difference) { this.difference = difference; }

    // true si la cuenta cuadra (difference == 0); null se considera no cuadrada
    public boolean isBalanced() { return difference != null && difference.signum() == 0; }
}
