package com.giozar04.shared.components.table;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class GenericTableModel<T> extends AbstractTableModel {

    private final List<ColumnDefinition<T>> columns;
    private List<T> data;

    public GenericTableModel(List<ColumnDefinition<T>> columns, List<T> data) {
        this.columns = columns;
        this.data = data;
    }

    public void setData(List<T> data) {
        this.data = data;
        fireTableDataChanged();
    }

    public T getItemAt(int row) {
        return data.get(row);
    }

    @Override
    public int getRowCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column).getHeader();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        T item = data.get(rowIndex);
        return columns.get(columnIndex).getValueExtractor().apply(item);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columns.get(columnIndex).getEditor() != null;
    }
}
