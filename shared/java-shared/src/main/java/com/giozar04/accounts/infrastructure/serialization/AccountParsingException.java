package com.giozar04.accounts.infrastructure.serialization;

public class AccountParsingException extends RuntimeException {
    public AccountParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
