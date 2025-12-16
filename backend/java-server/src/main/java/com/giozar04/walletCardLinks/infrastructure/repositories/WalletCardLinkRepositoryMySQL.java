package com.giozar04.walletCardLinks.infrastructure.repositories;

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

import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;
import com.giozar04.walletCardLinks.domain.exceptions.WalletCardLinkExceptions;
import com.giozar04.walletCardLinks.domain.models.WalletCardLinkRepositoryAbstract;

public class WalletCardLinkRepositoryMySQL extends WalletCardLinkRepositoryAbstract {

    private static final String SQL_INSERT = """
        INSERT INTO wallet_card_links (wallet_account_id, card_id, created_at, updated_at)
        VALUES (?, ?, ?, ?)
    """;

    private static final String SQL_SELECT_BY_ID = "SELECT * FROM wallet_card_links WHERE id = ?";
    private static final String SQL_UPDATE = """
        UPDATE wallet_card_links SET wallet_account_id = ?, card_id = ?, updated_at = ?
        WHERE id = ?
    """;
    private static final String SQL_DELETE = "DELETE FROM wallet_card_links WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT * FROM wallet_card_links";
    private static final String SQL_SELECT_BY_WALLET = "SELECT * FROM wallet_card_links WHERE wallet_account_id = ?";

    public WalletCardLinkRepositoryMySQL(DatabaseConnectionInterface databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public WalletCardLink createLink(WalletCardLink link) {
        validateLink(link);

        if (link.getCreatedAt() == null) link.setCreatedAt(ZonedDateTime.now());
        if (link.getUpdatedAt() == null) link.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, link.getWalletAccountId());
            stmt.setLong(2, link.getCardId());
            stmt.setTimestamp(3, Timestamp.valueOf(link.getCreatedAt().toLocalDateTime()));
            stmt.setTimestamp(4, Timestamp.valueOf(link.getUpdatedAt().toLocalDateTime()));

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("No se pudo insertar el enlace wallet-card");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    link.setId(keys.getLong(1));
                }
            }

            databaseConnection.commitTransaction();
            logger.info("Enlace wallet-card creado con ID: " + link.getId());
            return link;

        } catch (SQLException e) {
            rollback();
            throw new WalletCardLinkExceptions.CreationException("Error al crear el enlace", e);
        }
    }

    @Override
    public WalletCardLink getLinkById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                } else {
                    throw new WalletCardLinkExceptions.NotFoundException("Enlace no encontrado con ID: " + id, null);
                }
            }

        } catch (SQLException e) {
            throw new WalletCardLinkExceptions.RetrievalException("Error al obtener el enlace con ID: " + id, e);
        }
    }

    @Override
    public WalletCardLink updateLinkById(long id, WalletCardLink link) {
        validateId(id);
        validateLink(link);
        link.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setLong(1, link.getWalletAccountId());
            stmt.setLong(2, link.getCardId());
            stmt.setTimestamp(3, Timestamp.valueOf(link.getUpdatedAt().toLocalDateTime()));
            stmt.setLong(4, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new WalletCardLinkExceptions.NotFoundException("No se encontró el enlace para actualizar", null);
            }

            databaseConnection.commitTransaction();
            link.setId(id);
            return link;

        } catch (SQLException e) {
            rollback();
            throw new WalletCardLinkExceptions.UpdateException("Error al actualizar el enlace", e);
        }
    }

    @Override
    public void deleteLinkById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new WalletCardLinkExceptions.NotFoundException("No se encontró el enlace para eliminar", null);
            }

            databaseConnection.commitTransaction();
            logger.info("Enlace wallet-card eliminado con ID: " + id);

        } catch (SQLException e) {
            rollback();
            throw new WalletCardLinkExceptions.DeletionException("Error al eliminar el enlace", e);
        }
    }

    @Override
    public List<WalletCardLink> getAllLinks() {
        List<WalletCardLink> list = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

            return list;

        } catch (SQLException e) {
            throw new WalletCardLinkExceptions.RetrievalException("Error al obtener todos los enlaces", e);
        }
    }

    @Override
    public List<WalletCardLink> getLinksByWalletAccountId(long walletAccountId) {
        List<WalletCardLink> list = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_WALLET)) {

            stmt.setLong(1, walletAccountId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }

            return list;

        } catch (SQLException e) {
            throw new WalletCardLinkExceptions.RetrievalException("Error al obtener enlaces por walletAccountId", e);
        }
    }

    private WalletCardLink mapResultSet(ResultSet rs) throws SQLException {
        WalletCardLink link = new WalletCardLink();
        ZoneId zone = ZoneId.systemDefault();

        link.setId(rs.getLong("id"));
        link.setWalletAccountId(rs.getLong("wallet_account_id"));
        link.setCardId(rs.getLong("card_id"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            link.setCreatedAt(ZonedDateTime.of(created.toLocalDateTime(), zone));
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            link.setUpdatedAt(ZonedDateTime.of(updated.toLocalDateTime(), zone));
        }

        return link;
    }

    private void rollback() {
        try {
            databaseConnection.rollbackTransaction();
        } catch (SQLException e) {
            logger.error("Error al hacer rollback de wallet_card_links", e);
        }
    }
}
