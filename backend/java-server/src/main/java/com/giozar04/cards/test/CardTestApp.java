package com.giozar04.cards.test;

import java.time.ZonedDateTime;
import java.util.Scanner;

import com.giozar04.card.domain.entities.Card;
import com.giozar04.card.domain.enums.CardTypes;
import com.giozar04.cards.application.services.CardService;
import com.giozar04.cards.infrastructure.repositories.CardRepositoryMySQL;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.databases.infrastructure.repositories.DatabaseConnectionMySQL;

public class CardTestApp {

    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "finanzas";
    private static final String DB_USER = "giovanni";
    private static final String DB_PASSWORD = "finanzas123";

    public static void main(String[] args) {
        System.out.println("Iniciando prueba de tarjetas...");

        try {
            DatabaseConnectionInterface dbConnection = DatabaseConnectionMySQL.getInstance(
                DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
            );
            dbConnection.connect();

            CardRepositoryMySQL repository = new CardRepositoryMySQL(dbConnection);
            CardService service = new CardService(repository);

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n===== MENÚ DE PRUEBA DE TARJETAS =====");
                System.out.println("1. Crear tarjeta");
                System.out.println("2. Ver todas las tarjetas");
                System.out.println("3. Buscar tarjeta por ID");
                System.out.println("4. Actualizar tarjeta");
                System.out.println("5. Eliminar tarjeta");
                System.out.println("0. Salir");
                System.out.print("Seleccione una opción: ");
                int option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1 -> createCard(service, scanner);
                    case 2 -> getAllCards(service);
                    case 3 -> getCardById(service, scanner);
                    case 4 -> updateCard(service, scanner);
                    case 5 -> deleteCard(service, scanner);
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

    private static void createCard(CardService service, Scanner scanner) {
        Card card = new Card();

        System.out.print("ID de cuenta asociada: ");
        card.setAccountId(scanner.nextLong());
        scanner.nextLine();

        System.out.print("Nombre de la tarjeta (ej. Débito BBVA Física): ");
        card.setName(scanner.nextLine());

        System.out.print("Tipo de tarjeta (physical/digital): ");
        String type = scanner.nextLine().trim().toLowerCase();
        while (!type.equals("physical") && !type.equals("digital")) {
            System.out.print("Tipo inválido. Ingrese 'physical' o 'digital': ");
            type = scanner.nextLine().trim().toLowerCase();
        }
        card.setCardType(CardTypes.fromValue(type));

        System.out.print("Últimos 4 dígitos de la tarjeta: ");
        card.setCardNumber(scanner.nextLine());

        System.out.print("Fecha de expiración (YYYY-MM-DD): ");
        String fecha = scanner.nextLine();
        card.setExpirationDate(ZonedDateTime.parse(fecha + "T00:00:00Z"));

        System.out.print("Estado (ACTIVE/BLOCKED/EXPIRED) [ACTIVE]: ");
        String status = scanner.nextLine().trim().toUpperCase();
        card.setStatus(status.isBlank() ? "ACTIVE" : status);

        card.setCreatedAt(ZonedDateTime.now());
        card.setUpdatedAt(ZonedDateTime.now());

        Card created = service.createCard(card);
        System.out.println("Tarjeta creada con ID: " + created.getId());
    }

    private static void updateCard(CardService service, Scanner scanner) {
        System.out.print("ID de la tarjeta a actualizar: ");
        long id = scanner.nextLong();
        scanner.nextLine();

        Card card = service.getCardById(id);

        System.out.print("Nuevo nombre (" + card.getName() + "): ");
        String name = scanner.nextLine();
        if (!name.isBlank()) card.setName(name);

        System.out.print("Nuevo tipo (physical/digital) [" + card.getCardType().getValue() + "]: ");
        String type = scanner.nextLine().trim().toLowerCase();
        if (!type.isBlank()) card.setCardType(CardTypes.fromValue(type));

        System.out.print("Nuevos últimos 4 dígitos (" + card.getCardNumber() + "): ");
        String digits = scanner.nextLine();
        if (!digits.isBlank()) card.setCardNumber(digits);

        System.out.print("Nueva fecha de expiración (YYYY-MM-DD) [" + card.getExpirationDate().toLocalDate() + "]: ");
        String exp = scanner.nextLine();
        if (!exp.isBlank()) card.setExpirationDate(ZonedDateTime.parse(exp + "T00:00:00Z"));

        System.out.print("Nuevo estado (ACTIVE/BLOCKED/EXPIRED) [" + card.getStatus() + "]: ");
        String statusStr = scanner.nextLine().trim().toUpperCase();
        if (!statusStr.isBlank()) card.setStatus(statusStr);

        card.setUpdatedAt(ZonedDateTime.now());

        service.updateCardById(id, card);
        System.out.println("Tarjeta actualizada.");
    }

    private static void deleteCard(CardService service, Scanner scanner) {
        System.out.print("ID de tarjeta a eliminar: ");
        long id = scanner.nextLong();
        scanner.nextLine();

        service.deleteCardById(id);
        System.out.println("Tarjeta eliminada.");
    }

    private static void getAllCards(CardService service) {
        var cards = service.getAllCards();
        if (cards.isEmpty()) {
            System.out.println("No hay tarjetas registradas.");
            return;
        }

        cards.forEach(CardTestApp::printCardDetails);
    }

    private static void getCardById(CardService service, Scanner scanner) {
        System.out.print("ID de tarjeta: ");
        long id = scanner.nextLong();
        scanner.nextLine();

        try {
            Card card = service.getCardById(id);
            printCardDetails(card);
        } catch (Exception e) {
            System.err.println("Error al buscar tarjeta: " + e.getMessage());
        }
    }

    private static void printCardDetails(Card card) {
        System.out.println("ID: " + card.getId());
        System.out.println("Nombre: " + card.getName());
        System.out.println("Tipo: " + card.getCardType().getLabel());
        System.out.println("Últimos 4 dígitos: " + card.getCardNumber());
        System.out.println("Cuenta asociada: " + card.getAccountId());
        System.out.println("Estado: " + card.getStatus());
        System.out.println("Expira: " + card.getExpirationDate().toLocalDate());
        System.out.println("Creado: " + card.getCreatedAt());
        System.out.println("Actualizado: " + card.getUpdatedAt());
        System.out.println("----------------------------------------");
    }
}
