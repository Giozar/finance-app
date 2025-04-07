package com.giozar04.accounts.domain.exceptions;

public class AccountExceptions {

    public static class AccountCreationException extends RuntimeException {
        public AccountCreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class AccountRetrievalException extends RuntimeException {
        public AccountRetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class AccountParsingException extends RuntimeException {
        public AccountParsingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class AccountUpdateException extends RuntimeException {
        public AccountUpdateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class AccountDeletionException extends RuntimeException {
        public AccountDeletionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class AccountNotFoundException extends RuntimeException {
        public AccountNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
