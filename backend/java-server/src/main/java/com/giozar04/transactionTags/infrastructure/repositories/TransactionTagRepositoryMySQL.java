package com.giozar04.transactionTags.infrastructure.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.giozar04.transactionTags.domain.interfaces.TransactionTagRepositoryInterface;

public class TransactionTagRepositoryMySQL implements TransactionTagRepositoryInterface {

    private static final String SQL_DELETE_BY_TRANSACTION = "DELETE FROM transaction_tags WHERE transaction_id = ?";
    private static final String SQL_INSERT = "INSERT INTO transaction_tags (transaction_id, tag_id) VALUES (?, ?)";
    private static final String SQL_SELECT_TAG_IDS = "SELECT tag_id FROM transaction_tags WHERE transaction_id = ? ORDER BY tag_id";

    @Override
    public void replaceTags(Connection conn, long transactionId, List<Long> tagIds) throws SQLException {
        Objects.requireNonNull(conn, "La conexión no puede ser nula");

        try (PreparedStatement delete = conn.prepareStatement(SQL_DELETE_BY_TRANSACTION)) {
            delete.setLong(1, transactionId);
            delete.executeUpdate();
        }

        if (tagIds == null || tagIds.isEmpty()) return;

        // Sin duplicados (PK compuesta) y conservando el orden recibido
        Set<Long> unique = new LinkedHashSet<>(tagIds);
        unique.remove(null);

        try (PreparedStatement insert = conn.prepareStatement(SQL_INSERT)) {
            for (Long tagId : unique) {
                insert.setLong(1, transactionId);
                insert.setLong(2, tagId);
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    @Override
    public List<Long> findTagIds(Connection conn, long transactionId) throws SQLException {
        Objects.requireNonNull(conn, "La conexión no puede ser nula");
        List<Long> ids = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_TAG_IDS)) {
            stmt.setLong(1, transactionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) ids.add(rs.getLong("tag_id"));
            }
        }
        return ids;
    }
}
