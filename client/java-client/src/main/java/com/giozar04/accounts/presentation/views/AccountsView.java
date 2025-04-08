package com.giozar04.accounts.presentation.views;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.infrastructure.services.AccountService;
import com.giozar04.accounts.presentation.components.AccountFormPanel;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.MainContentPanel;
import com.giozar04.shared.components.table.ColumnDefinition;
import com.giozar04.shared.components.table.GenericTablePanel;
import com.giozar04.shared.components.table.OptionsCellEditor;
import com.giozar04.shared.components.table.OptionsCellRenderer;
import com.giozar04.shared.components.table.PopupMenuActionHandler;
import com.giozar04.shared.utils.DialogUtil;

public class AccountsView extends JPanel implements PopupMenuActionHandler {

    private final AccountService accountService;
    private JTextField searchField;
    private GenericTablePanel<Account> tablePanel;

    public AccountsView() {
        accountService = AccountService.getInstance();

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        initTablePanel();
    }

    private JPanel createTopPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("Gestión de Cuentas");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        JButton newButton = new JButton("Nueva Cuenta");
        newButton.addActionListener(this::handleNewAccount);
        headerPanel.add(newButton, BorderLayout.EAST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Buscar:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        JButton searchButton = new JButton("Buscar");
        searchButton.addActionListener(e -> performSearch());
        searchPanel.add(searchButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        return topPanel;
    }

    private void initTablePanel() {
        List<ColumnDefinition<Account>> columns = Arrays.asList(
                new ColumnDefinition<>("Nombre", Account::getName),
                new ColumnDefinition<>("Tipo", Account::getType),
                new ColumnDefinition<>("Balance", a -> String.format("$%.2f", a.getCurrentBalance())),
                new ColumnDefinition<>("Opciones", a -> "···")
        );

        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        columns.get(2).setRenderer(rightAlign);

        columns.get(3).setRenderer(new OptionsCellRenderer());
        columns.get(3).setEditor(new OptionsCellEditor(this));

        try {
            List<Account> accounts = accountService.getAllAccounts();
            tablePanel = new GenericTablePanel<>(columns, accounts);
            add(tablePanel, BorderLayout.CENTER);
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al cargar las cuentas.");
        }
    }

    private void loadAccounts() {
        try {
            List<Account> accounts = accountService.getAllAccounts();
            tablePanel.setData(accounts);
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al recargar las cuentas.");
        }
    }

    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();
        try {
            List<Account> accounts = accountService.getAllAccounts();
            List<Account> filtered = accounts.stream()
                    .filter(a -> a.getName().toLowerCase().contains(query))
                    .collect(Collectors.toList());
            tablePanel.setData(filtered);
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al buscar: " + e.getMessage());
        }
    }

    private void handleNewAccount(ActionEvent e) {
        JButton btn = (JButton) e.getSource();
        btn.setEnabled(false);
        MainContentPanel mainPanel = getMainContentPanel();
        if (mainPanel != null) {
            mainPanel.setView(new CreateAccountView());
        }
    }

    private MainContentPanel getMainContentPanel() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof MainContentPanel)) {
            parent = parent.getParent();
        }
        return (MainContentPanel) parent;
    }

    @Override
    public void onEdit(int rowIndex) {
        Account account = tablePanel.getItemAt(rowIndex);
        AccountFormPanel form = new AccountFormPanel();
        form.loadAccount(account);
        MainContentPanel main = getMainContentPanel();
        if (main != null) main.setView(form);
    }

    @Override
    public void onDelete(int rowIndex) {
        Account account = tablePanel.getItemAt(rowIndex);
        boolean confirm = DialogUtil.showConfirm(this, "¿Desea eliminar esta cuenta?", "Confirmar eliminación");
        if (confirm) {
            try {
                accountService.deleteAccountById(account.getId());
                JOptionPane.showMessageDialog(this, "Cuenta eliminada.");
                loadAccounts();
            } catch (ClientOperationException | HeadlessException ex) {
                DialogUtil.showError(this, "No se pudo eliminar: " + ex.getMessage());
            }
        }
    }

    @Override
    public void onViewDetails(int rowIndex) {
        JOptionPane.showMessageDialog(this, "Función no implementada.");
    }

    @Override
    public void addNotify() {
        super.addNotify();
        loadAccounts();
    }
}
