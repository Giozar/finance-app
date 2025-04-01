package com.giozar04.users.test;

import java.time.ZonedDateTime;
import java.util.Scanner;

import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.databases.infrastructure.repositories.DatabaseConnectionMySQL;
import com.giozar04.users.application.services.UserService;
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.infrastructure.repositories.UserRepositoryMySQL;

public class TestUserApp {

    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "finanzas";
    private static final String DB_USER = "giovanni";
    private static final String DB_PASSWORD = "finanzas123";

    public static void main(String[] args) {
        System.out.println("Iniciando prueba de usuarios...");

        try {
            DatabaseConnectionInterface dbConnection = DatabaseConnectionMySQL.getInstance(
                    DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
            );
            dbConnection.connect();

            UserRepositoryMySQL userRepository = new UserRepositoryMySQL(dbConnection);
            UserService userService = new UserService(userRepository);

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n===== MENÚ DE PRUEBA DE USUARIOS =====");
                System.out.println("1. Crear usuario");
                System.out.println("2. Obtener todos los usuarios");
                System.out.println("3. Buscar usuario por ID");
                System.out.println("4. Actualizar usuario");
                System.out.println("5. Eliminar usuario");
                System.out.println("0. Salir");
                System.out.print("Seleccione una opción: ");
                int option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1 ->
                        createUser(userService, scanner);
                    case 2 ->
                        getAllUsers(userService);
                    case 3 ->
                        getUserById(userService, scanner);
                    case 4 ->
                        updateUser(userService, scanner);
                    case 5 ->
                        deleteUser(userService, scanner);
                    case 0 ->
                        exit = true;
                    default ->
                        System.out.println("Opción no válida.");
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

    private static void createUser(UserService service, Scanner scanner) {
        User user = new User();
        System.out.print("Nombre: ");
        user.setName(scanner.nextLine());
        System.out.print("Correo electrónico: ");
        user.setEmail(scanner.nextLine());
        System.out.print("Contraseña (en texto plano para prueba): ");
        user.setPassword(scanner.nextLine());

        user.setCreatedAt(ZonedDateTime.now());
        user.setUpdatedAt(ZonedDateTime.now());

        User created = service.createUser(user);
        System.out.println("Usuario creado con éxito con ID: " + created.getId());
    }

    private static void getAllUsers(UserService service) {
        var users = service.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No hay usuarios.");
            return;
        }
        users.forEach(TestUserApp::printUserDetails);
    }

    private static void getUserById(UserService service, Scanner scanner) {
        System.out.print("ID de usuario (long): ");
        long id = scanner.nextLong();
        scanner.nextLine();
        try {
            User user = service.getUserById(id);
            printUserDetails(user);
        } catch (Exception e) {
            System.err.println("Error al buscar usuario: " + e.getMessage());
        }
    }

    private static void updateUser(UserService service, Scanner scanner) {
        System.out.print("ID de usuario a actualizar (long): ");
        long id = scanner.nextLong();
        scanner.nextLine();
        User user = service.getUserById(id);
        System.out.print("Nuevo nombre (" + user.getName() + "): ");
        String name = scanner.nextLine();
        if (!name.isBlank()) {
            user.setName(name);
        }
        System.out.print("Nuevo correo (" + user.getEmail() + "): ");
        String email = scanner.nextLine();
        if (!email.isBlank()) {
            user.setEmail(email);
        }
        service.updateUserById(id, user);
        System.out.println("Usuario actualizado.");
    }

    private static void deleteUser(UserService service, Scanner scanner) {
        System.out.print("ID de usuario a eliminar (long): ");
        long id = scanner.nextLong();
        scanner.nextLine();
        service.deleteUserById(id);
        System.out.println("Usuario eliminado.");
    }

    private static void printUserDetails(User user) {
        System.out.println("ID: " + user.getId());
        System.out.println("Nombre: " + user.getName());
        System.out.println("Correo: " + user.getEmail());
        System.out.println("Contraseña: " + user.getPassword());
        System.out.println("Balance Global: " + user.getGlobalBalance());
        System.out.println("Creado: " + user.getCreatedAt());
        System.out.println("Actualizado: " + user.getUpdatedAt());
        System.out.println("----------------------------------------");
    }
}
