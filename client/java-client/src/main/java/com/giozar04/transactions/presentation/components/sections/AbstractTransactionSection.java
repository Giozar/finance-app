package com.giozar04.transactions.presentation.components.sections;

import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.transactions.presentation.form.TransactionFormContext;
import com.giozar04.transactions.presentation.form.TransactionFormDataProvider;
import com.giozar04.transactions.presentation.form.TransactionFormSection;

/**
 * Base visual de las secciones del formulario de transacciones: panel con título, campos apilados
 * ({@code BoxLayout.Y_AXIS}) y utilidades comunes. La lógica de cada sección vive en su subclase.
 */
public abstract class AbstractTransactionSection extends JPanel implements TransactionFormSection {

    protected static final int FIELD_WIDTH = 520;
    protected static final int FIELD_HEIGHT = 40;
    private static final int ROW_GAP = 8;

    protected final TransactionFormContext context;
    protected final TransactionFormDataProvider provider;

    /** Separador que acompaña a cada fila, para ocultarlo junto con ella. */
    private final Map<Component, Component> spacers = new HashMap<>();

    protected AbstractTransactionSection(String title, TransactionFormContext context,
                                         TransactionFormDataProvider provider) {
        this.context = context;
        this.provider = provider;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                new EmptyBorder(5, 10, 10, 10)));
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    /** Añade una fila alineada a la izquierda seguida de un separador. */
    protected void addRow(JComponent row) {
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(row);
        Component spacer = Box.createRigidArea(new Dimension(0, ROW_GAP));
        add(spacer);
        spacers.put(row, spacer);
    }

    /** Muestra u oculta una fila y su separador. */
    protected void setRowVisible(JComponent row, boolean visible) {
        row.setVisible(visible);
        Component spacer = spacers.get(row);
        if (spacer != null) {
            spacer.setVisible(visible);
        }
    }

    /** Selecciona en un {@link FormComboBox} el primer elemento que cumpla {@code match}; si no hay, limpia. */
    protected static <T> void selectInCombo(FormComboBox<T> combo, Predicate<T> match) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            T item = combo.getItemAt(i);
            if (item != null && match.test(item)) {
                combo.setSelectedItem(item);
                return;
            }
        }
        combo.clearSelection();
    }

    /** Texto vacío o en blanco → null; si no, recortado. */
    protected static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** Revalida y repinta tras mostrar u ocultar filas. */
    protected void refreshLayout() {
        revalidate();
        repaint();
    }
}
