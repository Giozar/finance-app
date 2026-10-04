package com.giozar04.transactions.presentation.components.sections;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import javax.swing.JCheckBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.card.domain.entities.Card;
import com.giozar04.card.domain.enums.CardTypes;
import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;
import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.components.forms.FormHelpText;
import com.giozar04.shared.components.forms.FormLabel;
import com.giozar04.shared.utils.FormValidatorUtils;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.PaymentMethod;
import com.giozar04.transactions.presentation.form.TransactionFormContext;
import com.giozar04.transactions.presentation.form.TransactionFormDataProvider;

/**
 * Sección 4 – Detalle de tarjeta (solo con método CARD): tarjeta de la cuenta origen con filtro
 * física/digital, compra a meses (número de meses y "¿Sin intereses?") y mensualidad estimada.
 *
 * <p>El saldo lo mueve la transacción sobre la cuenta origen; el detalle (MSI) es informativo.</p>
 */
public class CardDetailsSection extends AbstractTransactionSection {

    private static final long serialVersionUID = 1L;

    private static final int MIN_MONTHS = 2;
    private static final int MAX_MONTHS = 60;

    private final FormComboBox<CardTypes> cardTypeFilter;
    private final FormComboBox<Card> cardCombo;
    private final FormHelpText cardHelp;
    private final JCheckBox installmentsCheck;
    private final FormLabel installmentsRow;
    private final FormField monthsField;
    private final JCheckBox interestFreeCheck;
    private final FormLabel interestFreeRow;
    private final FormHelpText monthlyHelp;

    private Long lastSourceAccountId;

