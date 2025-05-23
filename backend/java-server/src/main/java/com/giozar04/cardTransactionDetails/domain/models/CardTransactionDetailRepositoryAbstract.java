package com.giozar04.cardTransactionDetails.domain.models;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;
import com.giozar04.cardTransactionDetails.domain.interfaces.CardTransactionDetailRepositoryInterface;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.logging.CustomLogger;

public abstract class CardTransactionDetailRepositoryAbstract implements CardTransactionDetailRepositoryInterface {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final CustomLogger logger = CustomLogger.getInstance();

    protected CardTransactionDetailRepositoryAbstract(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, "La conexión a base de datos no puede ser nula");
    }

    protected void validateDetail(CardTransactionDetail detail) {
        Objects.requireNonNull(detail, "El detalle de transacción no puede ser nulo");

        if (detail.getTransactionId() <= 0) {
            throw new IllegalArgumentException("ID de transacción inválido");
        }

        if (detail.getCardId() <= 0) {
            throw new IllegalArgumentException("ID de tarjeta inválido");
        }

        if (detail.getAmount() == null || detail.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }

        if (detail.getInstallmentMonths() != null && detail.getInstallmentMonths() < 1) {
            throw new IllegalArgumentException("El número de meses debe ser mayor o igual a 1");
        }
    }

    protected void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
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
