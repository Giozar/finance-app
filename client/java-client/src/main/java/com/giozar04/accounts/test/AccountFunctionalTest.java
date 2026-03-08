package com.giozar04.accounts.test;

import java.util.List;
import java.util.Scanner;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.accounts.infrastructure.services.AccountService;
import com.giozar04.configs.ServerConnectionConfig;
import com.giozar04.serverConnection.application.services.ServerConnectionService;

public class AccountFunctionalTest {

    public static void main(String[] args) {
        System.out.println("=== PRUEBA FUNCIONAL: CUENTAS BASE (Paso 1) ===");

        try {
            ServerConnectionConfig config = new ServerConnectionConfig();
            ServerConnectionService connectionService = ServerConnectionService.getInstance(config.getHost(), config.getPort());
            connectionService.connect();
            System.out.println("✅ Conectado al servidor en " + config.getHost() + ":" + config.getPort());

            AccountService service = AccountService.connectService(connectionService);
            System.out.println("✅ Servicio de cuentas inicializado.");

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n--- MENÚ: CUENTAS BASE (CASH, WALLET, BENEFIT) ---");
                System.out.println("1. Crear Cuenta Base");
                System.out.println("2. Consultar Todas");
                System.out.println("3. Consultar por ID");
                System.out.println("4. Actualizar Cuenta Base");
                System.out.println("5. Eliminar Cuenta Base");
                System.out.println("0. Salir");
                System.out.print("Seleccione: ");

                int option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1 -> createBaseAccount(service, scanner);
                    case 2 -> getAllAccounts(service);
                    case 3 -> getAccountById(service, scanner);
                    case 4 -> updateBaseAccount(service, scanner);
                    case 5 -> deleteAccount(service, scanner);
                    case 0 -> exit = true;
                    default -> System.out.println("Opción inválida.");
                }
            }

            connectionService.disconnect();
            System.out.println("👋 Prueba finalizada.");

        } catch (Exception e) {
            System.err.println("❌ Error en la prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createBaseAccount(AccountService service, Scanner scanner) throws Exception {
        System.out.println("\n[Crear Cuenta Base]");
        Account account = new Account();
        
        System.out.print("ID de Usuario dueño: ");
        account.setUserId(scanner.nextLong());
        scanner.nextLine();
        
        System.out.print("Nombre de la cuenta: ");
        account.setName(scanner.nextLine());
        
        System.out.print("Tipo (CASH, WALLET, BENEFIT): ");
        String tipoStr = scanner.nextLine().trim().toUpperCase();
        try {
            account.setType(AccountTypes.fromValue(tipoStr));
        } catch (Exception e) {
            System.out.println("⚠️ Tipo no reconocido. Asignando CASH por defecto.");
            account.setType(AccountTypes.CASH);
        }
        
        System.out.print("Balance Actual: ");
        account.setCurrentBalance(scanner.nextDouble());
        scanner.nextLine();

        // Para cuentas base (Step 1), la entidad en la base de datos no usa las propiedades extendidas.
        // Al enviar esto sin bankClientId al servidor, el AccountRepositoryMySQL insertará nulos en los campos
        // bancarios y de crédito en la tabla `accounts`.
        
        Account created = service.createAccount(account);
        System.out.println("✅ Cuenta base creada con ID: " + created.getId());
    }

    private static void getAllAccounts(AccountService service) throws Exception {
        System.out.println("\n[Consultar Todas]");
        List<Account> accounts = service.getAllAccounts();
        if (accounts.isEmpty()) {
            System.out.println("No hay cuentas registradas.");
        } else {
            accounts.forEach(c -> System.out.println("- ID: " + c.getId() 
                + " | Usuario: " + c.getUserId() 
                + " | Nombre: " + c.getName() 
                + " | Tipo: " + c.getType() 
                + " | Balance: $" + c.getCurrentBalance()));
        }
    }

    private static void getAccountById(AccountService service, Scanner scanner) throws Exception {
        System.out.print("\nID de cuenta: ");
        long id = scanner.nextLong();
        scanner.nextLine();
        Account account = service.getAccountById(id);
        System.out.println("✅ Detalle: " + account.getName() 
            + " | Tipo: " + account.getType() 
            + " | Balance: $" + account.getCurrentBalance()
            + " | Creado: " + account.getCreatedAt());
    }

    private static void updateBaseAccount(AccountService service, Scanner scanner) throws Exception {
        System.out.print("\nID de cuenta a actualizar: ");
        long id = scanner.nextLong();
        scanner.nextLine();
        Account account = service.getAccountById(id);

        System.out.print("Nuevo Nombre (" + account.getName() + "): ");
        String name = scanner.nextLine();
        if (!name.isBlank()) account.setName(name);

        System.out.print("Nuevo Tipo (" + account.getType() + ") - CASH/WALLET/BENEFIT: ");
        String typeStr = scanner.nextLine().trim().toUpperCase();
        if (!typeStr.isBlank()) {
            try {
                account.setType(AccountTypes.fromValue(typeStr));
            } catch (Exception e) {
                System.out.println("⚠️ Tipo no válido. Omitiendo...");
            }
        }

        System.out.print("Nuevo Balance (" + account.getCurrentBalance() + "): ");
        String balanceStr = scanner.nextLine();
        if (!balanceStr.isBlank()) {
            account.setCurrentBalance(Double.parseDouble(balanceStr));
        }

        service.updateAccountById(id, account);
        System.out.println("✅ Cuenta base actualizada.");
    }

    private static void deleteAccount(AccountService service, Scanner scanner) throws Exception {
        System.out.print("\nID de cuenta a eliminar: ");
        long id = scanner.nextLong();
        scanner.nextLine();
        service.deleteAccountById(id);
        System.out.println("✅ Cuenta eliminada.");
    }
}
