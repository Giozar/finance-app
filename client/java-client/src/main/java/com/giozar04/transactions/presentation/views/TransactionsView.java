package com.giozar04.transactions.presentation.views;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.MainContentPanel;
import com.giozar04.shared.components.table.ColumnDefinition;
import com.giozar04.shared.components.table.GenericTablePanel;
import com.giozar04.shared.components.table.OptionsCellEditor;
import com.giozar04.shared.components.table.OptionsCellRenderer;
import com.giozar04.shared.components.table.PopupMenuActionHandler;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.infrastructure.services.TransactionService;
import com.giozar04.transactions.presentation.components.PaymentMethodCellRenderer;
import com.giozar04.transactions.presentation.components.TransactionFormPanel;
import com.giozar04.transactions.presentation.components.TransactionTypeCellRenderer;

public class TransactionsView extends JPanel implements PopupMenuActionHandler {

    private final JTextField searchField;
    private final JComboBox<String> filterCombo;
    private GenericTablePanel<Transaction> tablePanel;
    private TransactionService transactionService;

    public TransactionsView() {
        transactionService = TransactionService.getInstance();
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Encabezado y botón para nueva transacción
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("Gestión de Transacciones");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        JButton newTransactionButton = new JButton("Nueva Transacción");
        newTransactionButton.addActionListener(this::handleNewTransaction);
        headerPanel.add(newTransactionButton, BorderLayout.EAST);

        // Barra de búsqueda y filtro
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.add(new JLabel("Buscar:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);

        filterCombo = new JComboBox<>(new String[]{"Todos los tipos", "INCOME", "EXPENSE"});
        searchPanel.add(filterCombo);

        JButton searchButton = new JButton("Buscar");
        searchButton.addActionListener(e -> performSearch());
        searchPanel.add(searchButton);
        // Panel superior que agrupa encabezado y búsqueda
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Definición de columnas para la tabla de transacciones
        List<ColumnDefinition<Transaction>> columns = Arrays.asList(
                new ColumnDefinition<>("Título", Transaction::getTitle),
                new ColumnDefinition<>("Tipo", Transaction::getType),
                new ColumnDefinition<>("Categoría", Transaction::getCategory),
                new ColumnDefinition<>("Método de Pago", Transaction::getPaymentMethod),
                new ColumnDefinition<>("Monto", t -> String.format("$%.2f", t.getAmount())),
                new ColumnDefinition<>("Fecha", t -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    return sdf.format(java.util.Date.from(t.getDate().toInstant()));
                }),
                new ColumnDefinition<>("Opciones", t -> "···")
        );

        // Asignar renderizadores y editores específicos
        columns.get(1).setRenderer(new TransactionTypeCellRenderer());
        columns.get(3).setRenderer(new PaymentMethodCellRenderer());
        columns.get(6).setRenderer(new OptionsCellRenderer());
        columns.get(6).setEditor(new OptionsCellEditor(this));
        // Alinear la columna "Monto" a la derecha
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        columns.get(4).setRenderer(rightRenderer);

        // Crear el componente de tabla genérico con los datos de transacciones
        try {
            List<Transaction> transactions = transactionService.getAllTransactions();
            tablePanel = new GenericTablePanel<>(columns, transactions);
            add(tablePanel, BorderLayout.CENTER);
        } catch (ClientOperationException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar las transacciones", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTransactions() {
        try {
            List<Transaction> transactions = transactionService.getAllTransactions();
            tablePanel.setData(transactions);
        } catch (ClientOperationException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar las transacciones", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();
        String typeFilter = (String) filterCombo.getSelectedItem();
        try {
            List<Transaction> allTransactions = transactionService.getAllTransactions();
            List<Transaction> filtered = allTransactions.stream().filter(t -> {
                boolean matchesQuery = t.getTitle().toLowerCase().contains(query)
                        || t.getCategory().toLowerCase().contains(query)
                        || t.getPaymentMethod().toString().toLowerCase().contains(query);
                boolean matchesType = typeFilter.equals("Todos los tipos") || t.getType().equalsIgnoreCase(typeFilter);
                return matchesQuery && matchesType;
            }).toList();
            tablePanel.setData(filtered);
        } catch (ClientOperationException e) {
            JOptionPane.showMessageDialog(this, "Error al buscar las transacciones: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Maneja la acción de crear una nueva transacción.
     */
    private void handleNewTransaction(ActionEvent e) {
        Container parent = getParent();
        while (parent != null && !(parent instanceof MainContentPanel)) {
            parent = parent.getParent();
        }
        if (parent instanceof MainContentPanel mainContentPanel) {
            // Utiliza CreateTransactionView para crear una nueva transacción
            mainContentPanel.setView(new CreateTransactionView());
        }
    }

    // Implementación de PopupMenuActionHandler

    @Override
    public void onEdit(int rowIndex) {
        Transaction transaction = tablePanel.getItemAt(rowIndex);
        Container parent = getParent();
        while (parent != null && !(parent instanceof MainContentPanel)) {
            parent = parent.getParent();
        }
        if (parent instanceof MainContentPanel mainContentPanel) {
            // Utiliza TransactionFormPanel para la edición
            TransactionFormPanel editView = new TransactionFormPanel();
            editView.loadTransaction(transaction);
            mainContentPanel.setView(editView);
        }
    }

    @Override
    public void onDelete(int rowIndex) {
        Transaction transaction = tablePanel.getItemAt(rowIndex);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar la transacción seleccionada?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                transactionService.deleteTransactionById(transaction.getId());
                JOptionPane.showMessageDialog(this, "Transacción eliminada.");
                loadTransactions();
            } catch (ClientOperationException | HeadlessException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar la transacción: " + ex.getMessage());
            }
        }
    }

    @Override
    public void onViewDetails(int rowIndex) {
        JOptionPane.showMessageDialog(this, "Ver detalles aún no implementado.");
    }

    @Override
    public void addNotify() {
        super.addNotify();
        loadTransactions();
    }
}

