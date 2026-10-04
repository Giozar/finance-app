package com.giozar04.cardTransactionDetails.infrastructure.persistence.mysql;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;
import com.giozar04.cardTransactionDetails.application.ports.output.CardTransactionDetailRepository;
import com.giozar04.cardTransactionDetails.domain.policies.CardTransactionDetailPolicy;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.logging.infrastructure.ConsoleLogger;

public abstract class AbstractCardTransactionDetailJdbcRepository implements CardTransactionDetailRepository {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final ConsoleLogger logger = ConsoleLogger.getInstance();

    protected AbstractCardTransactionDetailJdbcRepository(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, "La conexión a base de datos no puede ser nula");
    }

    protected void validateDetail(CardTransactionDetail detail) {
        CardTransactionDetailPolicy.validateDetail(detail);
    }

    protected void validateId(long id) {
        CardTransactionDetailPolicy.validateId(id);
    }

    @Override
    public abstract CardTransactionDetail createDetail(CardTransactionDetail detail);

    @Override
    public abstract CardTransactionDetail getDetailById(long id);

    @Override
    public abstract CardTransactionDetail updateDetailById(long id, CardTransactionDetail detail);

    @Override
    public abstract void deleteDetailById(long id);

    @Override
    public abstract List<CardTransactionDetail> getAllDetails();

    @Override
    public abstract List<CardTransactionDetail> getDetailsByTransactionId(long transactionId);
}
