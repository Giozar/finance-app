package com.giozar04.accounts.presentation.components.subpanels;

import java.awt.Dimension;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.shared.components.CreditUsagePanel;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.components.forms.FormHelpText;
import com.giozar04.shared.utils.FormValidatorUtils;

/**
 * Subpanel de formulario para campos editables de credit_details (CREDIT).
 * Gestiona: límite de crédito, día de corte, día de pago y la deuda actual al crear.
 *
 * <p>{@code credit_used} ("Deuda actual") solo se captura al CREAR la cuenta: se envía en el alta
 * y el trigger de MySQL la guarda como deuda inicial ({@code opening_credit_used}). Al EDITAR
 * es de solo lectura (cambia con transacciones o desde Conciliación); el uso se muestra en
 * {@link CreditUsagePanel}.</p>
 *
 * <p>Requiere {@code BankDetailsSubPanel} para el cliente bancario (CLABE / número de cuenta).</p>
 */
public class CreditDetailsSubPanel extends JPanel {

    private final FormField      creditLimitField;
    private final FormField      cutoffDayField;
    private final FormField      paymentDayField;
    private final FormField      creditUsedField;
    private final FormHelpText   creditUsedHelp;
    private final CreditUsagePanel creditUsagePanel;

    /** true al editar una cuenta existente: la deuda es de solo lectura. */
    private boolean editMode = false;

    public CreditDetailsSubPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        creditLimitField = new FormField("Límite de crédito:", false, 400, 40);
        cutoffDayField   = new FormField("Día de corte (1-31):", false, 400, 40);
        paymentDayField  = new FormField("Día de pago (1-31):", false, 400, 40);
        creditUsedField  = new FormField("Deuda actual:", false, 400, 40);
        creditUsedHelp   = new FormHelpText("Se modifica con transacciones o desde Conciliación.", 400);
        creditUsedHelp.setVisible(false);
        creditUsagePanel = new CreditUsagePanel();
        creditUsagePanel.setMaximumSize(new Dimension(400, 80));
        creditUsagePanel.setAlignmentX(LEFT_ALIGNMENT);

        add(creditLimitField);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(cutoffDayField);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(paymentDayField);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(creditUsedField);
        add(creditUsedHelp);
        add(Box.createRigidArea(new Dimension(0, 14)));
        add(creditUsagePanel);
    }

    // -------------------------------------------------------------------------
    // Contrato público
    // -------------------------------------------------------------------------

    public void validate(List<String> errors) {
        FormValidatorUtils.isPositiveNumber(creditLimitField.getValue().trim(), "Límite de crédito", errors);
        FormValidatorUtils.isIntegerInRange(cutoffDayField.getValue().trim(), "Día de corte", 1, 31, errors);
        FormValidatorUtils.isIntegerInRange(paymentDayField.getValue().trim(), "Día de pago", 1, 31, errors);
        if (!editMode) {
            validateCreditUsed(errors);
        }
    }

    /**
     * Aplica los valores editables a la cuenta. Al crear, también la deuda actual
     * (vacía = 0); al editar, {@code credit_used} NO se toca.
     */
    public void applyTo(Account account) {
        account.setCreditLimit(Double.valueOf(creditLimitField.getValue().trim()));
        account.setCutoffDay(Integer.valueOf(cutoffDayField.getValue().trim()));
        account.setPaymentDay(Integer.valueOf(paymentDayField.getValue().trim()));
        if (!editMode) {
            String debt = creditUsedField.getValue().trim();
            account.setCreditUsed(debt.isEmpty() ? 0.0 : Double.valueOf(debt));
        }
    }

    /**
     * Modo edición: la deuda actual pasa a solo lectura y se muestra el texto de ayuda.
     * En modo creación (por defecto) es editable.
     */
    public void setEditMode(boolean editing) {
        this.editMode = editing;
        creditUsedField.getTextField().setEditable(!editing);
        creditUsedHelp.setVisible(editing);
    }

    public void loadFrom(Account account) {
        creditLimitField.setValue(account.getCreditLimit() != null ? account.getCreditLimit().toString() : "");
        cutoffDayField.setValue(account.getCutoffDay()    != null ? account.getCutoffDay().toString()    : "");
        paymentDayField.setValue(account.getPaymentDay()  != null ? account.getPaymentDay().toString()   : "");
        creditUsedField.setValue(account.getCreditUsed()  != null ? account.getCreditUsed().toString()   : "");
        creditUsagePanel.refresh(account.getCreditUsed(), account.getCreditLimit());
    }

    public void clear() {
        creditLimitField.clear();
        cutoffDayField.clear();
        paymentDayField.clear();
        creditUsedField.clear();
        creditUsagePanel.clear();
    }

    // -------------------------------------------------------------------------
    // Auxiliares
    // -------------------------------------------------------------------------

    /** Deuda actual: opcional, número ≥ 0 y no mayor que el límite de crédito. */
    private void validateCreditUsed(List<String> errors) {
        String debt = creditUsedField.getValue().trim();
        if (debt.isEmpty()) {
            return;
        }
        double value;
        try {
            value = Double.parseDouble(debt);
        } catch (NumberFormatException e) {
            errors.add("Deuda actual debe ser un número válido.");
            return;
        }
        if (value < 0) {
            errors.add("Deuda actual no puede ser negativa.");
            return;
        }
        try {
            double limit = Double.parseDouble(creditLimitField.getValue().trim());
            if (value > limit) {
                errors.add("Deuda actual no puede ser mayor que el límite de crédito.");
            }
        } catch (NumberFormatException e) {
            // El límite inválido ya se reporta en su propia validación
        }
    }
}
