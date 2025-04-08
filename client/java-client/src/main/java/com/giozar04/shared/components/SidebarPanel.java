package com.giozar04.shared.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class SidebarPanel extends JPanel {

    public SidebarPanel(Consumer<String> onMenuSelected) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(180, 0));
        setBackground(Color.WHITE);

        // Borde con sombra elegante
        Border outer = BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY);
        Border inner = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        setBorder(BorderFactory.createCompoundBorder(outer, inner));

        JLabel lblTitle = new JLabel("Dashboard");
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lblTitle);
        add(Box.createVerticalStrut(20));

        String[] menuItems = {"Inicio", "Usuarios", "Clientes", "Cuentas", "Transacciones"};
        for (String item : menuItems) {
            JButton button = new JButton(item);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.addActionListener(e -> onMenuSelected.accept(item));
            add(button);
            add(Box.createVerticalStrut(10));
        }
    }
}
