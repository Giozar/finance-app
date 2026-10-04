package com.giozar04.accounts.application.exceptions;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
