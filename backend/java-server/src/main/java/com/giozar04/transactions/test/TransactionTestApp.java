package com.giozar04.transactions.test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.giozar04.accounts.infrastructure.repositories.AccountRepositoryMySQL;
import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;
import com.giozar04.cardTransactionDetails.infrastructure.repositories.CardTransactionDetailRepositoryMySQL;
import com.giozar04.cards.infrastructure.repositories.CardRepositoryMySQL;
import com.giozar04.categories.infrastructure.persistence.mysql.CategoryRepositoryMySQL;
import com.giozar04.databases.application.services.TransactionalExecutor;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.databases.infrastructure.repositories.DatabaseConnectionMySQL;
import com.giozar04.externalEntities.infrastructure.repositories.ExternalEntityRepositoryMySQL;
import com.giozar04.tags.infrastructure.persistence.mysql.TagRepositoryMySQL;
import com.giozar04.transactionTags.infrastructure.repositories.TransactionTagRepositoryMySQL;
import com.giozar04.transactions.application.normalizers.TransactionNormalizer;
import com.giozar04.transactions.application.services.TransactionService;
import com.giozar04.transactions.application.validation.TransactionRules;
import com.giozar04.transactions.application.validation.TransactionValidator;
import com.giozar04.transactions.application.validation.ValidationContextFactory;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.domain.enums.PaymentMethod;
import com.giozar04.transactions.domain.enums.TransactionStatus;
import com.giozar04.transactions.infrastructure.repositories.TransactionRepositoryMySQL;
import com.giozar04.walletCardLinks.infrastructure.repositories.WalletCardLinkRepositoryMySQL;
import com.giozar04.walletTransactionDetails.infrastructure.repositories.WalletTransactionDetailRepositoryMySQL;

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

            TransactionService service = buildService(dbConnection);

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n===== MENÚ TRANSACCIONES =====");
                System.out.println("1. Crear gasto en efectivo (EXPENSE + CASH)");
                System.out.println("2. Crear gasto con tarjeta (EXPENSE + CARD)");
                System.out.println("3. Ver transacciones de un usuario");
                System.out.println("4. Ver todas");
                System.out.println("5. Buscar por ID");
                System.out.println("6. Eliminar");
                System.out.println("0. Salir");
                System.out.print("Opción: ");
                int option = scanner.nextInt();
                scanner.nextLine();

                try {
                    switch (option) {
                        case 1 -> createExpense(service, scanner, PaymentMethod.CASH);
                        case 2 -> createExpense(service, scanner, PaymentMethod.CARD);
                        case 3 -> getByUser(service, scanner);
                        case 4 -> printList(service.getAllTransactions());
                        case 5 -> getById(service, scanner);
                        case 6 -> deleteTransaction(service, scanner);
                        case 0 -> exit = true;
                        default -> System.out.println("Opción inválida.");
                    }
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }

            dbConnection.disconnect();
            scanner.close();
            System.out.println("Finalizado.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Mismo cableado que ApplicationInitializer. */
    private static TransactionService buildService(DatabaseConnectionInterface dbConnection) {
        TransactionRepositoryMySQL repository = new TransactionRepositoryMySQL(
                dbConnection,
                new TransactionalExecutor(dbConnection),
                new CardTransactionDetailRepositoryMySQL(dbConnection),
                new WalletTransactionDetailRepositoryMySQL(dbConnection),
                new TransactionTagRepositoryMySQL());

        ValidationContextFactory contextFactory = new ValidationContextFactory(
                new AccountRepositoryMySQL(dbConnection),
                new CardRepositoryMySQL(dbConnection),
                new WalletCardLinkRepositoryMySQL(dbConnection),
                new CategoryRepositoryMySQL(dbConnection),
                new ExternalEntityRepositoryMySQL(dbConnection),
                new TagRepositoryMySQL(dbConnection));

        return new TransactionService(repository, contextFactory,
                new TransactionNormalizer(), new TransactionValidator(TransactionRules.defaultRules()));
    }

    private static void createExpense(TransactionService service, Scanner scanner, PaymentMethod method) {
        Transaction tx = new Transaction();
        tx.setOperationType(OperationTypes.EXPENSE);
        tx.setPaymentMethod(method);
        tx.setStatus(TransactionStatus.COMPLETED);

        System.out.print("ID usuario: ");
        tx.setUserId(readLong(scanner));

        System.out.print("ID cuenta origen: ");
        tx.setSourceAccountId(readLong(scanner));

        System.out.print("ID entidad externa (a quién se paga): ");
        tx.setExternalEntityId(readLong(scanner));

        System.out.print("ID categoría (EXPENSE o BOTH): ");
        tx.setCategoryId(readLong(scanner));

        System.out.print("Monto: ");
        tx.setAmount(new BigDecimal(scanner.nextLine().trim()));

        System.out.print("Concepto: ");
        tx.setConcept(scanner.nextLine());

        System.out.print("IDs de etiquetas separados por coma (vacío = ninguna): ");
        tx.setTagIds(parseIds(scanner.nextLine()));

        if (method == PaymentMethod.CARD) {
            CardTransactionDetail detail = new CardTransactionDetail();
            System.out.print("ID tarjeta (de la cuenta origen): ");
            detail.setCardId(readLong(scanner));

            System.out.print("Meses (0 = contado): ");
            long months = readLong(scanner);
            detail.setInstallmentMonths(months > 0 ? (int) months : null);

            System.out.print("¿Sin intereses? (s/n): ");
            detail.setInterestFree(scanner.nextLine().trim().equalsIgnoreCase("s"));
            tx.setCardDetail(detail); // el monto lo fija el normalizador
        }

        String timezone = ZoneId.systemDefault().getId();
        tx.setTimezone(timezone);
        tx.setDate(ZonedDateTime.now(ZoneId.of(timezone)));

        Transaction created = service.createTransaction(tx);
        System.out.println("Transacción creada con ID: " + created.getId());
        print(created);
    }

    private static void getByUser(TransactionService service, Scanner scanner) {
        System.out.print("ID usuario: ");
        printList(service.getTransactionsByUserId(readLong(scanner)));
    }

    private static void getById(TransactionService service, Scanner scanner) {
        System.out.print("ID a buscar: ");
        print(service.getTransactionById(readLong(scanner)));
    }

    private static void deleteTransaction(TransactionService service, Scanner scanner) {
        System.out.print("ID a eliminar: ");
        service.deleteTransactionById(readLong(scanner));
        System.out.println("Transacción eliminada.");
    }

    private static void printList(List<Transaction> list) {
        if (list.isEmpty()) {
            System.out.println("No hay transacciones registradas.");
        } else {
            list.forEach(TransactionTestApp::print);
        }
    }

    private static void print(Transaction tx) {
        System.out.println("ID: " + tx.getId() + " | Usuario: " + tx.getUserId());
        System.out.println("Tipo: " + tx.getOperationType().getLabel() + " | Método: " + tx.getPaymentMethod().getLabel()
                + " | Estado: " + tx.getStatus().getLabel());
        System.out.println("Origen: " + tx.getSourceAccountId() + " | Destino: " + tx.getDestinationAccountId()
                + " | Entidad: " + tx.getExternalEntityId() + " | Categoría: " + tx.getCategoryId());
        System.out.println("Monto: $" + tx.getAmount() + " | Concepto: " + tx.getConcept());
        System.out.println("Fecha: " + tx.getDate() + " | Zona horaria: " + tx.getTimezone());
        System.out.println("Etiquetas: " + tx.getTagIds());
        if (tx.getCardDetail() != null) {
            System.out.println("Tarjeta: " + tx.getCardDetail().getCardId()
                    + " | Meses: " + tx.getCardDetail().getInstallmentMonths()
                    + " | Sin intereses: " + tx.getCardDetail().isInterestFree());
        }
        if (tx.getWalletDetail() != null) {
            System.out.println("Wallet: " + tx.getWalletDetail().getWalletAccountId()
                    + " | Origen: " + tx.getWalletDetail().getSourceType()
                    + " | Tarjeta vinculada: " + tx.getWalletDetail().getCardId());
        }
        System.out.println("----------------------------------------");
    }

    private static long readLong(Scanner scanner) {
        return Long.parseLong(scanner.nextLine().trim());
    }

    private static List<Long> parseIds(String raw) {
        List<Long> ids = new ArrayList<>();
        if (raw == null || raw.isBlank()) return ids;
        for (String part : raw.split(",")) {
            if (!part.isBlank()) ids.add(Long.valueOf(part.trim()));
        }
        return ids;
    }
}
