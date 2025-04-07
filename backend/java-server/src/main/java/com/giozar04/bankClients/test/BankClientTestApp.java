package com.giozar04.bankClients.test;

import java.util.List;
import java.util.Scanner;

import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.bankClients.application.services.BankClientService;
import com.giozar04.bankClients.infrastructure.repositories.BankClientRepositoryMySQL;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.databases.infrastructure.repositories.DatabaseConnectionMySQL;

/**
 * Aplicación de consola para probar las operaciones CRUD de BankClient con MySQL.
 */
public class BankClientTestApp {

    // Configuración de la base de datos
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "finanzas";
    private static final String DB_USER = "giovanni";
    private static final String DB_PASSWORD = "finanzas123";

    public static void main(String[] args) {
        System.out.println("Iniciando prueba de clientes bancarios con MySQL...");

        try {
            DatabaseConnectionInterface dbConnection = DatabaseConnectionMySQL.getInstance(
                    DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD);
            dbConnection.connect();

            BankClientRepositoryMySQL repository = new BankClientRepositoryMySQL(dbConnection);
            BankClientService service = new BankClientService(repository);

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n===== MENÚ DE CLIENTES BANCARIOS =====");
                System.out.println("1. Crear cliente bancario");
                System.out.println("2. Obtener todos los clientes");
                System.out.println("3. Buscar cliente por ID");
                System.out.println("4. Actualizar cliente por ID");
                System.out.println("5. Eliminar cliente por ID");
                System.out.println("0. Salir");
                System.out.print("Seleccione una opción: ");

                int option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1 -> createBankClient(service, scanner);
                    case 2 -> listAllClients(service);
                    case 3 -> getClientById(service, scanner);
                    case 4 -> updateClient(service, scanner);
                    case 5 -> deleteClient(service, scanner);
                    case 0 -> exit = true;
                    default -> System.out.println("Opción no válida.");
                }
            }

            scanner.close();
            dbConnection.disconnect();
            System.out.println("Finalizado.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createBankClient(BankClientService service, Scanner scanner) {
        try {
            BankClient client = new BankClient();

            System.out.print("Nombre del banco: ");
            client.setBankName(scanner.nextLine());

            System.out.print("Número de cliente: ");
            client.setClientNumber(scanner.nextLine());

            System.out.print("ID de usuario (entero): ");
            client.setUserId(scanner.nextLong());
            scanner.nextLine();

            BankClient saved = service.createBankClient(client);
            System.out.println("Cliente creado con ID: " + saved.getId());
            printClient(saved);
        } catch (Exception e) {
            System.err.println("Error al crear cliente: " + e.getMessage());
        }
    }

    private static void listAllClients(BankClientService service) {
        try {
            List<BankClient> clients = service.getAllBankClients();
            if (clients.isEmpty()) {
                System.out.println("No hay clientes registrados.");
            } else {
                System.out.println("Clientes encontrados:");
                clients.forEach(BankClientTestApp::printClient);
            }
        } catch (Exception e) {
            System.err.println("Error al obtener clientes: " + e.getMessage());
        }
    }

    private static void getClientById(BankClientService service, Scanner scanner) {
        try {
            System.out.print("Ingrese el ID del cliente: ");
            long id = scanner.nextLong();
            scanner.nextLine();

            BankClient client = service.getBankClientById(id);
            printClient(client);
        } catch (Exception e) {
            System.err.println("Error al obtener cliente: " + e.getMessage());
        }
    }

    private static void updateClient(BankClientService service, Scanner scanner) {
        try {
            System.out.print("ID del cliente a actualizar: ");
            long id = scanner.nextLong();
            scanner.nextLine();

            BankClient client = service.getBankClientById(id);
            System.out.println("Datos actuales:");
            printClient(client);

            System.out.print("Nuevo nombre del banco [" + client.getBankName() + "]: ");
            String bankName = scanner.nextLine();
            if (!bankName.isBlank()) client.setBankName(bankName);

            System.out.print("Nuevo número de cliente [" + client.getClientNumber() + "]: ");
            String clientNumber = scanner.nextLine();
            if (!clientNumber.isBlank()) client.setClientNumber(clientNumber);

            BankClient updated = service.updateBankClientById(id, client);
            System.out.println("Cliente actualizado:");
            printClient(updated);
        } catch (Exception e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
        }
    }

    private static void deleteClient(BankClientService service, Scanner scanner) {
        try {
            System.out.print("ID del cliente a eliminar: ");
            long id = scanner.nextLong();
            scanner.nextLine();

            System.out.print("¿Confirmar eliminación? (s/n): ");
            String confirm = scanner.nextLine();

            if (confirm.equalsIgnoreCase("s")) {
                service.deleteBankClientById(id);
                System.out.println("Cliente eliminado.");
            } else {
                System.out.println("Eliminación cancelada.");
            }
        } catch (Exception e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
        }
    }

    private static void printClient(BankClient client) {
        System.out.println("-----------------------------------------");
        System.out.println("ID: " + client.getId());
        System.out.println("Banco: " + client.getBankName());
        System.out.println("Número Cliente: " + client.getClientNumber());
        System.out.println("ID Usuario: " + client.getUserId());
        System.out.println("Creado: " + client.getCreatedAt());
        System.out.println("Actualizado: " + client.getUpdatedAt());
    }
}
