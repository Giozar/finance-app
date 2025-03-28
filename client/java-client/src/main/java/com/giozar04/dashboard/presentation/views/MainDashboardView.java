package com.giozar04.dashboard.presentation.views;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.giozar04.transactions.presentation.components.TransactionFormPanel;


public class MainDashboardView extends JFrame {
    public MainDashboardView () {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 650);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        
        JPanel panel1 = new JPanel();
        JLabel label1 = new JLabel("Inicio");
        panel1.add(label1);
        tabs.addTab("Inicio", panel1);
        
        JPanel transactionsPanel = new JPanel();
        TransactionFormPanel transactionFormPanel = new TransactionFormPanel();
        transactionsPanel.add(transactionFormPanel);
        tabs.addTab("Transacciones", transactionsPanel);
        
        getContentPane().add(tabs);
        setContentPane(tabs);
    }
}
