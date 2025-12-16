package com.giozar04.walletTransactionDetails.infrastructure.repositories;

import java.math.BigDecimal;
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
import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;
import com.giozar04.walletTransactionDetails.domain.enums.WalletTransactionSourceType;
import com.giozar04.walletTransactionDetails.domain.exceptions.WalletTransactionDetailExceptions;
import com.giozar04.walletTransactionDetails.domain.models.WalletTransactionDetailRepositoryAbstract;

public class WalletTransactionDetailRepositoryMySQL extends WalletTransactionDetailRepositoryAbstract {

    private static final String SQL_INSERT = """
        INSERT INTO wallet_transaction_details (
            transaction_id, source_type, wallet_account_id, card_id, amount, cashback_percentage, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """;

    private static final String SQL_SELECT_BY_ID = "SELECT * FROM wallet_transaction_details WHERE id = ?";
    private static final String SQL_UPDATE = """
        UPDATE wallet_transaction_details SET
            transaction_id = ?, source_type = ?, wallet_account_id = ?, card_id = ?, amount = ?, cashback_percentage = ?, updated_at = ?
        WHERE id = ?
    """;

    private static final String SQL_DELETE = "DELETE FROM wallet_transaction_details WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT * FROM wallet_transaction_details";
    private static final String SQL_SELECT_BY_TRANSACTION = "SELECT * FROM wallet_transaction_details WHERE transaction_id = ?";

    public WalletTransactionDetailRepositoryMySQL(DatabaseConnectionInterface databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public WalletTransactionDetail createDetail(WalletTransactionDetail detail) {
        validateDetail(detail);

        if (detail.getCreatedAt() == null) detail.setCreatedAt(ZonedDateTime.now());
        if (detail.getUpdatedAt() == null) detail.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, detail.getTransactionId());
            stmt.setString(2, detail.getSourceType().getValue());
            stmt.setLong(3, detail.getWalletAccountId());

            if (detail.getCardId() != null) stmt.setLong(4, detail.getCardId());
            else stmt.setNull(4, Types.BIGINT);

            stmt.setBigDecimal(5, detail.getAmount());
            if (detail.getCashbackPercentage() != null) {
                stmt.setBigDecimal(6, detail.getCashbackPercentage());
            } else {
                stmt.setNull(6, Types.DECIMAL);
            }

            stmt.setTimestamp(7, Timestamp.valueOf(detail.getCreatedAt().toLocalDateTime()));
            stmt.setTimestamp(8, Timestamp.valueOf(detail.getUpdatedAt().toLocalDateTime()));

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("No se pudo insertar el detalle de transacción wallet");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    detail.setId(keys.getLong(1));
                }
            }

            databaseConnection.commitTransaction();
            logger.info("Detalle creado con ID: " + detail.getId());
            return detail;

        } catch (SQLException e) {
            rollback();
            throw new WalletTransactionDetailExceptions.CreationException("Error al crear el detalle", e);
        }
    }

    @Override
    public WalletTransactionDetail getDetailById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
                throw new WalletTransactionDetailExceptions.NotFoundException("Detalle no encontrado con ID: " + id, null);
            }

        } catch (SQLException e) {
            throw new WalletTransactionDetailExceptions.RetrievalException("Error al obtener detalle", e);
        }
    }

    @Override
    public WalletTransactionDetail updateDetailById(long id, WalletTransactionDetail detail) {
        validateId(id);
        validateDetail(detail);
        detail.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setLong(1, detail.getTransactionId());
            stmt.setString(2, detail.getSourceType().getValue());
            stmt.setLong(3, detail.getWalletAccountId());

            if (detail.getCardId() != null) stmt.setLong(4, detail.getCardId());
            else stmt.setNull(4, Types.BIGINT);

            stmt.setBigDecimal(5, detail.getAmount());

            if (detail.getCashbackPercentage() != null) {
                stmt.setBigDecimal(6, detail.getCashbackPercentage());
            } else {
                stmt.setNull(6, Types.DECIMAL);
            }

            stmt.setTimestamp(7, Timestamp.valueOf(detail.getUpdatedAt().toLocalDateTime()));
            stmt.setLong(8, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new WalletTransactionDetailExceptions.NotFoundException("No se encontró el detalle para actualizar", null);

            databaseConnection.commitTransaction();
            detail.setId(id);
            return detail;

        } catch (SQLException e) {
            rollback();
            throw new WalletTransactionDetailExceptions.UpdateException("Error al actualizar detalle", e);
        }
    }

    @Override
    public void deleteDetailById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) throw new WalletTransactionDetailExceptions.NotFoundException("Detalle no encontrado", null);

            databaseConnection.commitTransaction();
            logger.info("Detalle eliminado con ID: " + id);

        } catch (SQLException e) {
            rollback();
            throw new WalletTransactionDetailExceptions.DeletionException("Error al eliminar detalle", e);
        }
    }

    @Override
    public List<WalletTransactionDetail> getAllDetails() {
        List<WalletTransactionDetail> list = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) list.add(mapResultSet(rs));
            return list;

        } catch (SQLException e) {
            throw new WalletTransactionDetailExceptions.RetrievalException("Error al obtener todos los detalles", e);
        }
    }

    @Override
    public List<WalletTransactionDetail> getDetailsByTransactionId(long transactionId) {
        List<WalletTransactionDetail> list = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_TRANSACTION)) {

            stmt.setLong(1, transactionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSet(rs));
            }

            return list;

        } catch (SQLException e) {
            throw new WalletTransactionDetailExceptions.RetrievalException("Error al obtener detalles por transacción", e);
        }
    }

    private WalletTransactionDetail mapResultSet(ResultSet rs) throws SQLException {
        WalletTransactionDetail detail = new WalletTransactionDetail();
        ZoneId zone = ZoneId.systemDefault();

        detail.setId(rs.getLong("id"));
        detail.setTransactionId(rs.getLong("transaction_id"));
        detail.setSourceType(WalletTransactionSourceType.fromValue(rs.getString("source_type")));
        detail.setWalletAccountId(rs.getLong("wallet_account_id"));

        long cardId = rs.getLong("card_id");
        detail.setCardId(rs.wasNull() ? null : cardId);

        detail.setAmount(rs.getBigDecimal("amount"));

        BigDecimal cashback = rs.getBigDecimal("cashback_percentage");
        detail.setCashbackPercentage(rs.wasNull() ? null : cashback);

        detail.setCreatedAt(ZonedDateTime.of(rs.getTimestamp("created_at").toLocalDateTime(), zone));
        detail.setUpdatedAt(ZonedDateTime.of(rs.getTimestamp("updated_at").toLocalDateTime(), zone));

        return detail;
    }

    private void rollback() {
        try {
            databaseConnection.rollbackTransaction();
        } catch (SQLException e) {
            logger.error("Rollback fallido", e);
        }
    }
}
