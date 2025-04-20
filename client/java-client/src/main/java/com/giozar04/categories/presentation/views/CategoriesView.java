package com.giozar04.categories.presentation.views;

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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import com.giozar04.categories.domain.entities.Category;
import com.giozar04.categories.infrastructure.services.CategoryService;
import com.giozar04.categories.presentation.components.CategoryFormPanel;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.MainContentPanel;
import com.giozar04.shared.components.table.ColumnDefinition;
import com.giozar04.shared.components.table.GenericTablePanel;
import com.giozar04.shared.components.table.OptionsCellEditor;
import com.giozar04.shared.components.table.OptionsCellRenderer;
import com.giozar04.shared.components.table.PopupMenuActionHandler;
import com.giozar04.shared.utils.DialogUtil;

public class CategoriesView extends JPanel implements PopupMenuActionHandler {

    private final CategoryService categoryService;
    private JTextField searchField;
    private GenericTablePanel<Category> tablePanel;

    public CategoriesView() {
        categoryService = CategoryService.getInstance();

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        initTablePanel();
    }

    private JPanel createTopPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("Gestión de Categorías");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        JButton newButton = new JButton("Nueva Categoría");
        newButton.addActionListener(this::handleNewCategory);
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
        List<ColumnDefinition<Category>> columns = Arrays.asList(
                new ColumnDefinition<>("Nombre", Category::getName),
                new ColumnDefinition<>("Tipo", c -> c.getType().getLabel()),
                new ColumnDefinition<>("Ícono", Category::getIcon),
                new ColumnDefinition<>("Opciones", c -> "···")
        );

        columns.get(2).setRenderer(centerAlign());
        columns.get(3).setRenderer(new OptionsCellRenderer());
        columns.get(3).setEditor(new OptionsCellEditor(this));

        try {
            List<Category> categories = categoryService.getAllCategories();
            tablePanel = new GenericTablePanel<>(columns, categories);
            add(tablePanel, BorderLayout.CENTER);
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al cargar las categorías.");
        }
    }

    private DefaultTableCellRenderer centerAlign() {
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        return center;
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            tablePanel.setData(categories);
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al recargar las categorías.");
        }
    }

    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();
        try {
            List<Category> categories = categoryService.getAllCategories();
            List<Category> filtered = categories.stream()
                    .filter(c -> c.getName().toLowerCase().contains(query))
                    .collect(Collectors.toList());
            tablePanel.setData(filtered);
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al buscar: " + e.getMessage());
        }
    }

    private void handleNewCategory(ActionEvent e) {
        JButton btn = (JButton) e.getSource();
        btn.setEnabled(false);
        MainContentPanel mainPanel = getMainContentPanel();
        if (mainPanel != null) {
            mainPanel.setView(new CreateCategoryView());
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
        Category category = tablePanel.getItemAt(rowIndex);
        CategoryFormPanel form = new CategoryFormPanel();
        form.loadCategory(category);
        MainContentPanel main = getMainContentPanel();
        if (main != null) main.setView(form);
    }

    @Override
    public void onDelete(int rowIndex) {
        Category category = tablePanel.getItemAt(rowIndex);
        boolean confirm = DialogUtil.showConfirm(this, "¿Desea eliminar esta categoría?", "Confirmar eliminación");
        if (confirm) {
            try {
                categoryService.deleteCategoryById(category.getId());
                JOptionPane.showMessageDialog(this, "Categoría eliminada.");
                loadCategories();
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
        loadCategories();
    }
}
