package com.giozar04.accounts.application.exceptions;

public class AccountCreationException extends RuntimeException {
    public AccountCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
