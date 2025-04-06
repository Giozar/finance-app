package com.giozar04.bankClients.domain.models;

import java.util.List;
import java.util.Objects;

import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.bankClients.domain.interfaces.BankClientRepositoryInterface;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.shared.logging.CustomLogger;

public abstract class BankClientRepositoryAbstract implements BankClientRepositoryInterface {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final CustomLogger logger;

    protected BankClientRepositoryAbstract(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, "La conexión no puede ser nula");
        this.logger = new CustomLogger();
    }

    protected void validateBankClient(BankClient client) {
        Objects.requireNonNull(client, "El objeto BankClient no puede ser nulo");
        if (client.getBankName() == null || client.getBankName().isBlank()) {
            throw new IllegalArgumentException("El nombre del banco es obligatorio");
        }
        if (client.getClientNumber() == null || client.getClientNumber().isBlank()) {
            throw new IllegalArgumentException("El número de cliente es obligatorio");
        }
        if (client.getUserId() <= 0) {
            throw new IllegalArgumentException("El ID del usuario debe ser válido");
        }
    }

    protected void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
    }

    @Override public abstract BankClient createBankClient(BankClient bankClient);
    @Override public abstract BankClient getBankClientById(long id);
    @Override public abstract List<BankClient> getBankClientsByUserId(long userId);
    @Override public abstract BankClient updateBankClientById(long id, BankClient updated);
    @Override public abstract void deleteBankClientById(long id);
    @Override public abstract List<BankClient> getAllBankClients();
}
