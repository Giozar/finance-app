package com.giozar04.walletCardLinks.domain.policies;

import java.util.List;
import java.util.Objects;
import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;

public final class WalletCardLinkPolicy {
    private WalletCardLinkPolicy() {}

    public static void validateLink(WalletCardLink link) {
        Objects.requireNonNull(link, "El enlace wallet-tarjeta no puede ser nulo");

        if (link.getWalletAccountId() <= 0) {
            throw new IllegalArgumentException("El ID de la cuenta wallet debe ser mayor que cero");
        }

        if (link.getCardId() <= 0) {
            throw new IllegalArgumentException("El ID de la tarjeta debe ser mayor que cero");
        }
    }

    public static void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
    }
}
