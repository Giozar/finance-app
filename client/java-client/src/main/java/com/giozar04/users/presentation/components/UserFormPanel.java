package com.giozar04.users.presentation.components;

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

import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.shared.utils.FormValidatorUtils;
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.infrastructure.services.UserService;

public class UserFormPanel extends JPanel {

    private final FormField nameField;
    private final FormField emailField;
    private final FormField passwordField;
    private final FormField balanceField;

    private final JButton saveButton;
    private final JButton cancelButton;

    private User currentUser;

    public UserFormPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        nameField = new FormField("Nombre:", false, 400, 40);
        emailField = new FormField("Correo electrónico:", false, 400, 40);
        passwordField = new FormField("Contraseña:", true, 400, 40);
        balanceField = new FormField("Balance global:", false, 400, 40);

        formPanel.add(nameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(emailField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(passwordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(balanceField);

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
        String email = emailField.getValue().trim();
        String password = passwordField.getValue();
        String balanceStr = balanceField.getValue().trim();

        FormValidatorUtils.isRequired(name, "Nombre", errors);
        FormValidatorUtils.isEmail(email, "Correo electrónico", errors);
        FormValidatorUtils.isPassword(password, "Contraseña", 6, errors);
        FormValidatorUtils.isPositiveNumber(balanceStr, "Balance global", errors);

        if (!errors.isEmpty()) {
            String message = FormValidatorUtils.formatErrorMessage(errors);
            DialogUtil.showError(this, message);
            return;
        }

        double balance = Double.parseDouble(balanceStr);
        User user = currentUser != null ? currentUser : new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setGlobalBalance(balance);

        if (currentUser == null) {
            user.setCreatedAt(ZonedDateTime.now());
        }
        user.setUpdatedAt(ZonedDateTime.now());

        try {
            if (currentUser == null) {
                UserService.getInstance().createUser(user);
                DialogUtil.showSuccess(this, "Usuario creado exitosamente.");
            } else {
                UserService.getInstance().updateUserById(user.getId(), user);
                DialogUtil.showSuccess(this, "Usuario actualizado exitosamente.");
            }
            clearForm();
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al guardar el usuario: " + ex.getMessage());
        }
    }

    public void loadUser(User user) {
        this.currentUser = user;
        nameField.setValue(user.getName());
        emailField.setValue(user.getEmail());
        passwordField.setValue(user.getPassword());
        balanceField.setValue(String.valueOf(user.getGlobalBalance()));
    }

    public void clearForm() {
        currentUser = null;
        nameField.clear();
        emailField.clear();
        passwordField.clear();
        balanceField.clear();
    }
}
