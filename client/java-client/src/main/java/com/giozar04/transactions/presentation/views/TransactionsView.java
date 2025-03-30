package com.giozar04.transactions.presentation.views;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.MainContentPanel;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.infrastructure.services.TransactionService;
import com.giozar04.transactions.presentation.components.TransactionFormPanel;
import com.giozar04.transactions.presentation.table.editors.OptionsCellEditor;
import com.giozar04.transactions.presentation.table.editors.PopupMenuActionHandler;
import com.giozar04.transactions.presentation.table.models.TransactionTableModel;
import com.giozar04.transactions.presentation.table.renders.OptionsCellRenderer;
import com.giozar04.transactions.presentation.table.renders.PaymentMethodCellRenderer;
import com.giozar04.transactions.presentation.table.renders.TransactionTypeCellRenderer;

/**
 * Panel principal para la gestión de transacciones.
 */
public class TransactionsView extends JPanel implements PopupMenuActionHandler {

    private final JTable transactionsTable;
    private final TransactionTableModel tableModel;
    private final JTextField searchField;
    private final JComboBox<String> filterCombo;

    public TransactionsView() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Encabezado y botón de nueva transacción ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("Gestión de Transacciones");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        JButton newTransactionButton = new JButton("Nueva Transacción");
        newTransactionButton.addActionListener(this::handleNewTransaction);
        headerPanel.add(newTransactionButton, BorderLayout.EAST);

        // --- Barra de búsqueda y filtro ---
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

        // --- Tabla de transacciones ---
        tableModel = new TransactionTableModel();
        transactionsTable = new JTable(tableModel);

        // Asignar renderizadores personalizados
        transactionsTable.getColumnModel().getColumn(1).setCellRenderer(new TransactionTypeCellRenderer());
        transactionsTable.getColumnModel().getColumn(3).setCellRenderer(new PaymentMethodCellRenderer());
        transactionsTable.getColumnModel().getColumn(6).setCellRenderer(new OptionsCellRenderer());

        // Asignar editor personalizado para la columna de "Opciones"
        transactionsTable.getColumnModel().getColumn(6)
                .setCellEditor(new OptionsCellEditor(this));

        // Alinear la columna "Monto" a la derecha
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        transactionsTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Cargar las transacciones desde el servicio
        loadTransactions();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        loadTransactions();
    }

    /**
     * Carga las transacciones utilizando el servicio y actualiza el modelo de la tabla.
     */
    private void loadTransactions() {
        TransactionService service = TransactionService.getInstance();
        try {
            List<Transaction> transactions = service.getAllTransactions();
            tableModel.setTransactions(transactions);
        } catch (ClientOperationException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar las transacciones", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Ejecuta la búsqueda en la tabla utilizando el término y filtro seleccionado.
     */
    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();
        String typeFilter = (String) filterCombo.getSelectedItem();
        tableModel.filter(query, typeFilter);
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
            // Se utiliza CreateTransactionView para crear una nueva transacción
            mainContentPanel.setView(new CreateTransactionView());
        }
    }

    // Implementación de PopupMenuActionHandler

    @Override
    public void onEditTransaction(int rowIndex) {
        Transaction transaction = tableModel.getTransactionAt(rowIndex);
        Container parent = getParent();
        while (parent != null && !(parent instanceof MainContentPanel)) {
            parent = parent.getParent();
        }
        if (parent instanceof MainContentPanel mainContentPanel) {
            // Reutiliza TransactionFormPanel para la edición
            TransactionFormPanel editView = new TransactionFormPanel();
            editView.loadTransaction(transaction);
            mainContentPanel.setView(editView);
        }
    }

    @Override
    public void onDeleteTransaction(int rowIndex) {
        Transaction transaction = tableModel.getTransactionAt(rowIndex);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar la transacción seleccionada?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                TransactionService.getInstance().deleteTransactionById(transaction.getId());
                JOptionPane.showMessageDialog(this, "Transacción eliminada.");
                tableModel.removeTransactionAt(rowIndex);
            } catch (ClientOperationException | HeadlessException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar la transacción: " + ex.getMessage());
            }
        }
    }

    @Override
    public void onViewDetails(int rowIndex) {
        JOptionPane.showMessageDialog(this, "Ver detalles aún no implementado.");
    }
}
