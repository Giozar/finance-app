package com.giozar04.accounts.presentation.components.subpanels;

import java.awt.Dimension;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.shared.components.CreditUsagePanel;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.utils.FormValidatorUtils;

/**
 * Subpanel de formulario para campos editables de credit_details (CREDIT).
 * Gestiona: límite de crédito, día de corte, día de pago.
 *
 * <p>El campo {@code credit_used} es completamente de solo lectura y se
 * delega al componente {@link CreditUsagePanel}. El trigger de MySQL
 * es el único responsable de modificar ese valor.</p>
 *
 * <p>Requiere {@code BankDetailsSubPanel} para el cliente bancario (CLABE / número de cuenta).</p>
 */
public class CreditDetailsSubPanel extends JPanel {

    private final FormField      creditLimitField;
    private final FormField      cutoffDayField;
    private final FormField      paymentDayField;
    private final CreditUsagePanel creditUsagePanel;

    public CreditDetailsSubPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        creditLimitField = new FormField("Límite de crédito:", false, 400, 40);
        cutoffDayField   = new FormField("Día de corte (1-31):", false, 400, 40);
        paymentDayField  = new FormField("Día de pago (1-31):", false, 400, 40);
        creditUsagePanel = new CreditUsagePanel();
        creditUsagePanel.setMaximumSize(new Dimension(400, 80));
        creditUsagePanel.setAlignmentX(LEFT_ALIGNMENT);

        add(creditLimitField);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(cutoffDayField);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(paymentDayField);
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
    }

    /** Aplica los valores editables a la cuenta. {@code credit_used} NO se toca. */
    public void applyTo(Account account) {
        account.setCreditLimit(Double.valueOf(creditLimitField.getValue().trim()));
        account.setCutoffDay(Integer.valueOf(cutoffDayField.getValue().trim()));
        account.setPaymentDay(Integer.valueOf(paymentDayField.getValue().trim()));
        // creditUsed es responsabilidad exclusiva del trigger de MySQL
    }

    public void loadFrom(Account account) {
        creditLimitField.setValue(account.getCreditLimit() != null ? account.getCreditLimit().toString() : "");
        cutoffDayField.setValue(account.getCutoffDay()    != null ? account.getCutoffDay().toString()    : "");
        paymentDayField.setValue(account.getPaymentDay()  != null ? account.getPaymentDay().toString()   : "");
        creditUsagePanel.refresh(account.getCreditUsed(), account.getCreditLimit());
    }

    public void clear() {
        creditLimitField.clear();
        cutoffDayField.clear();
        paymentDayField.clear();
        creditUsagePanel.clear();
    }
}
