package com.giozar04.accounts.infrastructure.persistence.mysql;

import java.util.List;
import java.util.Objects;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.accounts.application.ports.output.AccountRepository;
import com.giozar04.accounts.domain.policies.AccountPolicy;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.logging.infrastructure.ConsoleLogger;


public abstract class AbstractAccountJdbcRepository implements AccountRepository {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final ConsoleLogger logger = ConsoleLogger.getInstance();

    protected AbstractAccountJdbcRepository(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection,
            "La conexión a la base de datos no puede ser nula");
    }

    protected void validateAccount(Account account) {
        AccountPolicy.validateAccount(account);
    }

    /**
     * Solo al crear: la deuda inicial de un crédito (credit_used) debe estar entre 0 y el límite.
     * Igual que los CHECK chk_credit_used y chk_credit_used_limit. Al editar no aplica:
     * credit_used lo mueven los triggers de transacciones.
     */
    protected void validateInitialCreditUsed(Account account) {
        AccountPolicy.validateInitialCreditUsed(account);
    }

    protected void validateId(long id) {
        AccountPolicy.validateId(id);
    }

    @Override
    public abstract Account createAccount(Account account);

    @Override
    public abstract Account getAccountById(long id);

    @Override
    public abstract Account updateAccountById(long id, Account account);

    @Override
    public abstract void deleteAccountById(long id);

    @Override
    public abstract List<Account> getAllAccounts();

    @Override
    public abstract List<Account> getAccountsByUserId(long userId);
}
