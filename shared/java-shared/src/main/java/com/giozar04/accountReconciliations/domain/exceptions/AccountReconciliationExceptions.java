package com.giozar04.accountReconciliations.domain.exceptions;

public class AccountReconciliationExceptions {

    public static class AccountReconciliationRetrievalException extends RuntimeException {
        public AccountReconciliationRetrievalException(String message) {
            super(message);
        }

        public AccountReconciliationRetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class AccountReconcileException extends RuntimeException {
        public AccountReconcileException(String message) {
            super(message);
        }

        public AccountReconcileException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
