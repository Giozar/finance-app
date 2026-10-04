package com.giozar04.categories.presentation.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.infrastructure.services.UserService;

public class CategoryFormPanel extends JPanel {

    private final UserService userService = UserService.getInstance();

    private final FormComboBox<User> userCombo;
    private final FormField nameField;
    private final FormComboBox<CategoryTypes> typeCombo;
    private final FormField iconField;

    private final JButton saveButton;
    private final JButton cancelButton;

    private Category currentCategory;

    // --- Alta rápida (QuickCreateDialog) ---
    private Consumer<Category> onSaved;
    private User presetUser;
    private CategoryTypes presetType;

    public CategoryFormPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        userCombo = new FormComboBox<>("Usuario propietario:", 400, 40);
        userCombo.setPlaceholder("Seleccione un usuario...");
        loadUsers();

        nameField = new FormField("Nombre:", false, 400, 40);

        typeCombo = new FormComboBox<>("Tipo:", 400, 40);
        typeCombo.setPlaceholder("Seleccione un tipo...");
        typeCombo.setItems(List.of(CategoryTypes.values()));

        iconField = new FormField("Ícono (emoji o texto):", false, 400, 40);

        formPanel.add(userCombo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
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

    private void loadUsers() {
        try {
            List<User> users = userService.getAllUsers();
            userCombo.setItems(users);
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al cargar los usuarios: " + ex.getMessage());
        }
    }

    private void handleSave() {
        List<String> errors = new ArrayList<>();

        String name = nameField.getValue().trim();
        CategoryTypes type = typeCombo.getSelectedItem();
        String icon = iconField.getValue().trim();

        User user = userCombo.getSelectedItem();
        if (user == null || !userCombo.isSelectionValid()) {
            errors.add("Debe seleccionar un usuario propietario.");
        }
        FormValidatorUtils.isRequired(name, "Nombre", errors);
        FormValidatorUtils.isRequired(icon, "Ícono", errors);

        if (!typeCombo.isSelectionValid()) {
            errors.add("Debe seleccionar un tipo válido.");
        }

        if (!errors.isEmpty()) {
            DialogUtil.showError(this, FormValidatorUtils.formatErrorMessage(errors));
            return;
        }

        Category category = currentCategory != null ? currentCategory : new Category();
        category.setUserId(user.getId());
        category.setName(name);
        category.setType(type);
        category.setIcon(icon);

        if (currentCategory == null) {
            category.setCreatedAt(ZonedDateTime.now());
        }
        category.setUpdatedAt(ZonedDateTime.now());

        try {
            Category saved;
            if (currentCategory == null) {
                saved = CategoryService.getInstance().createCategory(category);
                DialogUtil.showSuccess(this, "Categoría creada exitosamente.");
            } else {
                saved = CategoryService.getInstance().updateCategoryById(category.getId(), category);
                DialogUtil.showSuccess(this, "Categoría actualizada exitosamente.");
            }
            clearForm();
            if (onSaved != null) {
                onSaved.accept(saved);
            }
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al guardar la categoría: " + ex.getMessage());
        }
    }

    public void loadCategory(Category category) {
        this.currentCategory = category;
        selectUser(category.getUserId());
        nameField.setValue(category.getName());
        iconField.setValue(category.getIcon());
        typeCombo.setSelectedItem(category.getType());
    }

    public void clearForm() {
        currentCategory = null;
        if (presetUser != null) {
            selectUser(presetUser.getId());
        } else {
            userCombo.clearSelection();
        }
        nameField.clear();
        iconField.clear();
        if (presetType != null) {
            typeCombo.setSelectedItem(presetType);
        } else {
            typeCombo.clearSelection();
        }
    }

    /**
     * Callback que recibe la categoría devuelta por el servidor tras guardar con éxito
     * (lo usa {@code QuickCreateDialog}). Con {@code null} se desactiva.
     */
    public void setOnSaved(Consumer<Category> onSaved) {
        this.onSaved = onSaved;
    }

    /**
     * Preselecciona el usuario propietario; si {@code lock} es true, no se puede cambiar.
     * Se mantiene al limpiar el formulario.
     */
    public void presetUser(User user, boolean lock) {
        this.presetUser = user;
        if (user != null) {
            selectUser(user.getId());
        } else {
            userCombo.clearSelection();
        }
        userCombo.getComboBox().setEnabled(!(lock && user != null));
    }

    /** Preselecciona el tipo (sigue siendo editable). Se mantiene al limpiar el formulario. */
    public void presetType(CategoryTypes type) {
        this.presetType = type;
        if (type != null) {
            typeCombo.setSelectedItem(type);
        } else {
            typeCombo.clearSelection();
        }
    }

    private void selectUser(long userId) {
        for (int i = 0; i < userCombo.getItemCount(); i++) {
            User u = userCombo.getItemAt(i);
            if (u != null && u.getId() == userId) {
                userCombo.setSelectedItem(u);
                break;
            }
        }
    }
}
