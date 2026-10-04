package com.giozar04.bankClients.infrastructure.persistence.mysql;

import java.util.List;
import java.util.Objects;

import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.bankClients.application.ports.output.BankClientRepository;
import com.giozar04.bankClients.domain.policies.BankClientPolicy;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.logging.infrastructure.ConsoleLogger;

public abstract class AbstractBankClientJdbcRepository implements BankClientRepository {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final ConsoleLogger logger = ConsoleLogger.getInstance();

    protected AbstractBankClientJdbcRepository(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, "La conexión no puede ser nula");
    }

    protected void validateBankClient(BankClient client) {
        BankClientPolicy.validateBankClient(client);
    }

    protected void validateId(long id) {
        BankClientPolicy.validateId(id);
    }

    @Override public abstract BankClient createBankClient(BankClient bankClient);
    @Override public abstract BankClient getBankClientById(long id);
    @Override public abstract List<BankClient> getBankClientsByUserId(long userId);
    @Override public abstract BankClient updateBankClientById(long id, BankClient updated);
    @Override public abstract void deleteBankClientById(long id);
    @Override public abstract List<BankClient> getAllBankClients();
}
