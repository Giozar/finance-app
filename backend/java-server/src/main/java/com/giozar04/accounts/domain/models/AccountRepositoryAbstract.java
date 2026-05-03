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

        if(account.getUserId() <= 0){
            throw new IllegalArgumentException("El ID del usuario debe ser mayor a cero");
        }

        if (account.getName() == null || account.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre de la cuenta no puede estar vacío");
        }

        AccountTypes tipoCuenta = account.getType();
        if (tipoCuenta == null) {
            throw new IllegalArgumentException("El tipo de cuenta no puede ser nulo");
        }

        if (account.getCurrentBalance() < 0) {
            throw new IllegalArgumentException("El balance actual no puede ser negativo");
        }

        // Tipos que usan bank_details y requieren CLABE + número de cuenta
        boolean usesBankDetails = tipoCuenta == AccountTypes.DEBIT
                || tipoCuenta == AccountTypes.CREDIT
                || tipoCuenta == AccountTypes.WALLET
                || tipoCuenta == AccountTypes.BENEFIT;

        if (usesBankDetails) {
            if (account.getClabe() == null || account.getClabe().isBlank()
                || account.getAccountNumber() == null || account.getAccountNumber().isBlank()) {
                throw new IllegalArgumentException("Las cuentas bancarias deben tener número de cuenta y CLABE.");
            }
        }

        // DEBIT y BENEFIT usan bank_client_id pero no es mandatorio si viene null
        // CREDIT requiere bank_client_id obligatoriamente
        if (tipoCuenta == AccountTypes.CREDIT) {
            if (account.getBankClientId() == null) {
                throw new IllegalArgumentException("Las cuentas de crédito deben estar ligadas a un cliente de banco.");
            }
            if (account.getCreditLimit() == null || account.getCutoffDay() == null || account.getPaymentDay() == null) {
                throw new IllegalArgumentException("Las cuentas de crédito requieren límite, día de corte y día de pago.");
            }
        }

        // SAVINGS requiere tasa de rendimiento
        if (tipoCuenta == AccountTypes.SAVINGS) {
            if (account.getAnnualYield() == null) {
                throw new IllegalArgumentException("Las cuentas de ahorro requieren una tasa de rendimiento anual.");
            }
            if (account.getAnnualYield() < 0 || account.getAnnualYield() > 1) {
                throw new IllegalArgumentException("La tasa de rendimiento debe estar entre 0 y 1 (ej. 0.15 = 15%).");
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
