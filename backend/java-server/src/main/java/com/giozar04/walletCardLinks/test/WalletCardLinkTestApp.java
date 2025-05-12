package com.giozar04.walletCardLinks.test;

import java.time.ZonedDateTime;
import java.util.Scanner;

import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.databases.infrastructure.repositories.DatabaseConnectionMySQL;
import com.giozar04.walletCardLinks.application.services.WalletCardLinkService;
import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;
import com.giozar04.walletCardLinks.infrastructure.repositories.WalletCardLinkRepositoryMySQL;

public class WalletCardLinkTestApp {

    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "finanzas";
    private static final String DB_USER = "giovanni";
    private static final String DB_PASSWORD = "finanzas123";

    public static void main(String[] args) {
        System.out.println("Iniciando prueba de vínculos wallet-tarjeta...");

        try {
            DatabaseConnectionInterface dbConnection = DatabaseConnectionMySQL.getInstance(
                DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
            );
            dbConnection.connect();

            WalletCardLinkRepositoryMySQL repository = new WalletCardLinkRepositoryMySQL(dbConnection);
            WalletCardLinkService service = new WalletCardLinkService(repository);

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n===== MENÚ DE VÍNCULOS WALLET-TARJETA =====");
                System.out.println("1. Crear vínculo");
                System.out.println("2. Ver vínculos por cuenta wallet");
                System.out.println("3. Ver todos los vínculos");
                System.out.println("4. Eliminar vínculo");
                System.out.println("0. Salir");
                System.out.print("Seleccione una opción: ");
                int option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1 -> createLink(service, scanner);
                    case 2 -> getLinksByWallet(service, scanner);
                    case 3 -> getAllLinks(service);
                    case 4 -> deleteLink(service, scanner);
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

    private static void createLink(WalletCardLinkService service, Scanner scanner) {
        WalletCardLink link = new WalletCardLink();

        System.out.print("ID de la cuenta tipo wallet: ");
        link.setWalletAccountId(scanner.nextLong());

        System.out.print("ID de la tarjeta a asociar: ");
        link.setCardId(scanner.nextLong());
        scanner.nextLine();

        link.setCreatedAt(ZonedDateTime.now());
        link.setUpdatedAt(ZonedDateTime.now());

        WalletCardLink created = service.createLink(link);
        System.out.println("Vínculo creado con ID: " + created.getId());
    }

    private static void getLinksByWallet(WalletCardLinkService service, Scanner scanner) {
        System.out.print("ID de la cuenta wallet: ");
        long walletId = scanner.nextLong();
        scanner.nextLine();

        var links = service.getLinksByWalletAccountId(walletId);
        if (links.isEmpty()) {
            System.out.println("No hay vínculos para esta cuenta wallet.");
            return;
        }

        links.forEach(WalletCardLinkTestApp::printLink);
    }

    private static void getAllLinks(WalletCardLinkService service) {
        var links = service.getAllLinks();
        if (links.isEmpty()) {
            System.out.println("No hay vínculos registrados.");
            return;
        }

        links.forEach(WalletCardLinkTestApp::printLink);
    }

    private static void deleteLink(WalletCardLinkService service, Scanner scanner) {
        System.out.print("ID del vínculo a eliminar: ");
        long id = scanner.nextLong();
        scanner.nextLine();

        service.deleteLinkById(id);
        System.out.println("Vínculo eliminado.");
    }

    private static void printLink(WalletCardLink link) {
        System.out.println("ID: " + link.getId());
        System.out.println("Wallet Account ID: " + link.getWalletAccountId());
        System.out.println("Card ID: " + link.getCardId());
        System.out.println("Creado: " + link.getCreatedAt());
        System.out.println("Actualizado: " + link.getUpdatedAt());
        System.out.println("----------------------------------------");
    }
}
