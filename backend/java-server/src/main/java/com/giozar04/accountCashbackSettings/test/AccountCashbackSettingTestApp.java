package com.giozar04.accountCashbackSettings.test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Scanner;

import com.giozar04.accountCashbackSettings.application.services.AccountCashbackSettingService;
import com.giozar04.accountCashbackSettings.domain.entities.AccountCashbackSetting;
import com.giozar04.accountCashbackSettings.infrastructure.repositories.AccountCashbackSettingRepositoryMySQL;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.databases.infrastructure.repositories.DatabaseConnectionMySQL;

public class AccountCashbackSettingTestApp {

    private static final String DB_HOST     = "localhost";
    private static final String DB_PORT     = "3306";
    private static final String DB_NAME     = "finanzas";
    private static final String DB_USER     = "giovanni";
    private static final String DB_PASSWORD = "finanzas123";

    public static void main(String[] args) {
        System.out.println("Iniciando prueba de configuraciones de cashback...");

        try {
            DatabaseConnectionInterface dbConnection = DatabaseConnectionMySQL.getInstance(
                DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
            );
            dbConnection.connect();

            AccountCashbackSettingRepositoryMySQL repository =
                new AccountCashbackSettingRepositoryMySQL(dbConnection);
            AccountCashbackSettingService service =
                new AccountCashbackSettingService(repository);

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n===== MENÚ DE CONFIGURACIONES DE CASHBACK =====");
                System.out.println("1. Crear configuración de cashback");
                System.out.println("2. Consultar configuración por accountId");
                System.out.println("3. Actualizar configuración de cashback");
                System.out.println("4. Eliminar configuración de cashback");
                System.out.println("5. Listar todas las configuraciones");
                System.out.println("0. Salir");
                System.out.print("Seleccione una opción: ");
                int option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1 -> createSetting(service, scanner);
                    case 2 -> getSetting(service, scanner);
                    case 3 -> updateSetting(service, scanner);
                    case 4 -> deleteSetting(service, scanner);
                    case 5 -> getAllSettings(service);
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

    private static void createSetting(AccountCashbackSettingService service, Scanner scanner) {
        AccountCashbackSetting setting = new AccountCashbackSetting();

        System.out.print("Account ID de la cuenta (DEBIT, CREDIT o WALLET): ");
        setting.setAccountId(scanner.nextLong());
        scanner.nextLine();

        System.out.print("¿Cashback habilitado? (true/false): ");
        setting.setCashbackEnabled(Boolean.parseBoolean(scanner.nextLine().trim()));

        System.out.print("Tasa de cashback como fracción (ej. 0.02 para 2%, dejar vacío para null): ");
        String rateInput = scanner.nextLine().trim();
        if (!rateInput.isEmpty()) {
            setting.setDefaultCashbackRate(new BigDecimal(rateInput));
        }

        setting.setCreatedAt(ZonedDateTime.now());
        setting.setUpdatedAt(ZonedDateTime.now());

        AccountCashbackSetting created = service.createAccountCashbackSetting(setting);
        System.out.println("Configuración creada para accountId: " + created.getAccountId());
        printSetting(created);
    }

    private static void getSetting(AccountCashbackSettingService service, Scanner scanner) {
        System.out.print("Account ID: ");
        long accountId = scanner.nextLong();
        scanner.nextLine();

        AccountCashbackSetting setting = service.getAccountCashbackSettingByAccountId(accountId);
        printSetting(setting);
    }

    private static void updateSetting(AccountCashbackSettingService service, Scanner scanner) {
        System.out.print("Account ID a actualizar: ");
        long accountId = scanner.nextLong();
        scanner.nextLine();

        AccountCashbackSetting setting = new AccountCashbackSetting();
        setting.setAccountId(accountId);

        System.out.print("¿Cashback habilitado? (true/false): ");
        setting.setCashbackEnabled(Boolean.parseBoolean(scanner.nextLine().trim()));

        System.out.print("Nueva tasa de cashback como fracción (ej. 0.05 para 5%, dejar vacío para null): ");
        String rateInput = scanner.nextLine().trim();
        if (!rateInput.isEmpty()) {
            setting.setDefaultCashbackRate(new BigDecimal(rateInput));
        }

        AccountCashbackSetting updated = service.updateAccountCashbackSettingByAccountId(accountId, setting);
        System.out.println("Configuración actualizada para accountId: " + updated.getAccountId());
        printSetting(updated);
    }

    private static void deleteSetting(AccountCashbackSettingService service, Scanner scanner) {
        System.out.print("Account ID a eliminar: ");
        long accountId = scanner.nextLong();
        scanner.nextLine();

        service.deleteAccountCashbackSettingByAccountId(accountId);
        System.out.println("Configuración de cashback eliminada para accountId: " + accountId);
    }

    private static void getAllSettings(AccountCashbackSettingService service) {
        List<AccountCashbackSetting> settings = service.getAllAccountCashbackSettings();
        if (settings.isEmpty()) {
            System.out.println("No hay configuraciones de cashback registradas.");
            return;
        }
        settings.forEach(AccountCashbackSettingTestApp::printSetting);
    }

    private static void printSetting(AccountCashbackSetting setting) {
        System.out.println("Account ID:         " + setting.getAccountId());
        System.out.println("Cashback Enabled:   " + setting.isCashbackEnabled());
        System.out.println("Default Rate:       " + (setting.getDefaultCashbackRate() != null
            ? setting.getDefaultCashbackRate().toPlainString() + " (" +
              setting.getDefaultCashbackRate().multiply(BigDecimal.valueOf(100)).toPlainString() + "%)"
            : "null"));
        System.out.println("Creado:             " + setting.getCreatedAt());
        System.out.println("Actualizado:        " + setting.getUpdatedAt());
        System.out.println("----------------------------------------");
    }
}
