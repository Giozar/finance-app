package com.giozar04.externalEntities.presentation.components;

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

import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.externalEntities.domain.enums.ExternalEntityTypes;
import com.giozar04.externalEntities.infrastructure.services.ExternalEntityService;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.shared.utils.FormValidatorUtils;
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.infrastructure.services.UserService;

public class ExternalEntityFormPanel extends JPanel {

    private final UserService userService = UserService.getInstance();

    private final FormComboBox<User> userCombo;
    private final FormField nameField;
    private final FormComboBox<ExternalEntityTypes> typeCombo;
    private final FormField contactField;

    private final JButton saveButton;
    private final JButton cancelButton;

    private ExternalEntity currentEntity;

    // --- Alta rápida (QuickCreateDialog) ---
    private Consumer<ExternalEntity> onSaved;
    private User presetUser;
    private ExternalEntityTypes presetType;

    public ExternalEntityFormPanel() {
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
        typeCombo.setItems(List.of(ExternalEntityTypes.values()));

        contactField = new FormField("Contacto (opcional):", false, 400, 40);

        formPanel.add(userCombo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(nameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(typeCombo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(contactField);

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
        ExternalEntityTypes type = typeCombo.getSelectedItem();
        String contact = contactField.getValue().trim();

        User user = userCombo.getSelectedItem();
        if (user == null || !userCombo.isSelectionValid()) {
            errors.add("Debe seleccionar un usuario propietario.");
        }
        FormValidatorUtils.isRequired(name, "Nombre", errors);

        if (!typeCombo.isSelectionValid()) {
            errors.add("Debe seleccionar un tipo válido.");
        }

        if (!errors.isEmpty()) {
            DialogUtil.showError(this, FormValidatorUtils.formatErrorMessage(errors));
            return;
        }

        ExternalEntity entity = currentEntity != null ? currentEntity : new ExternalEntity();
        entity.setUserId(user.getId());
        entity.setName(name);
        entity.setType(type);
        entity.setContact(contact.isEmpty() ? null : contact);

        if (currentEntity == null) {
            entity.setCreatedAt(ZonedDateTime.now());
        }
        entity.setUpdatedAt(ZonedDateTime.now());

        try {
            ExternalEntity saved;
            if (currentEntity == null) {
                saved = ExternalEntityService.getInstance().createExternalEntity(entity);
                DialogUtil.showSuccess(this, "Entidad externa creada exitosamente.");
            } else {
                saved = ExternalEntityService.getInstance().updateExternalEntityById(entity.getId(), entity);
                DialogUtil.showSuccess(this, "Entidad externa actualizada exitosamente.");
            }
            clearForm();
            if (onSaved != null) {
                onSaved.accept(saved);
            }
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al guardar la entidad externa: " + ex.getMessage());
        }
    }

    public void loadExternalEntity(ExternalEntity entity) {
        this.currentEntity = entity;
        selectUser(entity.getUserId());
        nameField.setValue(entity.getName());
        contactField.setValue(entity.getContact() != null ? entity.getContact() : "");
        typeCombo.setSelectedItem(entity.getType());
    }

    public void clearForm() {
        currentEntity = null;
        if (presetUser != null) {
            selectUser(presetUser.getId());
        } else {
            userCombo.clearSelection();
        }
        nameField.clear();
        contactField.clear();
        if (presetType != null) {
            typeCombo.setSelectedItem(presetType);
        } else {
            typeCombo.clearSelection();
        }
    }

    /**
     * Callback que recibe la entidad externa devuelta por el servidor tras guardar con éxito
     * (lo usa {@code QuickCreateDialog}). Con {@code null} se desactiva.
     */
    public void setOnSaved(Consumer<ExternalEntity> onSaved) {
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
    public void presetType(ExternalEntityTypes type) {
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
