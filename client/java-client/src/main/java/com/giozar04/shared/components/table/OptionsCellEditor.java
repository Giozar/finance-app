package com.giozar04.shared.components.table;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class OptionsCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final JButton button = new JButton("···");
    private final JPopupMenu popupMenu;
    private int currentRow;
    private final PopupMenuActionHandler actionHandler;


    public OptionsCellEditor(PopupMenuActionHandler actionHandler) {
        this.actionHandler = actionHandler;
        popupMenu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Editar");
        JMenuItem deleteItem = new JMenuItem("Eliminar");
        JMenuItem detailsItem = new JMenuItem("Ver Detalles");

        editItem.addActionListener((ActionEvent e) -> {
            getActionHandler().onEdit(currentRow);
            fireEditingStopped();
        });
        deleteItem.addActionListener((ActionEvent e) -> {
            getActionHandler().onDelete(currentRow);
            fireEditingStopped();
        });
        detailsItem.addActionListener((ActionEvent e) -> {
            getActionHandler().onViewDetails(currentRow);
            fireEditingStopped();
        });

        popupMenu.add(editItem);
        popupMenu.add(deleteItem);
        popupMenu.add(detailsItem);

        button.addActionListener(e -> {
            // Mostrar el menú emergente centrado en el botón
            popupMenu.show(button, button.getWidth() / 2, button.getHeight() / 2);
        });
    }

    public PopupMenuActionHandler getActionHandler() {
        return actionHandler;
    }

    @Override
    public Object getCellEditorValue() {
        return "···";
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        return true;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        currentRow = row;
        return button;
    }
}
