package com.giozar04.transactions.infrastructure.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.messages.domain.models.Message;
import com.giozar04.servers.domain.handlers.MessageHandler;
import com.giozar04.servers.domain.models.ClientConnection;
import com.giozar04.shared.logging.CustomLogger;
import com.giozar04.transactions.application.services.TransactionService;
import com.giozar04.transactions.application.utils.TransactionUtils;
import com.giozar04.transactions.domain.entities.Transaction;

/**
 * Clase que proporciona manejadores para las operaciones relacionadas con
 * transacciones. Utiliza serialización nativa de Java en lugar de bibliotecas
 * externas.
 */
public class TransactionHandlers {

    private static final CustomLogger LOGGER = new CustomLogger();

    /**
     * Tipos de mensajes para operaciones de transacciones.
     */
    public static final class TransactionMessageTypes {

        public static final String CREATE_TRANSACTION = "CREATE_TRANSACTION";
        public static final String GET_TRANSACTION = "GET_TRANSACTION";
        public static final String UPDATE_TRANSACTION = "UPDATE_TRANSACTION";
        public static final String DELETE_TRANSACTION = "DELETE_TRANSACTION";
        public static final String GET_ALL_TRANSACTIONS = "GET_ALL_TRANSACTIONS";
    }

    /**
     * Crea un manejador para la creación de transacciones.
     *
     * @param transactionService El servicio de transacciones a utilizar.
     * @return Un manejador para mensajes de creación de transacciones.
     */
    public static MessageHandler createTransactionHandler(TransactionService transactionService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de creación de transacción");

            // Extraer datos de la transacción del mensaje
            @SuppressWarnings("unchecked")
            Map<String, Object> transactionData = (Map<String, Object>) message.getData("transaction");
            if (transactionData == null) {
                return Message.createErrorMessage(TransactionMessageTypes.CREATE_TRANSACTION,
                        "Datos de transacción no proporcionados");
            }

            // Convertir mapa a objeto Transaction
            Transaction transaction = TransactionUtils.mapToTransaction(transactionData);

            // Crear la transacción usando el servicio
            Transaction createdTransaction = transactionService.createTransaction(transaction);

            // Crear mensaje de respuesta
            Message response = Message.createSuccessMessage(
                    TransactionMessageTypes.CREATE_TRANSACTION,
                    "Transacción creada exitosamente");

            // Convertir la transacción creada a un mapa para incluirla en la respuesta
            response.addData("transaction", TransactionUtils.transactionToMap(createdTransaction));

            return response;
        };
    }

    /**
     * Crea un manejador para obtener una transacción por ID.
     *
     * @param transactionService El servicio de transacciones a utilizar.
     * @return Un manejador para mensajes de obtención de transacciones.
     */
    public static MessageHandler getTransactionHandler(TransactionService transactionService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de obtención de transacción");

            // Extraer ID de la transacción del mensaje
           Object rawId = message.getData("id");
           Long id = null;
           if (rawId instanceof Long) {
               id = (Long) rawId;
           } else if (rawId instanceof String) {
               try {
                   id = Long.parseLong((String) rawId);
               } catch (NumberFormatException e) {
                   return Message.createErrorMessage(TransactionMessageTypes.UPDATE_TRANSACTION,
                           "ID de transacción inválido");
               }
           }
            if (id == null) {
                return Message.createErrorMessage(TransactionMessageTypes.GET_TRANSACTION,
                        "ID de transacción no proporcionado");
            }

            // Obtener la transacción usando el servicio
            Transaction transaction = transactionService.getTransactionById(id);

            // Crear mensaje de respuesta
            Message response = Message.createSuccessMessage(
                    TransactionMessageTypes.GET_TRANSACTION,
                    "Transacción obtenida exitosamente");

            // Convertir la transacción a un mapa para incluirla en la respuesta
            response.addData("transaction", TransactionUtils.transactionToMap(transaction));

            return response;
        };
    }

    /**
     * Crea un manejador para actualizar una transacción.
     *
     * @param transactionService El servicio de transacciones a utilizar.
     * @return Un manejador para mensajes de actualización de transacciones.
     */
    public static MessageHandler updateTransactionHandler(TransactionService transactionService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de actualización de transacción");

            // Extraer ID de la transacción del mensaje
            Object rawId = message.getData("id");
            Long id = null;
            if (rawId instanceof Long) {
                id = (Long) rawId;
            } else if (rawId instanceof String) {
                try {
                    id = Long.parseLong((String) rawId);
                } catch (NumberFormatException e) {
                    return Message.createErrorMessage(TransactionMessageTypes.UPDATE_TRANSACTION,
                            "ID de transacción inválido");
                }
            }
            if (id == null) {
                return Message.createErrorMessage(TransactionMessageTypes.UPDATE_TRANSACTION,
                        "ID de transacción no proporcionado");
            }

            // Extraer datos de la transacción del mensaje
            @SuppressWarnings("unchecked")
            Map<String, Object> transactionData = (Map<String, Object>) message.getData("transaction");
            if (transactionData == null) {
                return Message.createErrorMessage(TransactionMessageTypes.UPDATE_TRANSACTION,
                        "Datos de transacción no proporcionados");
            }

            // Convertir mapa a objeto Transaction
            Transaction transaction = TransactionUtils.mapToTransaction(transactionData);

            // Actualizar la transacción usando el servicio
            Transaction updatedTransaction = transactionService.updateTransactionById(id, transaction);

            // Crear mensaje de respuesta
            Message response = Message.createSuccessMessage(
                    TransactionMessageTypes.UPDATE_TRANSACTION,
                    "Transacción actualizada exitosamente");

            // Convertir la transacción actualizada a un mapa para incluirla en la respuesta
            response.addData("transaction", TransactionUtils.transactionToMap(updatedTransaction));

            return response;
        };
    }

    /**
     * Crea un manejador para eliminar una transacción.
     *
     * @param transactionService El servicio de transacciones a utilizar.
     * @return Un manejador para mensajes de eliminación de transacciones.
     */
    public static MessageHandler deleteTransactionHandler(TransactionService transactionService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de eliminación de transacción");

            // Extraer ID de la transacción del mensaje
            Object rawId = message.getData("id");
            Long id = null;
            if (rawId instanceof Long) {
                id = (Long) rawId;
            } else if (rawId instanceof String) {
                try {
                    id = Long.parseLong((String) rawId);
                } catch (NumberFormatException e) {
                    return Message.createErrorMessage(TransactionMessageTypes.UPDATE_TRANSACTION,
                            "ID de transacción inválido");
                }
            }
            if (id == null) {
                return Message.createErrorMessage(TransactionMessageTypes.DELETE_TRANSACTION,
                        "ID de transacción no proporcionado");
            }

            // Eliminar la transacción usando el servicio
            transactionService.deleteTransactionById(id);

            // Crear mensaje de respuesta
            return Message.createSuccessMessage(
                    TransactionMessageTypes.DELETE_TRANSACTION,
                    "Transacción eliminada exitosamente");
        };
    }

    /**
     * Crea un manejador para obtener todas las transacciones.
     *
     * @param transactionService El servicio de transacciones a utilizar.
     * @return Un manejador para mensajes de obtención de todas las
     * transacciones.
     */
    public static MessageHandler getAllTransactionsHandler(TransactionService transactionService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de obtención de todas las transacciones");

            // Obtener todas las transacciones usando el servicio
            List<Transaction> transactions = transactionService.getAllTransactions();

            // Crear mensaje de respuesta
            Message response = Message.createSuccessMessage(
                    TransactionMessageTypes.GET_ALL_TRANSACTIONS,
                    "Transacciones obtenidas exitosamente");

            // Convertir cada transacción a un mapa y añadirlas a una lista
            List<Map<String, Object>> transactionList = new ArrayList<>();
            for (Transaction t : transactions) {
                transactionList.add(TransactionUtils.transactionToMap(t));
            }
            response.addData("transactions", transactionList);
            response.addData("count", transactions.size());

            // System.out.println("🟦 Mensaje completo: " + response);
            return response;
        };
    }
}
