package com.giozar04.users.presentation.components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.utils.ErrorDialogUtil;
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.infrastructure.services.UserService;
import com.giozar04.users.presentation.validators.UserValidator;

public class UserFormPanel extends JPanel {

    private final FormField nameField;
    private final FormField emailField;
    private final FormField passwordField;
    private final FormField balanceField;

    private final JButton saveButton;
    private final JButton cancelButton;

    private User currentUser;

    public UserFormPanel() {
        setLayout(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 10));

        // Creamos los campos usando FormField
        nameField = new FormField("Nombre:");
        emailField = new FormField("Correo electrónico:");
        passwordField = new FormField("Contraseña:", true); // true = campo de contraseña
        balanceField = new FormField("Balance global:");

        // Agregamos al panel
        formPanel.add(nameField);
        formPanel.add(emailField);
        formPanel.add(passwordField);
        formPanel.add(balanceField);

        add(formPanel, BorderLayout.CENTER);

        // Botones de acción
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
        UserValidator validator = new UserValidator();

        String name = nameField.getValue();
        String email = emailField.getValue();
        String password = passwordField.getValue();
        String balanceStr = balanceField.getValue();

        validator.validateRequired(name, "Nombre");
        validator.validateEmail(email, "Correo electrónico");
        validator.validatePassword(password, "Contraseña");
        validator.validatePositiveNumber(balanceStr, "Balance global");

        if (validator.hasErrors()) {
            JOptionPane.showMessageDialog(this, validator.getErrorMessage(), "Errores de Validación", JOptionPane.ERROR_MESSAGE);
            ErrorDialogUtil.showError(this, validator.getErrorMessage());
            return;
        }

        double balance = balanceStr.isEmpty() ? 0 : Double.parseDouble(balanceStr);
        if (balance < 0) {
            ErrorDialogUtil.showError(this, "El balance global no puede ser negativo.");
            return;
        }
        if (balanceField.getValue().isEmpty()) {
            ErrorDialogUtil.showError(this, "El campo de balance global no puede estar vacío.");
            return;
        }
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            ErrorDialogUtil.showError(this, "Todos los campos son obligatorios.");
            return;
        }
        
        User user = currentUser != null ? currentUser : new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setGlobalBalance(balance);

//      Asignamos fechas si es creación
        if (currentUser == null) {
            user.setCreatedAt(java.time.ZonedDateTime.now());
        }
        user.setUpdatedAt(java.time.ZonedDateTime.now());

        try {
            if (currentUser == null) {
                UserService.getInstance().createUser(user);
                JOptionPane.showMessageDialog(this, "Usuario creado exitosamente.");
            } else {
                UserService.getInstance().updateUserById(user.getId(), user);
                JOptionPane.showMessageDialog(this, "Usuario actualizado exitosamente.");
            }
            clearForm();
        } catch (ClientOperationException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar el usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
