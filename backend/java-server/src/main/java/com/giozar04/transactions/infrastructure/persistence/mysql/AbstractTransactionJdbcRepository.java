package com.giozar04.transactions.infrastructure.persistence.mysql;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import com.giozar04.databases.infrastructure.persistence.mysql.DatabaseConnectionInterface;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.domain.enums.PaymentMethod;
import com.giozar04.transactions.application.ports.output.TransactionRepository;
import com.giozar04.transactions.domain.policies.TransactionPolicy;

/**
 * Validación estructural mínima para que el repositorio pueda persistir sin errores de nulos.
 * Las reglas de negocio completas viven en TransactionValidator (capa application) y se
 * ejecutan en TransactionUseCase antes de llegar aquí.
 */
public abstract class AbstractTransactionJdbcRepository implements TransactionRepository {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final ConsoleLogger logger = ConsoleLogger.getInstance();

    protected AbstractTransactionJdbcRepository(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, "La conexión a la base de datos no puede ser nula");
    }

    protected void validateTransaction(Transaction tx) {
        TransactionPolicy.validateTransaction(tx);
    }

    protected void validateId(long id) {
        TransactionPolicy.validateId(id);
    }

    @Override
    public abstract Transaction createTransaction(Transaction tx);

    @Override
    public abstract Transaction getTransactionById(long id);

    @Override
    public abstract Transaction updateTransactionById(long id, Transaction tx);

    @Override
    public abstract void deleteTransactionById(long id);

    @Override
    public abstract List<Transaction> getAllTransactions();

    @Override
    public abstract List<Transaction> getTransactionsByUserId(long userId);
}
