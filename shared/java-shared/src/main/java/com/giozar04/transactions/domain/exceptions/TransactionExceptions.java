package com.giozar04.transactions.domain.exceptions;

public class TransactionExceptions {

    public static class CreationException extends RuntimeException {
        public CreationException(String message, Throwable cause) { super(message, cause); }
    }

    public static class RetrievalException extends RuntimeException {
        public RetrievalException(String message, Throwable cause) { super(message, cause); }
    }

    public static class UpdateException extends RuntimeException {
        public UpdateException(String message, Throwable cause) { super(message, cause); }
    }

    public static class DeletionException extends RuntimeException {
        public DeletionException(String message, Throwable cause) { super(message, cause); }
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message, Throwable cause) { super(message, cause); }
    }
}