    public CardDetailsSection(TransactionFormContext context, TransactionFormDataProvider provider) {
        super("Detalle de tarjeta", context, provider);

        cardTypeFilter = new FormComboBox<>("Tipo de tarjeta:", FIELD_WIDTH, FIELD_HEIGHT);
        cardTypeFilter.setPlaceholder("Todas las tarjetas");
        cardTypeFilter.setItems(List.of(CardTypes.values()));

        cardCombo = new FormComboBox<>("Tarjeta:", FIELD_WIDTH, FIELD_HEIGHT);
        cardCombo.setPlaceholder("Seleccione una tarjeta...");
        cardHelp = new FormHelpText("", FIELD_WIDTH);

        installmentsCheck = new JCheckBox("Sí");
        installmentsRow = labeledRow("¿Compra a meses?", installmentsCheck);

        monthsField = new FormField("Número de meses:", false, FIELD_WIDTH, FIELD_HEIGHT);

        interestFreeCheck = new JCheckBox("Sí");
        interestFreeRow = labeledRow("¿Sin intereses?", interestFreeCheck);

        monthlyHelp = new FormHelpText("", FIELD_WIDTH);

        addRow(cardTypeFilter);
        addRow(cardCombo);
        addRow(cardHelp);
        addRow(installmentsRow);
        addRow(monthsField);
        addRow(interestFreeRow);
        addRow(monthlyHelp);

        cardTypeFilter.addActionListener(e -> reloadCards());
        installmentsCheck.addActionListener(e -> updateInstallmentsVisibility());
        interestFreeCheck.addActionListener(e -> updateMonthlyPayment());
        monthsField.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateMonthlyPayment(); }
            @Override public void removeUpdate(DocumentEvent e) { updateMonthlyPayment(); }
            @Override public void changedUpdate(DocumentEvent e) { updateMonthlyPayment(); }
        });

        updateInstallmentsVisibility();
        setVisible(false);
    }

    private static FormLabel labeledRow(String text, JCheckBox check) {
        check.setOpaque(false);
        FormLabel row = new FormLabel(text, check);
        java.awt.Dimension size = new java.awt.Dimension(FIELD_WIDTH, 30);
        row.getComponent(0).setPreferredSize(new java.awt.Dimension(150, 25));
        row.setPreferredSize(size);
        row.setMaximumSize(size);
        return row;
    }

    // ------------------------------------------------------------------
    // Contexto
    // ------------------------------------------------------------------

    private boolean isActive() {
        return context.getPaymentMethod() == PaymentMethod.CARD;
    }

    @Override
    public void onContextChanged(TransactionFormContext ctx) {
        boolean active = ctx.getPaymentMethod() == PaymentMethod.CARD;
        if (!active) {
            if (isVisible()) {
                clear();
                setVisible(false);
            }
            lastSourceAccountId = null;
            return;
        }
        setVisible(true);
        Account source = ctx.getSourceAccount();
        Long sourceId = source != null ? source.getId() : null;
        if (!Objects.equals(lastSourceAccountId, sourceId)) {
            lastSourceAccountId = sourceId;
            reloadCards();
        }
        updateMonthlyPayment();
    }

    /** Carga las tarjetas de la cuenta origen según el filtro; conserva la elegida si sigue en la lista. */
    private void reloadCards() {
        Card previous = cardCombo.getSelectedItem();
        List<Card> cards = lastSourceAccountId != null
                ? provider.getCardsByAccount(lastSourceAccountId, cardTypeFilter.getSelectedItem())
                : List.of();
        cardCombo.setItems(cards);
        if (previous != null) {
            selectInCombo(cardCombo, c -> c.getId() == previous.getId());
        }
        if (lastSourceAccountId == null) {
            cardHelp.setText("Seleccione la cuenta origen para ver sus tarjetas.");
        } else if (cards.isEmpty()) {
            cardHelp.setText(cardTypeFilter.getSelectedItem() == null
                    ? "La cuenta origen no tiene tarjetas registradas. Regístrelas desde Tarjetas."
                    : "La cuenta origen no tiene tarjetas de este tipo.");
        } else {
            cardHelp.setText("Tarjetas de la cuenta origen.");
        }
    }

    private void updateInstallmentsVisibility() {
        boolean installments = installmentsCheck.isSelected();
        setRowVisible(monthsField, installments);
        setRowVisible(interestFreeRow, installments);
        if (!installments) {
            monthsField.clear();
            interestFreeCheck.setSelected(false);
        }
        updateMonthlyPayment();
        refreshLayout();
    }

    /** Mensualidad = monto / meses (sin contar intereses). */
    private void updateMonthlyPayment() {
        if (!installmentsCheck.isSelected()) {
            monthlyHelp.setText("Compra de contado.");
            return;
        }
        Integer months = parseMonths();
        BigDecimal amount = context.getAmount();
        if (months == null || months <= 0 || amount == null || amount.signum() <= 0) {
            monthlyHelp.setText("Capture el monto y el número de meses para calcular la mensualidad.");
            return;
        }
        BigDecimal monthly = amount.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        String text = String.format("Mensualidad: $%,.2f durante %d meses", monthly, months);
        monthlyHelp.setText(interestFreeCheck.isSelected() ? text + " (sin intereses)."
                : text + " (sin contar intereses).");
    }

    private Integer parseMonths() {
        try {
            return Integer.valueOf(monthsField.getValue().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------
    // Contrato de sección
    // ------------------------------------------------------------------

    @Override
    public void validate(List<String> errors) {
        if (!isActive()) {
            return;
        }
        if (cardCombo.getSelectedItem() == null) {
            errors.add("Debe seleccionar la tarjeta con la que se pagó.");
        }
        if (installmentsCheck.isSelected()) {
            FormValidatorUtils.isIntegerInRange(monthsField.getValue(), "Número de meses", MIN_MONTHS, MAX_MONTHS, errors);
        }
    }

    @Override
    public void applyTo(Transaction tx) {
        if (!isActive()) {
            tx.setCardDetail(null);
            return;
        }
        CardTransactionDetail detail = new CardTransactionDetail();
        detail.setCardId(cardCombo.getSelectedItem().getId());
        if (installmentsCheck.isSelected()) {
            detail.setInstallmentMonths(parseMonths());
            detail.setInterestFree(interestFreeCheck.isSelected());
        } else {
            detail.setInstallmentMonths(null); // contado
            detail.setInterestFree(false);
        }
        tx.setCardDetail(detail);
    }

    @Override
    public void loadFrom(Transaction tx) {
        CardTransactionDetail detail = tx.getCardDetail();
        if (detail == null || !isActive()) {
            return;
        }
        cardTypeFilter.clearSelection();
        selectInCombo(cardCombo, c -> c.getId() == detail.getCardId());
        boolean installments = detail.getInstallmentMonths() != null;
        installmentsCheck.setSelected(installments);
        updateInstallmentsVisibility();
        if (installments) {
            monthsField.setValue(String.valueOf(detail.getInstallmentMonths()));
            interestFreeCheck.setSelected(detail.isInterestFree());
        }
        updateMonthlyPayment();
    }

    @Override
    public void clear() {
        cardTypeFilter.clearSelection();
        cardCombo.clearSelection();
        installmentsCheck.setSelected(false);
        updateInstallmentsVisibility();
    }
}
