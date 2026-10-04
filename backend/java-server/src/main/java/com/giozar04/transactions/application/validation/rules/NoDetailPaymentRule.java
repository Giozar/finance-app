package com.giozar04.transactions.application.validation.rules;

import java.util.List;

import com.giozar04.transactions.application.validation.TransactionRule;
import com.giozar04.transactions.application.validation.ValidationContext;
import com.giozar04.transactions.domain.entities.Transaction;

/**
 * CASH, WIRE_TRANSFER, QR y CODI: válidos con cualquier operación y sin detalles.
 */
public class NoDetailPaymentRule implements TransactionRule {

    @Override
    public void validate(Transaction tx, ValidationContext ctx, List<String> errors) {
        TransactionRuleSupport.rejectDetails(tx, errors);
    }
}
