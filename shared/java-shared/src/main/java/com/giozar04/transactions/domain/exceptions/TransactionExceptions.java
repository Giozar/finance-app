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

    public static class TransactionCreationException extends RuntimeException {
        public TransactionCreationException(String message, Throwable cause) { super(message, cause); }
    }

    public static class TransactionRetrievalException extends RuntimeException {
        public TransactionRetrievalException(String message, Throwable cause) { super(message, cause); }
    }

    public static class TransactionUpdateException extends RuntimeException {
        public TransactionUpdateException(String message, Throwable cause) { super(message, cause); }
    }

    public static class TransactionDeletionException extends RuntimeException {
        public TransactionDeletionException(String message, Throwable cause) { super(message, cause); }
    }

    public static class TransactionParsingException extends RuntimeException {
        public TransactionParsingException(String message, Throwable cause) { super(message, cause); }
    }
}
