package com.giozar04.shared.components.forms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.LayerUI;

/**
 * Combo con búsqueda: etiqueta + combo editable que filtra la lista mientras se escribe.
 *
 * <p>Mismo API que {@link FormComboBox} ({@code setPlaceholder}, {@code setItems},
 * {@code getSelectedItem}, {@code setSelectedItem}, {@code isSelectionValid},
 * {@code clearSelection}, {@code addActionListener}, {@code setEnabled}), sin modificarlo.</p>
 *
 * <ul>
 *   <li>El texto mostrado de cada elemento sale de {@code toString()} o de la función indicada en
 *       {@link #setDisplayFunction(Function)}.</li>
 *   <li>Los {@code ActionListener} se disparan solo cuando la selección confirmada cambia
 *       (clic, Enter, coincidencia única al salir del campo o cambio programático), nunca durante
 *       el filtrado.</li>
 *   <li>El placeholder no es un elemento de la lista: se dibuja como texto guía cuando no hay selección.</li>
 *   <li>Si las entidades no implementan {@code equals}, use {@link #setIdentityFunction(Function)}
 *       (p. ej. {@code User::getId}) para que {@link #setSelectedItem(Object)} encuentre el elemento.</li>
 * </ul>
 *
 * Uso:
 * <pre>
 *   FormSearchComboBox&lt;Category&gt; combo = new FormSearchComboBox&lt;&gt;("Categoría:", 400, 40);
 *   combo.setPlaceholder("Busque una categoría...");
 *   combo.setDisplayFunction(c -&gt; c.getIcon() + " " + c.getName());
 *   combo.setIdentityFunction(Category::getId);
 *   combo.setItems(categories);
 *   combo.addActionListener(e -&gt; onCategoryChanged(combo.getSelectedItem()));
 * </pre>
 */
