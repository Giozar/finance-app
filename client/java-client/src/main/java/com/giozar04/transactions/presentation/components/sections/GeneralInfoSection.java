package com.giozar04.transactions.presentation.components.sections;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.giozar04.shared.components.forms.FormDateTimeField;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.components.forms.FormHelpText;
import com.giozar04.shared.components.forms.FormTextArea;
import com.giozar04.shared.utils.FormValidatorUtils;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.presentation.form.TransactionFormContext;
import com.giozar04.transactions.presentation.form.TransactionFormDataProvider;

/**
 * Sección 7 – Datos generales: monto, concepto (máximo 100), fecha y hora (por defecto ahora), zona horaria
 * (solo lectura), descripción, comentarios y comprobante (URL o ruta). Publica el monto en el contexto.
 */
public class GeneralInfoSection extends AbstractTransactionSection {

    private static final long serialVersionUID = 1L;

    private static final int CONCEPT_MAX_LENGTH = 100;

    private final FormField amountField;
    private final FormField conceptField;
    private final FormDateTimeField dateTimeField;
    private final FormField timezoneField;
    private final FormTextArea descriptionArea;
    private final FormTextArea commentsArea;
    private final FormField receiptField;

    public GeneralInfoSection(TransactionFormContext context, TransactionFormDataProvider provider) {
        super("Datos generales", context, provider);

        amountField = new FormField("Monto:", false, FIELD_WIDTH, FIELD_HEIGHT);
        conceptField = new FormField("Concepto:", false, FIELD_WIDTH, FIELD_HEIGHT);
        dateTimeField = new FormDateTimeField("Fecha y hora:", FIELD_WIDTH + 100, FIELD_HEIGHT);
        timezoneField = new FormField("Zona horaria:", false, FIELD_WIDTH, FIELD_HEIGHT);
        timezoneField.getTextField().setEditable(false);
        timezoneField.setValue(dateTimeField.getZoneId().getId());

        descriptionArea = textArea("Descripción:");
        commentsArea = textArea("Comentarios:");

        receiptField = new FormField("Comprobante:", false, FIELD_WIDTH - 110, FIELD_HEIGHT);
        JButton browseButton = new JButton("Examinar...");
        browseButton.addActionListener(e -> chooseReceiptFile());
        JPanel receiptRow = new JPanel(new BorderLayout(5, 0));
        receiptRow.setOpaque(false);
        receiptRow.add(receiptField, BorderLayout.CENTER);
        JPanel browseHolder = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 4));
        browseHolder.setOpaque(false);
        browseHolder.add(browseButton);
        receiptRow.add(browseHolder, BorderLayout.EAST);
        Dimension receiptSize = new Dimension(FIELD_WIDTH, FIELD_HEIGHT);
        receiptRow.setPreferredSize(receiptSize);
        receiptRow.setMaximumSize(receiptSize);

        addRow(amountField);
        addRow(conceptField);
        addRow(new FormHelpText("Máximo " + CONCEPT_MAX_LENGTH + " caracteres.", FIELD_WIDTH));
        addRow(dateTimeField);
        addRow(timezoneField);
        addRow(descriptionArea);
        addRow(commentsArea);
        addRow(receiptRow);
        addRow(new FormHelpText("URL o ruta del archivo del comprobante (opcional).", FIELD_WIDTH));

        amountField.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { publishAmount(); }
            @Override public void removeUpdate(DocumentEvent e) { publishAmount(); }
            @Override public void changedUpdate(DocumentEvent e) { publishAmount(); }
        });
    }

    private static FormTextArea textArea(String label) {
        FormTextArea area = new FormTextArea(label, 3, 40);
        area.getTextArea().setLineWrap(true);
        area.getTextArea().setWrapStyleWord(true);
        Dimension size = new Dimension(FIELD_WIDTH, 90);
        area.setPreferredSize(size);
        area.setMaximumSize(size);
        return area;
    }

    private void chooseReceiptFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccione el comprobante");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            receiptField.setValue(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void publishAmount() {
        context.setAmount(parseAmount(amountField.getValue()));
    }

    /** Monto capturado (admite "$" y separadores de miles), o null si no es un número. */
    private static BigDecimal parseAmount(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.trim().replace("$", "").replace(",", "");
        if (cleaned.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------
    // Contrato de sección
    // ------------------------------------------------------------------

    @Override
    public void onContextChanged(TransactionFormContext ctx) {
        // Esta sección solo publica el monto en el contexto.
    }

    @Override
    public void validate(List<String> errors) {
        String amountText = amountField.getValue().trim();
        BigDecimal amount = parseAmount(amountText);
        if (amountText.isEmpty()) {
            errors.add("Monto es un campo obligatorio.");
        } else if (amount == null) {
            errors.add("Monto debe ser un número válido.");
        } else if (amount.signum() <= 0) {
            errors.add("El monto debe ser mayor que cero.");
        } else if (amount.scale() > 2) {
            errors.add("El monto admite como máximo 2 decimales.");
        }

        String concept = conceptField.getValue().trim();
        FormValidatorUtils.isRequired(concept, "Concepto", errors);
        if (concept.length() > CONCEPT_MAX_LENGTH) {
            errors.add("El concepto no puede superar " + CONCEPT_MAX_LENGTH + " caracteres (tiene "
                    + concept.length() + ").");
        }
    }

    @Override
    public void applyTo(Transaction tx) {
        tx.setAmount(parseAmount(amountField.getValue()));
        tx.setConcept(conceptField.getValue().trim());
        tx.setDate(dateTimeField.getDateTime());
        tx.setTimezone(dateTimeField.getZoneId().getId());
        tx.setDescription(trimToNull(descriptionArea.getValue()));
        tx.setComments(trimToNull(commentsArea.getValue()));
        tx.setReceiptUrl(trimToNull(receiptField.getValue()));
    }

    @Override
    public void loadFrom(Transaction tx) {
        amountField.setValue(tx.getAmount() != null ? tx.getAmount().toPlainString() : "");
        conceptField.setValue(tx.getConcept() != null ? tx.getConcept() : "");
        dateTimeField.setDateTime(tx.getDate());
        descriptionArea.setValue(tx.getDescription() != null ? tx.getDescription() : "");
        commentsArea.setValue(tx.getComments() != null ? tx.getComments() : "");
        receiptField.setValue(tx.getReceiptUrl() != null ? tx.getReceiptUrl() : "");
    }

    @Override
    public void clear() {
        amountField.clear();
        conceptField.clear();
        dateTimeField.clearToNow();
        descriptionArea.clear();
        commentsArea.clear();
        receiptField.clear();
    }
}
