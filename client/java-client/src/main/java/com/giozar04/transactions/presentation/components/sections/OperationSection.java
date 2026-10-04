package com.giozar04.transactions.presentation.components.sections;

import java.util.List;

import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.shared.components.forms.FormHelpText;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.domain.enums.TransactionStatus;
import com.giozar04.transactions.presentation.form.TransactionFormContext;
import com.giozar04.transactions.presentation.form.TransactionFormDataProvider;
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.application.ports.input.UserOperations;
import com.giozar04.bootstrap.ClientUseCases;

/**
 * Sección 1 – Operación: usuario propietario, tipo de operación y estado (por defecto COMPLETED).
 * Es la sección que fija el usuario y la operación en el contexto.
 */
public class OperationSection extends AbstractTransactionSection {

    private static final long serialVersionUID = 1L;

    private final FormComboBox<User> userCombo;
    private final FormComboBox<OperationTypes> operationCombo;
    private final FormComboBox<TransactionStatus> statusCombo;

    public OperationSection(TransactionFormContext context, TransactionFormDataProvider provider) {
        super("Operación", context, provider);

        userCombo = new FormComboBox<>("Usuario propietario:", FIELD_WIDTH, FIELD_HEIGHT);
        userCombo.setPlaceholder("Seleccione un usuario...");
        loadUsers();

        operationCombo = new FormComboBox<>("Tipo de operación:", FIELD_WIDTH, FIELD_HEIGHT);
        operationCombo.setPlaceholder("Seleccione un tipo...");
        operationCombo.setItems(List.of(OperationTypes.values()));

        statusCombo = new FormComboBox<>("Estado:", FIELD_WIDTH, FIELD_HEIGHT);
        statusCombo.setItems(List.of(TransactionStatus.values()));
        statusCombo.setSelectedItem(TransactionStatus.COMPLETED);

        addRow(userCombo);
        addRow(operationCombo);
        addRow(statusCombo);
        addRow(new FormHelpText("Solo las transacciones completadas afectan los saldos de las cuentas.", FIELD_WIDTH));

        userCombo.addActionListener(e -> context.setUser(userCombo.getSelectedItem()));
        operationCombo.addActionListener(e -> context.setOperation(operationCombo.getSelectedItem()));
    }

    private void loadUsers() {
        try {
            userCombo.setItems(ClientUseCases.get(UserOperations.class).getAllUsers());
        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al cargar los usuarios: " + ex.getMessage());
        }
    }

    /** Al editar, el usuario no se puede cambiar (la transacción y sus catálogos son suyos). */
    public void setEditMode(boolean editing) {
        userCombo.getComboBox().setEnabled(!editing);
    }

    /** Selecciona el usuario con ese id (o limpia si no existe). */
    public void selectUserById(long userId) {
        selectInCombo(userCombo, u -> u.getId() == userId);
    }

    @Override
    public void onContextChanged(TransactionFormContext ctx) {
        // Esta sección solo escribe en el contexto.
    }

    @Override
    public void validate(List<String> errors) {
        if (userCombo.getSelectedItem() == null || !userCombo.isSelectionValid()) {
            errors.add("Debe seleccionar un usuario propietario.");
        }
        if (operationCombo.getSelectedItem() == null) {
            errors.add("Debe seleccionar el tipo de operación.");
        }
        if (statusCombo.getSelectedItem() == null) {
            errors.add("Debe seleccionar el estado de la transacción.");
        }
    }

    @Override
    public void applyTo(Transaction tx) {
        tx.setUserId(userCombo.getSelectedItem().getId());
        tx.setOperationType(operationCombo.getSelectedItem());
        tx.setStatus(statusCombo.getSelectedItem());
    }

    @Override
    public void loadFrom(Transaction tx) {
        selectUserById(tx.getUserId());
        operationCombo.setSelectedItem(tx.getOperationType());
        statusCombo.setSelectedItem(tx.getStatus() != null ? tx.getStatus() : TransactionStatus.COMPLETED);
    }

    @Override
    public void clear() {
        userCombo.clearSelection();
        operationCombo.clearSelection();
        statusCombo.setSelectedItem(TransactionStatus.COMPLETED);
    }
}
