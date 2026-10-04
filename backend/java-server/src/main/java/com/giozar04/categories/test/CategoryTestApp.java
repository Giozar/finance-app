package com.giozar04.categories.test;

import java.time.ZonedDateTime;
import java.util.Scanner;

import com.giozar04.categories.application.usecases.CategoryUseCase;
import com.giozar04.categories.domain.entities.Category;
import com.giozar04.categories.domain.enums.CategoryTypes;
import com.giozar04.categories.infrastructure.persistence.mysql.CategoryRepositoryMySQL;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.databases.infrastructure.repositories.DatabaseConnectionMySQL;

public class CategoryTestApp {

    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "finanzas";
    private static final String DB_USER = "giovanni";
    private static final String DB_PASSWORD = "finanzas123";

    public static void main(String[] args) {
        System.out.println("Iniciando prueba de categorías...");

        try {
            DatabaseConnectionInterface dbConnection = DatabaseConnectionMySQL.getInstance(
                DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
            );
            dbConnection.connect();

            CategoryRepositoryMySQL repository = new CategoryRepositoryMySQL(dbConnection);
            CategoryUseCase service = new CategoryUseCase(repository);

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n===== MENÚ DE CATEGORÍAS =====");
                System.out.println("1. Crear categoría");
                System.out.println("2. Ver todas las categorías");
                System.out.println("3. Buscar categoría por ID");
                System.out.println("4. Actualizar categoría");
                System.out.println("5. Eliminar categoría");
                System.out.println("0. Salir");
                System.out.print("Seleccione una opción: ");
                int option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1 -> createCategory(service, scanner);
                    case 2 -> getAllCategories(service);
                    case 3 -> getCategoryById(service, scanner);
                    case 4 -> updateCategory(service, scanner);
                    case 5 -> deleteCategory(service, scanner);
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

    private static void createCategory(CategoryUseCase service, Scanner scanner) {
        Category category = new Category();

        System.out.print("ID del usuario dueño: ");
        category.setUserId(scanner.nextLong());
        scanner.nextLine();

        System.out.print("Nombre de la categoría: ");
        category.setName(scanner.nextLine());

        System.out.print("Tipo (INCOME/EXPENSE/BOTH): ");
        String type = scanner.nextLine().trim().toUpperCase();
        while (!type.equals("INCOME") && !type.equals("EXPENSE") && !type.equals("BOTH")) {
            System.out.print("Tipo inválido. Ingrese 'INCOME', 'EXPENSE' o 'BOTH': ");
            type = scanner.nextLine().trim().toUpperCase();
        }
        category.setType(CategoryTypes.fromValue(type));

        System.out.print("Ícono: ");
        category.setIcon(scanner.nextLine());

        category.setCreatedAt(ZonedDateTime.now());
        category.setUpdatedAt(ZonedDateTime.now());

        Category created = service.createCategory(category);
        System.out.println("Categoría creada con ID: " + created.getId());
    }

    private static void updateCategory(CategoryUseCase service, Scanner scanner) {
        System.out.print("ID de la categoría a actualizar: ");
        long id = scanner.nextLong();
        scanner.nextLine();

        Category category = service.getCategoryById(id);

        System.out.print("Nuevo ID de usuario (" + category.getUserId() + "): ");
        String userIdStr = scanner.nextLine();
        if (!userIdStr.isBlank()) category.setUserId(Long.parseLong(userIdStr));

        System.out.print("Nuevo nombre (" + category.getName() + "): ");
        String name = scanner.nextLine();
        if (!name.isBlank()) category.setName(name);

        System.out.print("Nuevo tipo (INCOME/EXPENSE/BOTH) [" + category.getType().getValue() + "]: ");
        String type = scanner.nextLine().trim().toUpperCase();
        if (!type.isBlank()) {
            while (!type.equals("INCOME") && !type.equals("EXPENSE") && !type.equals("BOTH")) {
                System.out.print("Tipo inválido. Ingrese 'INCOME', 'EXPENSE' o 'BOTH': ");
                type = scanner.nextLine().trim().toUpperCase();
            }
            category.setType(CategoryTypes.fromValue(type));
        }

        System.out.print("Nuevo ícono (" + category.getIcon() + "): ");
        String icon = scanner.nextLine();
        if (!icon.isBlank()) category.setIcon(icon);

        category.setUpdatedAt(ZonedDateTime.now());

        service.updateCategoryById(id, category);
        System.out.println("Categoría actualizada.");
    }

    private static void deleteCategory(CategoryUseCase service, Scanner scanner) {
        System.out.print("ID de categoría a eliminar: ");
        long id = scanner.nextLong();
        scanner.nextLine();

        service.deleteCategoryById(id);
        System.out.println("Categoría eliminada.");
    }

    private static void getAllCategories(CategoryUseCase service) {
        var categories = service.getAllCategories();
        if (categories.isEmpty()) {
            System.out.println("No hay categorías registradas.");
            return;
        }

        categories.forEach(CategoryTestApp::printCategoryDetails);
    }

    private static void getCategoryById(CategoryUseCase service, Scanner scanner) {
        System.out.print("ID de categoría: ");
        long id = scanner.nextLong();
        scanner.nextLine();

        try {
            Category category = service.getCategoryById(id);
            printCategoryDetails(category);
        } catch (Exception e) {
            System.err.println("Error al buscar categoría: " + e.getMessage());
        }
    }

    private static void printCategoryDetails(Category category) {
        System.out.println("ID: " + category.getId());
        System.out.println("Usuario: " + category.getUserId());
        System.out.println("Nombre: " + category.getName());
        System.out.println("Tipo: " + category.getType().getLabel());
        System.out.println("Ícono: " + category.getIcon());
        System.out.println("Creado: " + category.getCreatedAt());
        System.out.println("Actualizado: " + category.getUpdatedAt());
        System.out.println("----------------------------------------");
    }
}
