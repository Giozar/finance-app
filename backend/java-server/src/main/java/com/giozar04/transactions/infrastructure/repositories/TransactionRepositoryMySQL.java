package com.giozar04.transactions.infrastructure.repositories;

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

import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.domain.enums.PaymentMethod;
import com.giozar04.transactions.domain.exceptions.TransactionExceptions;
import com.giozar04.transactions.domain.models.TransactionRepositoryAbstract;

public class TransactionRepositoryMySQL extends TransactionRepositoryAbstract {

    private static final String SQL_INSERT = """
        INSERT INTO transactions (
            operation_type, payment_method, source_account_id, destination_account_id, external_entity_id,
            amount, concept, category, description, comments, date, timezone, tags, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

    private static final String SQL_SELECT_BY_ID = "SELECT * FROM transactions WHERE id = ?";
    private static final String SQL_UPDATE = """
        UPDATE transactions SET
            operation_type = ?, payment_method = ?, source_account_id = ?, destination_account_id = ?, external_entity_id = ?,
            amount = ?, concept = ?, category = ?, description = ?, comments = ?, date = ?, timezone = ?, tags = ?, updated_at = ?
        WHERE id = ?
    """;

    private static final String SQL_DELETE = "DELETE FROM transactions WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT * FROM transactions";

    public TransactionRepositoryMySQL(DatabaseConnectionInterface databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public Transaction createTransaction(Transaction tx) {
        validateTransaction(tx);

        if (tx.getCreatedAt() == null) tx.setCreatedAt(ZonedDateTime.now());
        if (tx.getUpdatedAt() == null) tx.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            setStatementValues(stmt, tx, false);

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("No se pudo insertar la transacción");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    tx.setId(keys.getLong(1));
                }
            }

            databaseConnection.commitTransaction();
            logger.info("Transacción creada con ID: " + tx.getId());
            return tx;

        } catch (SQLException e) {
            rollback();
            throw new TransactionExceptions.CreationException("Error al crear transacción", e);
        }
    }

    @Override
    public Transaction getTransactionById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
                throw new TransactionExceptions.NotFoundException("Transacción no encontrada con ID: " + id, null);
            }

        } catch (SQLException e) {
            throw new TransactionExceptions.RetrievalException("Error al obtener transacción", e);
        }
    }

    @Override
    public Transaction updateTransactionById(long id, Transaction tx) {
        validateId(id);
        validateTransaction(tx);
        tx.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            setStatementValues(stmt, tx, true);
            stmt.setLong(15, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new TransactionExceptions.NotFoundException("No se encontró la transacción a actualizar", null);

            databaseConnection.commitTransaction();
            tx.setId(id);
            return tx;

        } catch (SQLException e) {
            rollback();
            throw new TransactionExceptions.UpdateException("Error al actualizar transacción", e);
        }
    }

    @Override
    public void deleteTransactionById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, id);
            int affected = stmt.executeUpdate();
            if (affected == 0) throw new TransactionExceptions.NotFoundException("Transacción no encontrada", null);

            databaseConnection.commitTransaction();
            logger.info("Transacción eliminada con ID: " + id);

        } catch (SQLException e) {
            rollback();
            throw new TransactionExceptions.DeletionException("Error al eliminar transacción", e);
        }
    }

    @Override
    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

            return list;

        } catch (SQLException e) {
            throw new TransactionExceptions.RetrievalException("Error al obtener transacciones", e);
        }
    }

    private Transaction mapResultSet(ResultSet rs) throws SQLException {
        Transaction tx = new Transaction();
        ZoneId zone = ZoneId.systemDefault();

        tx.setId(rs.getLong("id"));
        tx.setOperationType(OperationTypes.fromValue(rs.getString("operation_type")));
        tx.setPaymentMethod(PaymentMethod.fromValue(rs.getString("payment_method")));

        tx.setSourceAccountId(rs.getObject("source_account_id") != null ? rs.getLong("source_account_id") : null);
        tx.setDestinationAccountId(rs.getObject("destination_account_id") != null ? rs.getLong("destination_account_id") : null);
        tx.setExternalEntityId(rs.getObject("external_entity_id") != null ? rs.getLong("external_entity_id") : null);

        tx.setAmount(rs.getBigDecimal("amount"));
        tx.setConcept(rs.getString("concept"));
        tx.setCategory(rs.getString("category"));
        tx.setDescription(rs.getString("description"));
        tx.setComments(rs.getString("comments"));
        tx.setDate(ZonedDateTime.of(rs.getTimestamp("date").toLocalDateTime(), zone));
        tx.setTimezone(rs.getString("timezone"));
        tx.setTags(rs.getString("tags"));

        tx.setCreatedAt(ZonedDateTime.of(rs.getTimestamp("created_at").toLocalDateTime(), zone));
        tx.setUpdatedAt(ZonedDateTime.of(rs.getTimestamp("updated_at").toLocalDateTime(), zone));

        return tx;
    }

    private void setStatementValues(PreparedStatement stmt, Transaction tx, boolean isUpdate) throws SQLException {
        stmt.setString(1, tx.getOperationType().getValue());
        stmt.setString(2, tx.getPaymentMethod().getValue());

        if (tx.getSourceAccountId() != null) stmt.setLong(3, tx.getSourceAccountId());
        else stmt.setNull(3, Types.BIGINT);

        if (tx.getDestinationAccountId() != null) stmt.setLong(4, tx.getDestinationAccountId());
        else stmt.setNull(4, Types.BIGINT);

        if (tx.getExternalEntityId() != null) stmt.setLong(5, tx.getExternalEntityId());
        else stmt.setNull(5, Types.BIGINT);

        stmt.setBigDecimal(6, tx.getAmount());
        stmt.setString(7, tx.getConcept());
        stmt.setString(8, tx.getCategory());
        stmt.setString(9, tx.getDescription());
        stmt.setString(10, tx.getComments());
        stmt.setTimestamp(11, Timestamp.valueOf(tx.getDate().toLocalDateTime()));
        stmt.setString(12, tx.getTimezone());
        stmt.setString(13, tx.getTags());

        if (!isUpdate) {
            stmt.setTimestamp(14, Timestamp.valueOf(tx.getCreatedAt().toLocalDateTime()));
            stmt.setTimestamp(15, Timestamp.valueOf(tx.getUpdatedAt().toLocalDateTime()));
        } else {
            stmt.setTimestamp(14, Timestamp.valueOf(tx.getUpdatedAt().toLocalDateTime()));
        }
    }

    private void rollback() {
        try {
            databaseConnection.rollbackTransaction();
        } catch (SQLException e) {
            logger.error("Error al hacer rollback en transacción", e);
        }
    }
}
