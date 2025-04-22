package com.giozar04.externalEntities.presentation.components;

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

import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.externalEntities.domain.enums.ExternalEntityTypes;
import com.giozar04.externalEntities.infrastructure.services.ExternalEntityService;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.shared.utils.FormValidatorUtils;

public class ExternalEntityFormPanel extends JPanel {

    private final FormField nameField;
    private final FormComboBox<ExternalEntityTypes> typeCombo;
    private final FormField contactField;

    private final JButton saveButton;
    private final JButton cancelButton;

    private ExternalEntity currentEntity;

    public ExternalEntityFormPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        nameField = new FormField("Nombre:", false, 400, 40);

        typeCombo = new FormComboBox<>("Tipo:", 400, 40);
        typeCombo.setPlaceholder("Selecciona un tipo...");
        typeCombo.setItems(List.of(ExternalEntityTypes.values()));

        contactField = new FormField("Contacto (opcional):", false, 400, 40);

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

    private void handleSave() {
        List<String> errors = new ArrayList<>();

        String name = nameField.getValue().trim();
        ExternalEntityTypes type = typeCombo.getSelectedItem();
        String contact = contactField.getValue().trim();

        FormValidatorUtils.isRequired(name, "Nombre", errors);

        if (!typeCombo.isSelectionValid()) {
            errors.add("Debes seleccionar un tipo válido.");
        }

        if (!errors.isEmpty()) {
            DialogUtil.showError(this, FormValidatorUtils.formatErrorMessage(errors));
            return;
        }

        ExternalEntity entity = currentEntity != null ? currentEntity : new ExternalEntity();
        entity.setName(name);
        entity.setType(type);
        entity.setContact(contact.isEmpty() ? null : contact);

        if (currentEntity == null) {
            entity.setCreatedAt(ZonedDateTime.now());
        }
        entity.setUpdatedAt(ZonedDateTime.now());

        try {
            if (currentEntity == null) {
                ExternalEntityService.getInstance().createExternalEntity(entity);
                DialogUtil.showSuccess(this, "Entidad externa creada exitosamente.");
            } else {
                ExternalEntityService.getInstance().updateExternalEntityById(entity.getId(), entity);
                DialogUtil.showSuccess(this, "Entidad externa actualizada exitosamente.");
            }
            clearForm();
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al guardar la entidad externa: " + ex.getMessage());
        }
    }

    public void loadExternalEntity(ExternalEntity entity) {
        this.currentEntity = entity;
        nameField.setValue(entity.getName());
        contactField.setValue(entity.getContact() != null ? entity.getContact() : "");
        typeCombo.setSelectedItem(entity.getType());
    }

    public void clearForm() {
        currentEntity = null;
        nameField.clear();
        contactField.clear();
        typeCombo.clearSelection();
    }
}
