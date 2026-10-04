package com.giozar04.card.application.exceptions;

public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
