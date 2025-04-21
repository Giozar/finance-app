package com.giozar04.tags.presentation.components;

import java.awt.BorderLayout;
import java.awt.Color;
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

import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.forms.ColorPickerField;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.shared.utils.FormValidatorUtils;
import com.giozar04.tags.domain.entities.Tag;
import com.giozar04.tags.infrastructure.services.TagService;

public class TagFormPanel extends JPanel {

    private final FormField nameField;
    private final ColorPickerField colorPicker;

    private final JButton saveButton;
    private final JButton cancelButton;

    private Tag currentTag;

    public TagFormPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        nameField = new FormField("Nombre de la etiqueta:", false, 400, 40);
        colorPicker = new ColorPickerField("Color:", 400, 40);

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

    private void handleSave() {
        List<String> errors = new ArrayList<>();

        String name = nameField.getValue().trim();
        String colorHex = colorPicker.getColorHex();

        FormValidatorUtils.isRequired(name, "Nombre", errors);
        if (colorHex == null) {
            errors.add("Debe seleccionar un color.");
        }

        if (!errors.isEmpty()) {
            DialogUtil.showError(this, FormValidatorUtils.formatErrorMessage(errors));
            return;
        }

        Tag tag = currentTag != null ? currentTag : new Tag();
        tag.setName(name);
        tag.setColor(colorHex);

        if (currentTag == null) {
            tag.setCreatedAt(ZonedDateTime.now());
        }
        tag.setUpdatedAt(ZonedDateTime.now());

        try {
            if (currentTag == null) {
                TagService.getInstance().createTag(tag);
                DialogUtil.showSuccess(this, "Etiqueta creada exitosamente.");
            } else {
                TagService.getInstance().updateTagById(tag.getId(), tag);
                DialogUtil.showSuccess(this, "Etiqueta actualizada exitosamente.");
            }
            clearForm();
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al guardar la etiqueta: " + ex.getMessage());
        }
    }

    public void loadTag(Tag tag) {
        this.currentTag = tag;
        nameField.setValue(tag.getName());
        if (tag.getColor() != null) {
            colorPicker.setColor(Color.decode(tag.getColor()));
        }
    }

    public void clearForm() {
        currentTag = null;
        nameField.clear();
        colorPicker.clear();
    }
}
