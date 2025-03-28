package com.giozar04.transactions.presentation.views;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;


public class TransactionsView extends JPanel {
    public TransactionsView() {
        setLayout(new BorderLayout());
        add(new JLabel("Transacciones"), BorderLayout.CENTER);
    }
}
