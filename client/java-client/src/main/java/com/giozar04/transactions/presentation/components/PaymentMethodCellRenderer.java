package com.giozar04.transactions.presentation.components;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderizador para "Método de Pago", simulando chips.
 */
public class PaymentMethodCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = new JLabel(value.toString());
        label.setOpaque(true);
        label.setBorder(new EmptyBorder(5, 10, 5, 10));
        // Asignar color de fondo según el método de pago
        switch (value.toString().toUpperCase()) {
            case "TARJETA" -> label.setBackground(new Color(220, 230, 241));
            case "EFECTIVO" -> label.setBackground(new Color(220, 241, 229));
            case "TRANSFERENCIA" -> label.setBackground(new Color(241, 220, 220));
            case "CÓDIGO QR", "QR" -> label.setBackground(new Color(241, 237, 220));
            case "CODI" -> label.setBackground(new Color(241, 241, 220));
            case "BILLETERA", "WALLET" -> label.setBackground(new Color(220, 241, 241));
            default -> label.setBackground(Color.LIGHT_GRAY);
        }
        label.setHorizontalAlignment(SwingConstants.CENTER);
        if (isSelected) {
            label.setBackground(table.getSelectionBackground());
            label.setForeground(table.getSelectionForeground());
        }
        return label;
    }
}
