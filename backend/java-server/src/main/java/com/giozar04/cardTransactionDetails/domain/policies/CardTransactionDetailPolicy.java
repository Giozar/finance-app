package com.giozar04.cardTransactionDetails.domain.policies;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;

public final class CardTransactionDetailPolicy {
    private CardTransactionDetailPolicy() {}

    public static void validateDetail(CardTransactionDetail detail) {
        Objects.requireNonNull(detail, "El detalle de transacción no puede ser nulo");

        if (detail.getTransactionId() <= 0) {
            throw new IllegalArgumentException("ID de transacción inválido");
        }

        if (detail.getCardId() <= 0) {
            throw new IllegalArgumentException("ID de tarjeta inválido");
        }

        if (detail.getAmount() == null || detail.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }

        if (detail.getInstallmentMonths() != null && detail.getInstallmentMonths() < 1) {
            throw new IllegalArgumentException("El número de meses debe ser mayor o igual a 1");
        }
    }

    public static void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
    }
}
