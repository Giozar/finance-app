package com.giozar04.tags.presentation.views;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.MainContentPanel;
import com.giozar04.shared.components.table.ColumnDefinition;
import com.giozar04.shared.components.table.GenericTablePanel;
import com.giozar04.shared.components.table.OptionsCellEditor;
import com.giozar04.shared.components.table.OptionsCellRenderer;
import com.giozar04.shared.components.table.PopupMenuActionHandler;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.tags.domain.entities.Tag;
import com.giozar04.tags.infrastructure.services.TagService;
import com.giozar04.tags.presentation.components.TagFormPanel;

public class TagsView extends JPanel implements PopupMenuActionHandler {

    private final TagService tagService;
    private JTextField searchField;
    private GenericTablePanel<Tag> tablePanel;

    public TagsView() {
        tagService = TagService.getInstance();

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        initTablePanel();
    }

    private JPanel createTopPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("Gestión de Etiquetas");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        JButton newButton = new JButton("Nueva Etiqueta");
        newButton.addActionListener(this::handleNewTag);
        headerPanel.add(newButton, BorderLayout.EAST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Buscar:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        JButton searchButton = new JButton("Buscar");
        searchButton.addActionListener(e -> performSearch());
        searchPanel.add(searchButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        return topPanel;
    }

    private void initTablePanel() {
        List<ColumnDefinition<Tag>> columns = Arrays.asList(
                new ColumnDefinition<>("Nombre", Tag::getName),
                new ColumnDefinition<>("Color", Tag::getColor),
                new ColumnDefinition<>("Opciones", t -> "···")
        );

        columns.get(1).setRenderer(createColorCellRenderer());
        columns.get(2).setRenderer(new OptionsCellRenderer());
        columns.get(2).setEditor(new OptionsCellEditor(this));

        try {
            List<Tag> tags = tagService.getAllTags();
            tablePanel = new GenericTablePanel<>(columns, tags);
            add(tablePanel, BorderLayout.CENTER);
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al cargar las etiquetas.");
        }
    }

    private DefaultTableCellRenderer createColorCellRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                java.awt.Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    try {
                        comp.setBackground(java.awt.Color.decode(value.toString()));
                        setText(" ");
                    } catch (NumberFormatException e) {
                        comp.setBackground(java.awt.Color.WHITE);
                        setText("Color inválido");
                    }
                }
                return comp;
            }
        };
    }

    private void loadTags() {
        try {
            List<Tag> tags = tagService.getAllTags();
            tablePanel.setData(tags);
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al recargar las etiquetas.");
        }
    }

    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();
        try {
            List<Tag> tags = tagService.getAllTags();
            List<Tag> filtered = tags.stream()
                    .filter(t -> t.getName().toLowerCase().contains(query))
                    .collect(Collectors.toList());
            tablePanel.setData(filtered);
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al buscar: " + e.getMessage());
        }
    }

    private void handleNewTag(ActionEvent e) {
        JButton btn = (JButton) e.getSource();
        btn.setEnabled(false);
        MainContentPanel mainPanel = getMainContentPanel();
        if (mainPanel != null) {
            mainPanel.setView(new CreateTagView());
        }
    }

    private MainContentPanel getMainContentPanel() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof MainContentPanel)) {
            parent = parent.getParent();
        }
        return (MainContentPanel) parent;
    }

    @Override
    public void onEdit(int rowIndex) {
        Tag tag = tablePanel.getItemAt(rowIndex);
        TagFormPanel form = new TagFormPanel();
        form.loadTag(tag);
        MainContentPanel main = getMainContentPanel();
        if (main != null) main.setView(form);
    }

    @Override
    public void onDelete(int rowIndex) {
        Tag tag = tablePanel.getItemAt(rowIndex);
        boolean confirm = DialogUtil.showConfirm(this, "¿Desea eliminar esta etiqueta?", "Confirmar eliminación");
        if (confirm) {
            try {
                tagService.deleteTagById(tag.getId());
                JOptionPane.showMessageDialog(this, "Etiqueta eliminada.");
                loadTags();
            } catch (ClientOperationException | HeadlessException ex) {
                DialogUtil.showError(this, "No se pudo eliminar: " + ex.getMessage());
            }
        }
    }

    @Override
    public void onViewDetails(int rowIndex) {
        JOptionPane.showMessageDialog(this, "Función no implementada.");
    }

    @Override
    public void addNotify() {
        super.addNotify();
        loadTags();
    }
}
