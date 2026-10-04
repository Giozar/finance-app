package com.giozar04.transactions.infrastructure.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;
import com.giozar04.cardTransactionDetails.infrastructure.persistence.mysql.CardTransactionDetailJdbcOperations;
import com.giozar04.databases.application.services.TransactionalExecutor;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.transactionTags.domain.interfaces.TransactionTagRepositoryInterface;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.domain.enums.PaymentMethod;
import com.giozar04.transactions.domain.enums.TransactionStatus;
import com.giozar04.transactions.application.exceptions.TransactionNotFoundException;
import com.giozar04.transactions.application.exceptions.TransactionCreationException;
import com.giozar04.transactions.application.exceptions.TransactionDeletionException;
import com.giozar04.transactions.application.exceptions.TransactionRetrievalException;
import com.giozar04.transactions.application.exceptions.TransactionUpdateException;
import com.giozar04.transactions.domain.exceptions.TransactionValidationException;
import com.giozar04.transactions.domain.models.TransactionRepositoryAbstract;
import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;
import com.giozar04.walletTransactionDetails.infrastructure.persistence.mysql.WalletTransactionDetailJdbcOperations;

/**
 * Persiste el agregado Transaction en una única unidad de trabajo (TransactionalExecutor):
 * transactions + card_transaction_details / wallet_transaction_details + transaction_tags.
 *
 * Orden obligatorio (los triggers de detalle validan contra la transacción padre):
 * - create: INSERT transactions → INSERT detalle → replaceTags.
 * - update: (bloquea la fila y comprueba que no cambie el usuario) → DELETE detalles → UPDATE transactions
 *           → INSERT detalle → replaceTags.
 * - delete: DELETE transactions (cascadas + trigger 4.1 revierten el resto).
 *
 * La columna date guarda la hora local de la zona {@code timezone} de la transacción.
 */
public class TransactionRepositoryMySQL extends TransactionRepositoryAbstract {

