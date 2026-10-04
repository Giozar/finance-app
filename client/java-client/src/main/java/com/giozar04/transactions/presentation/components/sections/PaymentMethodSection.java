package com.giozar04.transactions.presentation.components.sections;

import java.util.ArrayList;
import java.util.List;

import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.shared.components.forms.FormHelpText;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.domain.enums.PaymentMethod;
import com.giozar04.transactions.presentation.form.PaymentMethodPolicy;
import com.giozar04.transactions.presentation.form.TransactionFormContext;
import com.giozar04.transactions.presentation.form.TransactionFormDataProvider;

/**
 * Sección 3 – Método de pago, restringido por {@link PaymentMethodPolicy} según la operación y el tipo
 * de las cuentas del contexto. Si solo queda un método, se autoselecciona y se bloquea.
 * Fija el método de pago en el contexto.
 */
public class PaymentMethodSection extends AbstractTransactionSection {

    private static final long serialVersionUID = 1L;

    private final PaymentMethodPolicy policy;
    private final FormComboBox<PaymentMethod> methodCombo;
    private final FormHelpText help;

    private List<PaymentMethod> allowed = new ArrayList<>();
    private boolean adjusting;

    public PaymentMethodSection(TransactionFormContext context, TransactionFormDataProvider provider,
                                PaymentMethodPolicy policy) {
        super("Método de pago", context, provider);
        this.policy = policy;

        methodCombo = new FormComboBox<>("Método de pago:", FIELD_WIDTH, FIELD_HEIGHT);
        methodCombo.setPlaceholder("Seleccione un método...");
        help = new FormHelpText("", FIELD_WIDTH);

        addRow(methodCombo);
        addRow(help);

        methodCombo.addActionListener(e -> {
            if (!adjusting) {
                context.setPaymentMethod(methodCombo.getSelectedItem());
            }
        });
        refreshAllowed(context);
    }

    @Override
    public void onContextChanged(TransactionFormContext ctx) {
        refreshAllowed(ctx);
    }

    /** Recalcula los métodos permitidos; conserva el elegido si sigue permitido. */
    private void refreshAllowed(TransactionFormContext ctx) {
        List<PaymentMethod> newAllowed = policy.allowedMethods(
                ctx.getOperation(), ctx.getSourceAccountType(), ctx.getDestinationAccountType());

        if (!newAllowed.equals(allowed)) {
            PaymentMethod previous = methodCombo.getSelectedItem();
            allowed = new ArrayList<>(newAllowed);
            adjusting = true;
            try {
                methodCombo.setItems(allowed);
                if (allowed.size() == 1) {
                    methodCombo.setSelectedItem(allowed.get(0));
                } else if (previous != null && allowed.contains(previous)) {
                    methodCombo.setSelectedItem(previous);
                }
            } finally {
                adjusting = false;
            }
        }
        methodCombo.getComboBox().setEnabled(allowed.size() > 1);
        help.setText(helpText(ctx));
        ctx.setPaymentMethod(methodCombo.getSelectedItem());
    }

    private String helpText(TransactionFormContext ctx) {
        OperationTypes operation = ctx.getOperation();
        if (operation == null) {
            return "Seleccione el tipo de operación para ver los métodos disponibles.";
        }
        if (allowed.isEmpty()) {
            return switch (operation) {
                case INCOME -> "Seleccione la cuenta destino para ver los métodos disponibles.";
                case EXPENSE -> "Seleccione la cuenta origen para ver los métodos disponibles.";
                case REALLOCATION -> "Seleccione la cuenta origen y la cuenta destino para ver los métodos disponibles.";
            };
        }
        AccountTypes relevant = operation == OperationTypes.INCOME
                ? ctx.getDestinationAccountType() : ctx.getSourceAccountType();
        if (allowed.size() == 1) {
            if (allowed.get(0) == PaymentMethod.WALLET) {
                return "Pago con la wallet: elija abajo si se paga con su saldo o con una tarjeta vinculada.";
            }
            return "Único método permitido para una cuenta de tipo " + label(relevant) + ".";
        }
        return "Métodos permitidos según la operación y el tipo de las cuentas seleccionadas.";
    }

    private static String label(AccountTypes type) {
        return type != null ? "\"" + type.getLabel() + "\"" : "-";
    }

    @Override
    public void validate(List<String> errors) {
        if (context.getOperation() != null && methodCombo.getSelectedItem() == null) {
            errors.add("Debe seleccionar el método de pago.");
        }
    }

    @Override
    public void applyTo(Transaction tx) {
        tx.setPaymentMethod(methodCombo.getSelectedItem());
    }

    @Override
    public void loadFrom(Transaction tx) {
        if (tx.getPaymentMethod() != null && allowed.contains(tx.getPaymentMethod())) {
            methodCombo.setSelectedItem(tx.getPaymentMethod());
        }
    }

    @Override
    public void clear() {
        if (allowed.size() != 1) {
            methodCombo.clearSelection();
        }
    }
}
