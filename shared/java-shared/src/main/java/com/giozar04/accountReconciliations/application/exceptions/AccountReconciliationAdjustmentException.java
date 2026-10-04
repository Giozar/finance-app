package com.giozar04.accountReconciliations.application.exceptions;

public class AccountReconciliationAdjustmentException extends RuntimeException {
    public AccountReconciliationAdjustmentException(String message) {
        super(message);
    }

    public AccountReconciliationAdjustmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
