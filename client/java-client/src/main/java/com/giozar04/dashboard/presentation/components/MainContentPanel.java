package com.giozar04.dashboard.presentation.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JPanel;

public class MainContentPanel extends JPanel {

    public MainContentPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.LIGHT_GRAY);
    }

    public void setView(Component view) {
        removeAll();
        add(view, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
