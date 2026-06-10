package com.giozar04.accounts.presentation.views.detail;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.giozar04.accounts.domain.entities.Account;

/**
 * Vista de detalle para cuentas de tipo <b>CASH (Efectivo)</b>.
 * Solo muestra el balance; no hay datos adicionales para este tipo.
 */
public class CashAccountDetailView extends BaseAccountDetailView {

    public CashAccountDetailView(Account account) {
        super(account);
    }

    @Override
    protected JPanel buildContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        panel.add(buildSection("Información general"));
        panel.add(buildRow("Balance actual", String.format("$%,.2f", account.getCurrentBalance())));
        panel.add(Box.createRigidArea(new Dimension(0, 16)));
        panel.add(buildRow("Tipo", "Efectivo — sin datos bancarios adicionales"));

        return panel;
    }
}
