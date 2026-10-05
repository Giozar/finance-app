package com.giozar04.accounts.presentation.views.detail;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Font;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.application.ports.input.AccountOperations;
import com.giozar04.bootstrap.ClientUseCases;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.CreditUsagePanel;
import com.giozar04.shared.components.MainContentPanel;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.transactions.application.ports.input.TransactionOperations;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.domain.enums.TransactionStatus;
import com.giozar04.transactions.presentation.views.CreateTransactionView;

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

        // --- Abonos ---
        addGap(panel);
        panel.add(buildSection("Abonos a esta tarjeta"));
        JButton payButton = new JButton("Abonar a esta tarjeta");
        payButton.setAlignmentX(LEFT_ALIGNMENT);
        payButton.addActionListener(e -> openPaymentForm());
        panel.add(payButton);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        addPayments(panel);

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
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
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

        // Deuda inicial (solo lectura, la fija la base de datos)
        Double openingCreditUsed = account.getOpeningCreditUsed();
        JLabel openingDebtLabel = new JLabel(openingCreditUsed != null
                ? String.format("Deuda inicial: $%,.2f", openingCreditUsed)
                : "Deuda inicial: —");
        openingDebtLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        openingDebtLabel.setForeground(new Color(110, 110, 125));
        openingDebtLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(Box.createRigidArea(new Dimension(0, 2)));
        card.add(openingDebtLabel);

        return card;
    }

    private void openPaymentForm() {
        Transaction draft = new Transaction();
        draft.setUserId(account.getUserId());
        draft.setOperationType(OperationTypes.REALLOCATION);
        draft.setStatus(TransactionStatus.COMPLETED);
        draft.setDestinationAccountId(account.getId());
        draft.setConcept("Pago de " + account.getName());
        draft.setDate(ZonedDateTime.now());
        Container parent = getParent();
        while (parent != null && !(parent instanceof MainContentPanel)) {
            parent = parent.getParent();
        }
        if (parent instanceof MainContentPanel main) {
            main.setView(new CreateTransactionView(draft));
        }
    }

    /** Reubicaciones completadas cuyo destino es esta tarjeta: historial y total abonado. */
    private void addPayments(JPanel panel) {
        try {
            Map<Long, String> names = new HashMap<>();
            for (Account a : ClientUseCases.get(AccountOperations.class).getAllAccounts()) {
                names.put(a.getId(), a.getName());
            }
            List<Transaction> payments = new ArrayList<>();
            for (Transaction tx : ClientUseCases.get(TransactionOperations.class).getTransactionsByUserId(account.getUserId())) {
                if (tx.getOperationType() == OperationTypes.REALLOCATION
                        && tx.getStatus() == TransactionStatus.COMPLETED
                        && tx.getDestinationAccountId() != null
                        && tx.getDestinationAccountId() == account.getId()) {
                    payments.add(tx);
                }
            }
            payments.sort(Comparator.comparing(Transaction::getDate, Comparator.nullsLast(Comparator.reverseOrder())));

            BigDecimal total = BigDecimal.ZERO;
            for (Transaction tx : payments) {
                if (tx.getAmount() != null) {
                    total = total.add(tx.getAmount());
                }
                String date = tx.getDate() != null ? tx.getDate().toLocalDate().toString() : "—";
                String source = tx.getSourceAccountId() != null ? names.getOrDefault(tx.getSourceAccountId(), "—") : "—";
                panel.add(buildRow(date, String.format("$%,.2f  desde %s", tx.getAmount(), source)));
            }
            if (payments.isEmpty()) {
                panel.add(buildRow("Sin abonos", "Aún no hay abonos registrados"));
            } else {
                panel.add(buildRow("Total abonado", String.format("$%,.2f", total)));
            }
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al cargar los abonos: " + ex.getMessage());
        }
    }
}
