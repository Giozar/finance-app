package com.giozar04.accounts.presentation.components.subpanels;

import java.awt.Dimension;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.shared.components.forms.FormDateField;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.components.forms.PercentageField;
import com.giozar04.shared.utils.FormValidatorUtils;

/**
 * Subpanel para campos de savings_details (SAVINGS).
 * Gestiona: tasa de rendimiento anual, monto tope para rendimiento (opcional)
 * y fecha del último cálculo de rendimiento.
 *
 * Usa {@link PercentageField} para la tasa (el usuario ve %, la BD recibe fracción)
 * y {@link FormDateField} para la fecha (selector visual, devuelve "yyyy-MM-dd").
 */
public class SavingsDetailsSubPanel extends JPanel {

    private static final int FIELD_W = 500;
    private static final int FIELD_H = 40;

    private final PercentageField annualYieldField;
    private final FormField       yieldCapAmountField;
    private final FormDateField   lastYieldCalcField;

    public SavingsDetailsSubPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        annualYieldField    = new PercentageField("Rendimiento anual:", FIELD_W, FIELD_H + 10);
        yieldCapAmountField = new FormField("Monto tope rendimiento ($, opcional):", false, FIELD_W, FIELD_H);
        lastYieldCalcField  = new FormDateField("Último cálculo de rendimiento:", FIELD_W, FIELD_H + 10);

        add(annualYieldField);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(yieldCapAmountField);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(lastYieldCalcField);
    }

    public void validate(List<String> errors) {
        // Tasa anual
        double fraction = annualYieldField.getFraction();
        if (fraction < 0 || fraction > 1) {
            errors.add("El rendimiento anual debe estar entre 0 % y 100 %.");
        }

        // Monto tope (opcional)
        String capStr = yieldCapAmountField.getValue().trim();
        if (!capStr.isEmpty()) {
            try {
                double cap = Double.parseDouble(capStr);
                if (cap < 0) {
                    errors.add("El monto tope para rendimiento no puede ser negativo.");
                }
            } catch (NumberFormatException e) {
                errors.add("El monto tope para rendimiento debe ser un número válido.");
            }
        }

        // La fecha del último cálculo es opcional, FormDateField siempre tiene valor válido
    }

    public void applyTo(Account account) {
        account.setAnnualYield(annualYieldField.getFraction());

        String capStr = yieldCapAmountField.getValue().trim();
        account.setYieldCapAmount(capStr.isEmpty() ? null : Double.valueOf(capStr));

        // Solo guardamos fecha si fue cargada desde BD (el campo siempre tiene una fecha por defecto)
        account.setLastYieldCalculation(lastYieldCalcField.getDateString());
    }

    public void loadFrom(Account account) {
        annualYieldField.setFraction(account.getAnnualYield());

        yieldCapAmountField.setValue(
                account.getYieldCapAmount() != null ? account.getYieldCapAmount().toString() : "");

        if (account.getLastYieldCalculation() != null) {
            lastYieldCalcField.setDateString(account.getLastYieldCalculation());
        } else {
            lastYieldCalcField.clearToToday();
        }
    }

    public void clear() {
        annualYieldField.clear();
        yieldCapAmountField.clear();
        lastYieldCalcField.clearToToday();
    }
}
