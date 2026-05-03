package com.giozar04.accounts.presentation.components;

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
import com.giozar04.accounts.presentation.components.subpanels.BankDetailsSubPanel;
import com.giozar04.accounts.presentation.components.subpanels.CreditDetailsSubPanel;
import com.giozar04.accounts.presentation.components.subpanels.SavingsDetailsSubPanel;
import com.giozar04.accounts.presentation.views.AccountsView;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.MainContentPanel;
import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.shared.utils.FormValidatorUtils;
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.infrastructure.services.UserService;

/**
 * Panel principal de formulario para crear y editar cuentas.
 *
 * Principios SOLID aplicados:
 *  - Single Responsibility: cada subpanel maneja sus propios campos y validaciones.
 *  - Open/Closed: añadir un nuevo tipo de cuenta implica solo crear un nuevo subpanel.
 *  - Liskov: los subpaneles exponen una interfaz coherente (validate, applyTo, loadFrom, clear).
 */
public class AccountFormPanel extends JPanel {

    // --- Servicios ---
    private final UserService userService = UserService.getInstance();

    // --- Campos base (comunes a todos los tipos) ---
    private final FormComboBox<User> userCombo;
    private final FormField nameField;
    private final FormComboBox<AccountTypes> typeCombo;
    private final FormField balanceField;

    // --- Subpaneles por tipo ---
    private final BankDetailsSubPanel   bankDetailsPanel;
    private final CreditDetailsSubPanel creditDetailsPanel;
    private final SavingsDetailsSubPanel savingsDetailsPanel;

    // --- Botones ---
    private final JButton saveButton;
    private final JButton cancelButton;
    private final JButton backButton;

    // --- Estado ---
    private Account currentAccount;

    public AccountFormPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Campos base
        userCombo = new FormComboBox<>("Usuario propietario:", 400, 40);
        userCombo.setPlaceholder("Selecciona un usuario...");
        loadUsers();

        nameField = new FormField("Nombre:", false, 400, 40);

        typeCombo = new FormComboBox<>("Tipo de cuenta:", 400, 40);
        typeCombo.setPlaceholder("Selecciona un tipo...");
        typeCombo.setItems(List.of(AccountTypes.values()));

        balanceField = new FormField("Balance actual:", false, 400, 40);

        // Subpaneles de extensión
        bankDetailsPanel    = new BankDetailsSubPanel();
        creditDetailsPanel  = new CreditDetailsSubPanel();
        savingsDetailsPanel = new SavingsDetailsSubPanel();

        // Construir el panel
        formPanel.add(userCombo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(nameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(typeCombo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(balanceField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(bankDetailsPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(creditDetailsPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(savingsDetailsPanel);

        add(formPanel, BorderLayout.CENTER);

        // Botones
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

        // Escuchar cambio de tipo para mostrar/ocultar subpaneles
        typeCombo.addActionListener(e -> updateSubPanelVisibility());
        updateSubPanelVisibility();
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

    // ------------------------------------------------------------------
    // Visibilidad dinámica de subpaneles según el tipo seleccionado
    // ------------------------------------------------------------------

    private void updateSubPanelVisibility() {
        AccountTypes type = typeCombo.getSelectedItem();

        boolean usesBankDetails   = type == AccountTypes.DEBIT
                                 || type == AccountTypes.CREDIT
                                 || type == AccountTypes.WALLET
                                 || type == AccountTypes.BENEFIT;
        boolean usesCreditDetails = type == AccountTypes.CREDIT;
        boolean usesSavings       = type == AccountTypes.SAVINGS;

        bankDetailsPanel.setVisible(usesBankDetails);
        creditDetailsPanel.setVisible(usesCreditDetails);
        savingsDetailsPanel.setVisible(usesSavings);

        revalidate();
        repaint();
    }

    // ------------------------------------------------------------------
    // Guardar
    // ------------------------------------------------------------------

    private void handleSave() {
        List<String> errors = new ArrayList<>();
        AccountTypes type = typeCombo.getSelectedItem();
        User user = userCombo.getSelectedItem();

        // Validaciones base
        if (user == null || !userCombo.isSelectionValid()) {
            errors.add("Debe seleccionar un usuario propietario.");
        }
        FormValidatorUtils.isRequired(nameField.getValue().trim(), "Nombre", errors);
        FormValidatorUtils.isPositiveNumber(balanceField.getValue().trim(), "Balance actual", errors);

        // Validaciones por subpanel visible
        boolean usesBankDetails = type == AccountTypes.DEBIT
                               || type == AccountTypes.CREDIT
                               || type == AccountTypes.WALLET
                               || type == AccountTypes.BENEFIT;

        if (usesBankDetails) {
            bankDetailsPanel.validate(type, errors);
        }
        if (type == AccountTypes.CREDIT) {
            creditDetailsPanel.validate(errors);
        }
        if (type == AccountTypes.SAVINGS) {
            savingsDetailsPanel.validate(errors);
        }

        if (!errors.isEmpty()) {
            DialogUtil.showError(this, FormValidatorUtils.formatErrorMessage(errors));
            return;
        }

        // Construir entidad
        Account account = currentAccount != null ? currentAccount : new Account();
        account.setUserId(user.getId());
        account.setName(nameField.getValue().trim());
        account.setType(type);
        account.setCurrentBalance(Double.parseDouble(balanceField.getValue().trim()));

        // Limpiar campos extendidos antes de aplicar
        account.setBankClientId(null);
        account.setAccountNumber(null);
        account.setClabe(null);
        account.setCanTransferOut(true);
        account.setCreditLimit(null);
        account.setCutoffDay(null);
        account.setPaymentDay(null);
        account.setAnnualYield(null);
        account.setYieldCapAmount(null);
        account.setLastYieldCalculation(null);

        // Aplicar subpaneles según tipo
        if (usesBankDetails) {
            bankDetailsPanel.applyTo(account, type);
        }
        if (type == AccountTypes.CREDIT) {
            creditDetailsPanel.applyTo(account);
        }
        if (type == AccountTypes.SAVINGS) {
            savingsDetailsPanel.applyTo(account);
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

    // ------------------------------------------------------------------
    // Cargar para edición
    // ------------------------------------------------------------------

    public void loadAccount(Account account) {
        this.currentAccount = account;

        // Usuario propietario
        for (int i = 0; i < userCombo.getItemCount(); i++) {
            User u = userCombo.getItemAt(i);
            if (u.getId() == account.getUserId()) {
                userCombo.setSelectedItem(u);
                break;
            }
        }

        nameField.setValue(account.getName());
        typeCombo.setSelectedItem(account.getType());
        balanceField.setValue(String.valueOf(account.getCurrentBalance()));

        bankDetailsPanel.loadFrom(account);
        creditDetailsPanel.loadFrom(account);
        savingsDetailsPanel.loadFrom(account);

        updateSubPanelVisibility();
    }

    // ------------------------------------------------------------------
    // Limpiar
    // ------------------------------------------------------------------

    public void clearForm() {
        currentAccount = null;
        userCombo.clearSelection();
        nameField.clear();
        balanceField.clear();
        typeCombo.setSelectedIndex(0);
        bankDetailsPanel.clear();
        creditDetailsPanel.clear();
        savingsDetailsPanel.clear();
        updateSubPanelVisibility();
    }

    // ------------------------------------------------------------------
    // Navegación
    // ------------------------------------------------------------------

    private void handleBack() {
        MainContentPanel mainPanel = getMainContentPanel();
        if (mainPanel != null) {
            mainPanel.setView(new AccountsView());
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
