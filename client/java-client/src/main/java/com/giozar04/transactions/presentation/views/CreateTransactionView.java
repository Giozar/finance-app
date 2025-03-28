package com.giozar04.transactions.presentation.views;

import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import com.giozar04.transactions.presentation.components.TransactionFormPanel;

public class CreateTransactionView extends JPanel {

    private final TransactionFormPanel transactionFormPanel;

    public CreateTransactionView() {
        super(new BorderLayout());
        transactionFormPanel = new TransactionFormPanel();
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(transactionFormPanel, BorderLayout.CENTER);
    }

    public TransactionFormPanel getFormPanel() {
        return transactionFormPanel;
    }
}
