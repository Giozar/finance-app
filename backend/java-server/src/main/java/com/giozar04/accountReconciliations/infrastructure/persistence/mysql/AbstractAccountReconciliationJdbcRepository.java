package com.giozar04.accountReconciliations.infrastructure.persistence.mysql;

import java.util.List;
import java.util.Objects;

import com.giozar04.accountReconciliations.domain.entities.AccountReconciliation;
import com.giozar04.accountReconciliations.application.ports.output.AccountReconciliationRepository;
import com.giozar04.accountReconciliations.domain.policies.AccountReconciliationPolicy;
import com.giozar04.databases.infrastructure.persistence.mysql.DatabaseConnectionInterface;
import com.giozar04.logging.infrastructure.ConsoleLogger;

public abstract class AbstractAccountReconciliationJdbcRepository implements AccountReconciliationRepository {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final ConsoleLogger logger = ConsoleLogger.getInstance();

    protected AbstractAccountReconciliationJdbcRepository(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, "La conexión a base de datos no puede ser nula");
    }

    protected void validateId(long id) {
        AccountReconciliationPolicy.validateId(id);
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
