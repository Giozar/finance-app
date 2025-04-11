package com.giozar04.card.domain.entities;

import java.io.Serializable;
import java.time.ZonedDateTime;

import com.giozar04.card.domain.enums.CardTypes;

public class Card implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long accountId;
    private String name;
    private CardTypes cardType;
    private String cardNumber;
    private ZonedDateTime expirationDate;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Card(long id, long accountId, CardTypes cardType, String cardNumber, ZonedDateTime expirationDate, ZonedDateTime createdAt, ZonedDateTime updatedAt){
        this.id = id;
        this.accountId = accountId;
        this.cardType = cardType;
        this.cardNumber = cardNumber;
        this.expirationDate = expirationDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Card(){}

    // Getters y setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getName()  {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CardTypes getCardType() {
        return cardType;
    }

    public void setCardType(CardTypes cardType) {
        this.cardType = cardType;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public ZonedDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(ZonedDateTime expirationDate) {
        this.expirationDate = expirationDate;
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

    @Override
    public String toString() {
        return name + "( " +cardType.getLabel() + " )" ;
    }

}
