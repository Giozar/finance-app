package com.giozar04.walletTransactionDetails.domain.policies;

import java.util.List;
import java.util.Objects;
import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;
import com.giozar04.walletTransactionDetails.domain.enums.WalletTransactionSourceType;

public final class WalletTransactionDetailPolicy {
    private WalletTransactionDetailPolicy() {}

    public static void validateDetail(WalletTransactionDetail detail) {
        Objects.requireNonNull(detail, "El detalle de transacción wallet no puede ser nulo");

        if (detail.getTransactionId() <= 0) {
            throw new IllegalArgumentException("ID de transacción inválido");
        }

        if (detail.getWalletAccountId() <= 0) {
            throw new IllegalArgumentException("ID de cuenta wallet inválido");
        }

        if (detail.getSourceType() == null) {
            throw new IllegalArgumentException("El tipo de fuente es obligatorio");
        }

        try {
            WalletTransactionSourceType.fromValue(detail.getSourceType().getValue());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de fuente inválido: " + detail.getSourceType(), e);
        }

        if (detail.getAmount() == null || detail.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }

        if (detail.getCashbackRate() != null &&
            (detail.getCashbackRate().signum() < 0 || detail.getCashbackRate().compareTo(java.math.BigDecimal.ONE) > 0)) {
            throw new IllegalArgumentException("La tasa de cashback debe estar entre 0 y 1");
        }
    }

    public static void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
    }
}
