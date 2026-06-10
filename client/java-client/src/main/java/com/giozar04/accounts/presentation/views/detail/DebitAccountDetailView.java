package com.giozar04.accounts.presentation.views.detail;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.giozar04.accounts.domain.entities.Account;

/**
 * Vista de detalle para cuentas de tipo <b>DEBIT (Débito Bancario)</b>.
 * Muestra balance + datos bancarios (número de cuenta, CLABE, transferencias).
 */
public class DebitAccountDetailView extends BaseAccountDetailView {

    public DebitAccountDetailView(Account account) {
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

        // Datos bancarios
        addGap(panel);
        panel.add(buildSection("Datos bancarios"));
        panel.add(buildRow("Número de cuenta", account.getAccountNumber()));
        panel.add(buildRow("CLABE",            account.getClabe()));

        Boolean canTransfer = account.getCanTransferOut();
        String transfer = canTransfer != null
                ? (canTransfer ? "Sí — permite transferencias" : "No — restringido")
                : "—";
        panel.add(buildRow("Transferencias", transfer));

        return panel;
    }
}
