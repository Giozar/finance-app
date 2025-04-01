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

    public FormField(String labelText) {
        this(labelText, false);
    }

    public FormField(String labelText, boolean isPassword) {
        setLayout(new BorderLayout(5, 5));

        label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(150, 25));

        if (isPassword) {
            textField = new JPasswordField(20);
        } else {
            textField = new JTextField(20);
        }

        add(label, BorderLayout.WEST);
        add(textField, BorderLayout.CENTER);
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
