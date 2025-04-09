package com.giozar04.users.presentation.views;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.giozar04.users.presentation.components.UserFormPanel;

public class CreateUserView extends JPanel {

    private final UserFormPanel userFormPanel;

    public CreateUserView() {
        super(new BorderLayout());
        userFormPanel = new UserFormPanel();
        add(userFormPanel, BorderLayout.CENTER);
    }
}
