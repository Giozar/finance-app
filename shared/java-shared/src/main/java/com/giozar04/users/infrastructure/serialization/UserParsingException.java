package com.giozar04.users.infrastructure.serialization;

public class UserParsingException extends RuntimeException {
    public UserParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
