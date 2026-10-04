package com.giozar04.transactions.application.normalizers;

import com.giozar04.card.domain.entities.Card;
import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;
import com.giozar04.transactions.application.validation.ValidationContext;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.PaymentMethod;
import com.giozar04.transactions.domain.enums.TransactionStatus;
import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;
import com.giozar04.walletTransactionDetails.domain.enums.WalletTransactionSourceType;

/**
 * Aplica las reglas derivadas que el usuario no captura. Se ejecuta ANTES de validar, para que
 * el validador compruebe el agregado tal y como se guardará. Es defensivo: nunca lanza
 * excepciones por datos faltantes o inexistentes (eso lo reporta el validador).
 *
 * - status null → COMPLETED.
 * - Textos: concepto recortado; descripción, comentarios y comprobante vacíos → null.
 * - Se quitan los detalles que no corresponden al método (cardDetail solo CARD, walletDetail solo WALLET).
 * - Monto de los detalles = monto de la transacción (no hay pagos mixtos).
 * - WALLET + WALLET_BALANCE → sourceAccountId = la wallet y cardId = null.
 * - WALLET + LINKED_CARD → sourceAccountId = cuenta de la tarjeta (la que financia el pago).
 */
public class TransactionNormalizer {

    public void normalize(Transaction tx, ValidationContext ctx) {
        if (tx == null) return;

        if (tx.getStatus() == null) tx.setStatus(TransactionStatus.COMPLETED);

        if (tx.getConcept() != null) tx.setConcept(tx.getConcept().trim());
        tx.setDescription(blankToNull(tx.getDescription()));
        tx.setComments(blankToNull(tx.getComments()));
        tx.setReceiptUrl(blankToNull(tx.getReceiptUrl()));
        if (tx.getTimezone() != null) tx.setTimezone(tx.getTimezone().trim());

        PaymentMethod method = tx.getPaymentMethod();
        if (method != PaymentMethod.CARD) tx.setCardDetail(null);
        if (method != PaymentMethod.WALLET) tx.setWalletDetail(null);

        CardTransactionDetail cardDetail = tx.getCardDetail();
        if (cardDetail != null) {
            cardDetail.setAmount(tx.getAmount());
        }

        WalletTransactionDetail walletDetail = tx.getWalletDetail();
        if (walletDetail != null) {
            walletDetail.setAmount(tx.getAmount());
            deriveWalletSource(tx, walletDetail, ctx);
        }
    }

    private void deriveWalletSource(Transaction tx, WalletTransactionDetail detail, ValidationContext ctx) {
        if (detail.getSourceType() == WalletTransactionSourceType.WALLET_BALANCE) {
            detail.setCardId(null);
            if (detail.getWalletAccountId() > 0) {
                tx.setSourceAccountId(detail.getWalletAccountId());
            }
        } else if (detail.getSourceType() == WalletTransactionSourceType.LINKED_CARD && detail.getCardId() != null) {
            Card card = ctx.card(detail.getCardId());
            if (card != null) {
                tx.setSourceAccountId(card.getAccountId());
            }
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
