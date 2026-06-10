package com.giozar04.accounts.presentation.views.detail;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.giozar04.accounts.domain.entities.Account;

/**
 * Vista de detalle para cuentas de tipo <b>BENEFIT (Prestación/Vale)</b>.
 * Muestra balance + datos bancarios + nota sobre restricción de transferencias.
 */
public class BenefitAccountDetailView extends BaseAccountDetailView {

    public BenefitAccountDetailView(Account account) {
        super(account);
    }

    @Override
    protected JPanel buildContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // Balance
        panel.add(buildSection("Información general"));
        panel.add(buildRow("Balance actual", String.format("$%,.2f", account.getCurrentBalance())));
        panel.add(buildRow("Transferencias al exterior", "No permitidas (vales/prestaciones)"));

        // Datos bancarios
        addGap(panel);
        panel.add(buildSection("Datos bancarios"));
        panel.add(buildRow("Número de cuenta", account.getAccountNumber()));
        panel.add(buildRow("CLABE",            account.getClabe()));

        return panel;
    }
}
