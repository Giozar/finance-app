package com.giozar04.transactions.presentation.form;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.domain.enums.PaymentMethod;
import com.giozar04.users.domain.entities.User;

/**
 * Estado observable del formulario de transacciones (patrón Observer).
 *
 * <p>Guarda lo que condiciona a las secciones: usuario, operación, cuenta origen y destino (con su tipo),
 * método de pago y monto (para cálculos como la mensualidad). Las secciones escriben aquí lo que el usuario
 * elige y reaccionan a los cambios en {@link TransactionFormSection#onContextChanged}.</p>
 *
 * <p>Las notificaciones se agrupan: si un listener modifica el contexto mientras se notifica, no se anida
 * otra notificación; se repite la ronda al terminar, de modo que todos ven siempre el estado final.</p>
 */
public class TransactionFormContext {

    /** Límite de rondas encadenadas, para cortar un posible ciclo entre secciones. */
    private static final int MAX_ROUNDS = 10;

    private final List<Consumer<TransactionFormContext>> listeners = new ArrayList<>();

    private User user;
    private OperationTypes operation;
    private Account sourceAccount;
    private Account destinationAccount;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;

    private boolean notifying;
    private boolean pending;

    // ------------------------------------------------------------------
    // Listeners
    // ------------------------------------------------------------------

    /** Se notifican en el orden en que se registran. */
    public void addListener(Consumer<TransactionFormContext> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /** Fuerza una notificación (p. ej. para que las secciones calculen su estado inicial). */
    public void refresh() {
        fireChanged();
    }

    // ------------------------------------------------------------------
    // Lectura
    // ------------------------------------------------------------------

    public User getUser() { return user; }

    public Long getUserId() { return user != null ? user.getId() : null; }

    public OperationTypes getOperation() { return operation; }

    public Account getSourceAccount() { return sourceAccount; }

    public Account getDestinationAccount() { return destinationAccount; }

    public AccountTypes getSourceAccountType() { return sourceAccount != null ? sourceAccount.getType() : null; }

    public AccountTypes getDestinationAccountType() {
        return destinationAccount != null ? destinationAccount.getType() : null;
    }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }

    /** Monto capturado, o null si está vacío o no es un número válido. */
    public BigDecimal getAmount() { return amount; }

    // ------------------------------------------------------------------
    // Escritura (solo notifica si el valor cambia)
    // ------------------------------------------------------------------

    public void setUser(User user) {
        if (!Objects.equals(idOf(this.user), idOf(user))) {
            this.user = user;
            fireChanged();
        } else {
            this.user = user;
        }
    }

    public void setOperation(OperationTypes operation) {
        if (this.operation != operation) {
            this.operation = operation;
            fireChanged();
        }
    }

    public void setSourceAccount(Account account) {
        if (!sameAccount(sourceAccount, account)) {
            this.sourceAccount = account;
            fireChanged();
        } else {
            this.sourceAccount = account;
        }
    }

    public void setDestinationAccount(Account account) {
        if (!sameAccount(destinationAccount, account)) {
            this.destinationAccount = account;
            fireChanged();
        } else {
            this.destinationAccount = account;
        }
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        if (this.paymentMethod != paymentMethod) {
            this.paymentMethod = paymentMethod;
            fireChanged();
        }
    }

    public void setAmount(BigDecimal amount) {
        boolean changed = amount == null ? this.amount != null
                : this.amount == null || amount.compareTo(this.amount) != 0;
        if (changed) {
            this.amount = amount;
            fireChanged();
        }
    }

    // ------------------------------------------------------------------
    // Interno
    // ------------------------------------------------------------------

    private void fireChanged() {
        if (notifying) {
            pending = true;
            return;
        }
        notifying = true;
        try {
            int rounds = 0;
            do {
                pending = false;
                for (Consumer<TransactionFormContext> listener : new ArrayList<>(listeners)) {
                    listener.accept(this);
                }
            } while (pending && ++rounds < MAX_ROUNDS);
        } finally {
            notifying = false;
            pending = false;
        }
    }

    private static Long idOf(User user) {
        return user != null ? user.getId() : null;
    }

    private static boolean sameAccount(Account a, Account b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.getId() == b.getId();
    }
}
