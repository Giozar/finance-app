package com.giozar04.shared.components.table;

import java.util.function.Function;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ColumnDefinition<T> {
    private final String header;
    private final Function<T, Object> valueExtractor;
    private TableCellRenderer renderer;
    private TableCellEditor editor;

    public ColumnDefinition(String header, Function<T, Object> valueExtractor) {
        this.header = header;
        this.valueExtractor = valueExtractor;
    }

    public String getHeader() {
        return header;
    }

    public Function<T, Object> getValueExtractor() {
        return valueExtractor;
    }

    public TableCellRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(TableCellRenderer renderer) {
        this.renderer = renderer;
    }

    public TableCellEditor getEditor() {
        return editor;
    }

    public void setEditor(TableCellEditor editor) {
        this.editor = editor;
    }
}
