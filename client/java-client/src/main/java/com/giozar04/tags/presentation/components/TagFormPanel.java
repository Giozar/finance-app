package com.giozar04.tags.presentation.components;

import java.awt.BorderLayout;
import java.awt.Color;
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

import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.forms.ColorPickerField;
import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.shared.utils.FormValidatorUtils;
import com.giozar04.tags.domain.entities.Tag;
import com.giozar04.tags.application.ports.input.TagOperations;
import com.giozar04.bootstrap.ClientUseCases;
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.infrastructure.services.UserService;

public class TagFormPanel extends JPanel {

    private final UserService userService = UserService.getInstance();

    private final FormComboBox<User> userCombo;
    private final FormField nameField;
    private final ColorPickerField colorPicker;

    private final JButton saveButton;
    private final JButton cancelButton;

    private Tag currentTag;

    // --- Alta rápida (QuickCreateDialog) ---
    private Consumer<Tag> onSaved;
    private User presetUser;

    public TagFormPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        userCombo = new FormComboBox<>("Usuario propietario:", 400, 40);
        userCombo.setPlaceholder("Seleccione un usuario...");
        loadUsers();

        nameField = new FormField("Nombre de la etiqueta:", false, 400, 40);
        colorPicker = new ColorPickerField("Color:", 400, 40);

        formPanel.add(userCombo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(nameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(colorPicker);

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
        String colorHex = colorPicker.getColorHex();

        User user = userCombo.getSelectedItem();
        if (user == null || !userCombo.isSelectionValid()) {
            errors.add("Debe seleccionar un usuario propietario.");
        }
        FormValidatorUtils.isRequired(name, "Nombre", errors);
        if (colorHex == null) {
            errors.add("Debe seleccionar un color.");
        }

        if (!errors.isEmpty()) {
            DialogUtil.showError(this, FormValidatorUtils.formatErrorMessage(errors));
            return;
        }

        Tag tag = currentTag != null ? currentTag : new Tag();
        tag.setUserId(user.getId());
        tag.setName(name);
        tag.setColor(colorHex);

        if (currentTag == null) {
            tag.setCreatedAt(ZonedDateTime.now());
        }
        tag.setUpdatedAt(ZonedDateTime.now());

        try {
            Tag savedTag;
            if (currentTag == null) {
                savedTag = ClientUseCases.get(TagOperations.class).createTag(tag);
                DialogUtil.showSuccess(this, "Etiqueta creada exitosamente.");
            } else {
                savedTag = ClientUseCases.get(TagOperations.class).updateTagById(tag.getId(), tag);
                DialogUtil.showSuccess(this, "Etiqueta actualizada exitosamente.");
            }
            clearForm();
            if (onSaved != null) {
                onSaved.accept(savedTag);
            }
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al guardar la etiqueta: " + ex.getMessage());
        }
    }

    public void loadTag(Tag tag) {
        this.currentTag = tag;
        selectUser(tag.getUserId());
        nameField.setValue(tag.getName());
        if (tag.getColor() != null) {
            colorPicker.setColor(Color.decode(tag.getColor()));
        }
    }

    public void clearForm() {
        currentTag = null;
        if (presetUser != null) {
            selectUser(presetUser.getId());
        } else {
            userCombo.clearSelection();
        }
        nameField.clear();
        colorPicker.clear();
    }

    /**
     * Callback que recibe la etiqueta devuelta por el servidor tras guardar con éxito
     * (lo usa {@code QuickCreateDialog}). Con {@code null} se desactiva.
     */
    public void setOnSaved(Consumer<Tag> onSaved) {
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
