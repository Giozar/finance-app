package com.giozar04.dashboard.presentation.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class HeaderPanel extends JPanel {

    public HeaderPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(getWidth(), 60));

        // Panel izquierdo: notificación + perfil
        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

        JButton btnNotifications = new JButton("🔔");
        btnNotifications.setPreferredSize(new Dimension(40, 40));

        JLabel profileCircle = new JLabel("U", SwingConstants.CENTER);
        profileCircle.setPreferredSize(new Dimension(40, 40));
        profileCircle.setOpaque(true);
        profileCircle.setBackground(Color.GRAY);
        profileCircle.setForeground(Color.WHITE);
        profileCircle.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

        leftPanel.add(btnNotifications);
        leftPanel.add(profileCircle);

        // Panel derecho: búsqueda
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        JTextField searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setToolTipText("Buscar...");
        rightPanel.add(searchField);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }
}
