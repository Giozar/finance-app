package com.giozar04.transactions.application.validation.rules;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import com.giozar04.card.domain.entities.Card;
import com.giozar04.card.domain.enums.CardStatus;
import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;
import com.giozar04.transactions.application.validation.TransactionRule;
import com.giozar04.transactions.application.validation.ValidationContext;
import com.giozar04.transactions.domain.entities.Transaction;

/**
 * CARD: detalle de tarjeta obligatorio; la tarjeta existe, pertenece a la cuenta origen,
 * está ACTIVE y no estaba vencida en la fecha de la transacción; meses null (contado) o > 0.
 * Sin detalle de wallet. La BD repite "tarjeta de la cuenta origen" en sp_validate_card_detail.
 */
public class CardPaymentRule implements TransactionRule {

    private static final String ACTIVE = "ACTIVE";

    @Override
    public void validate(Transaction tx, ValidationContext ctx, List<String> errors) {
        if (tx.getWalletDetail() != null) {
            errors.add("El pago con tarjeta no lleva detalle de wallet");
        }

        CardTransactionDetail detail = tx.getCardDetail();
        if (detail == null) {
            errors.add("El pago con tarjeta requiere el detalle de tarjeta");
            return;
        }

        if (detail.getInstallmentMonths() != null && detail.getInstallmentMonths() <= 0) {
            errors.add("Los meses a pagar deben ser mayores que cero");
        }

        if (detail.getCardId() <= 0) {
            errors.add("Seleccione la tarjeta con la que se pagó");
            return;
        }

        Card card = ctx.card(detail.getCardId());
        if (card == null) {
            errors.add("La tarjeta con ID " + detail.getCardId() + " no existe");
            return;
        }

        if (tx.getSourceAccountId() == null) {
            errors.add("El pago con tarjeta requiere una cuenta origen");
        } else if (card.getAccountId() != tx.getSourceAccountId()) {
            errors.add("La tarjeta \"" + card.getName() + "\" no pertenece a la cuenta origen");
        }

        if (card.getStatus() != CardStatus.ACTIVE) {
            errors.add("La tarjeta \"" + card.getName() + "\" no está activa (estado: " + card.getStatus() + ")");
        }

        if (card.getExpirationDate() != null && isExpired(card, tx)) {
            errors.add("La tarjeta \"" + card.getName() + "\" está vencida");
        }
    }

    /** Vencida si su fecha de expiración es anterior a la fecha de la transacción (o a hoy si no hay fecha). */
    private boolean isExpired(Card card, Transaction tx) {
        LocalDate reference = tx.getDate() != null
                ? tx.getDate().toLocalDate()
                : LocalDate.now(ZoneId.systemDefault());
        return card.getExpirationDate().toLocalDate().isBefore(reference);
    }
}
