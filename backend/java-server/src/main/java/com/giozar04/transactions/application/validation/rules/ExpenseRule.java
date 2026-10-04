package com.giozar04.transactions.application.validation.rules;

import java.util.List;

import com.giozar04.transactions.application.validation.TransactionRule;
import com.giozar04.transactions.application.validation.ValidationContext;
import com.giozar04.transactions.domain.entities.Transaction;

/**
 * EXPENSE: cuenta origen y entidad externa (a quién se paga) obligatorias; sin cuenta destino.
 * Replica sp_validate_transaction_parties con mensajes equivalentes.
 */
public class ExpenseRule implements TransactionRule {

    @Override
    public void validate(Transaction tx, ValidationContext ctx, List<String> errors) {
        if (tx.getSourceAccountId() == null) {
            errors.add("Un gasto requiere una cuenta origen");
        }
        if (tx.getExternalEntityId() == null) {
            errors.add("Un gasto requiere una entidad externa (a quién se paga)");
        }
        if (tx.getDestinationAccountId() != null) {
            errors.add("Un gasto no puede tener cuenta destino");
        }

        TransactionRuleSupport.checkOwnAccount(tx, ctx, tx.getSourceAccountId(), "origen", errors);
        TransactionRuleSupport.checkOwnExternalEntity(tx, ctx, errors);
    }
}
