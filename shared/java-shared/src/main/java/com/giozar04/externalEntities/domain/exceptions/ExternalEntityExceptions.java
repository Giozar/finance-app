package com.giozar04.externalEntities.domain.exceptions;

public class ExternalEntityExceptions {

    public static class ExternalEntityCreationException extends RuntimeException {
        public ExternalEntityCreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ExternalEntityRetrievalException extends RuntimeException {
        public ExternalEntityRetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ExternalEntityUpdateException extends RuntimeException {
        public ExternalEntityUpdateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ExternalEntityDeletionException extends RuntimeException {
        public ExternalEntityDeletionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ExternalEntityNotFoundException extends RuntimeException {
        public ExternalEntityNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
