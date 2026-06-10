package com.giozar04.accounts.presentation.views.detail;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.shared.components.CreditUsagePanel;

/**
 * Vista de detalle para cuentas de tipo <b>CREDIT (Crédito)</b>.
 *
 * <p>Estructura:</p>
 * <ol>
 *   <li>Tarjeta de crédito disponible con barra visual ({@link CreditUsagePanel}) — prominente</li>
 *   <li>Ciclo de pago (día de corte, día de pago)</li>
 *   <li>Datos bancarios (número de cuenta, CLABE)</li>
 * </ol>
 */
public class CreditAccountDetailView extends BaseAccountDetailView {

    public CreditAccountDetailView(Account account) {
        super(account);
    }

    @Override
    protected JPanel buildContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // --- Tarjeta de crédito disponible ---
        panel.add(buildCreditCard());

        // --- Ciclo de pago ---
        addGap(panel);
        panel.add(buildSection("Ciclo de pago"));
        panel.add(buildRow("Día de corte", account.getCutoffDay()  != null ? "Día " + account.getCutoffDay()  : null));
        panel.add(buildRow("Día de pago",  account.getPaymentDay() != null ? "Día " + account.getPaymentDay() : null));

        // --- Datos bancarios ---
        addGap(panel);
        panel.add(buildSection("Datos bancarios"));
        panel.add(buildRow("Número de cuenta", account.getAccountNumber()));
        panel.add(buildRow("CLABE",            account.getClabe()));

        return panel;
    }

    /**
     * Tarjeta visual con la barra de crédito disponible destacada.
     */
    private JPanel buildCreditCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(true);
        card.setBackground(new Color(248, 248, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 215, 230), 1, true),
                new EmptyBorder(14, 16, 16, 16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel cardTitle = new JLabel("Crédito disponible");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        cardTitle.setForeground(new Color(55, 55, 75));
        cardTitle.setAlignmentX(LEFT_ALIGNMENT);

        // Barra de uso de crédito (solo lectura)
        CreditUsagePanel bar = new CreditUsagePanel();
        bar.setAlignmentX(LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        bar.refresh(account.getCreditUsed(), account.getCreditLimit());

        // Línea de límite
        Double limit = account.getCreditLimit();
        JLabel limitLabel = new JLabel(limit != null
                ? String.format("Límite total: $%,.2f", limit)
                : "Límite no configurado");
        limitLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        limitLabel.setForeground(new Color(110, 110, 125));
        limitLabel.setAlignmentX(LEFT_ALIGNMENT);

        card.add(cardTitle);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(bar);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(limitLabel);

        return card;
    }
}
