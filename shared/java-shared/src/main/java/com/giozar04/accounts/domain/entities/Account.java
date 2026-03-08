package com.giozar04.accounts.domain.entities;

import java.io.Serializable;
import java.time.ZonedDateTime;

import com.giozar04.accounts.domain.enums.AccountTypes;

public class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long userId;
    private Long bankClientId; // Puede ser null para cuentas de efectivo
    private String name;
    private AccountTypes type;
    private double currentBalance;
    private String accountNumber;
    private String clabe;
    private Double creditLimit;
    private Integer cutoffDay;
    private Integer paymentDay;
    private Boolean canTransferOut = true;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Account() {}

    // Getters y setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public Long getBankClientId() { return bankClientId; }
    public void setBankClientId(Long bankClientId) { this.bankClientId = bankClientId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public AccountTypes getType() { return type; }
    public void setType(AccountTypes type) { this.type = type; }

    public double getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(double currentBalance) { this.currentBalance = currentBalance; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getClabe() { return clabe; }
    public void setClabe(String clabe) { this.clabe = clabe; }

    public Double getCreditLimit() { return creditLimit; }
    public void setCreditLimit(Double creditLimit) { this.creditLimit = creditLimit; }

    public Integer getCutoffDay() { return cutoffDay; }
    public void setCutoffDay(Integer cutoffDay) { this.cutoffDay = cutoffDay; }

    public Integer getPaymentDay() { return paymentDay; }
    public void setPaymentDay(Integer paymentDay) { this.paymentDay = paymentDay; }

    public Boolean getCanTransferOut() { return canTransferOut; }
    public void setCanTransferOut(Boolean canTransferOut) { this.canTransferOut = canTransferOut; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return name + (type != null ? " ( " + type.getLabel() + " )" : "");
    }
}
