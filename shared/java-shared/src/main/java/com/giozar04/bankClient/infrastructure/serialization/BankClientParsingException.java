package com.giozar04.bankClient.infrastructure.serialization;

public class BankClientParsingException extends RuntimeException {
    public BankClientParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
