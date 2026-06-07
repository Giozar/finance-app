package com.giozar04.walletCardLinks.infrastructure.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;
import com.giozar04.walletCardLinks.domain.exceptions.WalletCardLinkExceptions;
import com.giozar04.walletCardLinks.domain.models.WalletCardLinkRepositoryAbstract;

/**
 * Repositorio MySQL para wallet_card_links.
 *
 * La tabla real (schemas.sql) usa clave compuesta (account_id, card_id) sin columna id propia.
 * El campo WalletCardLink.id se mapea al card_id para compatibilidad con los controladores.
 */
public class WalletCardLinkRepositoryMySQL extends WalletCardLinkRepositoryAbstract {

    // La tabla usa 'account_id' (no 'wallet_account_id')
    private static final String SQL_INSERT =
        "INSERT INTO wallet_card_links (account_id, card_id, created_at, updated_at) VALUES (?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_CARD_ID =
        "SELECT * FROM wallet_card_links WHERE card_id = ?";

    private static final String SQL_DELETE_BY_PK =
        "DELETE FROM wallet_card_links WHERE account_id = ? AND card_id = ?";

    private static final String SQL_SELECT_ALL =
        "SELECT * FROM wallet_card_links";

    private static final String SQL_SELECT_BY_WALLET =
        "SELECT * FROM wallet_card_links WHERE account_id = ?";

    public WalletCardLinkRepositoryMySQL(DatabaseConnectionInterface databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public WalletCardLink createLink(WalletCardLink link) {
        validateLink(link);

        if (link.getCreatedAt() == null) link.setCreatedAt(ZonedDateTime.now());
        if (link.getUpdatedAt() == null) link.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {

            stmt.setLong(1, link.getWalletAccountId());
            stmt.setLong(2, link.getCardId());
            stmt.setTimestamp(3, Timestamp.valueOf(link.getCreatedAt().toLocalDateTime()));
            stmt.setTimestamp(4, Timestamp.valueOf(link.getUpdatedAt().toLocalDateTime()));

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("No se pudo insertar el enlace wallet-card");

            // No hay id autoincremental; usamos cardId como identificador del vinculo
            link.setId(link.getCardId());

            databaseConnection.commitTransaction();
            logger.info("Enlace wallet-card creado: account_id=" + link.getWalletAccountId() + ", card_id=" + link.getCardId());
            return link;

        } catch (SQLException e) {
            rollback();
            throw new WalletCardLinkExceptions.CreationException("Error al crear el enlace", e);
        }
    }

    /**
     * Obtiene un vínculo usando card_id como identificador único del vínculo
     * (cada tarjeta solo puede estar vinculada a una wallet a la vez).
     */
    @Override
    public WalletCardLink getLinkById(long cardId) {
        validateId(cardId);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_CARD_ID)) {

            stmt.setLong(1, cardId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                } else {
                    throw new WalletCardLinkExceptions.NotFoundException("Enlace no encontrado con card_id: " + cardId, null);
                }
            }

        } catch (SQLException e) {
            throw new WalletCardLinkExceptions.RetrievalException("Error al obtener enlace con card_id: " + cardId, e);
        }
    }

    /**
     * Actualización: elimina el vínculo existente y crea uno nuevo.
     */
    @Override
    public WalletCardLink updateLinkById(long id, WalletCardLink link) {
        deleteLinkById(id);
        return createLink(link);
    }

    /**
     * Elimina un vínculo usando card_id para localizar el wallet_account_id y borrar la PK compuesta.
     */
    @Override
    public void deleteLinkById(long cardId) {
        validateId(cardId);

        WalletCardLink link = getLinkById(cardId);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_BY_PK)) {

            stmt.setLong(1, link.getWalletAccountId());
            stmt.setLong(2, link.getCardId());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new WalletCardLinkExceptions.NotFoundException("No se encontró el enlace para eliminar", null);
            }

            databaseConnection.commitTransaction();
            logger.info("Enlace wallet-card eliminado: account_id=" + link.getWalletAccountId() + ", card_id=" + cardId);

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

        // La tabla usa 'account_id', no 'wallet_account_id', y no hay columna 'id'
        link.setWalletAccountId(rs.getLong("account_id"));
        link.setCardId(rs.getLong("card_id"));
        link.setId(link.getCardId()); // cardId como identificador del vinculo

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
