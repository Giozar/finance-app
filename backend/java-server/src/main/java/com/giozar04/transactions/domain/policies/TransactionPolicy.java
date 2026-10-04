package com.giozar04.transactions.domain.policies;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.domain.enums.PaymentMethod;

public final class TransactionPolicy {
    private TransactionPolicy() {}

    public static void validateTransaction(Transaction tx) {
        Objects.requireNonNull(tx, "La transacción no puede ser nula");

        if (tx.getUserId() <= 0)
            throw new IllegalArgumentException("El usuario es obligatorio");

        if (tx.getOperationType() == null)
            throw new IllegalArgumentException("El tipo de operación es obligatorio");

        if (tx.getPaymentMethod() == null)
            throw new IllegalArgumentException("El método de pago es obligatorio");

        if (tx.getStatus() == null)
            throw new IllegalArgumentException("El estado es obligatorio");

        if (tx.getAmount() == null || tx.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("El monto debe ser mayor que cero");

        if (tx.getConcept() == null || tx.getConcept().isBlank())
            throw new IllegalArgumentException("El concepto es obligatorio");

        if (tx.getCategoryId() <= 0)
            throw new IllegalArgumentException("La categoría es obligatoria");

        if (tx.getDate() == null)
            throw new IllegalArgumentException("La fecha es obligatoria");

        if (tx.getTimezone() == null || tx.getTimezone().isBlank())
            throw new IllegalArgumentException("La zona horaria es obligatoria");

        // Igual que el CHECK chk_tx_internal_reallocation
        if (tx.getPaymentMethod() == PaymentMethod.INTERNAL && tx.getOperationType() != OperationTypes.REALLOCATION)
            throw new IllegalArgumentException("El método de pago \"" + PaymentMethod.INTERNAL.getLabel() + "\" solo se permite con el tipo \"" + OperationTypes.REALLOCATION.getLabel() + "\"");
    }

    public static void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
    }
}
