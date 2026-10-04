package com.giozar04.cardTransactionDetails.infrastructure.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;
import com.giozar04.cardTransactionDetails.application.exceptions.CardTransactionDetailCreationException;
import com.giozar04.cardTransactionDetails.application.exceptions.CardTransactionDetailDeletionException;
import com.giozar04.cardTransactionDetails.application.exceptions.CardTransactionDetailNotFoundException;
import com.giozar04.cardTransactionDetails.application.exceptions.CardTransactionDetailRetrievalException;
import com.giozar04.cardTransactionDetails.application.exceptions.CardTransactionDetailUpdateException;
import com.giozar04.cardTransactionDetails.domain.interfaces.CardTransactionDetailTransactionalRepositoryInterface;
import com.giozar04.cardTransactionDetails.domain.models.CardTransactionDetailRepositoryAbstract;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;

public class CardTransactionDetailRepositoryMySQL extends CardTransactionDetailRepositoryAbstract
        implements CardTransactionDetailTransactionalRepositoryInterface {

    private static final String SQL_INSERT = """
        INSERT INTO card_transaction_details (
            transaction_id, card_id, amount, installment_months, interest_free, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?)
    """;

    private static final String SQL_SELECT_BY_ID = "SELECT * FROM card_transaction_details WHERE id = ?";
    private static final String SQL_UPDATE = """
        UPDATE card_transaction_details SET
            transaction_id = ?, card_id = ?, amount = ?, installment_months = ?, interest_free = ?, updated_at = ?
        WHERE id = ?
    """;

    private static final String SQL_DELETE = "DELETE FROM card_transaction_details WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT * FROM card_transaction_details";
    private static final String SQL_SELECT_BY_TRANSACTION = "SELECT * FROM card_transaction_details WHERE transaction_id = ?";
    private static final String SQL_DELETE_BY_TRANSACTION = "DELETE FROM card_transaction_details WHERE transaction_id = ?";

    public CardTransactionDetailRepositoryMySQL(DatabaseConnectionInterface databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public CardTransactionDetail createDetail(CardTransactionDetail detail) {
        try (Connection conn = databaseConnection.getConnection()) {
            insert(conn, detail);
            databaseConnection.commitTransaction();
            logger.info("Detalle tarjeta creado con ID: " + detail.getId());
            return detail;

        } catch (SQLException e) {
            rollback();
            throw new CardTransactionDetailCreationException("Error al crear detalle tarjeta: " + e.getMessage(), e);
        }
    }

    @Override
    public CardTransactionDetail getDetailById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
                throw new CardTransactionDetailNotFoundException("Detalle tarjeta no encontrado con ID: " + id, null);
            }

        } catch (SQLException e) {
            throw new CardTransactionDetailRetrievalException("Error al obtener detalle", e);
        }
    }

    @Override
    public CardTransactionDetail updateDetailById(long id, CardTransactionDetail detail) {
        validateId(id);
        validateDetail(detail);
        detail.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setLong(1, detail.getTransactionId());
            stmt.setLong(2, detail.getCardId());
            stmt.setBigDecimal(3, detail.getAmount());

            if (detail.getInstallmentMonths() != null) {
                stmt.setInt(4, detail.getInstallmentMonths());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setBoolean(5, detail.isInterestFree());
            stmt.setTimestamp(6, Timestamp.valueOf(detail.getUpdatedAt().toLocalDateTime()));
            stmt.setLong(7, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new CardTransactionDetailNotFoundException("No se encontró el detalle para actualizar", null);

            databaseConnection.commitTransaction();
            detail.setId(id);
            return detail;

        } catch (SQLException e) {
            rollback();
            throw new CardTransactionDetailUpdateException("Error al actualizar detalle tarjeta", e);
        }
    }

    @Override
    public void deleteDetailById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) throw new CardTransactionDetailNotFoundException("Detalle no encontrado", null);

            databaseConnection.commitTransaction();
            logger.info("Detalle tarjeta eliminado con ID: " + id);

        } catch (SQLException e) {
            rollback();
            throw new CardTransactionDetailDeletionException("Error al eliminar detalle", e);
        }
    }

    @Override
    public List<CardTransactionDetail> getAllDetails() {
        List<CardTransactionDetail> list = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) list.add(mapResultSet(rs));
            return list;

        } catch (SQLException e) {
            throw new CardTransactionDetailRetrievalException("Error al obtener detalles", e);
        }
    }

    @Override
    public List<CardTransactionDetail> getDetailsByTransactionId(long transactionId) {
        try (Connection conn = databaseConnection.getConnection()) {
            return findByTransactionId(conn, transactionId);

        } catch (SQLException e) {
            throw new CardTransactionDetailRetrievalException("Error al obtener detalles por transacción", e);
        }
    }

    // ---- Operaciones dentro de una unidad de trabajo externa (no hacen commit ni cierran la conexión) ----

    @Override
    public CardTransactionDetail insert(Connection conn, CardTransactionDetail detail) throws SQLException {
        validateDetail(detail);

        if (detail.getCreatedAt() == null) detail.setCreatedAt(ZonedDateTime.now());
        if (detail.getUpdatedAt() == null) detail.setUpdatedAt(ZonedDateTime.now());

        try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, detail.getTransactionId());
            stmt.setLong(2, detail.getCardId());
            stmt.setBigDecimal(3, detail.getAmount());

            if (detail.getInstallmentMonths() != null) {
                stmt.setInt(4, detail.getInstallmentMonths());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setBoolean(5, detail.isInterestFree());
            stmt.setTimestamp(6, Timestamp.valueOf(detail.getCreatedAt().toLocalDateTime()));
            stmt.setTimestamp(7, Timestamp.valueOf(detail.getUpdatedAt().toLocalDateTime()));

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("No se pudo insertar el detalle de transacción con tarjeta");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    detail.setId(keys.getLong(1));
                }
            }
            return detail;
        }
    }

    @Override
    public int deleteByTransactionId(Connection conn, long transactionId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_BY_TRANSACTION)) {
            stmt.setLong(1, transactionId);
            return stmt.executeUpdate();
        }
    }

    @Override
    public List<CardTransactionDetail> findByTransactionId(Connection conn, long transactionId) throws SQLException {
        List<CardTransactionDetail> list = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_TRANSACTION)) {
            stmt.setLong(1, transactionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    private CardTransactionDetail mapResultSet(ResultSet rs) throws SQLException {
        CardTransactionDetail detail = new CardTransactionDetail();
        ZoneId zone = ZoneId.systemDefault();

        detail.setId(rs.getLong("id"));
        detail.setTransactionId(rs.getLong("transaction_id"));
        detail.setCardId(rs.getLong("card_id"));
        detail.setAmount(rs.getBigDecimal("amount"));

        int installments = rs.getInt("installment_months");
        detail.setInstallmentMonths(rs.wasNull() ? null : installments);

        detail.setInterestFree(rs.getBoolean("interest_free"));
        detail.setCreatedAt(ZonedDateTime.of(rs.getTimestamp("created_at").toLocalDateTime(), zone));
        detail.setUpdatedAt(ZonedDateTime.of(rs.getTimestamp("updated_at").toLocalDateTime(), zone));

        return detail;
    }

    private void rollback() {
        try {
            databaseConnection.rollbackTransaction();
        } catch (SQLException e) {
            logger.error("Error durante rollback", e);
        }
    }
}
