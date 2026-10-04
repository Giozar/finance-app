package com.giozar04.accountReconciliations.application.exceptions;

public class AccountReconciliationRetrievalException extends RuntimeException {
    public AccountReconciliationRetrievalException(String message) {
        super(message);
    }

    public AccountReconciliationRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
