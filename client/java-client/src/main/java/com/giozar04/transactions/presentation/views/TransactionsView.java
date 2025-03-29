package com.giozar04.transactions.presentation.views;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.giozar04.transactions.application.services.TransactionService;


public class TransactionsView extends JPanel {
    public TransactionsView() {
        setLayout(new BorderLayout());
        add(new JLabel("Transacciones"), BorderLayout.CENTER);

        TransactionService transactionService = TransactionService.getInstance();

        try {
            transactionService.getAllTransactions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
