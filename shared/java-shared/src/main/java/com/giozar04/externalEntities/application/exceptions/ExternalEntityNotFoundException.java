package com.giozar04.externalEntities.application.exceptions;

public class ExternalEntityNotFoundException extends RuntimeException {
    public ExternalEntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
