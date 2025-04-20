package com.giozar04.categories.presentation.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.giozar04.categories.domain.entities.Category;
import com.giozar04.categories.domain.enums.CategoryTypes;
import com.giozar04.categories.infrastructure.services.CategoryService;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.shared.utils.FormValidatorUtils;

public class CategoryFormPanel extends JPanel {

    private final FormField nameField;
    private final FormComboBox<CategoryTypes> typeCombo;
    private final FormField iconField;

    private final JButton saveButton;
    private final JButton cancelButton;

    private Category currentCategory;

    public CategoryFormPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        nameField = new FormField("Nombre:", false, 400, 40);

        typeCombo = new FormComboBox<>("Tipo:", 400, 40);
        typeCombo.setPlaceholder("Selecciona un tipo...");
        typeCombo.setItems(List.of(CategoryTypes.values()));

        iconField = new FormField("Ícono (emoji o texto):", false, 400, 40);

        formPanel.add(nameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(typeCombo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(iconField);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Guardar");
        cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(e -> handleSave());
        cancelButton.addActionListener(e -> clearForm());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void handleSave() {
        List<String> errors = new ArrayList<>();

        String name = nameField.getValue().trim();
        CategoryTypes type = typeCombo.getSelectedItem();
        String icon = iconField.getValue().trim();

        FormValidatorUtils.isRequired(name, "Nombre", errors);
        FormValidatorUtils.isRequired(icon, "Ícono", errors);

        if (!typeCombo.isSelectionValid()) {
            errors.add("Debes seleccionar un tipo válido.");
        }

        if (!errors.isEmpty()) {
            DialogUtil.showError(this, FormValidatorUtils.formatErrorMessage(errors));
            return;
        }

        Category category = currentCategory != null ? currentCategory : new Category();
        category.setName(name);
        category.setType(type);
        category.setIcon(icon);

        if (currentCategory == null) {
            category.setCreatedAt(ZonedDateTime.now());
        }
        category.setUpdatedAt(ZonedDateTime.now());

        try {
            if (currentCategory == null) {
                CategoryService.getInstance().createCategory(category);
                DialogUtil.showSuccess(this, "Categoría creada exitosamente.");
            } else {
                CategoryService.getInstance().updateCategoryById(category.getId(), category);
                DialogUtil.showSuccess(this, "Categoría actualizada exitosamente.");
            }
            clearForm();
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al guardar la categoría: " + ex.getMessage());
        }
    }

    public void loadCategory(Category category) {
        this.currentCategory = category;
        nameField.setValue(category.getName());
        iconField.setValue(category.getIcon());
        typeCombo.setSelectedItem(category.getType());
    }

    public void clearForm() {
        currentCategory = null;
        nameField.clear();
        iconField.clear();
        typeCombo.clearSelection();
    }
}
