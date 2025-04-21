package com.giozar04.shared.components.forms;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ColorPickerField extends JPanel {

    private final JLabel label;
    private final JButton colorButton;
    private Color selectedColor;

    public ColorPickerField(String labelText, int width, int height) {
        setLayout(null);
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));

        label = new JLabel(labelText);
        label.setBounds(0, 10, 150, 20);

        colorButton = new JButton();
        colorButton.setBounds(160, 5, width - 160, height - 10);
        colorButton.setBackground(Color.WHITE);
        colorButton.setFocusPainted(false);
        colorButton.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        colorButton.addActionListener(this::openColorPicker);

        add(label);
        add(colorButton);
    }

    private void openColorPicker(ActionEvent e) {
        Color color = JColorChooser.showDialog(this, "Selecciona un color", selectedColor);
        if (color != null) {
            selectedColor = color;
            colorButton.setBackground(color);
        }
    }

    public void setColor(Color color) {
        this.selectedColor = color;
        if (color != null) {
            colorButton.setBackground(color);
        }
    }

    public String getColorHex() {
        if (selectedColor == null) return null;
        return String.format("#%02x%02x%02x", selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue());
    }

    public void clear() {
        selectedColor = null;
        colorButton.setBackground(Color.WHITE);
    }

    public boolean isColorSelected() {
        return selectedColor != null;
    }
}
