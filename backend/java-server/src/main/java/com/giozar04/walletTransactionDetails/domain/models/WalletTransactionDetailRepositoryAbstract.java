package com.giozar04.walletTransactionDetails.domain.models;

import java.util.List;
import java.util.Objects;

import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.logging.CustomLogger;
import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;
import com.giozar04.walletTransactionDetails.domain.enums.WalletTransactionSourceType;
import com.giozar04.walletTransactionDetails.domain.interfaces.WalletTransactionDetailRepositoryInterface;

public abstract class WalletTransactionDetailRepositoryAbstract implements WalletTransactionDetailRepositoryInterface {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final CustomLogger logger = CustomLogger.getInstance();

    protected WalletTransactionDetailRepositoryAbstract(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, "La conexión a base de datos no puede ser nula");
    }

    protected void validateDetail(WalletTransactionDetail detail) {
        Objects.requireNonNull(detail, "El detalle de transacción wallet no puede ser nulo");

        if (detail.getTransactionId() <= 0) {
            throw new IllegalArgumentException("ID de transacción inválido");
        }

        if (detail.getWalletAccountId() <= 0) {
            throw new IllegalArgumentException("ID de cuenta wallet inválido");
        }

        if (detail.getSourceType() == null) {
            throw new IllegalArgumentException("El tipo de fuente es obligatorio");
        }

        try {
            WalletTransactionSourceType.fromValue(detail.getSourceType().getValue());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de fuente inválido: " + detail.getSourceType(), e);
        }

        if (detail.getAmount() == null || detail.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }

        if (detail.getCashbackPercentage() != null &&
            (detail.getCashbackPercentage().signum() < 0 || detail.getCashbackPercentage().compareTo(new java.math.BigDecimal("100")) > 0)) {
            throw new IllegalArgumentException("Porcentaje de cashback inválido");
        }
    }

    protected void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
    }

    @Override
    public abstract WalletTransactionDetail createDetail(WalletTransactionDetail detail);

    @Override
    public abstract WalletTransactionDetail getDetailById(long id);

    @Override
    public abstract WalletTransactionDetail updateDetailById(long id, WalletTransactionDetail detail);

    @Override
    public abstract void deleteDetailById(long id);

    @Override
    public abstract List<WalletTransactionDetail> getAllDetails();

    @Override
    public abstract List<WalletTransactionDetail> getDetailsByTransactionId(long transactionId);
}
