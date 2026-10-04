package com.giozar04.accounts.application.exceptions;

public class AccountRetrievalException extends RuntimeException {
    public AccountRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
