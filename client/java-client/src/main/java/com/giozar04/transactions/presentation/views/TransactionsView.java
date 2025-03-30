package com.giozar04.transactions.presentation.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.MainContentPanel;
import com.giozar04.transactions.application.services.TransactionService;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.presentation.components.TransactionFormPanel;

public class TransactionsView extends JPanel {

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
        newTransactionButton.addActionListener(e -> openCreateTransactionView());
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
        // Se inicializa el modelo de tabla antes de llamar a loadTransactions()
        tableModel = new TransactionTableModel();
        transactionsTable = new JTable(tableModel);

        // Renderizador personalizado para la columna "Tipo"
        transactionsTable.getColumnModel().getColumn(1).setCellRenderer(new TransactionTypeCellRenderer());
        // Renderizador para el "Método de Pago" (simulando chips)
        transactionsTable.getColumnModel().getColumn(3).setCellRenderer(new PaymentMethodCellRenderer());
        // Columna de opciones: se usa un renderizador y editor personalizados
        transactionsTable.getColumnModel().getColumn(6).setCellRenderer(new OptionsCellRenderer());
        transactionsTable.getColumnModel().getColumn(6).setCellEditor(new OptionsCellEditor());
        // Alinear la columna "Monto" a la derecha
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        transactionsTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Cargar las transacciones desde el servicio
        loadTransactions();
    }

    // Sobrescribimos addNotify para refrescar la lista cada vez que la vista se muestre
    @Override
    public void addNotify() {
        super.addNotify();
        loadTransactions();
    }

    private void loadTransactions() {
        TransactionService service = TransactionService.getInstance();
        try {
            List<Transaction> transactions = service.getAllTransactions();
            tableModel.setTransactions(transactions);
        } catch (ClientOperationException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar las transacciones", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();
        String typeFilter = (String) filterCombo.getSelectedItem();
        tableModel.filter(query, typeFilter);
    }

    private void openCreateTransactionView() {
        // Se recorre la jerarquía hasta encontrar el MainContentPanel para cambiar la vista
        Container parent = getParent();
        while (parent != null && !(parent instanceof MainContentPanel)) {
            parent = parent.getParent();
        }
        if (parent instanceof MainContentPanel mainContentPanel) {
            // CreateTransactionView es la vista para crear una transacción
            mainContentPanel.setView(new CreateTransactionView());
        }
    }

    // ===== Modelo de Tabla para Transacciones =====
    private class TransactionTableModel extends AbstractTableModel {

        private final String[] columnNames = {"Título", "Tipo", "Categoría", "Método de Pago", "Monto", "Fecha", "Opciones"};
        private List<Transaction> transactions = new ArrayList<>();
        private List<Transaction> filteredTransactions = new ArrayList<>();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        public void setTransactions(List<Transaction> transactions) {
            this.transactions = transactions;
            this.filteredTransactions = new ArrayList<>(transactions);
            fireTableDataChanged();
        }

        public void filter(String query, String typeFilter) {
            filteredTransactions = new ArrayList<>();
            for (Transaction t : transactions) {
                boolean matchesQuery = t.getTitle().toLowerCase().contains(query)
                        || t.getCategory().toLowerCase().contains(query)
                        || t.getPaymentMethod().toString().toLowerCase().contains(query);
                boolean matchesType = typeFilter.equals("Todos los tipos") || t.getType().equalsIgnoreCase(typeFilter);
                if (matchesQuery && matchesType) {
                    filteredTransactions.add(t);
                }
            }
            fireTableDataChanged();
        }

        public Transaction getTransactionAt(int row) {
            return filteredTransactions.get(row);
        }

        public void removeTransactionAt(int row) {
            Transaction t = filteredTransactions.get(row);
            transactions.remove(t);
            filteredTransactions.remove(row);
            fireTableRowsDeleted(row, row);
        }

        @Override
        public int getRowCount() {
            return filteredTransactions.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            Transaction t = filteredTransactions.get(row);
            return switch (col) {
                case 0 ->
                    t.getTitle();
                case 1 ->
                    t.getType();
                case 2 ->
                    t.getCategory();
                case 3 ->
                    t.getPaymentMethod();
                case 4 ->
                    String.format("$%.2f", t.getAmount());
                case 5 ->
                    dateFormat.format(java.util.Date.from(t.getDate().toInstant()));
                case 6 ->
                    "···"; // Texto para el botón de opciones
                default ->
                    null;
            };
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            // Solo la columna de opciones es editable para detectar clics
            return col == 6;
        }
    }

    // ===== Renderizador para la columna "Tipo" (colores diferenciados) =====
    private class TransactionTypeCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String type = value.toString();
            if (type.equalsIgnoreCase("INCOME")) {
                c.setBackground(new Color(198, 239, 206)); // Verde claro
                c.setForeground(new Color(0, 97, 0));
            } else if (type.equalsIgnoreCase("EXPENSE")) {
                c.setBackground(new Color(255, 199, 206)); // Rojo claro
                c.setForeground(new Color(156, 0, 6));
            } else {
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
            }
            if (isSelected) {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            }
            return c;
        }
    }

    // ===== Renderizador para "Método de Pago" (simula chips) =====
    private class PaymentMethodCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel(value.toString());
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            // Asignar color de fondo según el método de pago
            switch (value.toString().toUpperCase()) {
                case "DÉBITO", "DEBITO" -> label.setBackground(new Color(220, 230, 241));
                case "CRÉDITO", "CREDITO" -> label.setBackground(new Color(241, 220, 220));
                case "EFECTIVO" -> label.setBackground(new Color(220, 241, 229));
                case "DIGITAL" -> label.setBackground(new Color(241, 237, 220));
                default -> label.setBackground(Color.LIGHT_GRAY);
            }
            label.setHorizontalAlignment(SwingConstants.CENTER);
            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
            }
            return label;
        }
    }

    // ===== Renderizador para la columna de "Opciones" =====
    private class OptionsCellRenderer extends DefaultTableCellRenderer {

        private final JButton button = new JButton("···");

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return button;
        }
    }

    // ===== Editor para la columna de "Opciones" (menú emergente para editar, eliminar y ver detalles) =====
    private class OptionsCellEditor extends AbstractCellEditor implements TableCellEditor {

        private final JButton button = new JButton("···");
        private JPopupMenu popupMenu;
        private int currentRow;

        public OptionsCellEditor() {
            popupMenu = new JPopupMenu();
            JMenuItem editItem = new JMenuItem("Editar");
            JMenuItem deleteItem = new JMenuItem("Eliminar");
            JMenuItem detailsItem = new JMenuItem("Ver Detalles");

            editItem.addActionListener((ActionEvent e) -> editTransaction(currentRow));
            deleteItem.addActionListener((ActionEvent e) -> deleteTransaction(currentRow));
            detailsItem.addActionListener((ActionEvent e) -> viewDetails(currentRow));

            popupMenu.add(editItem);
            popupMenu.add(deleteItem);
            popupMenu.add(detailsItem);

            button.addActionListener(e -> {
                popupMenu.show(button, button.getWidth() / 2, button.getHeight() / 2);
            });
        }

        @Override
        public Object getCellEditorValue() {
            return "···";
        }

        @Override
        public boolean isCellEditable(EventObject e) {
            return true;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            return button;
        }

        private void editTransaction(int row) {
            Transaction transaction = tableModel.getTransactionAt(row);
            // Se recorre la jerarquía para encontrar el MainContentPanel y cambiar la vista al formulario de edición
            Container parent = TransactionsView.this.getParent();
            while (parent != null && !(parent instanceof MainContentPanel)) {
                parent = parent.getParent();
            }
            if (parent instanceof MainContentPanel mainContentPanel) {
                // Se utiliza CreateTransactionView para la edición, reutilizando TransactionFormPanel
                TransactionFormPanel editView = new TransactionFormPanel();
                editView.loadTransaction(transaction);
                mainContentPanel.setView(editView);
            }
            fireEditingStopped();
        }

        private void deleteTransaction(int row) {
            Transaction transaction = tableModel.getTransactionAt(row);
            int confirm = JOptionPane.showConfirmDialog(TransactionsView.this,
                    "¿Desea eliminar la transacción seleccionada?",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    TransactionService.getInstance().deleteTransactionById(transaction.getId());
                    JOptionPane.showMessageDialog(TransactionsView.this, "Transacción eliminada.");
                    tableModel.removeTransactionAt(row);
                } catch (ClientOperationException | HeadlessException ex) {
                    JOptionPane.showMessageDialog(TransactionsView.this, "Error al eliminar la transacción: " + ex.getMessage());
                }
            }
            fireEditingStopped();
        }

        private void viewDetails(int row) {
            JOptionPane.showMessageDialog(TransactionsView.this, "Ver detalles aún no implementado.");
            fireEditingStopped();
        }
    }
}
