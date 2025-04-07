package com.giozar04.shared.components.forms;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class FormTextArea extends JPanel {
    private final JLabel label;
    private final JTextArea textArea;

    public FormTextArea(String labelText, int rows, int cols) {
        this.setLayout(new BorderLayout(5, 5));
        this.label = new JLabel(labelText);
        this.textArea = new JTextArea(rows, cols);
        JScrollPane scrollPane = new JScrollPane(textArea);

        this.add(label, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    public String getValue() {
        return textArea.getText();
    }

    public void setValue(String value) {
        textArea.setText(value);
    }

    public void clear() {
        textArea.setText("");
    }

    public JTextArea getTextArea() {
        return textArea;
    }
}
