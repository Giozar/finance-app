package com.giozar04.accounts.domain.models;

import java.util.List;
import java.util.Objects;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.accounts.domain.interfaces.AccountRepositoryInterface;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.logging.CustomLogger;


public abstract class AccountRepositoryAbstract implements AccountRepositoryInterface {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final CustomLogger logger = CustomLogger.getInstance();

    protected AccountRepositoryAbstract(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, 
            "La conexión a la base de datos no puede ser nula");
    }

    protected void validateAccount(Account account) {
        Objects.requireNonNull(account, "La cuenta no puede ser nula");

        if (account.getName() == null || account.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre de la cuenta no puede estar vacío");
        }

        String type = account.getType();
        AccountTypes tipoCuenta;
        try {
            tipoCuenta = AccountTypes.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Tipo de cuenta inválido: " + type);
        }

        if (account.getCurrentBalance() < 0) {
            throw new IllegalArgumentException("El balance actual no puede ser negativo");
        }

        // Validaciones específicas por tipo
        if (tipoCuenta == AccountTypes.CREDIT) {
            if (account.getCreditLimit() == null || account.getCutoffDay() == null || account.getPaymentDay() == null) {
                throw new IllegalArgumentException("Las cuentas de crédito requieren límite, día de corte y día de pago.");
            }
        }

        if (tipoCuenta == AccountTypes.DEBIT || tipoCuenta == AccountTypes.CREDIT || tipoCuenta == AccountTypes.SAVINGS) {
            if (account.getBankClientId() == null) {
                throw new IllegalArgumentException("Las cuentas de tipo " + tipoCuenta.name().toLowerCase() + " deben estar ligadas a un cliente de banco.");
            }
            if (account.getClabe() == null || account.getClabe().isBlank()
                || account.getAccountNumber() == null || account.getAccountNumber().isBlank()) {
                throw new IllegalArgumentException("Las cuentas bancarias deben tener número de cuenta y CLABE.");
            }
        }

        if (tipoCuenta == AccountTypes.CASH) {
            if (account.getBankClientId() != null) {
                throw new IllegalArgumentException("Las cuentas de efectivo no deben estar ligadas a un cliente de banco.");
            }
        }
    }

    protected void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
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
}
