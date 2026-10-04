package com.giozar04.transactions.presentation.components;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.giozar04.transactions.domain.enums.OperationTypes;

/**
 * Renderizador personalizado para la columna "Tipo" con colores diferenciados.
 */
public class TransactionTypeCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String type = value != null ? value.toString() : "";
        if (matches(type, OperationTypes.INCOME)) {
            c.setBackground(new Color(198, 239, 206)); // Verde claro
            c.setForeground(new Color(0, 97, 0));
        } else if (matches(type, OperationTypes.EXPENSE)) {
            c.setBackground(new Color(255, 199, 206)); // Rojo claro
            c.setForeground(new Color(156, 0, 6));
        } else if (matches(type, OperationTypes.REALLOCATION)) {
            c.setBackground(new Color(204, 224, 255)); // Azul claro
            c.setForeground(new Color(0, 51, 153));
        } else {
            c.setBackground(Color.WHITE);
            c.setForeground(Color.BLACK);
        }
        if (isSelected) {
            c.setBackground(table.getSelectionBackground());
            c.setForeground(table.getSelectionForeground());
        }
        return c;
    }

    /** Reconoce tanto el nombre del enum como su etiqueta. */
    private static boolean matches(String text, OperationTypes type) {
        return text.equalsIgnoreCase(type.name()) || text.equalsIgnoreCase(type.getLabel());
    }
}
