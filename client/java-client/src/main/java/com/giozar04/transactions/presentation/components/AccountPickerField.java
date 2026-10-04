package com.giozar04.transactions.presentation.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.shared.components.forms.FormSearchComboBox;

/**
 * Selector de cuenta con filtro por tipo: combo "Todos los tipos" + {@link AccountTypes}
 * y {@link FormSearchComboBox} con las cuentas que cumplen el filtro.
 *
 * <p>Al cambiar el filtro se conserva la cuenta elegida si sigue cumpliéndolo. Los listeners de
 * {@link #addSelectionListener(ActionListener)} se disparan solo cuando cambia la cuenta seleccionada.</p>
 */
public class AccountPickerField extends JPanel {

    private static final long serialVersionUID = 1L;

    private final FormComboBox<AccountTypes> typeFilter;
    private final FormSearchComboBox<Account> accountCombo;
    private final List<ActionListener> listeners = new ArrayList<>();

    private List<Account> allAccounts = new ArrayList<>();
    private boolean adjusting;

    public AccountPickerField(String typeLabel, String accountLabel, int width, int height) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        typeFilter = new FormComboBox<>(typeLabel, width, height);
        typeFilter.setPlaceholder("Todos los tipos");
        typeFilter.setItems(List.of(AccountTypes.values()));
        typeFilter.setAlignmentX(Component.LEFT_ALIGNMENT);

        accountCombo = new FormSearchComboBox<>(accountLabel, width, height);
        accountCombo.setPlaceholder("Busque una cuenta...");
        accountCombo.setIdentityFunction(Account::getId);
        accountCombo.setDisplayFunction(a -> a.getName()
                + (a.getType() != null ? " (" + a.getType().getLabel() + ")" : ""));
        accountCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(typeFilter);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(accountCombo);

        typeFilter.addActionListener(e -> applyFilter());
        accountCombo.addActionListener(e -> {
            if (!adjusting) {
                fireSelectionChanged();
            }
        });
    }

    /** Catálogo de cuentas (se aplica el filtro actual). */
    public void setAccounts(List<Account> accounts) {
        allAccounts = accounts != null ? new ArrayList<>(accounts) : new ArrayList<>();
        applyFilter();
    }

    public Account getSelectedAccount() {
        return accountCombo.getSelectedItem();
    }

    /** Quita el filtro y selecciona la cuenta con ese id (o limpia si no existe). */
    public void selectAccountById(Long accountId) {
        Account target = null;
        if (accountId != null) {
            for (Account account : allAccounts) {
                if (account.getId() == accountId) {
                    target = account;
                    break;
                }
            }
        }
        resetFilterAndSelect(target);
    }

    /** Quita el filtro y la selección. */
    public void clear() {
        resetFilterAndSelect(null);
    }

    public void addSelectionListener(ActionListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        typeFilter.getComboBox().setEnabled(enabled);
        accountCombo.setEnabled(enabled);
    }

    private void applyFilter() {
        if (accountCombo == null || adjusting) {
            return; // durante la construcción o en un cambio interno
        }
        Account previous = accountCombo.getSelectedItem();
        AccountTypes type = typeFilter.getSelectedItem();
        List<Account> filtered = type == null ? allAccounts
                : allAccounts.stream().filter(a -> a.getType() == type).toList();
        Account keep = previous != null && filtered.stream().anyMatch(a -> a.getId() == previous.getId())
                ? previous : null;
        replaceItems(filtered, keep, previous);
    }

    private void resetFilterAndSelect(Account target) {
        Account previous = accountCombo.getSelectedItem();
        adjusting = true;
        try {
            typeFilter.clearSelection();
        } finally {
            adjusting = false;
        }
        replaceItems(allAccounts, target, previous);
    }

    /** Cambia la lista visible y la selección sin notificar pasos intermedios; notifica una vez si cambió. */
    private void replaceItems(List<Account> items, Account selection, Account previous) {
        adjusting = true;
        try {
            accountCombo.setItems(items);
            accountCombo.setSelectedItem(selection);
        } finally {
            adjusting = false;
        }
        Account current = accountCombo.getSelectedItem();
        boolean changed = previous == null ? current != null
                : current == null || current.getId() != previous.getId();
        if (changed) {
            fireSelectionChanged();
        }
    }

    private void fireSelectionChanged() {
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "accountChanged");
        for (ActionListener listener : new ArrayList<>(listeners)) {
            listener.actionPerformed(event);
        }
    }
}
