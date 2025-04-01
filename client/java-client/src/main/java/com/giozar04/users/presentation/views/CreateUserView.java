package com.giozar04.users.presentation.views;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.giozar04.users.presentation.components.UserFormPanel;

public class CreateUserView extends JPanel {

    private final UserFormPanel userFormPanel;

    public CreateUserView() {
        super(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        userFormPanel = new UserFormPanel();
        add(userFormPanel, BorderLayout.CENTER);
    }
}
