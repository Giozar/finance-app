package com.giozar04.transactions.application.validation.rules;

import java.util.List;

import com.giozar04.transactions.application.validation.TransactionRule;
import com.giozar04.transactions.application.validation.ValidationContext;
import com.giozar04.transactions.domain.entities.Transaction;

/**
 * INCOME: cuenta destino y entidad externa (quién paga) obligatorias; sin cuenta origen.
 * Replica sp_validate_transaction_parties con mensajes equivalentes.
 */
public class IncomeRule implements TransactionRule {

    @Override
    public void validate(Transaction tx, ValidationContext ctx, List<String> errors) {
        if (tx.getDestinationAccountId() == null) {
            errors.add("Un ingreso requiere una cuenta destino");
        }
        if (tx.getExternalEntityId() == null) {
            errors.add("Un ingreso requiere una entidad externa (quién paga)");
        }
        if (tx.getSourceAccountId() != null) {
            errors.add("Un ingreso no puede tener cuenta origen");
        }

        TransactionRuleSupport.checkOwnAccount(tx, ctx, tx.getDestinationAccountId(), "destino", errors);
        TransactionRuleSupport.checkOwnExternalEntity(tx, ctx, errors);
    }
}
