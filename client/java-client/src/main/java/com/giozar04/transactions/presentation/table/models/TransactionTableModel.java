package com.giozar04.transactions.presentation.table.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.giozar04.transactions.domain.entities.Transaction;

/**
 * Modelo de tabla para mostrar las transacciones.
 */
public class TransactionTableModel extends AbstractTableModel {

    private final String[] columnNames = {"Título", "Tipo", "Categoría", "Método de Pago", "Monto", "Fecha", "Opciones"};
    private List<Transaction> transactions = new ArrayList<>();
    private List<Transaction> filteredTransactions = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    /**
     * Establece la lista de transacciones y refresca la tabla.
     * @param transactions Lista de transacciones.
     */
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        this.filteredTransactions = new ArrayList<>(transactions);
        fireTableDataChanged();
    }

    /**
     * Aplica un filtro basado en el texto de búsqueda y el tipo de transacción.
     * @param query Texto de búsqueda.
     * @param typeFilter Filtro de tipo.
     */
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

    /**
     * Obtiene la transacción en una fila específica.
     * @param row Índice de la fila.
     * @return Transacción correspondiente.
     */
    public Transaction getTransactionAt(int row) {
        return filteredTransactions.get(row);
    }

    /**
     * Remueve la transacción de la fila especificada.
     * @param row Índice de la fila.
     */
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
            case 0 -> t.getTitle();
            case 1 -> t.getType();
            case 2 -> t.getCategory();
            case 3 -> t.getPaymentMethod();
            case 4 -> String.format("$%.2f", t.getAmount());
            case 5 -> dateFormat.format(java.util.Date.from(t.getDate().toInstant()));
            case 6 -> "···"; // Texto para el botón de opciones
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        // Solo la columna de opciones es editable para detectar clics
        return col == 6;
    }
}
