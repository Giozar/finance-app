package com.giozar04.walletTransactionDetails.infrastructure.persistence.mysql;

import java.util.List;
import java.util.Objects;

import com.giozar04.databases.infrastructure.persistence.mysql.DatabaseConnectionInterface;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;
import com.giozar04.walletTransactionDetails.domain.enums.WalletTransactionSourceType;
import com.giozar04.walletTransactionDetails.application.ports.output.WalletTransactionDetailRepository;
import com.giozar04.walletTransactionDetails.domain.policies.WalletTransactionDetailPolicy;

public abstract class AbstractWalletTransactionDetailJdbcRepository implements WalletTransactionDetailRepository {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final ConsoleLogger logger = ConsoleLogger.getInstance();

    protected AbstractWalletTransactionDetailJdbcRepository(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, "La conexión a base de datos no puede ser nula");
    }

    protected void validateDetail(WalletTransactionDetail detail) {
        WalletTransactionDetailPolicy.validateDetail(detail);
    }

    protected void validateId(long id) {
        WalletTransactionDetailPolicy.validateId(id);
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
