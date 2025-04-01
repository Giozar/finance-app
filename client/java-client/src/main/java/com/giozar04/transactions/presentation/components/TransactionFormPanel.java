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
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.DatePickerComponent;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.transactions.application.utils.TransactionUtils;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.PaymentMethod;
import com.giozar04.transactions.infrastructure.services.TransactionService;
import com.giozar04.transactions.presentation.validators.TransactionValidator;

/**
 * Formulario para la creación/edición de transacciones.
 */
public class TransactionFormPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    // Componentes del formulario
    private final JLabel titleLabel;
    private FormField titleField;
    private JComboBox<String> comboType;
    private JComboBox<String> comboPaymentMethod;
    private FormField amountField;
    private FormField categoryField;
    private DatePickerComponent datePicker;
    private JTextArea txtDescription;
    private JTextArea txtComments;
    private FormField tagsField;
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
        gbc.gridwidth = 2;
        titleField = new FormField("Título: *");
        panel.add(titleField, gbc);

        // Fila 1: Tipo
        gbc.gridy++;
        comboType = new JComboBox<>(new String[] { "INCOME", "EXPENSE" });
        panel.add(labeledComponent("Tipo: *", comboType), gbc);

        // Fila 2: Método de Pago
        gbc.gridy++;
        comboPaymentMethod = new JComboBox<>(
                Arrays.stream(PaymentMethod.values())
                        .map(Enum::name)
                        .toArray(String[]::new));
        panel.add(labeledComponent("Método de Pago: *", comboPaymentMethod), gbc);

        // Fila 3: Monto
        gbc.gridy++;
        amountField = new FormField("Monto: *");
        panel.add(amountField, gbc);

        // Fila 4: Categoría
        gbc.gridy++;
        categoryField = new FormField("Categoría:");
        panel.add(categoryField, gbc);

        // Fila 5: Fecha
        gbc.gridy++;
        datePicker = new DatePickerComponent();
        panel.add(labeledComponent("Fecha:", datePicker), gbc);

        // Fila 6: Descripción
        gbc.gridy++;
        txtDescription = new JTextArea(4, 20);
        panel.add(labeledComponent("Descripción:", new JScrollPane(txtDescription)), gbc);

        // Fila 7: Comentarios
        gbc.gridy++;
        txtComments = new JTextArea(4, 20);
        panel.add(labeledComponent("Comentarios:", new JScrollPane(txtComments)), gbc);

        // Fila 8: Tags
        gbc.gridy++;
        tagsField = new FormField("Tags (separados por comas):");
        panel.add(tagsField, gbc);

        return panel;
    }

    private JPanel labeledComponent(String label, java.awt.Component component) {
        JPanel wrapper = new JPanel(new BorderLayout(5, 5));
        wrapper.add(new JLabel(label), BorderLayout.WEST);
        wrapper.add(component, BorderLayout.CENTER);
        return wrapper;
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
        validator.validateRequired(titleField.getValue(), "Título");
        validator.validateRequired(amountField.getValue(), "Monto");
        validator.validateNumeric(amountField.getValue(), "Monto");
        validator.validatePositiveNumber(amountField.getValue(), "Monto");

        if (validator.hasErrors()) {
            JOptionPane.showMessageDialog(
                    this,
                    validator.getErrorMessage(),
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("title", titleField.getValue());
        transactionData.put("type", comboType.getSelectedItem());
        transactionData.put("paymentMethod", comboPaymentMethod.getSelectedItem());

        try {
            double amount = Double.parseDouble(amountField.getValue());
            transactionData.put("amount", amount);
        } catch (NumberFormatException e) {
            transactionData.put("amount", 0.0);
        }

        transactionData.put("category", categoryField.getValue());

        if (datePicker.getDate() != null) {
            transactionData.put("date", datePicker.getISODate());
        }

        transactionData.put("description", txtDescription.getText());
        transactionData.put("comments", txtComments.getText());

        String tagsText = tagsField.getValue().trim();
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
        titleField.clear();
        comboType.setSelectedIndex(0);
        comboPaymentMethod.setSelectedIndex(0);
        amountField.clear();
        categoryField.clear();
        datePicker.clear();
        txtDescription.setText("");
        txtComments.setText("");
        tagsField.clear();

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

        titleField.setValue(transaction.getTitle());
        comboType.setSelectedItem(transaction.getType());
        comboPaymentMethod.setSelectedItem(transaction.getPaymentMethod().name());
        amountField.setValue(String.valueOf(transaction.getAmount()));
        categoryField.setValue(transaction.getCategory());
        if (transaction.getDate() != null) {
            datePicker.setDate(transaction.getDate());
        } else {
            datePicker.clear();
        }
        txtDescription.setText(transaction.getDescription());
        txtComments.setText(transaction.getComments());
        tagsField.setValue(transaction.getTagsAsString());
    }
} 