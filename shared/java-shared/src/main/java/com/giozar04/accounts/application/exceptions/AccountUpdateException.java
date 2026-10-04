package com.giozar04.accounts.application.exceptions;

public class AccountUpdateException extends RuntimeException {
    public AccountUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
