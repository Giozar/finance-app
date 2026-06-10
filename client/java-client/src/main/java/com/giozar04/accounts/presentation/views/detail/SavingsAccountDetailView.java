package com.giozar04.accounts.presentation.views.detail;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.giozar04.accounts.domain.entities.Account;

/**
 * Vista de detalle para cuentas de tipo <b>SAVINGS (Ahorro con Rendimiento)</b>.
 * Muestra balance + parámetros del rendimiento (tasa, cap, último cálculo).
 */
public class SavingsAccountDetailView extends BaseAccountDetailView {

    public SavingsAccountDetailView(Account account) {
        super(account);
    }

    @Override
    protected JPanel buildContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // Balance
        panel.add(buildSection("Información general"));
        panel.add(buildRow("Balance / Capital", String.format("$%,.2f", account.getCurrentBalance())));

        // Rendimiento
        addGap(panel);
        panel.add(buildSection("Rendimiento"));

        String yieldStr = account.getAnnualYield() != null
                ? String.format("%.4f%%  (%.2f%% anual)", account.getAnnualYield() * 100, account.getAnnualYield() * 100)
                : null;
        panel.add(buildRow("Tasa anual",          yieldStr));

        String capStr = account.getYieldCapAmount() != null
                ? String.format("$%,.2f", account.getYieldCapAmount())
                : "Sin límite de rendimiento";
        panel.add(buildRow("Cap de rendimiento",  capStr));
        panel.add(buildRow("Último cálculo",      account.getLastYieldCalculation()));

        return panel;
    }
}
