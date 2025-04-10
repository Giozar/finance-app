package com.giozar04.bankClients.presentation.components;

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

import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.bankClients.infrastructure.services.BankClientService;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.shared.utils.FormValidatorUtils;
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.infrastructure.services.UserService;

public class BankClientFormPanel extends JPanel {

    private final UserService userService = UserService.getInstance();

    private final FormField bankNameField;
    private final FormField clientNumberField;
    private final FormComboBox<User> userComboBox;
    private final JButton saveButton;
    private final JButton cancelButton;

    private BankClient currentClient;

    public BankClientFormPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        bankNameField = new FormField("Nombre del Banco:", false, 400, 40);
        clientNumberField = new FormField("Número de Cliente:", false, 400, 40);
        userComboBox = new FormComboBox<>("Usuario:", 400, 40);
        userComboBox.setPlaceholder("Selecciona un usuario...");
        loadUsers();

        formPanel.add(bankNameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(clientNumberField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(userComboBox);

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
            userComboBox.setItems(users);
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al cargar a los usuarios: " + e.getMessage());
        }
    }

    private void selectUserById(long userId) {
        for (int i = 0; i < userComboBox.getItemCount(); i++) {
            User user = userComboBox.getItemAt(i);
            if (user.getId() == userId) {
                userComboBox.setSelectedItem(user);
                break;
            }
        }
    }

    private void handleSave() {
        List<String> errors = new ArrayList<>();
    
        String bankName = bankNameField.getValue().trim();
        String clientNumber = clientNumberField.getValue().trim();
        User user = userComboBox.getSelectedItem();
        long userId = 0;
        FormValidatorUtils.isRequired(bankName, "Nombre del Banco", errors);
        FormValidatorUtils.isRequired(clientNumber, "Número de Cliente", errors);

        if (user != null) {
            userId = user.getId();
        }

        if(userId == 0 || !userComboBox.isSelectionValid()) {
            errors.add("Debes seleccionar un Usuario válido");
        }
    
        if (!errors.isEmpty()) {
            String message = FormValidatorUtils.formatErrorMessage(errors);
            DialogUtil.showError(this, message);
            return;
        }
    
        BankClient client = currentClient != null ? currentClient : new BankClient();
        client.setBankName(bankName);
        client.setClientNumber(clientNumber);
        client.setUserId(userId);
    
        if (currentClient == null) {
            client.setCreatedAt(ZonedDateTime.now());
        }
        client.setUpdatedAt(ZonedDateTime.now());
    
        try {
            if (currentClient == null) {
                BankClientService.getInstance().createBankClient(client);
                DialogUtil.showSuccess(this, "Cliente bancario creado exitosamente.");
            } else {
                BankClientService.getInstance().updateBankClientById(client.getId(), client);
                DialogUtil.showSuccess(this, "Cliente bancario actualizado exitosamente.");
            }
            clearForm();
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al guardar el cliente: " + ex.getMessage());
        }
    }
    
    public void loadBankClient(BankClient client) {
        this.currentClient = client;
        bankNameField.setValue(client.getBankName());
        clientNumberField.setValue(client.getClientNumber());
        this.selectUserById(client.getUserId());
    }

    public void clearForm() {
        currentClient = null;
        bankNameField.clear();
        clientNumberField.clear();
        userComboBox.clearSelection();
    }
}
