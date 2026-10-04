package com.giozar04.transactions.presentation.components.sections;

import java.util.List;
import java.util.Objects;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.externalEntities.presentation.components.ExternalEntityFormPanel;
import com.giozar04.shared.components.QuickCreateDialog;
import com.giozar04.shared.components.forms.FormHelpText;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.presentation.components.AccountPickerField;
import com.giozar04.transactions.presentation.components.CreatableSearchField;
import com.giozar04.transactions.presentation.form.TransactionFormContext;
import com.giozar04.transactions.presentation.form.TransactionFormDataProvider;

/**
 * Sección 2 – Origen y destino, según la operación:
 * <ul>
 *   <li>INCOME: entidad externa origen + cuenta destino.</li>
 *   <li>EXPENSE: cuenta origen + entidad externa destino.</li>
 *   <li>REALLOCATION: cuenta origen + cuenta destino (distintas).</li>
 * </ul>
 * Las cuentas se eligen con filtro por tipo; las entidades admiten alta rápida ("+ Nueva").
 * Fija en el contexto la cuenta origen y la cuenta destino.
 */
public class PartiesSection extends AbstractTransactionSection {

    private static final long serialVersionUID = 1L;

    private final FormHelpText hint;
    private final CreatableSearchField<ExternalEntity> sourceEntityField;
    private final AccountPickerField sourceAccountPicker;
    private final AccountPickerField destinationAccountPicker;
    private final CreatableSearchField<ExternalEntity> destinationEntityField;

    private Long lastUserId;
    private OperationTypes lastOperation;

    public PartiesSection(TransactionFormContext context, TransactionFormDataProvider provider) {
        super("Origen y destino", context, provider);

        hint = new FormHelpText("Seleccione el usuario y el tipo de operación para capturar el origen y el destino.",
                FIELD_WIDTH);

        sourceEntityField = createEntityField("Entidad origen:", "Busque quién le paga...");
        sourceAccountPicker = new AccountPickerField("Tipo de origen:", "Cuenta origen:", FIELD_WIDTH, FIELD_HEIGHT);
        destinationAccountPicker = new AccountPickerField("Tipo de destino:", "Cuenta destino:", FIELD_WIDTH, FIELD_HEIGHT);
        destinationEntityField = createEntityField("Entidad destino:", "Busque a quién le paga...");

        addRow(hint);
        addRow(sourceEntityField);
        addRow(sourceAccountPicker);
        addRow(destinationAccountPicker);
        addRow(destinationEntityField);

        sourceAccountPicker.addSelectionListener(e -> context.setSourceAccount(sourceAccountPicker.getSelectedAccount()));
        destinationAccountPicker.addSelectionListener(
                e -> context.setDestinationAccount(destinationAccountPicker.getSelectedAccount()));

        updateVisibility(null, false);
    }

    private CreatableSearchField<ExternalEntity> createEntityField(String label, String placeholder) {
        CreatableSearchField<ExternalEntity> field = new CreatableSearchField<>(label, FIELD_WIDTH, FIELD_HEIGHT);
        field.getCombo().setPlaceholder(placeholder);
        field.getCombo().setIdentityFunction(ExternalEntity::getId);
        field.getCombo().setDisplayFunction(e -> e.getName()
                + (e.getType() != null ? " (" + e.getType().getLabel() + ")" : ""));
        field.setOnCreateNew(() -> quickCreateEntity(field));
        return field;
    }

    // ------------------------------------------------------------------
    // Contexto
    // ------------------------------------------------------------------

    @Override
    public void onContextChanged(TransactionFormContext ctx) {
        if (!Objects.equals(lastUserId, ctx.getUserId())) {
            lastUserId = ctx.getUserId();
            reloadCatalogs();
        }
        if (lastOperation != ctx.getOperation()) {
            lastOperation = ctx.getOperation();
            updateVisibility(lastOperation, true);
        }
        boolean hasUser = ctx.getUser() != null;
        sourceEntityField.setCreateEnabled(hasUser);
        destinationEntityField.setCreateEnabled(hasUser);
    }

    private void reloadCatalogs() {
        List<ExternalEntity> entities = provider.getExternalEntities();
        sourceEntityField.getCombo().setItems(entities);
        destinationEntityField.getCombo().setItems(entities);
        List<Account> accounts = provider.getAccounts();
        sourceAccountPicker.setAccounts(accounts);
        destinationAccountPicker.setAccounts(accounts);
    }

    /** Muestra solo lo que aplica a la operación y limpia lo que deja de aplicar. */
    private void updateVisibility(OperationTypes operation, boolean clearHidden) {
        boolean showSourceEntity = operation == OperationTypes.INCOME;
        boolean showSourceAccount = operation == OperationTypes.EXPENSE || operation == OperationTypes.REALLOCATION;
        boolean showDestinationAccount = operation == OperationTypes.INCOME || operation == OperationTypes.REALLOCATION;
        boolean showDestinationEntity = operation == OperationTypes.EXPENSE;

        setRowVisible(hint, operation == null);
        setRowVisible(sourceEntityField, showSourceEntity);
        setRowVisible(sourceAccountPicker, showSourceAccount);
        setRowVisible(destinationAccountPicker, showDestinationAccount);
        setRowVisible(destinationEntityField, showDestinationEntity);

        if (clearHidden) {
            if (!showSourceEntity) {
                sourceEntityField.getCombo().clearSelection();
            }
            if (!showSourceAccount) {
                sourceAccountPicker.clear();
            }
            if (!showDestinationAccount) {
                destinationAccountPicker.clear();
            }
            if (!showDestinationEntity) {
                destinationEntityField.getCombo().clearSelection();
            }
        }
        refreshLayout();
    }

