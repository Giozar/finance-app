package com.giozar04.cards.presentation.components;

import java.awt.BorderLayout;
import java.awt.Container;
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
import com.giozar04.card.domain.entities.Card;
import com.giozar04.card.domain.enums.CardTypes;
import com.giozar04.cards.infrastructure.services.CardService;
import com.giozar04.cards.presentation.views.CardsView;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.MainContentPanel;
import com.giozar04.shared.components.DatePickerComponent;
import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.shared.utils.FormValidatorUtils;
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.infrastructure.services.UserService;

/**
 * Formulario para crear y editar tarjetas.
 *
 * Flujo:
 *  1. El usuario selecciona el propietario (User).
 *  2. Se cargan reactivamente solo las cuentas DEBIT, CREDIT o BENEFIT de ese usuario.
 *  3. El usuario selecciona la cuenta a la que se vinculará la tarjeta.
 *  4. Completa los demás campos y guarda.
 */
public class CardFormPanel extends JPanel {

    // --- Servicios ---
    private final UserService    userService    = UserService.getInstance();
    private final AccountService accountService = AccountService.getInstance();

    // --- Campos del formulario ---
    private final FormComboBox<User>      userCombo;
    private final FormComboBox<Account>   accountCombo;
    private final FormField               nameField;
    private final FormComboBox<CardTypes> typeCombo;
    private final FormField               numberField;
    private final DatePickerComponent     expirationDatePicker;

    // --- Botones ---
    private final JButton saveButton;
    private final JButton cancelButton;
    private final JButton backButton;

    // --- Estado ---
    private Card currentCard;

    // Cache de cuentas de todos los usuarios (cargadas una vez)
    private List<Account> allEligibleAccounts = new ArrayList<>();

    public CardFormPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Usuario propietario ---
        userCombo = new FormComboBox<>("Usuario propietario:", 400, 40);
        userCombo.setPlaceholder("Selecciona un usuario...");
        loadUsers();

        // --- Cuenta asociada (se filtra según el usuario) ---
        accountCombo = new FormComboBox<>("Cuenta asociada:", 400, 40);
        accountCombo.setPlaceholder("Selecciona primero un usuario...");

        // --- Nombre de la tarjeta ---
        nameField = new FormField("Nombre / Alias:", false, 400, 40);

        // --- Tipo de tarjeta ---
        typeCombo = new FormComboBox<>("Tipo de tarjeta:", 400, 40);
        typeCombo.setPlaceholder("Selecciona un tipo...");
        typeCombo.setItems(List.of(CardTypes.values()));

        // --- Últimos 4 dígitos ---
        numberField = new FormField("Últimos 4 dígitos:", false, 400, 40);

        // --- Fecha de expiración ---
        expirationDatePicker = new DatePickerComponent();