    private static final String SQL_INSERT = """
        INSERT INTO transactions (
            user_id, parent_transaction_id, operation_type, payment_method, status,
            source_account_id, destination_account_id, external_entity_id, category_id,
            amount, concept, description, receipt_url, comments, date, timezone, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

    private static final String SQL_UPDATE = """
        UPDATE transactions SET
            user_id = ?, parent_transaction_id = ?, operation_type = ?, payment_method = ?, status = ?,
            source_account_id = ?, destination_account_id = ?, external_entity_id = ?, category_id = ?,
            amount = ?, concept = ?, description = ?, receipt_url = ?, comments = ?, date = ?, timezone = ?,
            updated_at = ?
        WHERE id = ?
    """;

    private static final String SQL_SELECT_BY_ID = "SELECT * FROM transactions WHERE id = ?";
    private static final String SQL_SELECT_USER_FOR_UPDATE = "SELECT user_id FROM transactions WHERE id = ? FOR UPDATE";
    private static final String SQL_SELECT_ALL = "SELECT * FROM transactions ORDER BY date DESC, id DESC";
    private static final String SQL_SELECT_BY_USER = "SELECT * FROM transactions WHERE user_id = ? ORDER BY date DESC, id DESC";
    private static final String SQL_DELETE = "DELETE FROM transactions WHERE id = ?";

    // Número de columnas comunes a INSERT y UPDATE (user_id ... timezone)
    private static final int COMMON_COLUMNS = 16;

    private final TransactionalExecutor executor;
    private final CardTransactionDetailJdbcOperations cardDetailRepository;
    private final WalletTransactionDetailJdbcOperations walletDetailRepository;
    private final TransactionTagRepositoryInterface transactionTagRepository;

    public TransactionRepositoryMySQL(DatabaseConnectionInterface databaseConnection,
                                      TransactionalExecutor executor,
                                      CardTransactionDetailJdbcOperations cardDetailRepository,
                                      WalletTransactionDetailJdbcOperations walletDetailRepository,
                                      TransactionTagRepositoryInterface transactionTagRepository) {
        super(databaseConnection);
        this.executor = Objects.requireNonNull(executor, "El ejecutor transaccional no puede ser nulo");
        this.cardDetailRepository = Objects.requireNonNull(cardDetailRepository, "El repositorio de detalles de tarjeta no puede ser nulo");
        this.walletDetailRepository = Objects.requireNonNull(walletDetailRepository, "El repositorio de detalles de wallet no puede ser nulo");
        this.transactionTagRepository = Objects.requireNonNull(transactionTagRepository, "El repositorio de etiquetas de transacción no puede ser nulo");
    }

    @Override
    public Transaction createTransaction(Transaction tx) {
        validateTransaction(tx);

        if (tx.getCreatedAt() == null) tx.setCreatedAt(ZonedDateTime.now());
        if (tx.getUpdatedAt() == null) tx.setUpdatedAt(ZonedDateTime.now());

        try {
            Transaction created = executor.inTransaction(conn -> {
                long id = insertTransaction(conn, tx);
                writeChildren(conn, id, tx);
                return findById(conn, id);
            });
            logger.info("Transacción creada con ID: " + created.getId());
            return created;

        } catch (SQLException e) {
            throw new TransactionCreationException("Error al crear la transacción: " + e.getMessage(), e);
        }
    }

    @Override
    public Transaction getTransactionById(long id) {
        validateId(id);

        try {
            Transaction tx = executor.inTransaction(conn -> findById(conn, id));
            if (tx == null) {
                throw new TransactionNotFoundException("Transacción no encontrada con ID: " + id, null);
            }
            return tx;

        } catch (SQLException e) {
            throw new TransactionRetrievalException("Error al obtener la transacción con ID: " + id, e);
        }
    }

    @Override
    public Transaction updateTransactionById(long id, Transaction tx) {
        validateId(id);
        validateTransaction(tx);
        tx.setUpdatedAt(ZonedDateTime.now());

        try {
            Transaction updated = executor.inTransaction(conn -> {
                // 0) Bloquea la fila y verifica que exista y que no cambie de usuario
                checkSameUser(conn, id, tx.getUserId());

                // 1) Primero se quitan los detalles: así sus triggers revierten su efecto
                //    contra el padre todavía sin cambiar.
                cardDetailRepository.deleteByTransactionId(conn, id);
                walletDetailRepository.deleteByTransactionId(conn, id);

                // 2) UPDATE del padre (los triggers revierten y reaplican su efecto)
                try (PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
                    setCommonValues(stmt, tx);
                    stmt.setTimestamp(COMMON_COLUMNS + 1, Timestamp.valueOf(tx.getUpdatedAt().toLocalDateTime()));
                    stmt.setLong(COMMON_COLUMNS + 2, id);

                    if (stmt.executeUpdate() == 0) {
                        throw new TransactionNotFoundException("Transacción no encontrada con ID: " + id, null);
                    }
                }

                // 3) Detalle nuevo + etiquetas
                writeChildren(conn, id, tx);
                return findById(conn, id);
            });
            logger.info("Transacción actualizada con ID: " + id);
            return updated;

        } catch (SQLException e) {
            throw new TransactionUpdateException("Error al actualizar la transacción: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteTransactionById(long id) {
        validateId(id);

        try {
            executor.inTransaction(conn -> {
                try (PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {
                    stmt.setLong(1, id);
                    if (stmt.executeUpdate() == 0) {
                        throw new TransactionNotFoundException("Transacción no encontrada con ID: " + id, null);
                    }
                }
                return null;
            });
            logger.info("Transacción eliminada con ID: " + id);

        } catch (SQLException e) {
            throw new TransactionDeletionException("Error al eliminar la transacción: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> getAllTransactions() {
        try {
            return executor.inTransaction(conn -> {
                try (PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL)) {
                    return loadAggregates(conn, stmt);
                }
            });

        } catch (SQLException e) {
            throw new TransactionRetrievalException("Error al obtener las transacciones", e);
        }
    }

    @Override
    public List<Transaction> getTransactionsByUserId(long userId) {
        validateId(userId);

        try {
            return executor.inTransaction(conn -> {
                try (PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_USER)) {
                    stmt.setLong(1, userId);
                    return loadAggregates(conn, stmt);
                }
            });

        } catch (SQLException e) {
            throw new TransactionRetrievalException("Error al obtener las transacciones del usuario con ID: " + userId, e);
        }
    }

    // ---- Escritura ----

    /** El usuario de una transacción no puede cambiar (sus cuentas, categoría y tags son de ese usuario). */
    private void checkSameUser(Connection conn, long id, long newUserId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_USER_FOR_UPDATE)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new TransactionNotFoundException("Transacción no encontrada con ID: " + id, null);
                }
                if (rs.getLong("user_id") != newUserId) {
                    throw new TransactionValidationException("No se puede cambiar el usuario de una transacción");
                }
            }
        }
    }

    private long insertTransaction(Connection conn, Transaction tx) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            setCommonValues(stmt, tx);
            stmt.setTimestamp(COMMON_COLUMNS + 1, Timestamp.valueOf(tx.getCreatedAt().toLocalDateTime()));
            stmt.setTimestamp(COMMON_COLUMNS + 2, Timestamp.valueOf(tx.getUpdatedAt().toLocalDateTime()));

            if (stmt.executeUpdate() == 0) throw new SQLException("No se pudo insertar la transacción");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No se obtuvo el ID de la transacción");
                long id = keys.getLong(1);
                tx.setId(id);
                return id;
            }
        }
    }

    /** Inserta el detalle que corresponda (ya normalizado) y reemplaza las etiquetas. */
    private void writeChildren(Connection conn, long transactionId, Transaction tx) throws SQLException {
        CardTransactionDetail cardDetail = tx.getCardDetail();
        if (cardDetail != null) {
            cardDetail.setId(0);
            cardDetail.setTransactionId(transactionId);
            cardDetail.setCreatedAt(null);
            cardDetail.setUpdatedAt(null);
            cardDetailRepository.insert(conn, cardDetail);
        }

        WalletTransactionDetail walletDetail = tx.getWalletDetail();
        if (walletDetail != null) {
            walletDetail.setId(0);
            walletDetail.setTransactionId(transactionId);
            walletDetail.setCreatedAt(null);
            walletDetail.setUpdatedAt(null);
            walletDetailRepository.insert(conn, walletDetail);
        }

        transactionTagRepository.replaceTags(conn, transactionId, tx.getTagIds());
    }

    private void setCommonValues(PreparedStatement stmt, Transaction tx) throws SQLException {
        stmt.setLong(1, tx.getUserId());
        setNullableLong(stmt, 2, tx.getParentTransactionId());
        stmt.setString(3, tx.getOperationType().getValue());
        stmt.setString(4, tx.getPaymentMethod().getValue());
        stmt.setString(5, tx.getStatus().getValue());
        setNullableLong(stmt, 6, tx.getSourceAccountId());
        setNullableLong(stmt, 7, tx.getDestinationAccountId());
        setNullableLong(stmt, 8, tx.getExternalEntityId());
        stmt.setLong(9, tx.getCategoryId());
        stmt.setBigDecimal(10, tx.getAmount());
        stmt.setString(11, tx.getConcept());
        stmt.setString(12, tx.getDescription());
        stmt.setString(13, tx.getReceiptUrl());
        stmt.setString(14, tx.getComments());
        // Hora local en la zona de la transacción (LocalDateTime: el driver no la convierte)
        stmt.setObject(15, tx.getDate().withZoneSameInstant(zoneOf(tx.getTimezone())).toLocalDateTime());
        stmt.setString(16, tx.getTimezone());
    }

    private void setNullableLong(PreparedStatement stmt, int index, Long value) throws SQLException {
        if (value != null) stmt.setLong(index, value);
        else stmt.setNull(index, Types.BIGINT);
    }

    // ---- Lectura del agregado ----

    private Transaction findById(Connection conn, long id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {
            stmt.setLong(1, id);
            List<Transaction> list = loadAggregates(conn, stmt);
            return list.isEmpty() ? null : list.get(0);
        }
    }

    /** Ejecuta la consulta y completa cada transacción con su detalle y sus tagIds. */
    private List<Transaction> loadAggregates(Connection conn, PreparedStatement stmt) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapResultSet(rs));
        }

        for (Transaction tx : list) {
            List<CardTransactionDetail> cardDetails = cardDetailRepository.findByTransactionId(conn, tx.getId());
            tx.setCardDetail(cardDetails.isEmpty() ? null : cardDetails.get(0));

            List<WalletTransactionDetail> walletDetails = walletDetailRepository.findByTransactionId(conn, tx.getId());
            tx.setWalletDetail(walletDetails.isEmpty() ? null : walletDetails.get(0));

            tx.setTagIds(transactionTagRepository.findTagIds(conn, tx.getId()));
        }
        return list;
    }

    private Transaction mapResultSet(ResultSet rs) throws SQLException {
        Transaction tx = new Transaction();
        ZoneId systemZone = ZoneId.systemDefault();

        tx.setId(rs.getLong("id"));
        tx.setUserId(rs.getLong("user_id"));
        tx.setParentTransactionId(getNullableLong(rs, "parent_transaction_id"));
        tx.setOperationType(OperationTypes.fromValue(rs.getString("operation_type")));
        tx.setPaymentMethod(PaymentMethod.fromValue(rs.getString("payment_method")));
        tx.setStatus(TransactionStatus.fromValue(rs.getString("status")));

        tx.setSourceAccountId(getNullableLong(rs, "source_account_id"));
        tx.setDestinationAccountId(getNullableLong(rs, "destination_account_id"));
        tx.setExternalEntityId(getNullableLong(rs, "external_entity_id"));
        tx.setCategoryId(rs.getLong("category_id"));

        tx.setAmount(rs.getBigDecimal("amount"));
        tx.setConcept(rs.getString("concept"));
        tx.setDescription(rs.getString("description"));
        tx.setReceiptUrl(rs.getString("receipt_url"));
        tx.setComments(rs.getString("comments"));

        String timezone = rs.getString("timezone");
        tx.setTimezone(timezone);
        LocalDateTime localDate = rs.getObject("date", LocalDateTime.class);
        if (localDate != null) tx.setDate(ZonedDateTime.of(localDate, zoneOf(timezone)));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) tx.setCreatedAt(ZonedDateTime.of(created.toLocalDateTime(), systemZone));

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) tx.setUpdatedAt(ZonedDateTime.of(updated.toLocalDateTime(), systemZone));

        return tx;
    }

    private Long getNullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    /** Zona de la transacción; si el valor guardado no es válido se usa la del sistema. */
    private ZoneId zoneOf(String timezone) {
        if (timezone == null || timezone.isBlank()) return ZoneId.systemDefault();
        try {
            return ZoneId.of(timezone.trim());
        } catch (DateTimeException e) {
            logger.warn("Zona horaria inválida en transacción: " + timezone + ", se usa la del sistema", null);
            return ZoneId.systemDefault();
        }
    }
}
