package com.giozar04.transactions.application.validation.rules;

import java.util.List;

import com.giozar04.transactions.application.validation.TransactionRule;
import com.giozar04.transactions.application.validation.ValidationContext;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.domain.enums.PaymentMethod;

/**
 * INTERNAL: solo en REALLOCATION (igual que chk_tx_internal_reallocation) y sin detalles.
 */
public class InternalPaymentRule implements TransactionRule {

    @Override
    public void validate(Transaction tx, ValidationContext ctx, List<String> errors) {
        if (tx.getOperationType() != null && tx.getOperationType() != OperationTypes.REALLOCATION) {
            errors.add("El método \"" + PaymentMethod.INTERNAL.getLabel() + "\" solo se permite en reubicaciones");
        }
        TransactionRuleSupport.rejectDetails(tx, errors);
    }
}