public class FormSearchComboBox<T> extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 40;

    private final JLabel label;
    private final JComboBox<Option<T>> comboBox;
    private final JLayer<JComboBox<Option<T>>> layer;
    private final JTextField editorField;
    private final FilterModel model = new FilterModel();

    private final List<T> allItems = new ArrayList<>();
    private final List<Option<T>> allOptions = new ArrayList<>();
    private final List<ActionListener> selectionListeners = new ArrayList<>();

    private Function<T, String> displayFunction = item -> String.valueOf(item);
    private Function<T, ?> identityFunction;
    private String placeholder;
    private T selectedItem;

    /** Evita reaccionar a los cambios internos de modelo y texto. */
    private boolean adjusting;

    public FormSearchComboBox(String labelText) {
        this(labelText, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public FormSearchComboBox(String labelText, int width, int height) {
        setLayout(new BorderLayout(5, 5));

        label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(150, 25));

        comboBox = new JComboBox<>(model);
        comboBox.setEditable(true);
        // Las flechas solo mueven el resaltado de la lista; Enter o clic confirman la selección
        comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        comboBox.setPreferredSize(new Dimension(width - 150, height));

        editorField = (JTextField) comboBox.getEditor().getEditorComponent();
        layer = new JLayer<>(comboBox, new PlaceholderLayerUI());

        add(label, BorderLayout.WEST);
        add(layer, BorderLayout.CENTER);

        Dimension size = new Dimension(width, height);
        setMaximumSize(size);
        setPreferredSize(size);

        installListeners();
    }

    // ------------------------------------------------------------------
    // API pública
    // ------------------------------------------------------------------

    /** Texto guía que se muestra cuando no hay selección. */
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        layer.repaint();
    }

    /** Define el texto con el que se muestra y se busca cada elemento (por defecto {@code toString()}). */
    public void setDisplayFunction(Function<T, String> displayFunction) {
        this.displayFunction = displayFunction != null ? displayFunction : item -> String.valueOf(item);
        rebuildOptions();
        syncView();
    }

    /** Define la clave de identidad de los elementos (p. ej. el id); por defecto se usa {@code equals}. */
    public void setIdentityFunction(Function<T, ?> identityFunction) {
        this.identityFunction = identityFunction;
    }

    /** Reemplaza la lista de elementos y limpia la selección (igual que {@link FormComboBox#setItems}). */
    public void setItems(List<T> items) {
        allItems.clear();
        if (items != null) {
            allItems.addAll(items);
        }
        rebuildOptions();
        updateSelection(null);
    }

    /** Elementos cargados (sin filtrar). */
    public List<T> getItems() {
        return Collections.unmodifiableList(allItems);
    }

    public int getItemCount() {
        return allItems.size();
    }

    public T getItemAt(int index) {
        return index >= 0 && index < allItems.size() ? allItems.get(index) : null;
    }

    public T getSelectedItem() {
        return selectedItem;
    }

    /** Selecciona el elemento de la lista equivalente a {@code item}; con {@code null} limpia la selección. */
    public void setSelectedItem(T item) {
        Option<T> option = findOption(item);
        updateSelection(option != null ? option.value : null);
    }

    public boolean isSelectionValid() {
        return selectedItem != null;
    }

    public void clearSelection() {
        updateSelection(null);
    }

    /** Se notifica solo cuando cambia la selección confirmada (nunca durante el filtrado). */
    public void addActionListener(ActionListener listener) {
        if (listener != null) {
            selectionListeners.add(listener);
        }
    }

    public void removeActionListener(ActionListener listener) {
        selectionListeners.remove(listener);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        label.setEnabled(enabled);
        comboBox.setEnabled(enabled);
    }

    // ------------------------------------------------------------------
    // Listeners internos
    // ------------------------------------------------------------------

    private void installListeners() {
        // Selección desde la lista, Enter o texto confirmado por el combo
        comboBox.addActionListener(e -> {
            if (!adjusting) {
                commitFromModel();
            }
        });

        // Filtrado mientras se escribe (diferido: no se puede mutar el documento durante su notificación)
        editorField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { scheduleFilter(); }
            @Override public void removeUpdate(DocumentEvent e)  { scheduleFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { /* sin cambios de texto */ }
        });

        // Al salir del campo, si el texto no corresponde a la selección, se restaura
        editorField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                layer.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    Option<T> current = findOption(selectedItem);
                    String expected = current != null ? current.display : "";
                    if (!editorField.getText().equals(expected) || model.getSize() != allOptions.size()) {
                        syncView();
                    }
                    layer.repaint();
                });
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void commitFromModel() {
        Object raw = model.getSelectedItem();
        Option<T> option = null;
        if (raw instanceof Option) {
            option = (Option<T>) raw;
        } else if (raw instanceof String text) {
            option = resolveText(text);
        }
        updateSelection(option != null ? option.value : null);
    }

    /** Resuelve un texto escrito: coincidencia exacta, resultado único del filtro o la selección previa. */
    private Option<T> resolveText(String text) {
        String query = text.trim();
        if (query.isEmpty()) {
            return null;
        }
        for (Option<T> option : allOptions) {
            if (option.display.equalsIgnoreCase(query)) {
                return option;
            }
        }
        if (model.getSize() == 1) {
            return model.getElementAt(0);
        }
        return findOption(selectedItem);
    }

    private void scheduleFilter() {
        if (!adjusting) {
            SwingUtilities.invokeLater(this::applyFilter);
        }
    }

    private void applyFilter() {
        if (adjusting) {
            return;
        }
        String text = editorField.getText();
        Option<T> current = findOption(selectedItem);
        if (current != null && current.display.equals(text)) {
            return; // el texto es el de la selección actual: no hay búsqueda en curso
        }

        String query = text.trim().toLowerCase(Locale.ROOT);
        List<Option<T>> filtered = new ArrayList<>();
        for (Option<T> option : allOptions) {
            if (query.isEmpty() || option.display.toLowerCase(Locale.ROOT).contains(query)) {
                filtered.add(option);
            }
        }

        int caret = editorField.getCaretPosition();
        adjusting = true;
        try {
            model.setVisibleOptions(filtered);
            // El cambio de modelo reconfigura el editor: se restaura lo escrito por el usuario
            if (!editorField.getText().equals(text)) {
                editorField.setText(text);
            }
            editorField.setCaretPosition(Math.min(caret, text.length()));
        } finally {
            adjusting = false;
        }

        if (editorField.isFocusOwner() && comboBox.isShowing()) {
            comboBox.hidePopup(); // recalcula el alto del popup
            if (!filtered.isEmpty()) {
                comboBox.showPopup();
            }
        }
    }

    // ------------------------------------------------------------------
    // Estado
    // ------------------------------------------------------------------

    private void updateSelection(T item) {
        T previous = selectedItem;
        selectedItem = item;
        syncView();
        if (!sameItem(previous, item)) {
            fireSelectionChanged();
        }
    }

    /** Muestra todos los elementos, marca la selección confirmada y su texto en el editor. */
    private void syncView() {
        Option<T> option = findOption(selectedItem);
        adjusting = true;
        try {
            model.setVisibleOptions(allOptions);
            model.setSelectedItem(option);
            editorField.setText(option != null ? option.display : "");
        } finally {
            adjusting = false;
        }
        layer.repaint();
    }

    private void rebuildOptions() {
        allOptions.clear();
        for (T item : allItems) {
            String display = displayFunction.apply(item);
            allOptions.add(new Option<>(item, display != null ? display : ""));
        }
        // Mantener la selección si sigue existiendo en la nueva lista
        Option<T> option = findOption(selectedItem);
        selectedItem = option != null ? option.value : null;
    }

    private Option<T> findOption(T item) {
        if (item == null) {
            return null;
        }
        for (Option<T> option : allOptions) {
            if (sameItem(option.value, item)) {
                return option;
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

    private void fireSelectionChanged() {
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "selectionChanged");
        for (ActionListener listener : new ArrayList<>(selectionListeners)) {
            listener.actionPerformed(event);
        }
    }

    // ------------------------------------------------------------------
    // Clases internas
    // ------------------------------------------------------------------

    /** Envoltorio de un elemento con su texto visible (lo usan el renderer y el editor del combo). */
    private static final class Option<V> {
        private final V value;
        private final String display;

        private Option(V value, String display) {
            this.value = value;
            this.display = display;
        }

        @Override
        public String toString() {
            return display;
        }
    }

    /** Modelo con la lista filtrada; no autoselecciona elementos al cambiar su contenido. */
    private final class FilterModel extends AbstractListModel<Option<T>> implements ComboBoxModel<Option<T>> {

        private static final long serialVersionUID = 1L;

        private List<Option<T>> visible = new ArrayList<>();
        private Object selected;

        void setVisibleOptions(List<Option<T>> options) {
            int previousSize = visible.size();
            visible = new ArrayList<>(options);
            int last = Math.max(previousSize, visible.size()) - 1;
            if (last >= 0) {
                fireContentsChanged(this, 0, last);
            }
        }

        @Override
        public int getSize() {
            return visible.size();
        }

        @Override
        public Option<T> getElementAt(int index) {
            return visible.get(index);
        }

        @Override
        public void setSelectedItem(Object item) {
            if (!Objects.equals(selected, item)) {
                selected = item;
                fireContentsChanged(this, -1, -1);
            }
        }

        @Override
        public Object getSelectedItem() {
            return selected;
        }
    }

    /** Dibuja el placeholder sobre el editor cuando no hay texto ni foco. */
    private final class PlaceholderLayerUI extends LayerUI<JComboBox<Option<T>>> {

        private static final long serialVersionUID = 1L;

        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);
            if (placeholder == null || !editorField.getText().isEmpty() || editorField.isFocusOwner()
                    || editorField.getParent() == null) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(Color.GRAY);
                g2.setFont(editorField.getFont().deriveFont(Font.ITALIC));
                Rectangle bounds = SwingUtilities.convertRectangle(
                        editorField.getParent(), editorField.getBounds(), c);
                Insets insets = editorField.getInsets();
                FontMetrics fm = g2.getFontMetrics();
                int y = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, bounds.x + insets.left, y);
            } finally {
                g2.dispose();
            }
        }
    }
}
