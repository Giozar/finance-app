package com.giozar04.shared.components.forms;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class FormField extends JPanel {

    private final JLabel label;
    private final JTextField textField;

    // Valores por defecto
    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 40;

    // Constructor básico (usa ancho y alto por defecto)
    public FormField(String labelText) {
        this(labelText, false, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    // Constructor con password (ancho y alto por defecto)
    public FormField(String labelText, boolean isPassword) {
        this(labelText, isPassword, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    // Constructor con ancho y alto personalizados
    public FormField(String labelText, boolean isPassword, int width, int height) {
        setLayout(new BorderLayout(5, 5));

        label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(150, 25)); // ancho fijo para etiquetas

        if (isPassword) {
            textField = new JPasswordField(20);
        } else {
            textField = new JTextField(20);
        }

        add(label, BorderLayout.WEST);
        add(textField, BorderLayout.CENTER);

        Dimension size = new Dimension(width, height);
        setMaximumSize(size);
        setPreferredSize(size);
    }

    public String getValue() {
        return textField.getText();
    }

    public void setValue(String value) {
        textField.setText(value);
    }

    public void clear() {
        textField.setText("");
    }

    public JTextField getTextField() {
        return textField;
    }
}
