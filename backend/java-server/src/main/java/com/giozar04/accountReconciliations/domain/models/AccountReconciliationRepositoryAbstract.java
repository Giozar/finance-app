package com.giozar04.accountReconciliations.domain.models;

import java.util.List;
import java.util.Objects;

import com.giozar04.accountReconciliations.domain.entities.AccountReconciliation;
import com.giozar04.accountReconciliations.domain.interfaces.AccountReconciliationRepositoryInterface;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.logging.CustomLogger;

public abstract class AccountReconciliationRepositoryAbstract implements AccountReconciliationRepositoryInterface {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final CustomLogger logger = CustomLogger.getInstance();

    protected AccountReconciliationRepositoryAbstract(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, "La conexión a base de datos no puede ser nula");
    }

    protected void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
    }

    @Override
    public abstract List<AccountReconciliation> getAllAccountReconciliations();

    @Override
    public abstract List<AccountReconciliation> getAccountReconciliationsByUserId(long userId);

    @Override
    public abstract AccountReconciliation getAccountReconciliationByAccountId(long accountId);

    @Override
    public abstract AccountReconciliation reconcileAccount(long accountId);
}