        // --- Construir el panel ---
        formPanel.add(userCombo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(accountCombo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(nameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(typeCombo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(numberField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(expirationDatePicker);

        add(formPanel, BorderLayout.CENTER);

        // --- Botones ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton   = new JButton("Guardar");
        cancelButton = new JButton("Cancelar");
        backButton   = new JButton("Regresar");

        saveButton.addActionListener(e -> handleSave());
        cancelButton.addActionListener(e -> clearForm());
        backButton.addActionListener(e -> handleBack());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Reacción al cambio de usuario ---
        userCombo.addActionListener(e -> onUserSelected());

        // Carga previa de todas las cuentas elegibles (para modo edición y para filtrar)
        loadAllEligibleAccounts();
    }

    // ------------------------------------------------------------------
    // Carga de datos
    // ------------------------------------------------------------------

    private void loadUsers() {
        try {
            List<User> users = userService.getAllUsers();
            userCombo.setItems(users);
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al cargar los usuarios: " + ex.getMessage());
        }
    }

    private void loadAllEligibleAccounts() {
        try {
            List<Account> all = accountService.getAllAccounts();
            allEligibleAccounts = new ArrayList<>();
            for (Account acc : all) {
                if (acc.getType() == AccountTypes.DEBIT
                        || acc.getType() == AccountTypes.CREDIT
                        || acc.getType() == AccountTypes.BENEFIT) {
                    allEligibleAccounts.add(acc);
                }
            }
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al cargar las cuentas: " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Evento: cambio de usuario → filtrar cuentas
    // ------------------------------------------------------------------

    private void onUserSelected() {
        User selectedUser = userCombo.getSelectedItem();

        if (selectedUser == null || !userCombo.isSelectionValid()) {
            accountCombo.setPlaceholder("Selecciona primero un usuario...");
            accountCombo.setItems(List.of());
            return;
        }

        List<Account> userAccounts = new ArrayList<>();
        for (Account acc : allEligibleAccounts) {
            if (acc.getUserId() == selectedUser.getId()) {
                userAccounts.add(acc);
            }
        }

        if (userAccounts.isEmpty()) {
            accountCombo.setPlaceholder("Este usuario no tiene cuentas elegibles.");
            accountCombo.setItems(List.of());
        } else {
            accountCombo.setPlaceholder("Selecciona una cuenta...");
            accountCombo.setItems(userAccounts);
        }
    }

    // ------------------------------------------------------------------
    // Guardar
    // ------------------------------------------------------------------

    private void handleSave() {
        List<String> errors = new ArrayList<>();

        User selectedUser       = userCombo.getSelectedItem();
        Account selectedAccount = accountCombo.getSelectedItem();
        String name             = nameField.getValue().trim();
        String cardNumber       = numberField.getValue().trim();
        ZonedDateTime expiration = expirationDatePicker.getDate();
        CardTypes type          = typeCombo.getSelectedItem();

        // Validaciones
        if (selectedUser == null || !userCombo.isSelectionValid()) {
            errors.add("Debe seleccionar un usuario propietario.");
        }
        if (selectedAccount == null || !accountCombo.isSelectionValid()) {
            errors.add("Debe seleccionar una cuenta válida.");
        }
        FormValidatorUtils.isRequired(name, "Nombre / Alias", errors);
        if (type == null) {
            errors.add("Debe seleccionar el tipo de tarjeta.");
        }
        FormValidatorUtils.isRequired(cardNumber, "Últimos 4 dígitos", errors);
        if (!cardNumber.isEmpty() && !cardNumber.matches("\\d{4}")) {
            errors.add("Los últimos 4 dígitos deben ser exactamente 4 números.");
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
        card.setAccountId(selectedAccount.getId());

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
            handleBack();
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al guardar la tarjeta: " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Cargar para edición
    // ------------------------------------------------------------------

    public void loadCard(Card card) {
        this.currentCard = card;

        // Recargar cuentas si aún no se han cargado
        if (allEligibleAccounts.isEmpty()) {
            loadAllEligibleAccounts();
        }

        // 1. Buscar la cuenta de la tarjeta para conocer el usuario propietario
        Account cardAccount = null;
        for (Account acc : allEligibleAccounts) {
            if (acc.getId() == card.getAccountId()) {
                cardAccount = acc;
                break;
            }
        }

        // 2. Seleccionar el usuario propietario en el combo
        if (cardAccount != null) {
            long ownerId = cardAccount.getUserId();
            for (int i = 0; i < userCombo.getItemCount(); i++) {
                User u = userCombo.getItemAt(i);
                if (u.getId() == ownerId) {
                    // Temporalmente quitamos el listener para evitar doble disparo
                    userCombo.setSelectedItem(u);
                    break;
                }
            }
        }

        // 3. Forzar la carga de cuentas del usuario seleccionado
        //    (el ActionListener puede no haberse disparado si el item ya estaba seleccionado)
        onUserSelected();

        // 4. Ahora sí seleccionar la cuenta correcta en el combo ya poblado
        for (int i = 0; i < accountCombo.getItemCount(); i++) {
            Account acc = accountCombo.getItemAt(i);
            if (acc.getId() == card.getAccountId()) {
                accountCombo.setSelectedItem(acc);
                break;
            }
        }

        // 5. Rellenar el resto de campos
        nameField.setValue(card.getName());
        typeCombo.setSelectedItem(card.getCardType());
        numberField.setValue(card.getCardNumber());
        expirationDatePicker.setDate(card.getExpirationDate());
    }

    // ------------------------------------------------------------------
    // Limpiar
    // ------------------------------------------------------------------

    public void clearForm() {
        currentCard = null;
        userCombo.clearSelection();
        accountCombo.clearSelection();
        accountCombo.setItems(List.of());
        accountCombo.setPlaceholder("Selecciona primero un usuario...");
        nameField.clear();
        numberField.clear();
        expirationDatePicker.clear();
        typeCombo.setSelectedIndex(0);
    }

    // ------------------------------------------------------------------
    // Navegación
    // ------------------------------------------------------------------

    private void handleBack() {
        MainContentPanel mainPanel = getMainContentPanel();
        if (mainPanel != null) {
            mainPanel.setView(new CardsView());
        }
    }

    private MainContentPanel getMainContentPanel() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof MainContentPanel)) {
            parent = parent.getParent();
        }
        return (MainContentPanel) parent;
    }
}
