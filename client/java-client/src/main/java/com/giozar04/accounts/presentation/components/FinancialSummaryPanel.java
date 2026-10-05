package com.giozar04.accounts.presentation.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.enums.AccountTypes;

/** Resumen de posición: disponible, deuda acumulada en todos los créditos y balance real (disponible − deuda). */
public class FinancialSummaryPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Color NEGATIVE = new Color(190, 40, 40);
    private static final Color NORMAL = new Color(30, 30, 50);

    private final JLabel availableValue = new JLabel();
    private final JLabel debtValue = new JLabel();
    private final JLabel realValue = new JLabel();

    public FinancialSummaryPanel() {
        super(new GridLayout(1, 3, 12, 0));
        setBorder(new EmptyBorder(8, 0, 8, 0));
        add(card("Disponible (balance global)", availableValue));
        add(card("Deuda en créditos", debtValue));
        add(card("Balance real (disponible − deuda)", realValue));
        refresh(List.of());
    }

    public void refresh(List<Account> accounts) {
        double available = 0;
        double debt = 0;
        for (Account account : accounts) {
            available += account.getCurrentBalance();
            if (account.getType() == AccountTypes.CREDIT && account.getCreditUsed() != null) {
                debt += account.getCreditUsed();
            }
        }
        double real = available - debt;
        availableValue.setText(String.format("$%,.2f", available));
        debtValue.setText(String.format("$%,.2f", debt));
        realValue.setText(String.format("$%,.2f", real));
        realValue.setForeground(real < 0 ? NEGATIVE : NORMAL);
    }

    private JPanel card(String title, JLabel value) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 4));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 215, 230), 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        titleLabel.setForeground(new Color(110, 110, 125));
        value.setFont(new Font("SansSerif", Font.BOLD, 18));
        value.setForeground(NORMAL);
        panel.add(titleLabel);
        panel.add(value);
        return panel;
    }
}
