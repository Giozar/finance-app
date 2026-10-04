package com.giozar04.shared.components.forms;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Texto de ayuda pequeño (gris, cursiva) alineado con la columna de los campos
 * ({@link FormField}, {@link FormComboBox}: etiqueta de 150 px).
 *
 * Uso:
 * <pre>
 *   FormHelpText help = new FormHelpText("Se modifica con transacciones o desde Conciliación.", 400);
 *   formPanel.add(balanceField);
 *   formPanel.add(help);
 *   help.setVisible(isEditing);
 * </pre>
 */
public class FormHelpText extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int LABEL_COLUMN = 155;
    private static final int HEIGHT = 32;

    private final JLabel textLabel;
    private final int textWidth;

    /**
     * @param text  texto de ayuda (se ajusta en varias líneas si no cabe)
     * @param width ancho total, igual al del campo al que acompaña
     */
    public FormHelpText(String text, int width) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(0, LABEL_COLUMN, 0, 0));
        this.textWidth = Math.max(50, width - LABEL_COLUMN - 10);

        textLabel = new JLabel();
        textLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        textLabel.setForeground(new Color(130, 130, 145));
        add(textLabel);
        setText(text);

        Dimension size = new Dimension(width, HEIGHT);
        setPreferredSize(size);
        setMaximumSize(size);
    }

    public void setText(String text) {
        textLabel.setText(text == null ? ""
                : "<html><div style='width:" + textWidth + "px'>" + text + "</div></html>");
    }
}
