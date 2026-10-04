package com.giozar04.transactions.application.validation.rules;

import java.util.List;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.transactions.application.validation.ValidationContext;
import com.giozar04.transactions.domain.entities.Transaction;

/**
 * Comprobaciones compartidas por varias reglas (existencia y pertenencia al usuario).
 */
final class TransactionRuleSupport {

    private TransactionRuleSupport() {}

    /** Verifica que la cuenta exista y sea del usuario. Devuelve la cuenta o null si no existe. */
    static Account checkOwnAccount(Transaction tx, ValidationContext ctx, Long accountId, String role, List<String> errors) {
        if (accountId == null) return null;

        Account account = ctx.account(accountId);
        if (account == null) {
            errors.add("La cuenta " + role + " con ID " + accountId + " no existe");
            return null;
        }
        if (tx.getUserId() > 0 && account.getUserId() != tx.getUserId()) {
            errors.add("La cuenta " + role + " \"" + account.getName() + "\" no pertenece al usuario");
        }
        return account;
    }

    /** Verifica que la entidad externa exista y sea del usuario. */
    static void checkOwnExternalEntity(Transaction tx, ValidationContext ctx, List<String> errors) {
        Long entityId = tx.getExternalEntityId();
        if (entityId == null) return;

        ExternalEntity entity = ctx.externalEntity(entityId);
        if (entity == null) {
            errors.add("La entidad externa con ID " + entityId + " no existe");
            return;
        }
        if (tx.getUserId() > 0 && entity.getUserId() != tx.getUserId()) {
            errors.add("La entidad externa \"" + entity.getName() + "\" no pertenece al usuario");
        }
    }

    /** Los métodos sin detalle no pueden traer detalle de tarjeta ni de wallet. */
    static void rejectDetails(Transaction tx, List<String> errors) {
        String method = tx.getPaymentMethod() != null ? tx.getPaymentMethod().getLabel() : "";
        if (tx.getCardDetail() != null) {
            errors.add("El método \"" + method + "\" no lleva detalle de tarjeta");
        }
        if (tx.getWalletDetail() != null) {
            errors.add("El método \"" + method + "\" no lleva detalle de wallet");
        }
    }
}
