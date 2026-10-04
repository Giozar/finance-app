package com.giozar04.transactions.presentation.views;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.MainContentPanel;
import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.shared.components.table.ColumnDefinition;
import com.giozar04.shared.components.table.GenericTablePanel;
import com.giozar04.shared.components.table.OptionsCellEditor;
import com.giozar04.shared.components.table.OptionsCellRenderer;
import com.giozar04.shared.components.table.PopupMenuActionHandler;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.domain.enums.TransactionStatus;
import com.giozar04.transactions.infrastructure.services.TransactionService;
import com.giozar04.transactions.presentation.components.PaymentMethodCellRenderer;
import com.giozar04.transactions.presentation.components.TransactionDetailsDialog;
import com.giozar04.transactions.presentation.components.TransactionFormPanel;
import com.giozar04.transactions.presentation.components.TransactionNameLookup;
import com.giozar04.transactions.presentation.components.TransactionTypeCellRenderer;
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.infrastructure.services.UserService;

/**
 * Listado de transacciones con filtros por usuario (consulta al servidor), tipo, estado y texto (en memoria).
 * Origen, destino y categoría se muestran por nombre con {@link TransactionNameLookup}.
 */
public class TransactionsView extends JPanel implements PopupMenuActionHandler {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TransactionService transactionService;

    private FormComboBox<User> userFilter;
    private FormComboBox<OperationTypes> typeFilter;
    private FormComboBox<TransactionStatus> statusFilter;
    private JTextField searchField;
    private GenericTablePanel<Transaction> tablePanel;

    private List<Transaction> loadedTransactions = new ArrayList<>();
    private TransactionNameLookup lookup = TransactionNameLookup.empty();
    private boolean initializing = true;

    public TransactionsView() {
        transactionService = TransactionService.getInstance();
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(createTopPanel(), BorderLayout.NORTH);
        initTablePanel();
        initializing = false;
    }

    // ----------------------------
    // Construcción de componentes
    // ----------------------------

    private JPanel createTopPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("Gestión de Transacciones");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        JButton newTransactionButton = new JButton("Nueva Transacción");
        newTransactionButton.addActionListener(e -> handleNewTransaction());
        headerPanel.add(newTransactionButton, BorderLayout.EAST);

        // Filtros: usuario (consulta al servidor), tipo y estado (en memoria)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        userFilter = new FormComboBox<>("Usuario:", 400, 30);
        userFilter.setPlaceholder("Todos los usuarios");
        loadUsers();
        typeFilter = new FormComboBox<>("Tipo:", 300, 30);
        typeFilter.setPlaceholder("Todos los tipos");
        typeFilter.setItems(List.of(OperationTypes.values()));
        statusFilter = new FormComboBox<>("Estado:", 300, 30);
        statusFilter.setPlaceholder("Todos los estados");
        statusFilter.setItems(List.of(TransactionStatus.values()));
        filterPanel.add(userFilter);
        filterPanel.add(typeFilter);
        filterPanel.add(statusFilter);

        userFilter.addActionListener(e -> {
            if (!initializing) {
                loadTransactions();
            }
        });
        typeFilter.addActionListener(e -> applyFilters());
        statusFilter.addActionListener(e -> applyFilters());

        // Búsqueda por texto
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.add(new JLabel("Buscar:"));
        searchField = new JTextField(25);
        searchField.addActionListener(e -> applyFilters());
        searchPanel.add(searchField);
        JButton searchButton = new JButton("Buscar");
        searchButton.addActionListener(e -> applyFilters());
        searchPanel.add(searchButton);
        JButton refreshButton = new JButton("Actualizar");
        refreshButton.addActionListener(e -> loadTransactions());
        searchPanel.add(refreshButton);