    private void quickCreateEntity(CreatableSearchField<ExternalEntity> target) {
        if (context.getUser() == null) {
            return;
        }
        ExternalEntityFormPanel form = new ExternalEntityFormPanel();
        form.presetUser(context.getUser(), true);
        QuickCreateDialog.<ExternalEntity>show(this, "Nueva entidad externa", form, form::setOnSaved)
                .ifPresent(entity -> {
                    provider.addExternalEntity(entity);
                    reloadEntities();
                    target.getCombo().setSelectedItem(entity);
                });
    }

    /** Recarga el catálogo de entidades conservando lo elegido en ambos campos. */
    private void reloadEntities() {
        List<ExternalEntity> entities = provider.getExternalEntities();
        ExternalEntity source = sourceEntityField.getCombo().getSelectedItem();
        ExternalEntity destination = destinationEntityField.getCombo().getSelectedItem();
        sourceEntityField.getCombo().setItems(entities);
        destinationEntityField.getCombo().setItems(entities);
        sourceEntityField.getCombo().setSelectedItem(source);
        destinationEntityField.getCombo().setSelectedItem(destination);
    }

    // ------------------------------------------------------------------
    // Contrato de sección
    // ------------------------------------------------------------------

    @Override
    public void validate(List<String> errors) {
        OperationTypes operation = context.getOperation();
        if (operation == null) {
            return; // lo reporta OperationSection
        }
        switch (operation) {
            case INCOME -> {
                if (sourceEntityField.getCombo().getSelectedItem() == null) {
                    errors.add("Debe seleccionar la entidad externa de origen (quién le paga).");
                }
                if (destinationAccountPicker.getSelectedAccount() == null) {
                    errors.add("Debe seleccionar la cuenta destino.");
                }
            }
            case EXPENSE -> {
                if (sourceAccountPicker.getSelectedAccount() == null) {
                    errors.add("Debe seleccionar la cuenta origen.");
                }
                if (destinationEntityField.getCombo().getSelectedItem() == null) {
                    errors.add("Debe seleccionar la entidad externa de destino (a quién le paga).");
                }
            }
            case REALLOCATION -> {
                Account source = sourceAccountPicker.getSelectedAccount();
                Account destination = destinationAccountPicker.getSelectedAccount();
                if (source == null) {
                    errors.add("Debe seleccionar la cuenta origen.");
                }
                if (destination == null) {
                    errors.add("Debe seleccionar la cuenta destino.");
                }
                if (source != null && destination != null && source.getId() == destination.getId()) {
                    errors.add("La cuenta destino debe ser distinta de la cuenta origen.");
                }
            }
        }
    }

    @Override
    public void applyTo(Transaction tx) {
        OperationTypes operation = context.getOperation();
        Account source = sourceAccountPicker.getSelectedAccount();
        Account destination = destinationAccountPicker.getSelectedAccount();
        ExternalEntity sourceEntity = sourceEntityField.getCombo().getSelectedItem();
        ExternalEntity destinationEntity = destinationEntityField.getCombo().getSelectedItem();

        tx.setSourceAccountId(null);
        tx.setDestinationAccountId(null);
        tx.setExternalEntityId(null);
        switch (operation) {
            case INCOME -> {
                tx.setExternalEntityId(sourceEntity.getId());
                tx.setDestinationAccountId(destination.getId());
            }
            case EXPENSE -> {
                // En WALLET es la wallet; el servidor la sustituye por la cuenta de la tarjeta si paga una vinculada
                tx.setSourceAccountId(source.getId());
                tx.setExternalEntityId(destinationEntity.getId());
            }
            case REALLOCATION -> {
                tx.setSourceAccountId(source.getId());
                tx.setDestinationAccountId(destination.getId());
            }
        }
    }

    @Override
    public void loadFrom(Transaction tx) {
        OperationTypes operation = tx.getOperationType();
        if (operation == null) {
            return;
        }
        // En WALLET el origen guardado puede ser la cuenta de la tarjeta vinculada: en el formulario es la wallet
        Long sourceId = tx.getWalletDetail() != null ? Long.valueOf(tx.getWalletDetail().getWalletAccountId())
                : tx.getSourceAccountId();
        ExternalEntity entity = provider.findExternalEntity(tx.getExternalEntityId());
        switch (operation) {
            case INCOME -> {
                sourceEntityField.getCombo().setSelectedItem(entity);
                destinationAccountPicker.selectAccountById(tx.getDestinationAccountId());
            }
            case EXPENSE -> {
                sourceAccountPicker.selectAccountById(sourceId);
                destinationEntityField.getCombo().setSelectedItem(entity);
            }
            case REALLOCATION -> {
                sourceAccountPicker.selectAccountById(sourceId);
                destinationAccountPicker.selectAccountById(tx.getDestinationAccountId());
            }
        }
    }

    @Override
    public void clear() {
        sourceEntityField.getCombo().clearSelection();
        destinationEntityField.getCombo().clearSelection();
        sourceAccountPicker.clear();
        destinationAccountPicker.clear();
    }
}
