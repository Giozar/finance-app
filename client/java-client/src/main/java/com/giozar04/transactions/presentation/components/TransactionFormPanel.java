package com.giozar04.transactions.presentation.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.DatePickerComponent;
import com.giozar04.transactions.application.services.TransactionService;
import com.giozar04.transactions.application.utils.TransactionUtils;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.PaymentMethod;
import com.giozar04.transactions.presentation.validators.TransactionValidator;

/**
 * Formulario para la creación/edición de transacciones.
 */
public class TransactionFormPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    // Componentes del formulario
    private final JLabel titleLabel;
    private JTextField txtTitle;
    private JComboBox<String> comboType;
    private JComboBox<String> comboPaymentMethod;
    private JTextField txtAmount;
    private JTextField txtCategory;
    private DatePickerComponent datePicker;
    private JTextArea txtDescription;
    private JTextArea txtComments;
    private JTextField txtTags;
    private JButton btnSubmit;
    private JButton btnClear;
    
    // Validador para el formulario
    private final TransactionValidator validator = new TransactionValidator();
    
    // Servicio de transacciones
    private final TransactionService transactionService = TransactionService.getInstance();
    
    // Variable para almacenar la transacción en modo edición (null = creación)
    private Transaction editingTransaction = null;

    public TransactionFormPanel() {
        super(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Título del formulario
        titleLabel = new JLabel("Nueva Transacción");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Panel del formulario
        add(createFormFieldsPanel(), BorderLayout.CENTER);

        // Panel de botones
        add(createFormButtonsPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFormFieldsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(180, 180, 180)),
                        "Datos de la Transacción",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 12)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Fila 0: Título
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        panel.add(new JLabel("Título: *"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        txtTitle = new JTextField(20);
        panel.add(txtTitle, gbc);
        
        // Fila 1: Tipo (INCOME o EXPENSE)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        panel.add(new JLabel("Tipo: *"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        comboType = new JComboBox<>(new String[] { "INCOME", "EXPENSE" });
        panel.add(comboType, gbc);
        
        // Fila 2: Método de Pago
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.2;
        panel.add(new JLabel("Método de Pago: *"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        comboPaymentMethod = new JComboBox<>(
                Arrays.stream(PaymentMethod.values())
                      .map(Enum::name)
                      .toArray(String[]::new)
        );
        panel.add(comboPaymentMethod, gbc);
        
        // Fila 3: Monto
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.2;
        panel.add(new JLabel("Monto: *"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        txtAmount = new JTextField(20);
        panel.add(txtAmount, gbc);
        
        // Fila 4: Categoría
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.2;
        panel.add(new JLabel("Categoría:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        txtCategory = new JTextField(20);
        panel.add(txtCategory, gbc);
        
        // Fila 5: Fecha
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.2;
        panel.add(new JLabel("Fecha:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        datePicker = new DatePickerComponent();
        panel.add(datePicker, gbc);
        
        // Fila 6: Descripción
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Descripción:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        txtDescription = new JTextArea(4, 20);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        panel.add(scrollDesc, gbc);
        
        // Fila 7: Comentarios
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 0.2;
        panel.add(new JLabel("Comentarios:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        txtComments = new JTextArea(4, 20);
        JScrollPane scrollComments = new JScrollPane(txtComments);
        panel.add(scrollComments, gbc);
        
        // Fila 8: Tags
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Tags (separados por comas):"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        txtTags = new JTextField(20);
        panel.add(txtTags, gbc);
        
        return panel;
    }
    
    private JPanel createFormButtonsPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        btnSubmit = new JButton("Crear");
        btnSubmit.setFont(new Font("Arial", Font.BOLD, 12));
        
        btnClear = new JButton("Limpiar");
        
        btnSubmit.addActionListener((ActionEvent e) -> saveTransaction());
        btnClear.addActionListener((ActionEvent e) -> clearForm());
        
        panel.add(btnSubmit);
        panel.add(btnClear);
        
        return panel;
    }
    
    private void saveTransaction() {
        validator.clearErrors();
        validator.validateRequired(txtTitle.getText(), "Título");
        validator.validateRequired(txtAmount.getText(), "Monto");
        validator.validateNumeric(txtAmount.getText(), "Monto");
        validator.validatePositiveNumber(txtAmount.getText(), "Monto");
        
        if (validator.hasErrors()) {
            JOptionPane.showMessageDialog(
                this, 
                validator.getErrorMessage(),
                "Error de validación", 
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("title", txtTitle.getText());
        transactionData.put("type", comboType.getSelectedItem());
        transactionData.put("paymentMethod", comboPaymentMethod.getSelectedItem());
        
        try {
            double amount = Double.parseDouble(txtAmount.getText());
            transactionData.put("amount", amount);
        } catch (NumberFormatException e) {
            transactionData.put("amount", 0.0);
        }
        
        transactionData.put("category", txtCategory.getText());
        
        if (datePicker.getDate() != null) {
            transactionData.put("date", datePicker.getISODate());
        }
        
        transactionData.put("description", txtDescription.getText());
        transactionData.put("comments", txtComments.getText());
        
        String tagsText = txtTags.getText().trim();
        if (!tagsText.isEmpty()) {
            List<String> tagsList = Arrays.stream(tagsText.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toList());
            transactionData.put("tags", tagsList);
        }
        
        Transaction transaction = TransactionUtils.mapToTransaction(transactionData);
        
        try {
            if (editingTransaction == null) {
                // Creación de nueva transacción
                transactionService.createTransaction(transaction);
                JOptionPane.showMessageDialog(this, "Transacción enviada correctamente al servidor.", 
                        "Transacción Enviada", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Actualización de la transacción existente
                transactionService.updateTransactionById(editingTransaction.getId(), transaction);
                JOptionPane.showMessageDialog(this, "Transacción actualizada correctamente.", 
                        "Transacción Actualizada", JOptionPane.INFORMATION_MESSAGE);
            }
            clearForm();
        } catch (ClientOperationException e) {
            JOptionPane.showMessageDialog(this, "Error al enviar la transacción: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void clearForm() {
        txtTitle.setText("");
        comboType.setSelectedIndex(0);
        comboPaymentMethod.setSelectedIndex(0);
        txtAmount.setText("");
        txtCategory.setText("");
        datePicker.clear();
        txtDescription.setText("");
        txtComments.setText("");
        txtTags.setText("");
        
        validator.clearErrors();
        editingTransaction = null;
        titleLabel.setText("Nueva Transacción");
        btnSubmit.setText("Crear");
    }
    
    /**
     * Carga una transacción en el formulario para su edición.
     * @param transaction La transacción a editar.
     */
    public void loadTransaction(Transaction transaction) {
        this.editingTransaction = transaction;
        titleLabel.setText("Editar Transacción");
        btnSubmit.setText("Actualizar");
        
        txtTitle.setText(transaction.getTitle());
        comboType.setSelectedItem(transaction.getType());
        comboPaymentMethod.setSelectedItem(transaction.getPaymentMethod().name());
        txtAmount.setText(String.valueOf(transaction.getAmount()));
        txtCategory.setText(transaction.getCategory());
        if (transaction.getDate() != null) {
            datePicker.setDate(transaction.getDate());
        } else {
            datePicker.clear();
        }
        txtDescription.setText(transaction.getDescription());
        txtComments.setText(transaction.getComments());
        txtTags.setText(transaction.getTagsAsString());
    }
    
}
