package com.giozar04.bankClient.domain.exceptions;

public class BankClientValidationException extends RuntimeException {
    public BankClientValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
