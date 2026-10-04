package com.giozar04.bankClient.application.exceptions;

public class BankClientNotFoundException extends RuntimeException {
    public BankClientNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
