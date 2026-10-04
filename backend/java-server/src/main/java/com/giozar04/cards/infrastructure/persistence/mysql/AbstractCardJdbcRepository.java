package com.giozar04.cards.infrastructure.persistence.mysql;

import java.util.List;
import java.util.Objects;

import com.giozar04.card.domain.entities.Card;
import com.giozar04.card.domain.enums.CardTypes;
import com.giozar04.cards.application.ports.output.CardRepository;
import com.giozar04.cards.domain.policies.CardPolicy;
import com.giozar04.databases.infrastructure.persistence.mysql.DatabaseConnectionInterface;
import com.giozar04.logging.infrastructure.ConsoleLogger;

public abstract class AbstractCardJdbcRepository implements CardRepository {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final ConsoleLogger logger = ConsoleLogger.getInstance();

    protected AbstractCardJdbcRepository(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection,
                "La conexión a base de datos no puede ser nula");
    }

    protected void validateCard(Card card) {
        CardPolicy.validateCard(card);
    }

    protected void validateId(long id) {
        CardPolicy.validateId(id);
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

    @Override
    public abstract List<Card> getCardsByAccountId(long accountId);
}
