package com.giozar04.transactions.application.validation.rules;

import java.util.List;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.transactions.application.validation.TransactionRule;
import com.giozar04.transactions.application.validation.ValidationContext;
import com.giozar04.transactions.domain.entities.Transaction;

/**
 * REALLOCATION: cuentas origen y destino obligatorias, distintas y del usuario; sin entidad externa.
 * La cuenta origen debe permitir salidas (no BENEFIT ni can_transfer_out = false).
 * Replica sp_validate_transaction_reallocation y sp_validate_transaction_parties.
 */
public class ReallocationRule implements TransactionRule {

    @Override
    public void validate(Transaction tx, ValidationContext ctx, List<String> errors) {
        Long source = tx.getSourceAccountId();
        Long destination = tx.getDestinationAccountId();

        if (source == null || destination == null) {
            errors.add("Una reubicación requiere cuenta origen y cuenta destino");
        } else if (source.equals(destination)) {
            errors.add("La cuenta origen y la cuenta destino no pueden ser la misma");
        }

        if (tx.getExternalEntityId() != null) {
            errors.add("Una reubicación entre cuentas propias no puede tener entidad externa");
        }

        Account sourceAccount = TransactionRuleSupport.checkOwnAccount(tx, ctx, source, "origen", errors);
        TransactionRuleSupport.checkOwnAccount(tx, ctx, destination, "destino", errors);

        if (sourceAccount != null
                && (sourceAccount.getType() == AccountTypes.BENEFIT || Boolean.FALSE.equals(sourceAccount.getCanTransferOut()))) {
            errors.add("La cuenta origen \"" + sourceAccount.getName() + "\" no permite salidas de dinero (reubicaciones)");
        }
    }
}
