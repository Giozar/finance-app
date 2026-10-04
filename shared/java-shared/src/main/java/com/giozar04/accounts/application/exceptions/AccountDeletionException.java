package com.giozar04.accounts.application.exceptions;

public class AccountDeletionException extends RuntimeException {
    public AccountDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
