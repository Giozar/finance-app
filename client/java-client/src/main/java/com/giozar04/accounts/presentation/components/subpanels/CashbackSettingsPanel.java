package com.giozar04.accounts.presentation.components.subpanels;

import java.awt.Dimension;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.giozar04.accountCashbackSettings.domain.entities.AccountCashbackSetting;
import com.giozar04.shared.components.forms.PercentageField;

/**
 * Subpanel para configurar cashback (reutilizable para DEBIT, CREDIT, WALLET).
 * Gestiona la casilla de activación de cashback y la tasa de cashback por defecto.
 */
public class CashbackSettingsPanel extends JPanel {

    private final JCheckBox cashbackEnabledCheckbox;
    private final PercentageField defaultCashbackRateField;

    private static final int FIELD_W = 500;
    private static final int FIELD_H = 40;

    public CashbackSettingsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        cashbackEnabledCheckbox = new JCheckBox("Habilitar Cashback");
        cashbackEnabledCheckbox.setPreferredSize(new Dimension(FIELD_W, 30));
        cashbackEnabledCheckbox.setOpaque(false);

        defaultCashbackRateField = new PercentageField("Tasa de Cashback:", FIELD_W, FIELD_H + 10);
        defaultCashbackRateField.setEnabled(false);

        cashbackEnabledCheckbox.addActionListener(e -> {
            defaultCashbackRateField.setEnabled(cashbackEnabledCheckbox.isSelected());
            if (!cashbackEnabledCheckbox.isSelected()) {
                defaultCashbackRateField.clear();
            }
        });

        add(cashbackEnabledCheckbox);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(defaultCashbackRateField);
    }

    public void validate(List<String> errors) {
        if (cashbackEnabledCheckbox.isSelected()) {
            double fraction = defaultCashbackRateField.getFraction();
            if (fraction <= 0 || fraction > 1) {
                errors.add("La tasa de cashback debe ser mayor que 0 % y menor o igual a 100 %.");
            }
        }
    }

    public void applyTo(AccountCashbackSetting setting) {
        setting.setCashbackEnabled(cashbackEnabledCheckbox.isSelected());
        if (cashbackEnabledCheckbox.isSelected()) {
            setting.setDefaultCashbackRate(BigDecimal.valueOf(defaultCashbackRateField.getFraction()));
        } else {
            setting.setDefaultCashbackRate(null);
        }
    }

    public void loadFrom(AccountCashbackSetting setting) {
        if (setting == null) {
            clear();
            return;
        }
        cashbackEnabledCheckbox.setSelected(setting.isCashbackEnabled());
        defaultCashbackRateField.setEnabled(setting.isCashbackEnabled());
        if (setting.isCashbackEnabled() && setting.getDefaultCashbackRate() != null) {
            defaultCashbackRateField.setFraction(setting.getDefaultCashbackRate().doubleValue());
        } else {
            defaultCashbackRateField.clear();
        }
    }

    public void clear() {
        cashbackEnabledCheckbox.setSelected(false);
        defaultCashbackRateField.clear();
        defaultCashbackRateField.setEnabled(false);
    }
}
