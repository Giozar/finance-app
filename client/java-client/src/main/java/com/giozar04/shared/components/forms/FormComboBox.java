package com.giozar04.shared.components.forms;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FormComboBox<T> extends JPanel {

    private final JLabel label;
    private final JComboBox<T> comboBox;
    private final DefaultComboBoxModel<T> model;

    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 40;

    private String placeholder;

    public FormComboBox(String labelText) {
        this(labelText, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public FormComboBox(String labelText, int width, int height) {
        setLayout(new BorderLayout(5, 5));

        label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(150, 25));

        model = new DefaultComboBoxModel<>();
        comboBox = new JComboBox<>(model);
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        comboBox.setPreferredSize(new Dimension(width - 150, height));

        add(label, BorderLayout.WEST);
        add(comboBox, BorderLayout.CENTER);

        Dimension size = new Dimension(width, height);
        setMaximumSize(size);
        setPreferredSize(size);
    }

    @SuppressWarnings("unchecked")
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        model.removeAllElements();
        model.addElement((T) placeholder);
        comboBox.setSelectedIndex(0);
    }

    @SuppressWarnings("unchecked")
    public void setItems(List<T> items) {
        model.removeAllElements();
        if (placeholder != null) {
            model.addElement((T) placeholder);
        }
        for (T item : items) {
            model.addElement(item);
        }
        comboBox.setSelectedIndex(placeholder != null ? 0 : -1);
    }

    public void setSelectedItem(T item) {
        if (item != null) {
            comboBox.setSelectedItem(item);
        } else if (placeholder != null) {
            comboBox.setSelectedIndex(0);
        } else {
            comboBox.setSelectedIndex(-1);
        }
    }

    @SuppressWarnings("unchecked")
    public T getSelectedItem() {
        Object selected = comboBox.getSelectedItem();
        if (placeholder != null && placeholder.equals(selected)) {
            return null;
        }
        try {
            return (T) selected;
        } catch (ClassCastException e) {
            return null;
        }
    }

    public boolean isSelectionValid() {
        return getSelectedItem() != null;
    }

    public void clearSelection() {
        comboBox.setSelectedIndex(placeholder != null ? 0 : -1);
    }

    public JComboBox<T> getComboBox() {
        return comboBox;
    }

    public int getItemCount() {
        int count = comboBox.getItemCount();
        if (placeholder != null && count > 0) {
            return count - 1;
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    public T getItemAt(int index) {
        int realIndex = placeholder != null ? index + 1 : index;
        if (realIndex >= 0 && realIndex < comboBox.getItemCount()) {
            Object item = comboBox.getItemAt(realIndex);
            if (placeholder != null && placeholder.equals(item)) {
                return null;
            }
            try {
                return (T) item;
            } catch (ClassCastException e) {
                return null;
            }
        }
        return null;
    }

    // permite que setVisible afecte correctamente al panel
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        label.setVisible(visible);
        comboBox.setVisible(visible);
    }

    // selecciona por índice (respetando placeholder)
    public void setSelectedIndex(int index) {
        if (placeholder != null) {
            index += 1; // compensar el índice real
        }
        comboBox.setSelectedIndex(index);
    }

    // obtener índice visible actual (sin placeholder)
    public int getSelectedIndex() {
        int index = comboBox.getSelectedIndex();
        return placeholder != null ? index - 1 : index;
    }

    public void addActionListener(java.awt.event.ActionListener listener) {
        comboBox.addActionListener(listener);
    }
    
}
