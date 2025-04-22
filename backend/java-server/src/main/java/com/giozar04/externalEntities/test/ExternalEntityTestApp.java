package com.giozar04.externalEntities.test;

import java.time.ZonedDateTime;
import java.util.Scanner;

import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.databases.infrastructure.repositories.DatabaseConnectionMySQL;
import com.giozar04.externalEntities.application.services.ExternalEntityService;
import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.externalEntities.domain.enums.ExternalEntityTypes;
import com.giozar04.externalEntities.infrastructure.repositories.ExternalEntityRepositoryMySQL;

public class ExternalEntityTestApp {

    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "finanzas";
    private static final String DB_USER = "giovanni";
    private static final String DB_PASSWORD = "finanzas123";

    public static void main(String[] args) {
        System.out.println("Iniciando prueba de entidades externas...");

        try {
            DatabaseConnectionInterface dbConnection = DatabaseConnectionMySQL.getInstance(
                DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
            );
            dbConnection.connect();

            ExternalEntityRepositoryMySQL repository = new ExternalEntityRepositoryMySQL(dbConnection);
            ExternalEntityService service = new ExternalEntityService(repository);

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n===== MENÚ DE ENTIDADES EXTERNAS =====");
                System.out.println("1. Crear entidad externa");
                System.out.println("2. Ver todas las entidades");
                System.out.println("3. Buscar entidad por ID");
                System.out.println("4. Actualizar entidad");
                System.out.println("5. Eliminar entidad");
                System.out.println("0. Salir");
                System.out.print("Seleccione una opción: ");
                int option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1 -> createEntity(service, scanner);
                    case 2 -> getAllEntities(service);
                    case 3 -> getEntityById(service, scanner);
                    case 4 -> updateEntity(service, scanner);
                    case 5 -> deleteEntity(service, scanner);
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

    private static void createEntity(ExternalEntityService service, Scanner scanner) {
        ExternalEntity entity = new ExternalEntity();

        System.out.print("Nombre de la entidad externa: ");
        entity.setName(scanner.nextLine());

        System.out.print("Tipo (person/service/store): ");
        String type = scanner.nextLine().trim().toLowerCase();
        while (!type.equals("person") && !type.equals("service") && !type.equals("store")) {
            System.out.print("Tipo inválido. Ingrese 'person', 'service' o 'store': ");
            type = scanner.nextLine().trim().toLowerCase();
        }
        entity.setType(ExternalEntityTypes.fromValue(type));

        System.out.print("Contacto (opcional): ");
        String contact = scanner.nextLine();
        entity.setContact(contact.isBlank() ? null : contact);

        entity.setCreatedAt(ZonedDateTime.now());
        entity.setUpdatedAt(ZonedDateTime.now());

        ExternalEntity created = service.createExternalEntity(entity);
        System.out.println("Entidad externa creada con ID: " + created.getId());
    }

    private static void updateEntity(ExternalEntityService service, Scanner scanner) {
        System.out.print("ID de la entidad a actualizar: ");
        long id = scanner.nextLong();
        scanner.nextLine();

        ExternalEntity entity = service.getExternalEntityById(id);

        System.out.print("Nuevo nombre (" + entity.getName() + "): ");
        String name = scanner.nextLine();
        if (!name.isBlank()) entity.setName(name);

        System.out.print("Nuevo tipo (person/service/store) [" + entity.getType().getValue() + "]: ");
        String type = scanner.nextLine().trim().toLowerCase();
        if (!type.isBlank()) entity.setType(ExternalEntityTypes.fromValue(type));

        System.out.print("Nuevo contacto (" + entity.getContact() + "): ");
        String contact = scanner.nextLine();
        if (!contact.isBlank()) entity.setContact(contact);

        entity.setUpdatedAt(ZonedDateTime.now());

        service.updateExternalEntityById(id, entity);
        System.out.println("Entidad externa actualizada.");
    }

    private static void deleteEntity(ExternalEntityService service, Scanner scanner) {
        System.out.print("ID de entidad a eliminar: ");
        long id = scanner.nextLong();
        scanner.nextLine();

        service.deleteExternalEntityById(id);
        System.out.println("Entidad externa eliminada.");
    }

    private static void getAllEntities(ExternalEntityService service) {
        var entities = service.getAllExternalEntities();
        if (entities.isEmpty()) {
            System.out.println("No hay entidades registradas.");
            return;
        }

        entities.forEach(ExternalEntityTestApp::printEntityDetails);
    }

    private static void getEntityById(ExternalEntityService service, Scanner scanner) {
        System.out.print("ID de entidad: ");
        long id = scanner.nextLong();
        scanner.nextLine();

        try {
            ExternalEntity entity = service.getExternalEntityById(id);
            printEntityDetails(entity);
        } catch (Exception e) {
            System.err.println("Error al buscar entidad externa: " + e.getMessage());
        }
    }

    private static void printEntityDetails(ExternalEntity entity) {
        System.out.println("ID: " + entity.getId());
        System.out.println("Nombre: " + entity.getName());
        System.out.println("Tipo: " + entity.getType().getLabel());
        System.out.println("Contacto: " + (entity.getContact() != null ? entity.getContact() : "(sin contacto)"));
        System.out.println("Creado: " + entity.getCreatedAt());
        System.out.println("Actualizado: " + entity.getUpdatedAt());
        System.out.println("----------------------------------------");
    }
}
