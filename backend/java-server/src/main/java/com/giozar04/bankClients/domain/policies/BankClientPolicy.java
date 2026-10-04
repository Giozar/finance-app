package com.giozar04.bankClients.domain.policies;

import java.util.List;
import java.util.Objects;
import com.giozar04.bankClient.domain.entities.BankClient;

public final class BankClientPolicy {
    private BankClientPolicy() {}

    public static void validateBankClient(BankClient client) {
        Objects.requireNonNull(client, "El objeto BankClient no puede ser nulo");
        if (client.getBankName() == null || client.getBankName().isBlank()) {
            throw new IllegalArgumentException("El nombre del banco es obligatorio");
        }
        if (client.getClientNumber() == null || client.getClientNumber().isBlank()) {
            throw new IllegalArgumentException("El número de cliente es obligatorio");
        }
        if (client.getUserId() <= 0) {
            throw new IllegalArgumentException("El ID del usuario debe ser válido");
        }
    }

    public static void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
    }
}
