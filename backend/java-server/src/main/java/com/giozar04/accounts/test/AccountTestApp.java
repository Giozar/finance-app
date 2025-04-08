package com.giozar04.accounts.test;

import java.time.ZonedDateTime;
import java.util.Scanner;

import com.giozar04.accounts.application.services.AccountService;
import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.infrastructure.repositories.AccountRepositoryMySQL;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.databases.infrastructure.repositories.DatabaseConnectionMySQL;

public class AccountTestApp {

    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "finanzas";
    private static final String DB_USER = "giovanni";
    private static final String DB_PASSWORD = "finanzas123";

    public static void main(String[] args) {
        System.out.println("Iniciando prueba de cuentas...");

        try {
            DatabaseConnectionInterface dbConnection = DatabaseConnectionMySQL.getInstance(
                DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
            );
            dbConnection.connect();

            AccountRepositoryMySQL repository = new AccountRepositoryMySQL(dbConnection);
            AccountService service = new AccountService(repository);

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n===== MENÚ DE PRUEBA DE CUENTAS =====");
                System.out.println("1. Crear cuenta");
                System.out.println("2. Ver todas las cuentas");
                System.out.println("3. Buscar cuenta por ID");
                System.out.println("4. Actualizar cuenta");
                System.out.println("5. Eliminar cuenta");
                System.out.println("0. Salir");
                System.out.print("Seleccione una opción: ");
                int option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1 -> createAccount(service, scanner);
                    case 2 -> getAllAccounts(service);
                    case 3 -> getAccountById(service, scanner);
                    case 4 -> updateAccount(service, scanner);
                    case 5 -> deleteAccount(service, scanner);
                    case 0 -> exit = true;
                    default -> System.out.println("Opción no válida.");
                }
            }

            dbConnection.disconnect();
            scanner.close();
            System.out.println("Prueba finalizada.");

        } catch (Exception e) {
            System.err.println("Error durante la prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createAccount(AccountService service, Scanner scanner) {
        Account account = new Account();

        System.out.print("ID del usuario dueño: ");
        account.setUserId(scanner.nextLong());
        scanner.nextLine();

        System.out.print("¿Desea ligar la cuenta a un cliente de banco? (s/n): ");
        String ligaABanco = scanner.nextLine().trim().toLowerCase();

        if (ligaABanco.equals("s")) {
            System.out.print("ID del cliente de banco: ");
            account.setBankClientId(scanner.nextLong());
            scanner.nextLine();

            System.out.print("Tipo de cuenta (debit, credit o savings): ");
            String tipo = scanner.nextLine().trim().toLowerCase();
            while (!tipo.equals("debit") && !tipo.equals("credit") && !tipo.equals("savings")) {
                System.out.print("Tipo inválido. Ingrese 'debit', 'credit' o 'savings': ");
                tipo = scanner.nextLine().trim().toLowerCase();
            }
            account.setType(tipo);

            System.out.print("Nombre de la cuenta: ");
            account.setName(scanner.nextLine());

            System.out.print("Número de cuenta: ");
            account.setAccountNumber(scanner.nextLine());

            System.out.print("CLABE: ");
            account.setClabe(scanner.nextLine());

            if (tipo.equals("credit")) {
                System.out.print("Límite de crédito: ");
                account.setCreditLimit(scanner.nextDouble());
                scanner.nextLine();

                System.out.print("Día de corte (1-31): ");
                account.setCutoffDay(scanner.nextInt());
                scanner.nextLine();

                System.out.print("Día de pago (1-31): ");
                account.setPaymentDay(scanner.nextInt());
                scanner.nextLine();
            }

        } else {
            account.setType("cash");
            account.setBankClientId(null);
            account.setAccountNumber(null);
            account.setClabe(null);
            account.setCreditLimit(null);
            account.setCutoffDay(null);
            account.setPaymentDay(null);

            System.out.print("Nombre de la cuenta: ");
            account.setName(scanner.nextLine());
        }

        System.out.print("Balance actual: ");
        account.setCurrentBalance(scanner.nextDouble());
        scanner.nextLine();

        account.setCreatedAt(ZonedDateTime.now());
        account.setUpdatedAt(ZonedDateTime.now());

        Account created = service.createAccount(account);
        System.out.println("Cuenta creada con ID: " + created.getId());
    }

    private static void getAllAccounts(AccountService service) {
        var accounts = service.getAllAccounts();
        if (accounts.isEmpty()) {
            System.out.println("No hay cuentas registradas.");
            return;
        }
        accounts.forEach(AccountTestApp::printAccountDetails);
    }

    private static void getAccountById(AccountService service, Scanner scanner) {
        System.out.print("ID de cuenta (long): ");
        long id = scanner.nextLong();
        scanner.nextLine();

        try {
            Account account = service.getAccountById(id);
            printAccountDetails(account);
        } catch (Exception e) {
            System.err.println("Error al buscar cuenta: " + e.getMessage());
        }
    }

    private static void updateAccount(AccountService service, Scanner scanner) {
        System.out.print("ID de cuenta a actualizar (long): ");
        long id = scanner.nextLong();
        scanner.nextLine();

        Account account = service.getAccountById(id);

        System.out.print("Nuevo nombre (" + account.getName() + "): ");
        String name = scanner.nextLine();
        if (!name.isBlank()) account.setName(name);

        System.out.print("Nuevo tipo (" + account.getType() + "): ");
        String type = scanner.nextLine();
        if (!type.isBlank()) account.setType(type);

        System.out.print("Nuevo balance (" + account.getCurrentBalance() + "): ");
        String balanceStr = scanner.nextLine();
        if (!balanceStr.isBlank()) account.setCurrentBalance(Double.parseDouble(balanceStr));

        account.setUpdatedAt(ZonedDateTime.now());

        service.updateAccountById(id, account);
        System.out.println("Cuenta actualizada.");
    }

    private static void deleteAccount(AccountService service, Scanner scanner) {
        System.out.print("ID de cuenta a eliminar (long): ");
        long id = scanner.nextLong();
        scanner.nextLine();

        service.deleteAccountById(id);
        System.out.println("Cuenta eliminada.");
    }

    private static void printAccountDetails(Account account) {
        System.out.println("ID: " + account.getId());
        System.out.println("User ID: " + account.getUserId());
        System.out.println("Tipo: " + account.getType());
        System.out.println("Nombre: " + account.getName());

        if (account.getBankClientId() != null) {
            System.out.println("ID Cliente de banco: " + account.getBankClientId());
            System.out.println("Número de cuenta: " + account.getAccountNumber());
            System.out.println("CLABE: " + account.getClabe());
        }

        if ("credit".equals(account.getType())) {
            System.out.println("Límite de crédito: " + account.getCreditLimit());
            System.out.println("Día de corte: " + account.getCutoffDay());
            System.out.println("Día de pago: " + account.getPaymentDay());
        }

        System.out.printf("Balance: $%.2f%n", account.getCurrentBalance());
        System.out.println("Fecha de creación: " + account.getCreatedAt());
        System.out.println("Última actualización: " + account.getUpdatedAt());
        System.out.println("----------------------------------------");
    }
}
