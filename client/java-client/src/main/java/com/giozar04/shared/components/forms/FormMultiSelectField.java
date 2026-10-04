package com.giozar04.shared.components.forms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Selección múltiple: etiqueta + buscador ({@link FormSearchComboBox}) para añadir elementos y un
 * panel de "chips" con botón ✕ para quitarlos.
 *
 * <ul>
 *   <li>El buscador solo ofrece los elementos que aún no están seleccionados.</li>
 *   <li>Si se configura {@link #setOnCreateNew(Runnable)}, aparece el botón "+ Nueva" junto al buscador;
 *       tras crear el elemento, llame a {@link #addSelected(Object)} para añadirlo ya seleccionado.</li>
 *   <li>Si las entidades no implementan {@code equals}, use {@link #setIdentityFunction(Function)}
 *       (p. ej. {@code Tag::getId}).</li>
 * </ul>
 *
 * Uso:
 * <pre>
 *   FormMultiSelectField&lt;Tag&gt; tagsField = new FormMultiSelectField&lt;&gt;("Etiquetas:", 500, 40);
 *   tagsField.setPlaceholder("Busque una etiqueta...");
 *   tagsField.setIdentityFunction(Tag::getId);
 *   tagsField.setDisplayFunction(Tag::getName);
 *   tagsField.setItems(tags);
 *   tagsField.setOnCreateNew(() -&gt; openQuickCreateTag());
 *   List&lt;Tag&gt; selected = tagsField.getSelectedItems();
 * </pre>
 */
public class FormMultiSelectField<T> extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 40;
    private static final int LABEL_WIDTH = 150;
    private static final int CHIP_GAP = 6;

    private final FormSearchComboBox<T> searchCombo;
    private final JButton createButton;
    private final JPanel chipsPanel;
    private final int width;
    private final int rowHeight;

    private final List<T> allItems = new ArrayList<>();
    private final List<T> selectedItems = new ArrayList<>();
    private final List<ChangeListener> changeListeners = new ArrayList<>();

    private Function<T, String> displayFunction = item -> String.valueOf(item);
    private Function<T, ?> identityFunction;
    private Runnable onCreateNew;

    /** Evita reaccionar a los cambios internos del buscador. */
    private boolean updating;

    public FormMultiSelectField(String labelText) {
        this(labelText, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public FormMultiSelectField(String labelText, int width, int height) {
        this.width = width;
        this.rowHeight = height;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Fila superior: buscador + botón opcional "+ Nueva"
        searchCombo = new FormSearchComboBox<>(labelText, width, height);
        searchCombo.addActionListener(e -> handleSearchSelection());

        createButton = new JButton("+ Nueva");
        createButton.setVisible(false);
        createButton.addActionListener(e -> {
            if (onCreateNew != null) {
                onCreateNew.run();
            }
        });

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(LEFT_ALIGNMENT);
        searchRow.add(searchCombo);
        searchRow.add(createButton);

        // Chips (con salto de línea calculado para el ancho disponible)
        chipsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, CHIP_GAP, CHIP_GAP)) {
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(FormMultiSelectField.this.width, computeChipsHeight());
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        chipsPanel.setOpaque(false);
        chipsPanel.setAlignmentX(LEFT_ALIGNMENT);
        chipsPanel.setBorder(new EmptyBorder(0, LABEL_WIDTH, 0, 0));

        Component spacer = Box.createRigidArea(new Dimension(0, 4));
        ((JComponent) spacer).setAlignmentX(LEFT_ALIGNMENT);

        add(searchRow);
        add(spacer);
        add(chipsPanel);

        refreshView();
    }

    /** No se estira más allá de su tamaño preferido (igual que los demás campos de formulario). */
    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    // ------------------------------------------------------------------
    // API pública
    // ------------------------------------------------------------------

    public void setPlaceholder(String placeholder) {
        searchCombo.setPlaceholder(placeholder);
    }

    /** Texto con el que se muestran y buscan los elementos (por defecto {@code toString()}). */
    public void setDisplayFunction(Function<T, String> displayFunction) {
        this.displayFunction = displayFunction != null ? displayFunction : item -> String.valueOf(item);
        searchCombo.setDisplayFunction(this.displayFunction);
        refreshView();
    }

    /** Clave de identidad de los elementos (p. ej. el id); por defecto se usa {@code equals}. */
    public void setIdentityFunction(Function<T, ?> identityFunction) {
        this.identityFunction = identityFunction;
        searchCombo.setIdentityFunction(identityFunction);
    }

    /** Reemplaza el catálogo; conserva los seleccionados que sigan existiendo en él. */
    public void setItems(List<T> items) {
        allItems.clear();
        if (items != null) {
            allItems.addAll(items);
        }
        List<T> kept = new ArrayList<>();
        for (T selected : selectedItems) {
            T match = findIn(allItems, selected);
            if (match != null) {
                kept.add(match);
            }
        }
        boolean changed = kept.size() != selectedItems.size();
        selectedItems.clear();
        selectedItems.addAll(kept);
        refreshView();
        if (changed) {
            fireChange();
        }
    }

    public List<T> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    /** Selecciona los elementos indicados (se usan las instancias del catálogo si existen). */
    public void setSelectedItems(List<T> items) {
        selectedItems.clear();
        if (items != null) {
            for (T item : items) {
                T match = findIn(allItems, item);
                T toAdd = match != null ? match : item;
                if (toAdd != null && findIn(selectedItems, toAdd) == null) {
                    selectedItems.add(toAdd);
                }
            }
        }
        refreshView();
        fireChange();
    }

    /** Añade un elemento (p. ej. recién creado) al catálogo si no existe y lo deja seleccionado. */
    public void addSelected(T item) {
        if (item == null) {
            return;
        }
        T match = findIn(allItems, item);
        if (match == null) {
            allItems.add(item);
            match = item;
        }
        if (findIn(selectedItems, match) == null) {
            selectedItems.add(match);
            refreshView();
            fireChange();
        } else {
            refreshView();
        }
    }

    /** Quita todas las selecciones. */
    public void clear() {
        boolean changed = !selectedItems.isEmpty();
        selectedItems.clear();
        refreshView();
        if (changed) {
            fireChange();
        }
    }

    /** Muestra el botón "+ Nueva" que ejecuta {@code action}; con {@code null} se oculta. */
    public void setOnCreateNew(Runnable action) {
        setOnCreateNew("+ Nueva", action);
    }

    /** Igual que {@link #setOnCreateNew(Runnable)} con un texto de botón propio. */
    public void setOnCreateNew(String buttonText, Runnable action) {
        this.onCreateNew = action;
        createButton.setText(buttonText);
        createButton.setVisible(action != null);
        revalidate();
    }

    /** Se notifica cada vez que cambia la lista de seleccionados. */
    public void addChangeListener(ChangeListener listener) {
        if (listener != null) {
            changeListeners.add(listener);
        }
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        searchCombo.setEnabled(enabled);
        createButton.setEnabled(enabled);
        refreshChips();
    }

    // ------------------------------------------------------------------
    // Lógica interna
    // ------------------------------------------------------------------

    private void handleSearchSelection() {
        if (updating) {
            return;
        }
        T item = searchCombo.getSelectedItem();
        if (item == null) {
            return;
        }
        if (findIn(selectedItems, item) == null) {
            selectedItems.add(item);
        }
        refreshView();
        fireChange();
    }

    private void remove(T item) {
        T match = findIn(selectedItems, item);
        if (match != null) {
            selectedItems.remove(match);
            refreshView();
            fireChange();
        }
    }

    /** El buscador ofrece solo los no seleccionados; los chips muestran los seleccionados. */
    private void refreshView() {
        List<T> available = new ArrayList<>();
        for (T item : allItems) {
            if (findIn(selectedItems, item) == null) {
                available.add(item);
            }
        }
        updating = true;
        try {
            searchCombo.setItems(available);
        } finally {
            updating = false;
        }
        refreshChips();
    }

    private void refreshChips() {
        chipsPanel.removeAll();
        for (T item : selectedItems) {
            chipsPanel.add(buildChip(item));
        }
        chipsPanel.revalidate();
        chipsPanel.repaint();
        revalidate();
    }

    /** "Chip" con el estilo de las tarjetas vinculadas de la vista de detalle de wallet. */
    private JPanel buildChip(T item) {
        JPanel chip = new JPanel();
        chip.setLayout(new BoxLayout(chip, BoxLayout.X_AXIS));
        chip.setOpaque(true);
        chip.setBackground(new Color(240, 240, 248));
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 225), 1, true),
                new EmptyBorder(2, 8, 2, 4)));

        String text = displayFunction.apply(item);
        JLabel name = new JLabel(text != null ? text : "");
        name.setFont(new Font("SansSerif", Font.PLAIN, 12));
        name.setForeground(new Color(40, 40, 60));

        JButton removeButton = new JButton("✕");
        removeButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
        removeButton.setMargin(new Insets(0, 4, 0, 4));
        removeButton.setBorderPainted(false);
        removeButton.setContentAreaFilled(false);
        removeButton.setFocusable(false);
        removeButton.setToolTipText("Quitar");
        removeButton.setEnabled(isEnabled());
        removeButton.addActionListener(e -> remove(item));

        chip.add(name);
        chip.add(Box.createRigidArea(new Dimension(4, 0)));
        chip.add(removeButton);
        return chip;
    }

    /** Alto necesario para los chips con salto de línea dentro del ancho disponible. */
    private int computeChipsHeight() {
        int available = Math.max(1, width - LABEL_WIDTH - CHIP_GAP);
        int lineWidth = 0;
        int lineHeight = 0;
        int total = CHIP_GAP;
        for (Component chip : chipsPanel.getComponents()) {
            Dimension d = chip.getPreferredSize();
            if (lineWidth > 0 && lineWidth + d.width + CHIP_GAP > available) {
                total += lineHeight + CHIP_GAP;
                lineWidth = 0;
                lineHeight = 0;
            }
            lineWidth += d.width + CHIP_GAP;
            lineHeight = Math.max(lineHeight, d.height);
        }
        if (lineHeight > 0) {
            total += lineHeight + CHIP_GAP;
        }
        return Math.max(total, rowHeight / 2);
    }

    private T findIn(List<T> list, T item) {
        if (item == null) {
            return null;
        }
        for (T candidate : list) {
            if (sameItem(candidate, item)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean sameItem(T a, T b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (identityFunction != null) {
            return Objects.equals(identityFunction.apply(a), identityFunction.apply(b));
        }
        return a.equals(b);
    }

    private void fireChange() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : new ArrayList<>(changeListeners)) {
            listener.stateChanged(event);
        }
    }
}
