package com.giozar04.transactions.test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Scanner;

import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.databases.infrastructure.repositories.DatabaseConnectionMySQL;
import com.giozar04.transactions.application.services.TransactionService;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.domain.enums.PaymentMethod;
import com.giozar04.transactions.infrastructure.repositories.TransactionRepositoryMySQL;

public class TransactionTestApp {

    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "finanzas";
    private static final String DB_USER = "giovanni";
    private static final String DB_PASSWORD = "finanzas123";

    public static void main(String[] args) {
        System.out.println("Iniciando prueba de transacciones...");

        try {
            DatabaseConnectionInterface dbConnection = DatabaseConnectionMySQL.getInstance(
                DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
            );
            dbConnection.connect();

            TransactionRepositoryMySQL repository = new TransactionRepositoryMySQL(dbConnection);
            TransactionService service = new TransactionService(repository);

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n===== MENÚ TRANSACCIONES =====");
                System.out.println("1. Crear transacción");
                System.out.println("2. Ver todas");
                System.out.println("3. Buscar por ID");
                System.out.println("4. Actualizar");
                System.out.println("5. Eliminar");
                System.out.println("0. Salir");
                System.out.print("Opción: ");
                int option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1 -> createTransaction(service, scanner);
                    case 2 -> getAll(service);
                    case 3 -> getById(service, scanner);
                    case 4 -> updateTransaction(service, scanner);
                    case 5 -> deleteTransaction(service, scanner);
                    case 0 -> exit = true;
                    default -> System.out.println("Opción inválida.");
                }
            }

            dbConnection.disconnect();
            scanner.close();
            System.out.println("Finalizado.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createTransaction(TransactionService service, Scanner scanner) {
        Transaction tx = new Transaction();

        System.out.print("Tipo de operación (income / expense): ");
        tx.setOperationType(OperationTypes.fromValue(scanner.nextLine()));

        System.out.print("Método de pago (cash / card / transfer / qr / codi / wallet): ");
        tx.setPaymentMethod(PaymentMethod.fromValue(scanner.nextLine()));

        System.out.print("ID cuenta origen (0 si nulo): ");
        long srcId = scanner.nextLong(); scanner.nextLine();
        tx.setSourceAccountId(srcId > 0 ? srcId : null);

        System.out.print("ID cuenta destino (0 si nulo): ");
        long dstId = scanner.nextLong(); scanner.nextLine();
        tx.setDestinationAccountId(dstId > 0 ? dstId : null);

        System.out.print("ID entidad externa (0 si nulo): ");
        long entityId = scanner.nextLong(); scanner.nextLine();
        tx.setExternalEntityId(entityId > 0 ? entityId : null);

        System.out.print("Monto: ");
        tx.setAmount(scanner.nextBigDecimal());
        scanner.nextLine();

        System.out.print("Concepto: ");
        tx.setConcept(scanner.nextLine());

        System.out.print("Categoría: ");
        tx.setCategory(scanner.nextLine());

        System.out.print("Descripción (opcional): ");
        String desc = scanner.nextLine();
        tx.setDescription(desc.isBlank() ? null : desc);

        System.out.print("Comentarios (opcional): ");
        String comments = scanner.nextLine();
        tx.setComments(comments.isBlank() ? null : comments);

        System.out.print("Zona horaria (ej. America/Mexico_City): ");
        tx.setTimezone(scanner.nextLine());

        System.out.print("Tags separados por coma: ");
        tx.setTags(scanner.nextLine());

        tx.setDate(ZonedDateTime.now());
        tx.setCreatedAt(ZonedDateTime.now());
        tx.setUpdatedAt(ZonedDateTime.now());

        Transaction created = service.createTransaction(tx);
        System.out.println("Transacción creada con ID: " + created.getId());
    }

    private static void updateTransaction(TransactionService service, Scanner scanner) {
        System.out.print("ID de la transacción a actualizar: ");
        long id = scanner.nextLong(); scanner.nextLine();

        Transaction tx = service.getTransactionById(id);

        System.out.print("Nuevo concepto (" + tx.getConcept() + "): ");
        String concept = scanner.nextLine();
        if (!concept.isBlank()) tx.setConcept(concept);

        System.out.print("Nuevo monto (" + tx.getAmount() + "): ");
        String monto = scanner.nextLine();
        if (!monto.isBlank()) tx.setAmount(new BigDecimal(monto));

        System.out.print("Nueva categoría (" + tx.getCategory() + "): ");
        String category = scanner.nextLine();
        if (!category.isBlank()) tx.setCategory(category);

        tx.setUpdatedAt(ZonedDateTime.now());

        service.updateTransactionById(id, tx);
        System.out.println("Transacción actualizada.");
    }

    private static void deleteTransaction(TransactionService service, Scanner scanner) {
        System.out.print("ID a eliminar: ");
        long id = scanner.nextLong();
        scanner.nextLine();
        service.deleteTransactionById(id);
        System.out.println("Transacción eliminada.");
    }

    private static void getById(TransactionService service, Scanner scanner) {
        System.out.print("ID a buscar: ");
        long id = scanner.nextLong(); scanner.nextLine();
        Transaction tx = service.getTransactionById(id);
        print(tx);
    }

    private static void getAll(TransactionService service) {
        var list = service.getAllTransactions();
        if (list.isEmpty()) {
            System.out.println("No hay transacciones registradas.");
        } else {
            list.forEach(TransactionTestApp::print);
        }
    }

    private static void print(Transaction tx) {
        System.out.println("ID: " + tx.getId());
        System.out.println("Tipo: " + tx.getOperationType().getLabel());
        System.out.println("Método: " + tx.getPaymentMethod().getLabel());
        System.out.println("Cuenta origen: " + tx.getSourceAccountId());
        System.out.println("Cuenta destino: " + tx.getDestinationAccountId());
        System.out.println("Entidad externa: " + tx.getExternalEntityId());
        System.out.println("Monto: $" + tx.getAmount());
        System.out.println("Concepto: " + tx.getConcept());
        System.out.println("Categoría: " + tx.getCategory());
        System.out.println("Descripción: " + tx.getDescription());
        System.out.println("Comentarios: " + tx.getComments());
        System.out.println("Fecha: " + tx.getDate());
        System.out.println("Zona horaria: " + tx.getTimezone());
        System.out.println("Tags: " + tx.getTags());
        System.out.println("Creado: " + tx.getCreatedAt());
        System.out.println("Actualizado: " + tx.getUpdatedAt());
        System.out.println("----------------------------------------");
    }
}
