package com.giozar04.dashboard.presentation.views;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
public class MainDashboardView extends JPanel {
    public MainDashboardView() {
        setLayout(new BorderLayout());
        setBackground(java.awt.Color.LIGHT_GRAY);

        JLabel titleLabel = new JLabel("Bienvenido al Dashboard", JLabel.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(24f));
        titleLabel.setForeground(java.awt.Color.DARK_GRAY);
        titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);
    }

}
