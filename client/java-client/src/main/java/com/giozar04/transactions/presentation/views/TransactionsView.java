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
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.infrastructure.services.TransactionService;
import com.giozar04.transactions.presentation.components.PaymentMethodCellRenderer;
import com.giozar04.transactions.presentation.components.TransactionFormPanel;
import com.giozar04.transactions.presentation.components.TransactionTypeCellRenderer;

public class TransactionsView extends JPanel implements PopupMenuActionHandler {

    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private GenericTablePanel<Transaction> tablePanel;
    private final TransactionService transactionService;

    public TransactionsView() {
        transactionService = TransactionService.getInstance();
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Inicializa la parte superior (encabezado y búsqueda)
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Inicializa la tabla de transacciones
        initTablePanel();
    }

    // ----------------------------
    // Construcción de componentes
    // ----------------------------
    /**
     * Crea el panel superior que contiene el encabezado y la barra de búsqueda.
     */
    private JPanel createTopPanel() {
        // Panel de encabezado con título y botón para nueva transacción
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("Gestión de Transacciones");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        JButton newTransactionButton = new JButton("Nueva Transacción");
        newTransactionButton.addActionListener(this::handleNewTransaction);
        headerPanel.add(newTransactionButton, BorderLayout.EAST);

        // Panel de búsqueda y filtro
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.add(new JLabel("Buscar:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);

        filterCombo = new JComboBox<>(new String[]{"Todos los tipos", "INCOME", "EXPENSE"});
        searchPanel.add(filterCombo);

        JButton searchButton = new JButton("Buscar");
        searchButton.addActionListener(e -> performSearch());
        searchPanel.add(searchButton);

        // Agrupa encabezado y búsqueda en un panel superior
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        return topPanel;
    }

    /**
     * Inicializa y configura la tabla de transacciones.
     */
    private void initTablePanel() {
        // Definición de columnas para la tabla
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

        // Asignar renderizadores y editores a columnas específicas
        columns.get(1).setRenderer(new TransactionTypeCellRenderer());
        columns.get(3).setRenderer(new PaymentMethodCellRenderer());
        columns.get(6).setRenderer(new OptionsCellRenderer());
        columns.get(6).setEditor(new OptionsCellEditor(this));

        // Alinear la columna "Monto" a la derecha
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        columns.get(4).setRenderer(rightRenderer);

        // Crear el panel de la tabla con los datos iniciales
        try {
            List<Transaction> transactions = transactionService.getAllTransactions();
            tablePanel = new GenericTablePanel<>(columns, transactions);
            add(tablePanel, BorderLayout.CENTER);
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al cargar las transacciones");
        }
    }

    // ------------------------
    // Funcionalidades lógicas
    // ------------------------
    /**
     * Carga o recarga las transacciones en la tabla.
     */
    private void loadTransactions() {
        try {
            List<Transaction> transactions = transactionService.getAllTransactions();
            tablePanel.setData(transactions);
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al cargar las transacciones");
        }
    }

    /**
     * Realiza la búsqueda de transacciones en función del texto y filtro
     * seleccionado.
     */
    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();
        String typeFilter = (String) filterCombo.getSelectedItem();
        try {
            List<Transaction> allTransactions = transactionService.getAllTransactions();
            List<Transaction> filtered = allTransactions.stream().filter(t -> {
                boolean matchesQuery = t.getTitle().toLowerCase().contains(query)
                        || t.getCategory().toLowerCase().contains(query)
                        || t.getPaymentMethod().toString().toLowerCase().contains(query);
                boolean matchesType = "Todos los tipos".equals(typeFilter)
                        || t.getType().equalsIgnoreCase(typeFilter);
                return matchesQuery && matchesType;
            }).toList();
            tablePanel.setData(filtered);
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al buscar las transacciones: " + e.getMessage());
        }

    }

    /**
     * Maneja la acción para crear una nueva transacción.
     */
    private void handleNewTransaction(ActionEvent e) {
        // Deshabilitar el botón que disparó la acción para evitar múltiples clics
        Object source = e.getSource();
        if (source instanceof JButton button) {
            button.setEnabled(false);
            // Aquí podrías programar más lógica si es necesario
        }
        MainContentPanel mainContentPanel = getMainContentPanel();
        if (mainContentPanel != null) {
            // Se asume que CreateTransactionView es la vista para crear transacciones
            mainContentPanel.setView(new CreateTransactionView());
        }
    }

    /**
     * Busca y retorna el panel principal de contenido.
     *
     * @return MainContentPanel si se encuentra; de lo contrario, null.
     */
    private MainContentPanel getMainContentPanel() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof MainContentPanel)) {
            parent = parent.getParent();
        }
        return (parent instanceof MainContentPanel) ? (MainContentPanel) parent : null;
    }

    // -------------------------------
    // Implementación de Popup Handler
    // --------------------------------
    @Override
    public void onEdit(int rowIndex) {
        Transaction transaction = tablePanel.getItemAt(rowIndex);
        MainContentPanel mainContentPanel = getMainContentPanel();
        if (mainContentPanel != null) {
            TransactionFormPanel editView = new TransactionFormPanel();
            editView.loadTransaction(transaction);
            mainContentPanel.setView(editView);
        }
    }

    @Override
    public void onDelete(int rowIndex) {
        Transaction transaction = tablePanel.getItemAt(rowIndex);
        boolean confirmed = DialogUtil.showConfirm(this, "¿Desea eliminar la transacción seleccionada?", "Confirmar eliminación");
    
        if (confirmed) {
            try {
                transactionService.deleteTransactionById(transaction.getId());
                DialogUtil.showSuccess(this, "Transacción eliminada.");
                loadTransactions();
            } catch (ClientOperationException | HeadlessException ex) {
                DialogUtil.showError(this, "Error al eliminar la transacción: " + ex.getMessage());
            }
        }
    }
    

    @Override
    public void onViewDetails(int rowIndex) {
        DialogUtil.showWarning(this, "Ver detalles aún no implementado.");
    }

    @Override
    public void addNotify() {
        super.addNotify();
        loadTransactions();
    }
}
