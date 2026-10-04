package com.giozar04.transactions.presentation.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;
import com.giozar04.walletTransactionDetails.domain.enums.WalletTransactionSourceType;

/**
 * Resumen de solo lectura de una transacción: datos generales, origen y destino, clasificación y,
 * si aplica, el detalle de tarjeta o de wallet.
 */
public final class TransactionDetailsDialog {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private TransactionDetailsDialog() {
    }

    public static void show(Component parent, Transaction tx, TransactionNameLookup lookup) {
        JTextArea text = new JTextArea(buildSummary(tx, lookup));
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(new Font("SansSerif", Font.PLAIN, 13));
        text.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(text);
        scroll.setPreferredSize(new Dimension(520, 420));
        JOptionPane.showMessageDialog(parent, scroll, "Detalle de la transacción", JOptionPane.INFORMATION_MESSAGE);
    }

    static String buildSummary(Transaction tx, TransactionNameLookup lookup) {
        StringBuilder sb = new StringBuilder();
        line(sb, "Concepto", tx.getConcept());
        line(sb, "Tipo", tx.getOperationType() != null ? tx.getOperationType().getLabel() : null);
        line(sb, "Estado", tx.getStatus() != null ? tx.getStatus().getLabel() : null);
        line(sb, "Fecha", formatDate(tx));
        line(sb, "Monto", money(tx.getAmount()));
        line(sb, "Método de pago", tx.getPaymentMethod() != null ? tx.getPaymentMethod().getLabel() : null);
        line(sb, "Origen", lookup.originOf(tx));
        line(sb, "Destino", lookup.destinationOf(tx));
        line(sb, "Categoría", lookup.categoryName(tx.getCategoryId()));
        line(sb, "Etiquetas", lookup.tagNames(tx.getTagIds()));
        line(sb, "Descripción", tx.getDescription());
        line(sb, "Comentarios", tx.getComments());
        line(sb, "Comprobante", tx.getReceiptUrl());

        CardTransactionDetail card = tx.getCardDetail();
        if (card != null) {
            sb.append("\nDetalle de tarjeta\n");
            line(sb, "  Tarjeta", lookup.cardName(card.getCardId()));
            if (card.getInstallmentMonths() == null) {
                line(sb, "  Forma de pago", "Contado");
            } else {
                int months = card.getInstallmentMonths();
                line(sb, "  Forma de pago", months + " meses" + (card.isInterestFree() ? " sin intereses" : " con intereses"));
                BigDecimal amount = card.getAmount() != null ? card.getAmount() : tx.getAmount();
                if (amount != null && months > 0) {
                    line(sb, "  Mensualidad", money(amount.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP)));
                }
            }
        }

        WalletTransactionDetail wallet = tx.getWalletDetail();
        if (wallet != null) {
            sb.append("\nDetalle de wallet\n");
            line(sb, "  Wallet", lookup.accountName(wallet.getWalletAccountId()));
            boolean linked = wallet.getSourceType() == WalletTransactionSourceType.LINKED_CARD;
            line(sb, "  Pagado con", linked ? "Tarjeta vinculada" : "Saldo de la wallet");
            if (linked) {
                line(sb, "  Tarjeta", lookup.cardName(wallet.getCardId()));
                line(sb, "  Se cargó a", lookup.accountName(tx.getSourceAccountId()));
            }
            if (wallet.getCashbackRate() != null) {
                line(sb, "  Cashback", String.format("%.2f %%", wallet.getCashbackRate().doubleValue() * 100));
            }
        }
        return sb.toString();
    }

    private static void line(StringBuilder sb, String label, String value) {
        sb.append(label).append(": ").append(value != null && !value.isBlank() ? value : "-").append('\n');
    }

    private static String money(BigDecimal amount) {
        return amount != null ? String.format("$%,.2f", amount) : null;
    }

    private static String formatDate(Transaction tx) {
        if (tx.getDate() == null) {
            return null;
        }
        String zone = tx.getTimezone() != null ? tx.getTimezone() : tx.getDate().getZone().getId();
        try {
            return tx.getDate().withZoneSameInstant(ZoneId.of(zone)).format(DATE_FORMAT) + " (" + zone + ")";
        } catch (RuntimeException e) {
            return tx.getDate().format(DATE_FORMAT);
        }
    }
}
