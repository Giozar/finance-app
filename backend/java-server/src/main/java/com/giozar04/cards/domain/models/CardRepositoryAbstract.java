package com.giozar04.cards.domain.models;

import java.util.List;
import java.util.Objects;

import com.giozar04.card.domain.entities.Card;
import com.giozar04.card.domain.enums.CardTypes;
import com.giozar04.cards.domain.interfaces.CardRepositoryInterface;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.logging.CustomLogger;

public abstract class CardRepositoryAbstract implements CardRepositoryInterface {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final CustomLogger logger = CustomLogger.getInstance();

    protected CardRepositoryAbstract(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection,
                "La conexión a base de datos no puede ser nula");
    }

    protected void validateCard(Card card) {
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
    }

    protected void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
    }

    @Override
    public abstract Card createCard(Card card);

    @Override
    public abstract Card getCardById(long id);

    @Override
    public abstract Card updateCardById(long id, Card card);

    @Override
    public abstract void deleteCardById(long id);

    @Override
    public abstract List<Card> getAllCards();
}
