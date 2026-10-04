package com.giozar04.transactions.presentation.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.MainContentPanel;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.shared.utils.FormValidatorUtils;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.infrastructure.services.TransactionService;
import com.giozar04.transactions.presentation.components.sections.CardDetailsSection;
import com.giozar04.transactions.presentation.components.sections.ClassificationSection;
import com.giozar04.transactions.presentation.components.sections.GeneralInfoSection;
import com.giozar04.transactions.presentation.components.sections.OperationSection;
import com.giozar04.transactions.presentation.components.sections.PartiesSection;
import com.giozar04.transactions.presentation.components.sections.PaymentMethodSection;
import com.giozar04.transactions.presentation.components.sections.WalletDetailsSection;
import com.giozar04.transactions.presentation.form.PaymentMethodPolicy;
import com.giozar04.transactions.presentation.form.TransactionFormContext;
import com.giozar04.transactions.presentation.form.TransactionFormDataProvider;
import com.giozar04.transactions.presentation.form.TransactionFormSection;
import com.giozar04.transactions.presentation.views.TransactionsView;

/**
 * Formulario único y dinámico para crear y editar transacciones (orquestador).
 *
 * <ul>
 *   <li>{@link TransactionFormContext}: estado observable (usuario, operación, cuentas, método, monto).</li>
 *   <li>{@link TransactionFormDataProvider}: catálogos del usuario, cacheados.</li>
 *   <li>{@link TransactionFormSection}: cada sección se muestra, oculta o limpia según el contexto y sabe
 *       validarse, aplicarse al agregado y cargarse.</li>
 *   <li>{@link PaymentMethodPolicy}: métodos de pago permitidos.</li>
 * </ul>
 *
 * Este panel solo compone las secciones, valida, construye el agregado {@link Transaction} y lo guarda.
 */
public class TransactionFormPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final TransactionFormContext context = new TransactionFormContext();
    private final TransactionFormDataProvider provider = new TransactionFormDataProvider();

    private final OperationSection operationSection;
    private final List<TransactionFormSection> sections;

    private final JLabel titleLabel;

    private Transaction currentTransaction;

    public TransactionFormPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        provider.setOnError(message -> DialogUtil.showError(this, message));
        // Primer listener: los catálogos del usuario deben estar cargados antes de que reaccionen las secciones
        context.addListener(ctx -> provider.loadForUser(ctx.getUserId()));

        operationSection = new OperationSection(context, provider);
        sections = List.of(
                operationSection,
                new PartiesSection(context, provider),
                new PaymentMethodSection(context, provider, new PaymentMethodPolicy()),
                new CardDetailsSection(context, provider),
                new WalletDetailsSection(context, provider),
                new ClassificationSection(context, provider),
                new GeneralInfoSection(context, provider));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        for (TransactionFormSection section : sections) {
            context.addListener(section::onContextChanged);
            Component component = (Component) section;
            formPanel.add(component);
            formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JPanel content = new JPanel(new BorderLayout());
        content.add(formPanel, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        titleLabel = new JLabel();
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancelar");
        JButton saveButton = new JButton("Guardar");
        JButton backButton = new JButton("Regresar");

        cancelButton.addActionListener(e -> clearForm());
        saveButton.addActionListener(e -> handleSave());
        backButton.addActionListener(e -> handleBack());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        context.refresh(); // estado inicial de las secciones
        updateTitle();
    }

    // ------------------------------------------------------------------
    // Guardar
    // ------------------------------------------------------------------

    private void handleSave() {
        List<String> errors = new ArrayList<>();
        for (TransactionFormSection section : sections) {
            section.validate(errors);
        }
        if (!errors.isEmpty()) {
            DialogUtil.showError(this, FormValidatorUtils.formatErrorMessage(errors));
            return;
        }

        Transaction tx = buildTransaction();
        boolean creating = currentTransaction == null;
        try {
            if (creating) {
                TransactionService.getInstance().createTransaction(tx);
                DialogUtil.showSuccess(this, "Transacción registrada exitosamente.");
            } else {
                TransactionService.getInstance().updateTransactionById(tx.getId(), tx);
                DialogUtil.showSuccess(this, "Transacción actualizada exitosamente.");
            }
            // Se conserva el usuario para capturar la siguiente transacción (los catálogos se recargan)
            long userId = tx.getUserId();
            clearForm();
            operationSection.selectUserById(userId);
        } catch (ClientOperationException | RuntimeException ex) {
            DialogUtil.showError(this, formatServerError(ex.getMessage()));
        }
    }

    /** Construye el agregado: cada sección aplica lo suyo; los detalles toman el monto de la transacción. */
    private Transaction buildTransaction() {
        Transaction tx = new Transaction();
        ZonedDateTime now = ZonedDateTime.now();
        if (currentTransaction != null) {
            tx.setId(currentTransaction.getId());
            tx.setParentTransactionId(currentTransaction.getParentTransactionId());
            tx.setCreatedAt(currentTransaction.getCreatedAt());
        } else {
            tx.setCreatedAt(now);
        }
        tx.setUpdatedAt(now);

        for (TransactionFormSection section : sections) {
            section.applyTo(tx);
        }

        if (tx.getCardDetail() != null) {
            tx.getCardDetail().setAmount(tx.getAmount());
        }
        if (tx.getWalletDetail() != null) {
            tx.getWalletDetail().setAmount(tx.getAmount());
        }
        return tx;
    }

    /** El servidor envía los errores de validación juntos, separados por "; ". */
    private static String formatServerError(String message) {
        if (message == null || message.isBlank()) {
            return "Error al guardar la transacción.";
        }
        List<String> parts = Arrays.stream(message.split(";\\s+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        if (parts.size() <= 1) {
            return "Error al guardar la transacción: " + message;
        }
        StringBuilder sb = new StringBuilder("No se pudo guardar la transacción:\n\n");
        for (String part : parts) {
            sb.append("• ").append(part).append("\n");
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Cargar para edición
    // ------------------------------------------------------------------

    /**
     * Rellena el formulario para editar. Primero el usuario (carga sus catálogos) y después cada sección
     * en orden, porque cada una depende del contexto que fijan las anteriores.
     */
    public void loadTransaction(Transaction transaction) {
        clearForm();
        currentTransaction = transaction;
        for (TransactionFormSection section : sections) {
            section.loadFrom(transaction);
        }
        operationSection.setEditMode(true);
        updateTitle();
    }

    // ------------------------------------------------------------------
    // Limpiar
    // ------------------------------------------------------------------

    public void clearForm() {
        currentTransaction = null;
        operationSection.setEditMode(false);
        for (TransactionFormSection section : sections) {
            section.clear();
        }
        updateTitle();
    }

    private void updateTitle() {
        titleLabel.setText(currentTransaction == null ? "Nueva transacción" : "Editar transacción");
    }

    // ------------------------------------------------------------------
    // Navegación
    // ------------------------------------------------------------------

    private void handleBack() {
        MainContentPanel mainPanel = getMainContentPanel();
        if (mainPanel != null) {
            mainPanel.setView(new TransactionsView());
        }
    }

    private MainContentPanel getMainContentPanel() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof MainContentPanel)) {
            parent = parent.getParent();
        }
        return (MainContentPanel) parent;
    }
}
