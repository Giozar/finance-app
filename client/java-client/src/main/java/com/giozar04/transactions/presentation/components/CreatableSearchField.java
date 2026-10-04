package com.giozar04.transactions.presentation.components;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.giozar04.shared.components.forms.FormSearchComboBox;

/**
 * {@link FormSearchComboBox} con un botón "+ Nueva" a la derecha para el alta rápida
 * (p. ej. con {@code QuickCreateDialog}). El combo se usa directamente con {@link #getCombo()}.
 */
public class CreatableSearchField<T> extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int BUTTON_WIDTH = 95;

    private final FormSearchComboBox<T> combo;
    private final JButton createButton;
    private Runnable onCreateNew;

    public CreatableSearchField(String labelText, int width, int height) {
        setLayout(new BorderLayout(5, 0));
        setOpaque(false);

        combo = new FormSearchComboBox<>(labelText, width - BUTTON_WIDTH - 5, height);
        createButton = new JButton("+ Nueva");
        createButton.setPreferredSize(new Dimension(BUTTON_WIDTH, Math.min(height, 32)));
        createButton.addActionListener(e -> {
            if (onCreateNew != null) {
                onCreateNew.run();
            }
        });

        JPanel buttonHolder = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 4));
        buttonHolder.setOpaque(false);
        buttonHolder.add(createButton);

        add(combo, BorderLayout.CENTER);
        add(buttonHolder, BorderLayout.EAST);

        Dimension size = new Dimension(width, height);
        setPreferredSize(size);
        setMaximumSize(size);
    }

    public FormSearchComboBox<T> getCombo() {
        return combo;
    }

    /** Acción del botón "+ Nueva". */
    public void setOnCreateNew(Runnable action) {
        this.onCreateNew = action;
    }

    /** Habilita solo el botón "+ Nueva" (p. ej. cuando aún no hay usuario). */
    public void setCreateEnabled(boolean enabled) {
        createButton.setEnabled(enabled);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        combo.setEnabled(enabled);
        createButton.setEnabled(enabled);
    }
}
