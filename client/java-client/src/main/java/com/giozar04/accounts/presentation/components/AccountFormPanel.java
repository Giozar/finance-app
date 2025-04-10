package com.giozar04.accounts.presentation.components;

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

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.accounts.infrastructure.services.AccountService;
import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.bankClients.infrastructure.services.BankClientService;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.shared.utils.FormValidatorUtils;

public class AccountFormPanel extends JPanel {

    private final BankClientService bankClientService = BankClientService.getInstance();

    private final FormField nameField;
    private final FormComboBox<AccountTypes> typeCombo;
    private final FormComboBox<BankClient> bankClientCombo;
    private final FormField balanceField;
    private final FormField accountNumberField;
    private final FormField clabeField;
    private final FormField creditLimitField;
    private final FormField cutoffDayField;
    private final FormField paymentDayField;

    private final JButton saveButton;
    private final JButton cancelButton;

    private Account currentAccount;
    private boolean clientsLoaded = false;

    public AccountFormPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        nameField = new FormField("Nombre:", false, 400, 40);
        typeCombo = new FormComboBox<>("Tipo de cuenta:", 400, 40);
        typeCombo.setPlaceholder("Selecciona un tipo...");
        typeCombo.setItems(List.of(AccountTypes.values()));

        bankClientCombo = new FormComboBox<>("Cliente bancario:", 400, 40);
        bankClientCombo.setPlaceholder("Selecciona un cliente...");
        loadBankClients();

        balanceField = new FormField("Balance actual:", false, 400, 40);
        accountNumberField = new FormField("Número de cuenta:", false, 400, 40);
        clabeField = new FormField("CLABE:", false, 400, 40);
        creditLimitField = new FormField("Límite de crédito:", false, 400, 40);
        cutoffDayField = new FormField("Día de corte:", false, 400, 40);
        paymentDayField = new FormField("Día de pago:", false, 400, 40);

        formPanel.add(nameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(typeCombo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(bankClientCombo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(balanceField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(accountNumberField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(clabeField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(creditLimitField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(cutoffDayField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
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
        try {
            List<BankClient> clients = bankClientService.getAllBankClients();
            bankClientCombo.setItems(clients);
            clientsLoaded = true;
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al cargar los clientes: " + ex.getMessage());
        }
    }

    private void updateFieldVisibility() {
        AccountTypes type = typeCombo.getSelectedItem();

        boolean isLinked = type != AccountTypes.CASH;
        boolean isCredit = type == AccountTypes.CREDIT;

        bankClientCombo.setVisible(isLinked);
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

        String name = nameField.getValue().trim();
        String balanceStr = balanceField.getValue().trim();
        String accountNumber = accountNumberField.getValue().trim();
        String clabe = clabeField.getValue().trim();
        String creditLimitStr = creditLimitField.getValue().trim();
        String cutoffDayStr = cutoffDayField.getValue().trim();
        String paymentDayStr = paymentDayField.getValue().trim();
        AccountTypes type = typeCombo.getSelectedItem();

        FormValidatorUtils.isRequired(name, "Nombre", errors);
        FormValidatorUtils.isPositiveNumber(balanceStr, "Balance actual", errors);

        if (type != AccountTypes.CASH) {
            BankClient selected = bankClientCombo.getSelectedItem();
            if (selected == null || !bankClientCombo.isSelectionValid()) {
                errors.add("Debe seleccionar un cliente de banco válido.");
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

        if (type != AccountTypes.CASH) {
            BankClient selected = bankClientCombo.getSelectedItem();
            account.setBankClientId(selected != null ? selected.getId() : null);
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

        if (!clientsLoaded) {
            loadBankClients();
        }

        if (account.getBankClientId() != null) {
            for (int i = 0; i < bankClientCombo.getItemCount(); i++) {
                BankClient bc = bankClientCombo.getItemAt(i);
                if (bc.getId() == account.getBankClientId()) {
                    bankClientCombo.setSelectedItem(bc);
                    break;
                }
            }
        }

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
        bankClientCombo.clearSelection();
        updateFieldVisibility();
    }
}
