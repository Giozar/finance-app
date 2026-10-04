package com.giozar04.transactions.application.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.exceptions.TransactionExceptions.TransactionValidationException;

/**
 * Composite de reglas: ejecuta todas, acumula los errores y lanza una sola
 * {@link TransactionValidationException} con los mensajes unidos por "; ".
 */
public class TransactionValidator {

    private final List<TransactionRule> rules;

    public TransactionValidator(List<TransactionRule> rules) {
        this.rules = List.copyOf(Objects.requireNonNull(rules, "Las reglas no pueden ser nulas"));
    }

    public void validate(Transaction tx, ValidationContext ctx) {
        if (tx == null) {
            throw new TransactionValidationException("La transacción no puede ser nula");
        }

        List<String> errors = new ArrayList<>();
        for (TransactionRule rule : rules) {
            rule.validate(tx, ctx, errors);
        }

        if (!errors.isEmpty()) {
            throw new TransactionValidationException(String.join("; ", errors));
        }
    }
}
