package com.giozar04.transactions.application.validation;

import java.util.List;

import com.giozar04.transactions.domain.entities.Transaction;

/**
 * Regla de validación de una transacción (Strategy).
 * No lanza excepciones: añade a {@code errors} un mensaje en español por cada problema.
 */
@FunctionalInterface
public interface TransactionRule {
    void validate(Transaction tx, ValidationContext ctx, List<String> errors);
}
