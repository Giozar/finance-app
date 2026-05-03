package com.giozar04.accounts.presentation.components.subpanels;

import java.awt.Dimension;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.utils.FormValidatorUtils;

/**
 * Subpanel para campos de savings_details (SAVINGS).
 * Gestiona: tasa de rendimiento anual (fracción 0.0–1.0),
 * monto tope para rendimiento (opcional) y fecha del último cálculo (solo lectura/referencia).
 */
public class SavingsDetailsSubPanel extends JPanel {

    private final FormField annualYieldField;    // Fracción ej. "0.15" = 15% anual
    private final FormField yieldCapAmountField; // Opcional: monto tope
    private final FormField lastYieldCalcField;  // Informativo, ej. "2025-01-01"

    public SavingsDetailsSubPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        annualYieldField    = new FormField("Rendimiento anual (ej. 0.15 = 15%):", false, 400, 40);
        yieldCapAmountField = new FormField("Monto tope para rendimiento (opcional):", false, 400, 40);
        lastYieldCalcField  = new FormField("Último cálculo de rendimiento (yyyy-MM-dd):", false, 400, 40);

        add(annualYieldField);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(yieldCapAmountField);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(lastYieldCalcField);
    }

    public void validate(List<String> errors) {
        String yieldStr = annualYieldField.getValue().trim();
        FormValidatorUtils.isRequired(yieldStr, "Rendimiento anual", errors);
        if (!yieldStr.isEmpty()) {
            try {
                double val = Double.parseDouble(yieldStr);
                if (val < 0 || val > 1) {
                    errors.add("El rendimiento anual debe ser una fracción entre 0 y 1 (ej. 0.15 = 15%).");
                }
            } catch (NumberFormatException e) {
                errors.add("El rendimiento anual debe ser un número válido (ej. 0.15).");
            }
        }

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
    }

    public void applyTo(Account account) {
        account.setAnnualYield(Double.valueOf(annualYieldField.getValue().trim()));

        String capStr = yieldCapAmountField.getValue().trim();
        account.setYieldCapAmount(capStr.isEmpty() ? null : Double.valueOf(capStr));

        String lastCalc = lastYieldCalcField.getValue().trim();
        account.setLastYieldCalculation(lastCalc.isEmpty() ? null : lastCalc);
    }

    public void loadFrom(Account account) {
        annualYieldField.setValue(account.getAnnualYield() != null ? account.getAnnualYield().toString() : "");
        yieldCapAmountField.setValue(account.getYieldCapAmount() != null ? account.getYieldCapAmount().toString() : "");
        lastYieldCalcField.setValue(account.getLastYieldCalculation() != null ? account.getLastYieldCalculation() : "");
    }

    public void clear() {
        annualYieldField.clear();
        yieldCapAmountField.clear();
        lastYieldCalcField.clear();
    }
}
