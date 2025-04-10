package com.giozar04.transactions.presentation.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.DatePickerComponent;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.components.forms.FormLabel;
import com.giozar04.shared.components.forms.FormTextArea;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.shared.utils.FormValidatorUtils;
import com.giozar04.transactions.application.utils.TransactionUtils;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.PaymentMethod;
import com.giozar04.transactions.infrastructure.services.TransactionService;

public class TransactionFormPanel extends JPanel {

    private final JLabel titleLabel;
    private FormField titleField;
    private JComboBox<String> comboType;
    private JComboBox<String> comboPaymentMethod;
    private FormField amountField;
    private FormField categoryField;
    private DatePickerComponent datePicker;
    private FormTextArea descriptionField;
    private FormTextArea commentsField;
    private FormField tagsField;

    private JButton saveButton;
    private JButton cancelButton;

    private Transaction editingTransaction = null;
    private final TransactionService transactionService = TransactionService.getInstance();

    public TransactionFormPanel() {
        super(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        titleLabel = new JLabel("Nueva Transacción");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        add(createFormFieldsPanel(), BorderLayout.CENTER);
        add(createButtonsPanel(), BorderLayout.SOUTH);

        saveButton.addActionListener(e -> handleSave());
        cancelButton.addActionListener(e -> clearForm());
    }

    private JPanel createFormFieldsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(180, 180, 180)),
                        "Datos de la Transacción",
                        TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 12)),
                new EmptyBorder(10, 10, 10, 10)));

        titleField = new FormField("Título:", false, 400, 40);
        comboType = new JComboBox<>(new String[]{"INCOME", "EXPENSE"});
        comboPaymentMethod = new JComboBox<>(Arrays.stream(PaymentMethod.values()).map(Enum::name).toArray(String[]::new));
        amountField = new FormField("Monto:", false, 400, 40);
        categoryField = new FormField("Categoría:", false, 400, 40);
        datePicker = new DatePickerComponent();
        descriptionField = new FormTextArea("Descripción:", 3, 20);
        commentsField = new FormTextArea("Comentarios:", 3, 20);
        tagsField = new FormField("Tags (separados por comas):", false, 400, 40);

        // Añadir campos con separación
        panel.add(titleField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(new FormLabel("Tipo:", comboType));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(new FormLabel("Método de Pago:", comboPaymentMethod));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(amountField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(categoryField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(new FormLabel("Fecha:", datePicker));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(descriptionField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(commentsField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(tagsField);

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Guardar");
        cancelButton = new JButton("Cancelar");
        panel.add(cancelButton);
        panel.add(saveButton);
        return panel;
    }

    private void handleSave() {
        List<String> errors = new ArrayList<>();

        String title = titleField.getValue().trim();
        String amount = amountField.getValue().trim();

        FormValidatorUtils.isRequired(title, "Título", errors);
        FormValidatorUtils.isRequired(amount, "Monto", errors);
        FormValidatorUtils.isNumeric(amount, "Monto", errors);
        FormValidatorUtils.isPositiveNumber(amount, "Monto", errors);

        if (!errors.isEmpty()) {
            String message = FormValidatorUtils.formatErrorMessage(errors);
            DialogUtil.showError(this, message, "Errores de Validación");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("type", comboType.getSelectedItem());
        data.put("paymentMethod", comboPaymentMethod.getSelectedItem());
        data.put("amount", Double.valueOf(amount));
        data.put("category", categoryField.getValue());
        data.put("description", descriptionField.getValue());
        data.put("comments", commentsField.getValue());

        if (datePicker.getDate() != null) {
            data.put("date", datePicker.getISODate());
        }

        String tagsText = tagsField.getValue().trim();
        if (!tagsText.isEmpty()) {
            List<String> tagsList = Arrays.stream(tagsText.split(","))
                    .map(String::trim).filter(tag -> !tag.isEmpty()).collect(Collectors.toList());
            data.put("tags", tagsList);
        }

        Transaction transaction = TransactionUtils.mapToTransaction(data);

        try {
            if (editingTransaction == null) {
                transactionService.createTransaction(transaction);
                DialogUtil.showSuccess(this, "Transacción creada correctamente.");
            } else {
                transactionService.updateTransactionById(editingTransaction.getId(), transaction);
                DialogUtil.showSuccess(this, "Transacción actualizada correctamente.");
            }
            clearForm();
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al guardar: " + e.getMessage());
        }
    }

    public void loadTransaction(Transaction t) {
        this.editingTransaction = t;
        titleLabel.setText("Editar Transacción");
        saveButton.setText("Actualizar");

        titleField.setValue(t.getTitle());
        comboType.setSelectedItem(t.getType());
        comboPaymentMethod.setSelectedItem(t.getPaymentMethod().name());
        amountField.setValue(String.valueOf(t.getAmount()));
        categoryField.setValue(t.getCategory());
        datePicker.setDate(t.getDate());
        descriptionField.setValue(t.getDescription());
        commentsField.setValue(t.getComments());
        tagsField.setValue(t.getTagsAsString());
    }

    public void clearForm() {
        editingTransaction = null;
        titleLabel.setText("Nueva Transacción");
        saveButton.setText("Guardar");

        titleField.clear();
        comboType.setSelectedIndex(0);
        comboPaymentMethod.setSelectedIndex(0);
        amountField.clear();
        categoryField.clear();
        datePicker.clear();
        descriptionField.clear();
        commentsField.clear();
        tagsField.clear();
    }
}
