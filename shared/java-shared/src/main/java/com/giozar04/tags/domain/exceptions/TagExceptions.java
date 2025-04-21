package com.giozar04.tags.domain.exceptions;

public class TagExceptions {

    public static class TagCreationException extends RuntimeException {
        public TagCreationException(String message) {
            super(message);
        }

        public TagCreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class TagRetrievalException extends RuntimeException {
        public TagRetrievalException(String message) {
            super(message);
        }

        public TagRetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class TagUpdateException extends RuntimeException {
        public TagUpdateException(String message) {
            super(message);
        }

        public TagUpdateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class TagDeletionException extends RuntimeException {
        public TagDeletionException(String message) {
            super(message);
        }

        public TagDeletionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class TagNotFoundException extends RuntimeException {
        public TagNotFoundException(String message) {
            super(message);
        }

        public TagNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
