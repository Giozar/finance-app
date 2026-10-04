package com.giozar04.cards.domain.policies;

import java.util.List;
import java.util.Objects;
import com.giozar04.card.domain.entities.Card;
import com.giozar04.card.domain.enums.CardTypes;

public final class CardPolicy {
    private CardPolicy() {}

    public static void validateCard(Card card) {
        Objects.requireNonNull(card, "La tarjeta no puede ser nula");

        if (card.getName() == null || card.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre de la tarjeta no puede estar vacío");
        }

        if (card.getCardType() == null) {
            throw new IllegalArgumentException("El tipo de tarjeta es obligatorio");
        }

        try {
            CardTypes.valueOf(card.getCardType().name());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de tarjeta no válido: " + card.getCardType());
        }

        if (card.getAccountId() <= 0) {
            throw new IllegalArgumentException("ID de cuenta inválido para la tarjeta");
        }

        if (card.getCardNumber() == null || card.getCardNumber().isBlank()) {
            throw new IllegalArgumentException("El número de tarjeta (últimos 4 dígitos) no puede estar vacío");
        }

        if (card.getCardNumber().length() != 4 || !card.getCardNumber().matches("\\d{4}")) {
            throw new IllegalArgumentException("El número de tarjeta debe contener exactamente 4 dígitos");
        }

        if (card.getExpirationDate() == null) {
            throw new IllegalArgumentException("La fecha de expiración de la tarjeta no puede estar vacía");
        }

        // Normaliza el estado: null -> ACTIVE; en otro caso, mayúsculas
        String status = card.getStatus() == null ? "ACTIVE" : card.getStatus().trim().toUpperCase();
        if (!status.equals("ACTIVE") && !status.equals("BLOCKED") && !status.equals("EXPIRED")) {
            throw new IllegalArgumentException("Estado de tarjeta no válido: " + card.getStatus() + " (use ACTIVE, BLOCKED o EXPIRED)");
        }
        card.setStatus(status);
    }

    public static void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
    }
}
