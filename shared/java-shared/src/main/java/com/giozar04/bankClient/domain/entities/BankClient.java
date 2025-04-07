package com.giozar04.bankClient.domain.entities;

import java.time.ZonedDateTime;

public class BankClient {
    private long id;
    private long userId;
    private String bankName;
    private String clientNumber;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public BankClient(long id, long userId,  String bankName, String clientNumber, ZonedDateTime createdAt, ZonedDateTime updatedAt ) {
        this.id = id;
        this.userId = userId;
        this.bankName = bankName;
        this.clientNumber = clientNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt; 
    }

    public BankClient() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getClientNumber() {
        return clientNumber;
    }

    public void setClientNumber(String clientNumber) {
        this.clientNumber = clientNumber;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    
    
    


}
