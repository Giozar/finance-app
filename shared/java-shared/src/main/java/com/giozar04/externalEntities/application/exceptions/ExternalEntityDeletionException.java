package com.giozar04.externalEntities.application.exceptions;

public class ExternalEntityDeletionException extends RuntimeException {
    public ExternalEntityDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
