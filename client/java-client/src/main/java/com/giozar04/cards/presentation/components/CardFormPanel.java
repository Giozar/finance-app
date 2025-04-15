package com.giozar04.cards.presentation.components;

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
import com.giozar04.accounts.infrastructure.services.AccountService;
import com.giozar04.card.domain.entities.Card;
import com.giozar04.card.domain.enums.CardTypes;
import com.giozar04.cards.infrastructure.services.CardService;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.DatePickerComponent;
import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.shared.utils.FormValidatorUtils;

public class CardFormPanel extends JPanel {

    private final AccountService accountService = AccountService.getInstance();

    private final FormField nameField;
    private final FormComboBox<CardTypes> typeCombo;
    private final FormComboBox<Account> accountCombo;
    private final FormField numberField;
    private final DatePickerComponent expirationDatePicker;

    private final JButton saveButton;
    private final JButton cancelButton;

    private Card currentCard;

    public CardFormPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        nameField = new FormField("Nombre:", false, 400, 40);

        typeCombo = new FormComboBox<>("Tipo de tarjeta:", 400, 40);
        typeCombo.setPlaceholder("Selecciona un tipo...");
        typeCombo.setItems(List.of(CardTypes.values()));


        accountCombo = new FormComboBox<>("Cuenta asociada:", 400, 40);
        accountCombo.setPlaceholder("Selecciona una cuenta...");
        loadAccounts();

        numberField = new FormField("Últimos 4 dígitos:", false, 400, 40);
        expirationDatePicker = new DatePickerComponent();

        formPanel.add(nameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(typeCombo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(accountCombo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(numberField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(expirationDatePicker);

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

    private void loadAccounts() {
        try {
            List<Account> accounts = accountService.getAllAccounts();
            accountCombo.setItems(accounts);
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al cargar las cuentas: " + ex.getMessage());
        }
    }

    private void handleSave() {
        List<String> errors = new ArrayList<>();

        String name = nameField.getValue().trim();
        String cardNumber = numberField.getValue().trim();
        ZonedDateTime expiration = expirationDatePicker.getDate();
        CardTypes type = typeCombo.getSelectedItem();
        Account selectedAccount = accountCombo.getSelectedItem();

        FormValidatorUtils.isRequired(name, "Nombre", errors);
        FormValidatorUtils.isRequired(cardNumber, "Últimos 4 dígitos", errors);

        if (selectedAccount == null || !accountCombo.isSelectionValid()) {
            errors.add("Debe seleccionar una cuenta válida.");
        }

        if (expiration == null) {
            errors.add("Debe seleccionar una fecha de expiración válida.");
        }

        if (!errors.isEmpty()) {
            DialogUtil.showError(this, FormValidatorUtils.formatErrorMessage(errors));
            return;
        }

        Card card = currentCard != null ? currentCard : new Card();
        card.setName(name);
        card.setCardType(type);
        card.setCardNumber(cardNumber);
        card.setExpirationDate(expiration);
        
        if (selectedAccount != null) {
            card.setAccountId(selectedAccount.getId());
        }

        if (currentCard == null) {
            card.setCreatedAt(ZonedDateTime.now());
        }
        card.setUpdatedAt(ZonedDateTime.now());

        try {
            if (currentCard == null) {
                CardService.getInstance().createCard(card);
                DialogUtil.showSuccess(this, "Tarjeta creada exitosamente.");
            } else {
                CardService.getInstance().updateCardById(card.getId(), card);
                DialogUtil.showSuccess(this, "Tarjeta actualizada exitosamente.");
            }
            clearForm();
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al guardar la tarjeta: " + ex.getMessage());
        }
    }

    public void loadCard(Card card) {
        this.currentCard = card;
        nameField.setValue(card.getName());
        typeCombo.setSelectedItem(card.getCardType());
        numberField.setValue(card.getCardNumber());
        expirationDatePicker.setDate(card.getExpirationDate());

        for (int i = 0; i < accountCombo.getItemCount(); i++) {
            Account acc = accountCombo.getItemAt(i);
            if (acc.getId() == card.getAccountId()) {
                accountCombo.setSelectedItem(acc);
                break;
            }
        }
    }

    public void clearForm() {
        currentCard = null;
        nameField.clear();
        numberField.clear();
        expirationDatePicker.clear();
        typeCombo.setSelectedIndex(0);
        accountCombo.clearSelection();
    }
}
