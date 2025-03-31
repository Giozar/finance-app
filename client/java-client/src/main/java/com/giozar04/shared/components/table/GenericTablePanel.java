package com.giozar04.shared.components.table;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class GenericTablePanel<T> extends JPanel {
    private final GenericTableModel<T> tableModel;
    private final JTable table;

    public GenericTablePanel(List<ColumnDefinition<T>> columns, List<T> data) {
        setLayout(new BorderLayout());
        tableModel = new GenericTableModel<>(columns, data);
        table = new JTable(tableModel);
        
        // Configurar renderizadores y editores de cada columna según la definición
        for (int i = 0; i < columns.size(); i++) {
            ColumnDefinition<T> colDef = columns.get(i);
            TableCellRenderer renderer = colDef.getRenderer();
            if (renderer != null) {
                table.getColumnModel().getColumn(i).setCellRenderer(renderer);
            }
            TableCellEditor editor = colDef.getEditor();
            if (editor != null) {
                table.getColumnModel().getColumn(i).setCellEditor(editor);
            }
        }
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void setData(List<T> data) {
        tableModel.setData(data);
    }

    public T getItemAt(int row) {
        return tableModel.getItemAt(row);
    }

    public JTable getTable() {
        return table;
    }
}
