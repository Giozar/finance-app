package com.giozar04.bankClients.presentation.components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.bankClients.infrastructure.services.BankClientService;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.shared.utils.FormValidatorUtils;
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.infrastructure.services.UserService;

public class BankClientFormPanel extends JPanel {

    private final UserService userService = UserService.getInstance();

    private final FormField bankNameField;
    private final FormField clientNumberField;
    private final JComboBox<User> userCombo;
    private final JButton saveButton;
    private final JButton cancelButton;

    private BankClient currentClient;

    public BankClientFormPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        bankNameField = new FormField("Nombre del Banco:");
        clientNumberField = new FormField("Número de Cliente:");
        userCombo = new JComboBox<>();
        loadUsers();

        formPanel.add(bankNameField);
        formPanel.add(clientNumberField);
        formPanel.add(userCombo);

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
        userCombo.removeAllItems();
        try {
            List<User> users = userService.getAllUsers();
            for (User user: users) {
                userCombo.addItem(user);
            }
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al cargar a los usuarios " + e.getMessage());
        }
    }

    private void selectUserById(long userId) {
        for (int i = 0; i < userCombo.getItemCount(); i++) {
            User user = userCombo.getItemAt(i);
            if (user.getId() == userId) {
                userCombo.setSelectedItem(user);
                break;
            }
        }
    }
    

    private void handleSave() {
        List<String> errors = new ArrayList<>();

        String bankName = bankNameField.getValue().trim();
        String clientNumber = clientNumberField.getValue().trim();
        User user = (User) userCombo.getSelectedItem();
        long userId = user.getId();
        FormValidatorUtils.isRequired(bankName, "Nombre del Banco", errors);
        FormValidatorUtils.isRequired(clientNumber, "Número de Cliente", errors);
        FormValidatorUtils.isLongPositive(userId+ "", "ID de Usuario", errors);

        if(userCombo.getSelectedItem() == null ){
            errors.add("Debes seleccionar un Usuario");
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
        userCombo.setSelectedIndex(-1);
    }
}
