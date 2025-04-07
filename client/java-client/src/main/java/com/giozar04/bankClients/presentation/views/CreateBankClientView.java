package com.giozar04.bankClients.presentation.views;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.giozar04.bankClients.presentation.components.BankClientFormPanel;

public class CreateBankClientView extends JPanel {

    private final BankClientFormPanel formPanel;

    public CreateBankClientView() {
        super(new BorderLayout());
        formPanel = new BankClientFormPanel();
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(formPanel, BorderLayout.CENTER);
    }
}
