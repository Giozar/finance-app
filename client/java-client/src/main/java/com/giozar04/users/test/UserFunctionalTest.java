package com.giozar04.users.test;

import java.util.List;
import java.util.Scanner;

import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.users.infrastructure.services.UserService;
import com.giozar04.users.domain.entities.User;
import com.giozar04.configs.ServerConnectionConfig;

public class UserFunctionalTest {

    public static void main(String[] args) {
        System.out.println("=== PRUEBA FUNCIONAL DE USUARIO (CLIENTE) ===");

        try {
            // 1. Inicializar conexión
            ServerConnectionConfig config = new ServerConnectionConfig();
            ServerConnectionService connectionService = ServerConnectionService.getInstance(config.getHost(), config.getPort());
            connectionService.connect();
            System.out.println("✅ Conectado al servidor en " + config.getHost() + ":" + config.getPort());

            // 2. Inicializar servicio
            UserService userService = UserService.connectService(connectionService);
            System.out.println("✅ Servicio de usuarios inicializado.");

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n--- MENÚ DE PRUEBA FUNCIONAL ---");
                System.out.println("1. Crear Usuario");
                System.out.println("2. Consultar Todos");
                System.out.println("3. Consultar por ID");
                System.out.println("4. Actualizar Usuario");
                System.out.println("5. Eliminar Usuario");
                System.out.println("0. Salir");
                System.out.print("Seleccione: ");

                int option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1 -> createUser(userService, scanner);
                    case 2 -> getAllUsers(userService);
                    case 3 -> getUserById(userService, scanner);
                    case 4 -> updateUser(userService, scanner);
                    case 5 -> deleteUser(userService, scanner);
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

    private static void createUser(UserService service, Scanner scanner) throws Exception {
        System.out.println("\n[Crear Usuario]");
        User user = new User();
        System.out.print("Nombre: ");
        user.setName(scanner.nextLine());
        System.out.print("Email: ");
        user.setEmail(scanner.nextLine());
        System.out.print("Password: ");
        user.setPassword(scanner.nextLine());
        user.setGlobalBalance(0.0);

        User created = service.createUser(user);
        System.out.println("✅ Usuario creado con ID: " + created.getId());
    }

    private static void getAllUsers(UserService service) throws Exception {
        System.out.println("\n[Consultar Todos]");
        List<User> users = service.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No hay usuarios registrados.");
        } else {
            users.forEach(u -> System.out.println("- ID: " + u.getId() + " | Nombre: " + u.getName() + " | Email: " + u.getEmail()));
        }
    }

    private static void getUserById(UserService service, Scanner scanner) throws Exception {
        System.out.print("\nID de usuario: ");
        long id = scanner.nextLong();
        scanner.nextLine();
        User user = service.getUserById(id);
        System.out.println("✅ Detalle: " + user.getName() + " (" + user.getEmail() + ")");
    }

    private static void updateUser(UserService service, Scanner scanner) throws Exception {
        System.out.print("\nID de usuario a actualizar: ");
        long id = scanner.nextLong();
        scanner.nextLine();
        User user = service.getUserById(id);

        System.out.print("Nuevo Nombre (" + user.getName() + "): ");
        String name = scanner.nextLine();
        if (!name.isBlank()) user.setName(name);

        System.out.print("Nuevo Email (" + user.getEmail() + "): ");
        String email = scanner.nextLine();
        if (!email.isBlank()) user.setEmail(email);

        User updated = service.updateUserById(id, user);
        System.out.println("✅ Usuario actualizado.");
    }

    private static void deleteUser(UserService service, Scanner scanner) throws Exception {
        System.out.print("\nID de usuario a eliminar: ");
        long id = scanner.nextLong();
        scanner.nextLine();
        service.deleteUserById(id);
        System.out.println("✅ Usuario eliminado.");
    }
}
