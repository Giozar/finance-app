package com.giozar04.bankClient.domain.exceptions;

public class BankClientExceptions {

    public static class BankClientCreationException extends RuntimeException {
        public BankClientCreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class BankClientRetrievalException extends RuntimeException {
        public BankClientRetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class BankClientUpdateException extends RuntimeException {
        public BankClientUpdateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class BankClientDeletionException extends RuntimeException {
        public BankClientDeletionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class BankClientNotFoundException extends RuntimeException {
        public BankClientNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
