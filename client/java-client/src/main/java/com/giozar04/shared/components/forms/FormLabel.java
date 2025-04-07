package com.giozar04.shared.components.forms;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FormLabel extends JPanel {

    public FormLabel(String labelText, JComponent component) {
        this.setLayout(new BorderLayout(5, 5));
        this.add(new JLabel(labelText), BorderLayout.WEST);
        this.add(component, BorderLayout.CENTER);
    }
}
