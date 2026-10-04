package com.giozar04.transactions.application.validation.rules;

import java.math.BigDecimal;
import java.util.List;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.card.domain.entities.Card;
import com.giozar04.transactions.application.validation.TransactionRule;
import com.giozar04.transactions.application.validation.ValidationContext;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;
import com.giozar04.walletTransactionDetails.domain.enums.WalletTransactionSourceType;

/**
 * WALLET (solo EXPENSE): detalle de wallet obligatorio; la wallet es una cuenta tipo WALLET
 * del usuario; origen obligatorio; LINKED_CARD exige una tarjeta vinculada a esa wallet
 * (wallet_card_links); cashback null o entre 0 y 1. Sin detalle de tarjeta.
 * La BD repite estas reglas en chk_tx_wallet_expense y sp_validate_wallet_detail.
 */
public class WalletPaymentRule implements TransactionRule {

    @Override
    public void validate(Transaction tx, ValidationContext ctx, List<String> errors) {
        if (tx.getOperationType() != null && tx.getOperationType() != OperationTypes.EXPENSE) {
            errors.add("El pago con wallet solo se permite en gastos");
        }

        if (tx.getCardDetail() != null) {
            errors.add("El pago con wallet no lleva detalle de tarjeta");
        }

        WalletTransactionDetail detail = tx.getWalletDetail();
        if (detail == null) {
            errors.add("El pago con wallet requiere el detalle de wallet");
            return;
        }

        if (detail.getCashbackRate() != null
                && (detail.getCashbackRate().signum() < 0 || detail.getCashbackRate().compareTo(BigDecimal.ONE) > 0)) {
            errors.add("La tasa de cashback debe estar entre 0 y 1");
        }

        Account wallet = validateWalletAccount(tx, ctx, detail, errors);

        if (detail.getSourceType() == null) {
            errors.add("Indique si el pago es con saldo de la wallet o con tarjeta vinculada");
            return;
        }

        if (detail.getSourceType() == WalletTransactionSourceType.LINKED_CARD) {
            validateLinkedCard(ctx, detail, wallet, errors);
        }
    }

    private Account validateWalletAccount(Transaction tx, ValidationContext ctx, WalletTransactionDetail detail, List<String> errors) {
        if (detail.getWalletAccountId() <= 0) {
            errors.add("Seleccione la wallet con la que se pagó");
            return null;
        }

        Account wallet = ctx.account(detail.getWalletAccountId());
        if (wallet == null) {
            errors.add("La wallet con ID " + detail.getWalletAccountId() + " no existe");
            return null;
        }
        if (wallet.getType() != AccountTypes.WALLET) {
            errors.add("La cuenta \"" + wallet.getName() + "\" no es de tipo wallet");
        }
        if (tx.getUserId() > 0 && wallet.getUserId() != tx.getUserId()) {
            errors.add("La wallet \"" + wallet.getName() + "\" no pertenece al usuario");
        }
        return wallet;
    }

    private void validateLinkedCard(ValidationContext ctx, WalletTransactionDetail detail, Account wallet, List<String> errors) {
        if (detail.getCardId() == null || detail.getCardId() <= 0) {
            errors.add("Un pago con tarjeta vinculada requiere seleccionar la tarjeta");
            return;
        }

        Card card = ctx.card(detail.getCardId());
        if (card == null) {
            errors.add("La tarjeta con ID " + detail.getCardId() + " no existe");
            return;
        }

        if (wallet != null && !ctx.isCardLinkedToWallet(wallet.getId(), card.getId())) {
            errors.add("La tarjeta \"" + card.getName() + "\" no está vinculada a la wallet \"" + wallet.getName() + "\"");
        }
    }
}
