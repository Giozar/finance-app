package com.giozar04.bankClients.test;

import java.util.List;
import java.util.Scanner;

import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.bankClients.infrastructure.services.BankClientService;
import com.giozar04.configs.ServerConnectionConfig;
import com.giozar04.serverConnection.application.services.ServerConnectionService;

public class BankClientFunctionalTest {

    public static void main(String[] args) {
        System.out.println("=== PRUEBA FUNCIONAL DE BANK_CLIENT (CLIENTE) ===");

        try {
            ServerConnectionConfig config = new ServerConnectionConfig();
            ServerConnectionService connectionService = ServerConnectionService.getInstance(config.getHost(), config.getPort());
            connectionService.connect();
            System.out.println("✅ Conectado al servidor en " + config.getHost() + ":" + config.getPort());

            BankClientService service = BankClientService.connectService(connectionService);
            System.out.println("✅ Servicio de clientes bancarios inicializado.");

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n--- MENÚ DE PRUEBA FUNCIONAL (BANK_CLIENT) ---");
                System.out.println("1. Crear Cliente Bancario");
                System.out.println("2. Consultar Todos");
                System.out.println("3. Consultar por ID");
                System.out.println("4. Consultar por ID de Usuario");
                System.out.println("5. Actualizar Cliente");
                System.out.println("6. Eliminar Cliente");
                System.out.println("0. Salir");
                System.out.print("Seleccione: ");

                int option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1 -> createBankClient(service, scanner);
                    case 2 -> getAllBankClients(service);
                    case 3 -> getBankClientById(service, scanner);
                    case 4 -> getBankClientsByUserId(service, scanner);
                    case 5 -> updateBankClient(service, scanner);
                    case 6 -> deleteBankClient(service, scanner);
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

    private static void createBankClient(BankClientService service, Scanner scanner) throws Exception {
        System.out.println("\n[Crear Cliente Bancario]");
        BankClient client = new BankClient();
        System.out.print("ID de Usuario dueño: ");
        client.setUserId(scanner.nextLong());
        scanner.nextLine();
        System.out.print("Nombre del Banco: ");
        client.setBankName(scanner.nextLine());
        System.out.print("Número de Cliente: ");
        client.setClientNumber(scanner.nextLine());

        BankClient created = service.createBankClient(client);
        System.out.println("✅ Cliente creado con ID: " + created.getId());
    }

    private static void getAllBankClients(BankClientService service) throws Exception {
        System.out.println("\n[Consultar Todos]");
        List<BankClient> clients = service.getAllBankClients();
        if (clients.isEmpty()) {
            System.out.println("No hay clientes registrados.");
        } else {
            clients.forEach(c -> System.out.println("- ID: " + c.getId() + " | Usuario: " + c.getUserId() + " | Banco: " + c.getBankName() + " | No: " + c.getClientNumber()));
        }
    }

    private static void getBankClientById(BankClientService service, Scanner scanner) throws Exception {
        System.out.print("\nID de cliente: ");
        long id = scanner.nextLong();
        scanner.nextLine();
        BankClient client = service.getBankClientById(id);
        System.out.println("✅ Detalle: " + client.getBankName() + " (" + client.getClientNumber() + ") - Usuario: " + client.getUserId());
    }

    private static void getBankClientsByUserId(BankClientService service, Scanner scanner) throws Exception {
        System.out.print("\nID de usuario: ");
        long userId = scanner.nextLong();
        scanner.nextLine();
        List<BankClient> clients = service.getAllBankClients(); // Could use filtering or specific service method if available
        // Note: service.getBankClientsByUserId exists in backend service but client service might not have it yet?
        // Let's check client service again. Yes, it doesn't have it in the file I read!
        // Wait, let's re-read client BankClientService.java.
        System.out.println("Consultando clientes para el usuario...");
        List<BankClient> userClients = service.getAllBankClients().stream()
                .filter(c -> c.getUserId() == userId)
                .toList();
        
        if (userClients.isEmpty()) {
            System.out.println("El usuario no tiene clientes bancarios.");
        } else {
            userClients.forEach(c -> System.out.println("- ID: " + c.getId() + " | Banco: " + c.getBankName() + " | No: " + c.getClientNumber()));
        }
    }

    private static void updateBankClient(BankClientService service, Scanner scanner) throws Exception {
        System.out.print("\nID de cliente a actualizar: ");
        long id = scanner.nextLong();
        scanner.nextLine();
        BankClient client = service.getBankClientById(id);

        System.out.print("Nuevo Nombre de Banco (" + client.getBankName() + "): ");
        String bankName = scanner.nextLine();
        if (!bankName.isBlank()) client.setBankName(bankName);

        System.out.print("Nuevo Número de Cliente (" + client.getClientNumber() + "): ");
        String clientNumber = scanner.nextLine();
        if (!clientNumber.isBlank()) client.setClientNumber(clientNumber);

        BankClient updated = service.updateBankClientById(id, client);
        System.out.println("✅ Cliente actualizado.");
    }

    private static void deleteBankClient(BankClientService service, Scanner scanner) throws Exception {
        System.out.print("\nID de cliente a eliminar: ");
        long id = scanner.nextLong();
        scanner.nextLine();
        service.deleteBankClientById(id);
        System.out.println("✅ Cliente eliminado.");
    }
}