        JPanel filtersPanel = new JPanel(new BorderLayout());
        filtersPanel.add(filterPanel, BorderLayout.NORTH);
        filtersPanel.add(searchPanel, BorderLayout.SOUTH);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(filtersPanel, BorderLayout.SOUTH);
        return topPanel;
    }

    private void initTablePanel() {
        List<ColumnDefinition<Transaction>> columns = Arrays.asList(
                new ColumnDefinition<>("Fecha", this::formatDate),
                new ColumnDefinition<>("Concepto", Transaction::getConcept),
                new ColumnDefinition<>("Tipo", t -> t.getOperationType() != null ? t.getOperationType().getLabel() : ""),
                new ColumnDefinition<>("Método", t -> t.getPaymentMethod() != null ? t.getPaymentMethod().getLabel() : ""),
                new ColumnDefinition<>("Origen", t -> lookup.originOf(t)),
                new ColumnDefinition<>("Destino", t -> lookup.destinationOf(t)),
                new ColumnDefinition<>("Categoría", t -> lookup.categoryName(t.getCategoryId())),
                new ColumnDefinition<>("Monto", t -> t.getAmount() != null ? String.format("$%,.2f", t.getAmount()) : ""),
                new ColumnDefinition<>("Estado", t -> t.getStatus() != null ? t.getStatus().getLabel() : ""),
                new ColumnDefinition<>("Opciones", t -> "···")
        );

        columns.get(2).setRenderer(new TransactionTypeCellRenderer());
        columns.get(3).setRenderer(new PaymentMethodCellRenderer());

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        columns.get(7).setRenderer(rightRenderer);

        columns.get(9).setRenderer(new OptionsCellRenderer());
        columns.get(9).setEditor(new OptionsCellEditor(this));

        tablePanel = new GenericTablePanel<>(columns, new ArrayList<>());
        add(tablePanel, BorderLayout.CENTER);
    }

    private void loadUsers() {
        try {
            userFilter.setItems(UserService.getInstance().getAllUsers());
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al cargar los usuarios: " + e.getMessage());
        }
    }

    // ------------------------
    // Funcionalidades lógicas
    // ------------------------

    /** Consulta las transacciones (todas o del usuario filtrado) y los nombres para mostrarlas. */
    private void loadTransactions() {
        try {
            User user = userFilter.getSelectedItem();
            loadedTransactions = user != null
                    ? transactionService.getTransactionsByUserId(user.getId())
                    : transactionService.getAllTransactions();
        } catch (ClientOperationException | RuntimeException e) {
            loadedTransactions = new ArrayList<>();
            DialogUtil.showError(this, "Error al cargar las transacciones: " + e.getMessage());
        }
        try {
            lookup = TransactionNameLookup.load();
        } catch (ClientOperationException | RuntimeException e) {
            lookup = TransactionNameLookup.empty();
            DialogUtil.showError(this, "Error al cargar los nombres de cuentas, entidades y categorías: " + e.getMessage());
        }
        applyFilters();
    }

    /** Filtra en memoria por tipo, estado y texto. */
    private void applyFilters() {
        if (tablePanel == null) {
            return;
        }
        OperationTypes type = typeFilter.getSelectedItem();
        TransactionStatus status = statusFilter.getSelectedItem();
        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);

        List<Transaction> filtered = loadedTransactions.stream()
                .filter(t -> type == null || t.getOperationType() == type)
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> query.isEmpty() || matchesText(t, query))
                .toList();
        tablePanel.setData(filtered);
    }

    private boolean matchesText(Transaction t, String query) {
        return contains(t.getConcept(), query)
                || contains(t.getDescription(), query)
                || contains(lookup.categoryName(t.getCategoryId()), query)
                || contains(lookup.originOf(t), query)
                || contains(lookup.destinationOf(t), query)
                || (t.getPaymentMethod() != null && contains(t.getPaymentMethod().getLabel(), query));
    }

    private static boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private String formatDate(Transaction t) {
        if (t.getDate() == null) {
            return "";
        }
        return t.getDate().withZoneSameInstant(ZoneId.systemDefault()).format(DATE_FORMAT);
    }

    /** Abre el formulario de alta (el botón no se deshabilita: la vista se reemplaza al navegar). */
    private void handleNewTransaction() {
        MainContentPanel mainContentPanel = getMainContentPanel();
        if (mainContentPanel != null) {
            mainContentPanel.setView(new CreateTransactionView());
        }
    }

    private MainContentPanel getMainContentPanel() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof MainContentPanel)) {
            parent = parent.getParent();
        }
        return (parent instanceof MainContentPanel) ? (MainContentPanel) parent : null;
    }

    /** El listado puede no traer el agregado completo: se pide al servidor antes de editar o ver detalle. */
    private Transaction fetchFull(Transaction transaction) {
        try {
            return transactionService.getTransactionById(transaction.getId());
        } catch (ClientOperationException | RuntimeException e) {
            DialogUtil.showError(this, "Error al obtener la transacción: " + e.getMessage());
            return null;
        }
    }

    // -------------------------------
    // Implementación de Popup Handler
    // --------------------------------

    @Override
    public void onEdit(int rowIndex) {
        Transaction transaction = fetchFull(tablePanel.getItemAt(rowIndex));
        MainContentPanel mainContentPanel = getMainContentPanel();
        if (transaction != null && mainContentPanel != null) {
            TransactionFormPanel editView = new TransactionFormPanel();
            editView.loadTransaction(transaction);
            mainContentPanel.setView(editView);
        }
    }

    @Override
    public void onDelete(int rowIndex) {
        Transaction transaction = tablePanel.getItemAt(rowIndex);
        boolean confirmed = DialogUtil.showConfirm(this,
                "¿Desea eliminar la transacción \"" + transaction.getConcept() + "\"?\n"
                        + "Se revertirá su efecto en los saldos de las cuentas.",
                "Confirmar eliminación");
        if (confirmed) {
            try {
                transactionService.deleteTransactionById(transaction.getId());
                DialogUtil.showSuccess(this, "Transacción eliminada.");
                loadTransactions();
            } catch (ClientOperationException | RuntimeException ex) {
                DialogUtil.showError(this, "Error al eliminar la transacción: " + ex.getMessage());
            }
        }
    }

    @Override
    public void onViewDetails(int rowIndex) {
        Transaction transaction = fetchFull(tablePanel.getItemAt(rowIndex));
        if (transaction != null) {
            TransactionDetailsDialog.show(this, transaction, lookup);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        loadTransactions();
    }
}
