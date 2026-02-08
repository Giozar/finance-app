package com.giozar04.tags.test;

import java.time.ZonedDateTime;
import java.util.Scanner;

import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.databases.infrastructure.repositories.DatabaseConnectionMySQL;
import com.giozar04.tags.application.services.TagService;
import com.giozar04.tags.domain.entities.Tag;
import com.giozar04.tags.infrastructure.repositories.TagRepositoryMySQL;

public class TagTestApp {

    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "finanzas";
    private static final String DB_USER = "giovanni";
    private static final String DB_PASSWORD = "finanzas123";

    public static void main(String[] args) {
        System.out.println("Iniciando prueba de etiquetas...");

        try {
            DatabaseConnectionInterface dbConnection = DatabaseConnectionMySQL.getInstance(
                DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
            );
            dbConnection.connect();

            TagRepositoryMySQL repository = new TagRepositoryMySQL(dbConnection);
            TagService service = new TagService(repository);

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n===== MENÚ DE ETIQUETAS =====");
                System.out.println("1. Crear etiqueta");
                System.out.println("2. Ver todas las etiquetas");
                System.out.println("3. Buscar etiqueta por ID");
                System.out.println("4. Actualizar etiqueta");
                System.out.println("5. Eliminar etiqueta");
                System.out.println("0. Salir");
                System.out.print("Seleccione una opción: ");
                int option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1 -> createTag(service, scanner);
                    case 2 -> getAllTags(service);
                    case 3 -> getTagById(service, scanner);
                    case 4 -> updateTag(service, scanner);
                    case 5 -> deleteTag(service, scanner);
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

    private static void createTag(TagService service, Scanner scanner) {
        Tag tag = new Tag();
        
        System.out.print("ID del usuario dueño: ");
        tag.setUserId(scanner.nextLong());
        scanner.nextLine();

        System.out.print("Nombre de la etiqueta: ");
        tag.setName(scanner.nextLine());

        System.out.print("Color (nombre o código HEX): ");
        tag.setColor(scanner.nextLine());

        tag.setCreatedAt(ZonedDateTime.now());
        tag.setUpdatedAt(ZonedDateTime.now());

        Tag created = service.createTag(tag);
        System.out.println("Etiqueta creada con ID: " + created.getId());
    }

    private static void updateTag(TagService service, Scanner scanner) {
        System.out.print("ID de la etiqueta a actualizar: ");
        long id = scanner.nextLong();
        scanner.nextLine();

        Tag tag = service.getTagById(id);

        System.out.print("Nuevo ID de usuario (" + tag.getUserId() + "): ");
        String userIdStr = scanner.nextLine();
        if (!userIdStr.isBlank()) tag.setUserId(Long.parseLong(userIdStr));

        System.out.print("Nuevo nombre (" + tag.getName() + "): ");
        String name = scanner.nextLine();
        if (!name.isBlank()) tag.setName(name);

        System.out.print("Nuevo color (" + tag.getColor() + "): ");
        String color = scanner.nextLine();
        if (!color.isBlank()) tag.setColor(color);

        tag.setUpdatedAt(ZonedDateTime.now());

        service.updateTagById(id, tag);
        System.out.println("Etiqueta actualizada.");
    }

    private static void deleteTag(TagService service, Scanner scanner) {
        System.out.print("ID de etiqueta a eliminar: ");
        long id = scanner.nextLong();
        scanner.nextLine();

        service.deleteTagById(id);
        System.out.println("Etiqueta eliminada.");
    }

    private static void getAllTags(TagService service) {
        var tags = service.getAllTags();
        if (tags.isEmpty()) {
            System.out.println("No hay etiquetas registradas.");
            return;
        }

        tags.forEach(TagTestApp::printTagDetails);
    }

    private static void getTagById(TagService service, Scanner scanner) {
        System.out.print("ID de etiqueta: ");
        long id = scanner.nextLong();
        scanner.nextLine();

        try {
            Tag tag = service.getTagById(id);
            printTagDetails(tag);
        } catch (Exception e) {
            System.err.println("Error al buscar etiqueta: " + e.getMessage());
        }
    }

    private static void printTagDetails(Tag tag) {
        System.out.println("ID: " + tag.getId());
        System.out.println("Usuario: " + tag.getUserId());
        System.out.println("Nombre: " + tag.getName());
        System.out.println("Color: " + tag.getColor());
        System.out.println("Creado: " + tag.getCreatedAt());
        System.out.println("Actualizado: " + tag.getUpdatedAt());
        System.out.println("----------------------------------------");
    }
}
