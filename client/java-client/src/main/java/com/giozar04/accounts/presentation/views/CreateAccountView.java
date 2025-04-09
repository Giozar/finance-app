package com.giozar04.accounts.presentation.views;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.giozar04.accounts.presentation.components.AccountFormPanel;
public class CreateAccountView extends JPanel {

    private final AccountFormPanel accountFormPanel;

    public CreateAccountView() {
        super(new BorderLayout());
        accountFormPanel = new AccountFormPanel();
        add(accountFormPanel, BorderLayout.CENTER);
    }
    
}
