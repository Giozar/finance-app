package com.giozar04.card.domain.exceptions;

public class CardExceptions {

    public static class CardCreationException extends RuntimeException {

        public CardCreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class CardUpdateException extends RuntimeException {

        public CardUpdateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class CardDeletionException extends RuntimeException {

        public CardDeletionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class CardNotFoundException extends RuntimeException {

        public CardNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class CardRetrievalException extends RuntimeException {

        public CardRetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class CardParsingException extends RuntimeException {

        public CardParsingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
