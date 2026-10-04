package com.giozar04.accountReconciliations.presentation.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import com.giozar04.accountReconciliations.domain.entities.AccountReconciliation;
import com.giozar04.accountReconciliations.infrastructure.services.AccountReconciliationService;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.shared.components.table.ColumnDefinition;
import com.giozar04.shared.components.table.GenericTablePanel;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.infrastructure.services.UserService;

/**
 * Vista de conciliación de cuentas: compara el saldo esperado según el historial
 * de transacciones con el saldo real de cada cuenta y permite ajustar las descuadradas.
 */
public class AccountReconciliationsView extends JPanel {

    private static final String STATUS_BALANCED = "Cuadrada";
    private static final String STATUS_UNBALANCED = "Descuadrada";

    private final AccountReconciliationService reconciliationService;
    private final UserService userService;

    private FormComboBox<User> userCombo;
    private GenericTablePanel<AccountReconciliation> tablePanel;
    private JButton reconcileButton;

    public AccountReconciliationsView() {
        reconciliationService = AccountReconciliationService.getInstance();
        userService = UserService.getInstance();

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(createTopPanel(), BorderLayout.NORTH);
        initTablePanel();
        add(createBottomPanel(), BorderLayout.SOUTH);

        loadUsers();
        // El listener se añade después de cargar usuarios para no recargar en cada setItems
        userCombo.addActionListener(e -> loadReconciliations());
    }

    private JPanel createTopPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("Conciliación de cuentas");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        JButton refreshButton = new JButton("Actualizar");
        refreshButton.addActionListener(e -> loadReconciliations());
        headerPanel.add(refreshButton, BorderLayout.EAST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userCombo = new FormComboBox<>("Usuario:", 450, 30);
        userCombo.setPlaceholder("Todos los usuarios");
        filterPanel.add(userCombo);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);

        return topPanel;
    }

    private void initTablePanel() {
        List<ColumnDefinition<AccountReconciliation>> columns = Arrays.asList(
                new ColumnDefinition<>("Cuenta", AccountReconciliation::getAccountName),
                new ColumnDefinition<>("Tipo", r -> r.getAccountType() != null ? r.getAccountType().getLabel() : "—"),
                new ColumnDefinition<>("Saldo inicial", r -> formatAmount(r.getOpeningNet())),
                new ColumnDefinition<>("Entradas", r -> formatAmount(r.getTotalInflows())),
                new ColumnDefinition<>("Salidas", r -> formatAmount(r.getTotalOutflows())),
                new ColumnDefinition<>("Esperado", r -> formatAmount(r.getExpectedNet())),
                new ColumnDefinition<>("Real", r -> formatAmount(r.getActualNet())),
                new ColumnDefinition<>("Diferencia", r -> formatAmount(r.getDifference())),
                new ColumnDefinition<>("Estado", r -> r.isBalanced() ? STATUS_BALANCED : STATUS_UNBALANCED)
        );

        columns.get(8).setRenderer(createStatusCellRenderer());

        tablePanel = new GenericTablePanel<>(columns, new ArrayList<>());
        JTable table = tablePanel.getTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateReconcileButtonState();
            }
        });
        add(tablePanel, BorderLayout.CENTER);
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        reconcileButton = new JButton("Ajustar saldo de la cuenta seleccionada");
        reconcileButton.setEnabled(false);
        reconcileButton.addActionListener(e -> handleReconcile());
        bottomPanel.add(reconcileButton);
        return bottomPanel;
    }

    private DefaultTableCellRenderer createStatusCellRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(getFont().deriveFont(Font.BOLD));
                if (!isSelected) {
                    comp.setForeground(STATUS_BALANCED.equals(value)
                            ? new Color(30, 140, 60)
                            : new Color(200, 40, 40));
                }
                return comp;
            }
        };
    }

    private void loadUsers() {
        try {
            List<User> users = userService.getAllUsers();
            userCombo.setItems(users);
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al cargar los usuarios: " + e.getMessage());
        }
    }

    private void loadReconciliations() {
        try {
            User user = userCombo.getSelectedItem();
            List<AccountReconciliation> reconciliations = user == null
                    ? reconciliationService.getAllAccountReconciliations()
                    : reconciliationService.getAccountReconciliationsByUserId(user.getId());
            tablePanel.setData(reconciliations);
        } catch (ClientOperationException | RuntimeException e) {
            DialogUtil.showError(this, e.getMessage());
        }
        updateReconcileButtonState();
    }

    private AccountReconciliation getSelectedReconciliation() {
        int row = tablePanel.getTable().getSelectedRow();
        return row >= 0 ? tablePanel.getItemAt(row) : null;
    }

    private void updateReconcileButtonState() {
        AccountReconciliation selected = getSelectedReconciliation();
        reconcileButton.setEnabled(selected != null && !selected.isBalanced());
    }

    private void handleReconcile() {
        AccountReconciliation selected = getSelectedReconciliation();
        if (selected == null || selected.isBalanced()) {
            return;
        }

        String message = String.format(
                "Se ajustará el saldo de la cuenta \"%s\" al valor esperado según su historial de transacciones.%n%n"
                + "Saldo real actual: %s%nSaldo esperado: %s%n%n¿Desea continuar?",
                selected.getAccountName(),
                formatAmount(selected.getActualNet()),
                formatAmount(selected.getExpectedNet()));

        boolean confirm = DialogUtil.showConfirm(this, message, "Confirmar ajuste de saldo");
        if (!confirm) {
            return;
        }

        try {
            reconciliationService.reconcileAccount(selected.getAccountId());
            DialogUtil.showSuccess(this, "El saldo de la cuenta \"" + selected.getAccountName() + "\" se ajustó correctamente.");
            loadReconciliations();
        } catch (ClientOperationException | RuntimeException ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private String formatAmount(BigDecimal amount) {
        return amount != null ? String.format("$%,.2f", amount) : "—";
    }

    @Override
    public void addNotify() {
        super.addNotify();
        loadReconciliations();
    }
}
