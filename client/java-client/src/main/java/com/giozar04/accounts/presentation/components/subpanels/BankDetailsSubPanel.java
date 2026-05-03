package com.giozar04.accounts.presentation.components.subpanels;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.bankClients.infrastructure.services.BankClientService;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.shared.utils.FormValidatorUtils;

/**
 * Subpanel para campos de bank_details (DEBIT, WALLET, BENEFIT, CREDIT).
 * Gestiona: cliente bancario, número de cuenta, CLABE.
 * El campo canTransferOut se controla automáticamente según el AccountType
 * (BENEFIT = false por defecto; DEBIT/WALLET = true por defecto).
 */
public class BankDetailsSubPanel extends JPanel {

    private final FormComboBox<BankClient> bankClientCombo;
    private final FormField accountNumberField;
    private final FormField clabeField;

    private final BankClientService bankClientService = BankClientService.getInstance();

    public BankDetailsSubPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        bankClientCombo = new FormComboBox<>("Cliente bancario:", 400, 40);
        bankClientCombo.setPlaceholder("Selecciona un cliente...");
        loadBankClients();

        accountNumberField = new FormField("Número de cuenta:", false, 400, 40);
        clabeField = new FormField("CLABE:", false, 400, 40);

        add(bankClientCombo);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(accountNumberField);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(clabeField);
    }

    private void loadBankClients() {
        try {
            List<BankClient> clients = bankClientService.getAllBankClients();
            bankClientCombo.setItems(clients);
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al cargar los clientes bancarios: " + ex.getMessage());
        }
    }

    /** Valida los campos requeridos para un tipo que usa bank_details. */
    public void validate(AccountTypes type, List<String> errors) {
        BankClient selected = bankClientCombo.getSelectedItem();
        if (selected == null || !bankClientCombo.isSelectionValid()) {
            errors.add("Debe seleccionar un cliente bancario.");
        }
        FormValidatorUtils.isRequired(accountNumberField.getValue().trim(), "Número de cuenta", errors);
        FormValidatorUtils.isRequired(clabeField.getValue().trim(), "CLABE", errors);
    }

    /** Aplica los valores capturados a la entidad Account. */
    public void applyTo(Account account, AccountTypes type) {
        BankClient selected = bankClientCombo.getSelectedItem();
        account.setBankClientId(selected != null ? selected.getId() : null);
        account.setAccountNumber(accountNumberField.getValue().trim());
        account.setClabe(clabeField.getValue().trim());
        // BENEFIT = vales (sin transferencia), DEBIT/WALLET = con transferencia
        account.setCanTransferOut(type != AccountTypes.BENEFIT);
    }

    /** Carga los valores desde una cuenta existente para edición. */
    public void loadFrom(Account account) {
        accountNumberField.setValue(account.getAccountNumber() != null ? account.getAccountNumber() : "");
        clabeField.setValue(account.getClabe() != null ? account.getClabe() : "");

        if (account.getBankClientId() != null) {
            for (int i = 0; i < bankClientCombo.getItemCount(); i++) {
                BankClient bc = bankClientCombo.getItemAt(i);
                if (bc.getId() == account.getBankClientId()) {
                    bankClientCombo.setSelectedItem(bc);
                    break;
                }
            }
        }
    }

    public void clear() {
        bankClientCombo.clearSelection();
        accountNumberField.clear();
        clabeField.clear();
    }
}
