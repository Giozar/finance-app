package com.giozar04.accounts.presentation.components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.accounts.infrastructure.services.AccountService;
import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.bankClients.infrastructure.services.BankClientService;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.shared.utils.FormValidatorUtils;

public class AccountFormPanel extends JPanel {

    private final BankClientService bankClientService = BankClientService.getInstance();

    private final FormField nameField;
    private final JComboBox<AccountTypes> typeCombo;
    private final JComboBox<BankClient> bankClientCombo;
    private final FormField balanceField;
    private final FormField accountNumberField;
    private final FormField clabeField;
    private final FormField creditLimitField;
    private final FormField cutoffDayField;
    private final FormField paymentDayField;

    private final JButton saveButton;
    private final JButton cancelButton;

    private Account currentAccount;

    public AccountFormPanel() {
        setLayout(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 10));

        nameField = new FormField("Nombre:");
        typeCombo = new JComboBox<>(AccountTypes.values());
        bankClientCombo = new JComboBox<>();
        loadBankClients();

        balanceField = new FormField("Balance actual:");
        accountNumberField = new FormField("Número de cuenta:");
        clabeField = new FormField("CLABE:");
        creditLimitField = new FormField("Límite de crédito:");
        cutoffDayField = new FormField("Día de corte:");
        paymentDayField = new FormField("Día de pago:");

        formPanel.add(nameField);
        formPanel.add(typeCombo);
        formPanel.add(bankClientCombo);
        formPanel.add(balanceField);
        formPanel.add(accountNumberField);
        formPanel.add(clabeField);
        formPanel.add(creditLimitField);
        formPanel.add(cutoffDayField);
        formPanel.add(paymentDayField);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Guardar");
        cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(e -> handleSave());
        cancelButton.addActionListener(e -> clearForm());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);

        typeCombo.addActionListener(e -> updateFieldVisibility());
        updateFieldVisibility();
    }

    private void loadBankClients() {
        bankClientCombo.removeAllItems();
        try {
            List<BankClient> clients = bankClientService.getAllBankClients();
            for (BankClient client : clients) {
                bankClientCombo.addItem(client); // debe tener override toString()
            }
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al cargar los clientes: " + ex.getMessage());
        }
    }

    private void updateFieldVisibility() {
        AccountTypes type = (AccountTypes) typeCombo.getSelectedItem();

        boolean isLinked = type != AccountTypes.CASH;
        boolean isCredit = type == AccountTypes.CREDIT;

        bankClientCombo.setEnabled(isLinked);
        accountNumberField.setVisible(isLinked);
        clabeField.setVisible(isLinked);

        creditLimitField.setVisible(isCredit);
        cutoffDayField.setVisible(isCredit);
        paymentDayField.setVisible(isCredit);

        revalidate();
        repaint();
    }

    private void handleSave() {
        List<String> errors = new ArrayList<>();
    
        String name = nameField.getValue();
        String balanceStr = balanceField.getValue();
        String accountNumber = accountNumberField.getValue();
        String clabe = clabeField.getValue();
        String creditLimitStr = creditLimitField.getValue();
        String cutoffDayStr = cutoffDayField.getValue();
        String paymentDayStr = paymentDayField.getValue();
        AccountTypes type = (AccountTypes) typeCombo.getSelectedItem();
    
        FormValidatorUtils.isRequired(name, "Nombre", errors);
        FormValidatorUtils.isPositiveNumber(balanceStr, "Balance actual", errors);
    
        if (type != AccountTypes.CASH) {
            if (bankClientCombo.getSelectedItem() == null) {
                errors.add("Debe seleccionar un cliente de banco.");
            }
            FormValidatorUtils.isRequired(accountNumber, "Número de cuenta", errors);
            FormValidatorUtils.isRequired(clabe, "CLABE", errors);
        }
    
        if (type == AccountTypes.CREDIT) {
            FormValidatorUtils.isPositiveNumber(creditLimitStr, "Límite de crédito", errors);
            FormValidatorUtils.isIntegerInRange(cutoffDayStr, "Día de corte", 1, 31, errors);
            FormValidatorUtils.isIntegerInRange(paymentDayStr, "Día de pago", 1, 31, errors);
        }
    
        if (!errors.isEmpty()) {
            DialogUtil.showError(this, FormValidatorUtils.formatErrorMessage(errors));
            return;
        }
    
        double balance = Double.parseDouble(balanceStr);
        Account account = currentAccount != null ? currentAccount : new Account();
        account.setName(name);
        account.setType(type.name().toLowerCase());
        account.setCurrentBalance(balance);
    
        // ✅ Corrección aquí: aseguramos actualización de bankClientId al editar
        if (type != AccountTypes.CASH) {
            BankClient selected = (BankClient) bankClientCombo.getSelectedItem();
            if (selected != null) {
                account.setBankClientId(selected.getId());
            } else {
                account.setBankClientId(null);
            }
            account.setAccountNumber(accountNumber);
            account.setClabe(clabe);
        } else {
            account.setBankClientId(null);
            account.setAccountNumber(null);
            account.setClabe(null);
        }
    
        if (type == AccountTypes.CREDIT) {
            account.setCreditLimit(Double.valueOf(creditLimitStr));
            account.setCutoffDay(Integer.valueOf(cutoffDayStr));
            account.setPaymentDay(Integer.valueOf(paymentDayStr));
        } else {
            account.setCreditLimit(null);
            account.setCutoffDay(null);
            account.setPaymentDay(null);
        }
    
        if (currentAccount == null) {
            account.setCreatedAt(ZonedDateTime.now());
        }
        account.setUpdatedAt(ZonedDateTime.now());
    
        try {
            if (currentAccount == null) {
                AccountService.getInstance().createAccount(account);
                DialogUtil.showSuccess(this, "Cuenta creada exitosamente.");
            } else {
                AccountService.getInstance().updateAccountById(account.getId(), account);
                DialogUtil.showSuccess(this, "Cuenta actualizada exitosamente.");
            }
            clearForm();
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al guardar la cuenta: " + ex.getMessage());
        }
    }

    public void loadAccount(Account account) {
        this.currentAccount = account;
        nameField.setValue(account.getName());
        typeCombo.setSelectedItem(AccountTypes.valueOf(account.getType().toUpperCase()));
        balanceField.setValue(String.valueOf(account.getCurrentBalance()));
        accountNumberField.setValue(account.getAccountNumber());
        clabeField.setValue(account.getClabe());
        creditLimitField.setValue(account.getCreditLimit() != null ? account.getCreditLimit().toString() : "");
        cutoffDayField.setValue(account.getCutoffDay() != null ? account.getCutoffDay().toString() : "");
        paymentDayField.setValue(account.getPaymentDay() != null ? account.getPaymentDay().toString() : "");
        updateFieldVisibility();
    }

    public void clearForm() {
        currentAccount = null;
        nameField.clear();
        balanceField.clear();
        accountNumberField.clear();
        clabeField.clear();
        creditLimitField.clear();
        cutoffDayField.clear();
        paymentDayField.clear();
        typeCombo.setSelectedIndex(0);
        bankClientCombo.setSelectedIndex(-1);
        updateFieldVisibility();
    }
}
