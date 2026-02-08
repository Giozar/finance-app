package com.giozar04.cards.infrastructure.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.giozar04.card.domain.entities.Card;
import com.giozar04.card.domain.enums.CardTypes;
import com.giozar04.card.domain.exceptions.CardExceptions;
import com.giozar04.cards.domain.models.CardRepositoryAbstract;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;

public class CardRepositoryMySQL extends CardRepositoryAbstract {

    private static final String SQL_INSERT = """
        INSERT INTO cards (account_id, name, card_type, card_number, expiration_date, status, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """;

    private static final String SQL_SELECT_BY_ID = "SELECT * FROM cards WHERE id = ?";
    private static final String SQL_UPDATE = """
        UPDATE cards SET account_id = ?, name = ?, card_type = ?, card_number = ?, expiration_date = ?, status = ?, updated_at = ?
        WHERE id = ?
    """;

    private static final String SQL_DELETE = "DELETE FROM cards WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT * FROM cards";

    public CardRepositoryMySQL(DatabaseConnectionInterface databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public Card createCard(Card card) {
        validateCard(card);

        if (card.getCreatedAt() == null) card.setCreatedAt(ZonedDateTime.now());
        if (card.getUpdatedAt() == null) card.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, card.getAccountId());
            stmt.setString(2, card.getName());
            stmt.setString(3, card.getCardType().getValue());
            stmt.setString(4, card.getCardNumber());
            stmt.setTimestamp(5, Timestamp.valueOf(card.getExpirationDate().toLocalDateTime()));
            stmt.setString(6, card.getStatus());
            stmt.setTimestamp(7, Timestamp.valueOf(card.getCreatedAt().toLocalDateTime()));
            stmt.setTimestamp(8, Timestamp.valueOf(card.getUpdatedAt().toLocalDateTime()));

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("No se pudo insertar la tarjeta");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    card.setId(keys.getLong(1));
                }
            }

            databaseConnection.commitTransaction();
            logger.info("Tarjeta creada con ID: " + card.getId());
            return card;

        } catch (SQLException e) {
            rollback();
            throw new CardExceptions.CardCreationException("Error al crear la tarjeta", e);
        }
    }

    @Override
    public Card getCardById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCard(rs);
                } else {
                    throw new CardExceptions.CardNotFoundException("Tarjeta no encontrada con ID: " + id, null);
                }
            }

        } catch (SQLException e) {
            throw new CardExceptions.CardRetrievalException("Error al obtener tarjeta con ID: " + id, e);
        }
    }

    @Override
    public Card updateCardById(long id, Card card) {
        validateId(id);
        validateCard(card);
        card.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setLong(1, card.getAccountId());
            stmt.setString(2, card.getName());
            stmt.setString(3, card.getCardType().getValue());
            stmt.setString(4, card.getCardNumber());
            stmt.setTimestamp(5, Timestamp.valueOf(card.getExpirationDate().toLocalDateTime()));
            stmt.setString(6, card.getStatus());
            stmt.setTimestamp(7, Timestamp.valueOf(card.getUpdatedAt().toLocalDateTime()));
            stmt.setLong(8, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new CardExceptions.CardNotFoundException("Tarjeta no encontrada con ID: " + id, null);
            }

            databaseConnection.commitTransaction();
            card.setId(id);
            return card;

        } catch (SQLException e) {
            rollback();
            throw new CardExceptions.CardUpdateException("Error al actualizar tarjeta con ID: " + id, e);
        }
    }

    @Override
    public void deleteCardById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new CardExceptions.CardNotFoundException("Tarjeta no encontrada con ID: " + id, null);
            }

            databaseConnection.commitTransaction();
            logger.info("Tarjeta eliminada con ID: " + id);

        } catch (SQLException e) {
            rollback();
            throw new CardExceptions.CardDeletionException("Error al eliminar tarjeta con ID: " + id, e);
        }
    }

    @Override
    public List<Card> getAllCards() {
        List<Card> cards = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                cards.add(mapResultSetToCard(rs));
            }

            return cards;

        } catch (SQLException e) {
            throw new CardExceptions.CardRetrievalException("Error al obtener todas las tarjetas", e);
        }
    }

    private Card mapResultSetToCard(ResultSet rs) throws SQLException {
        Card card = new Card();
        ZoneId zone = ZoneId.systemDefault();

        card.setId(rs.getLong("id"));
        card.setAccountId(rs.getLong("account_id"));
        card.setName(rs.getString("name"));
        card.setCardType(CardTypes.fromValue(rs.getString("card_type")));
        card.setCardNumber(rs.getString("card_number"));
        card.setStatus(rs.getString("status"));

        Timestamp expiration = rs.getTimestamp("expiration_date");
        if (expiration != null) {
            card.setExpirationDate(ZonedDateTime.of(expiration.toLocalDateTime(), zone));
        }

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            card.setCreatedAt(ZonedDateTime.of(created.toLocalDateTime(), zone));
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            card.setUpdatedAt(ZonedDateTime.of(updated.toLocalDateTime(), zone));
        }

        return card;
    }

    private void rollback() {
        try {
            databaseConnection.rollbackTransaction();
        } catch (SQLException e2) {
            logger.error("Error al hacer rollback de la transacción", e2);
        }
    }
}
