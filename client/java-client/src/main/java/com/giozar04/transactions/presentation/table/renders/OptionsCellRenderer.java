package com.giozar04.transactions.presentation.table.renders;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderizador para la columna de "Opciones".
 */
public class OptionsCellRenderer extends DefaultTableCellRenderer {

    private final JButton button = new JButton("···");

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        return button;
    }
}
