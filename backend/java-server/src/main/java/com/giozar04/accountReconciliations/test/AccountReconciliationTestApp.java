package com.giozar04.accountReconciliations.test;

import java.util.List;
import java.util.Scanner;

import com.giozar04.accountReconciliations.application.services.AccountReconciliationService;
import com.giozar04.accountReconciliations.domain.entities.AccountReconciliation;
import com.giozar04.accountReconciliations.infrastructure.repositories.AccountReconciliationRepositoryMySQL;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.databases.infrastructure.repositories.DatabaseConnectionMySQL;

public class AccountReconciliationTestApp {

    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "finanzas";
    private static final String DB_USER = "giovanni";
    private static final String DB_PASSWORD = "finanzas123";

    public static void main(String[] args) {
        System.out.println("Iniciando prueba de reconciliación de cuentas...");

        try {
            DatabaseConnectionInterface dbConnection = DatabaseConnectionMySQL.getInstance(
                DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
            );
            dbConnection.connect();

            AccountReconciliationRepositoryMySQL repository = new AccountReconciliationRepositoryMySQL(dbConnection);
            AccountReconciliationService service = new AccountReconciliationService(repository);

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n===== MENÚ DE RECONCILIACIÓN DE CUENTAS =====");
                System.out.println("1. Ver todas las reconciliaciones");
                System.out.println("2. Ver reconciliaciones por usuario");
                System.out.println("3. Ver reconciliación de una cuenta");
                System.out.println("4. Reconciliar una cuenta");
                System.out.println("0. Salir");
                System.out.print("Seleccione una opción: ");
                int option = scanner.nextInt();
                scanner.nextLine();

                try {
                    switch (option) {
                        case 1 -> printList(service.getAllAccountReconciliations());
                        case 2 -> getByUser(service, scanner);
                        case 3 -> getByAccount(service, scanner);
                        case 4 -> reconcile(service, scanner);
                        case 0 -> exit = true;
                        default -> System.out.println("Opción no válida.");
                    }
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
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

    private static void getByUser(AccountReconciliationService service, Scanner scanner) {
        System.out.print("ID del usuario: ");
        long userId = scanner.nextLong();
        scanner.nextLine();

        printList(service.getAccountReconciliationsByUserId(userId));
    }

    private static void getByAccount(AccountReconciliationService service, Scanner scanner) {
        System.out.print("ID de la cuenta: ");
        long accountId = scanner.nextLong();
        scanner.nextLine();

        printDetails(service.getAccountReconciliationByAccountId(accountId));
    }

    private static void reconcile(AccountReconciliationService service, Scanner scanner) {
        System.out.print("ID de la cuenta a reconciliar: ");
        long accountId = scanner.nextLong();
        scanner.nextLine();

        System.out.println("Antes:");
        printDetails(service.getAccountReconciliationByAccountId(accountId));

        AccountReconciliation reconciled = service.reconcileAccount(accountId);
        System.out.println("Después:");
        printDetails(reconciled);
    }

    private static void printList(List<AccountReconciliation> reconciliations) {
        if (reconciliations.isEmpty()) {
            System.out.println("No hay cuentas registradas.");
            return;
        }

        reconciliations.forEach(AccountReconciliationTestApp::printDetails);
    }

    private static void printDetails(AccountReconciliation r) {
        System.out.println("Cuenta: " + r.getAccountId() + " - " + r.getAccountName() + " (" + r.getAccountType() + ")");
        System.out.println("Usuario: " + r.getUserId());
        System.out.println("Apertura neta: " + r.getOpeningNet());
        System.out.println("Entradas: " + r.getTotalInflows());
        System.out.println("Salidas: " + r.getTotalOutflows());
        System.out.println("Esperado: " + r.getExpectedNet());
        System.out.println("Actual: " + r.getActualNet());
        System.out.println("Diferencia: " + r.getDifference() + (r.isBalanced() ? " (cuadrada)" : " (DESCUADRADA)"));
        System.out.println("----------------------------------------");
    }
}
