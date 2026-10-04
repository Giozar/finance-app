package com.giozar04.transactions.infrastructure.transport.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.messages.infrastructure.transport.Message;
import com.giozar04.servers.infrastructure.transport.socket.MessageHandler;
import com.giozar04.servers.infrastructure.transport.socket.ClientConnection;
import com.giozar04.transactions.application.ports.input.TransactionOperations;
import com.giozar04.transactions.infrastructure.serialization.TransactionMapper;
import com.giozar04.transactions.domain.entities.Transaction;

public class TransactionControllers {

    private static final ConsoleLogger LOGGER = ConsoleLogger.getInstance();

    public static final class TransactionMessageTypes {
        public static final String CREATE_TRANSACTION = "CREATE_TRANSACTION";
        public static final String GET_TRANSACTION = "GET_TRANSACTION";
        public static final String UPDATE_TRANSACTION = "UPDATE_TRANSACTION";
        public static final String DELETE_TRANSACTION = "DELETE_TRANSACTION";
        public static final String GET_ALL_TRANSACTIONS = "GET_ALL_TRANSACTIONS";
        public static final String GET_TRANSACTIONS_BY_USER = "GET_TRANSACTIONS_BY_USER";
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler createTransactionController(TransactionOperations service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando creación de transacción");

            Map<String, Object> data = (Map<String, Object>) message.getData("transaction");
            if (data == null) {
                return Message.createErrorMessage(TransactionMessageTypes.CREATE_TRANSACTION, "Datos no proporcionados");
            }

            Transaction created = service.createTransaction(TransactionMapper.fromMap(data));

            Message response = Message.createSuccessMessage(TransactionMessageTypes.CREATE_TRANSACTION, "Transacción creada exitosamente");
            response.addData("transaction", TransactionMapper.toMap(created));
            return response;
        };
    }

    public static MessageHandler getTransactionController(TransactionOperations service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando obtención de transacción");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(TransactionMessageTypes.GET_TRANSACTION, "ID inválido");
            }

            Transaction tx = service.getTransactionById(id);
            Message response = Message.createSuccessMessage(TransactionMessageTypes.GET_TRANSACTION, "Transacción obtenida exitosamente");
            response.addData("transaction", TransactionMapper.toMap(tx));
            return response;
        };
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler updateTransactionController(TransactionOperations service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando actualización de transacción");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(TransactionMessageTypes.UPDATE_TRANSACTION, "ID inválido");
            }

            Map<String, Object> data = (Map<String, Object>) message.getData("transaction");
            if (data == null) {
                return Message.createErrorMessage(TransactionMessageTypes.UPDATE_TRANSACTION, "Datos no proporcionados");
            }

            Transaction updated = service.updateTransactionById(id, TransactionMapper.fromMap(data));

            Message response = Message.createSuccessMessage(TransactionMessageTypes.UPDATE_TRANSACTION, "Transacción actualizada exitosamente");
            response.addData("transaction", TransactionMapper.toMap(updated));
            return response;
        };
    }

    public static MessageHandler deleteTransactionController(TransactionOperations service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando eliminación de transacción");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(TransactionMessageTypes.DELETE_TRANSACTION, "ID inválido");
            }

            service.deleteTransactionById(id);
            return Message.createSuccessMessage(TransactionMessageTypes.DELETE_TRANSACTION, "Transacción eliminada exitosamente");
        };
    }

    public static MessageHandler getAllTransactionsController(TransactionOperations service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando obtención de todas las transacciones");
            return listResponse(TransactionMessageTypes.GET_ALL_TRANSACTIONS, "Transacciones obtenidas exitosamente",
                    service.getAllTransactions());
        };
    }

    public static MessageHandler getTransactionsByUserController(TransactionOperations service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando obtención de transacciones por usuario");

            Long userId = parseId(message.getData("userId"));
            if (userId == null) {
                return Message.createErrorMessage(TransactionMessageTypes.GET_TRANSACTIONS_BY_USER, "userId inválido");
            }

            return listResponse(TransactionMessageTypes.GET_TRANSACTIONS_BY_USER, "Transacciones del usuario obtenidas exitosamente",
                    service.getTransactionsByUserId(userId));
        };
    }

    private static Message listResponse(String type, String successMessage, List<Transaction> transactions) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Transaction tx : transactions) {
            result.add(TransactionMapper.toMap(tx));
        }

        Message response = Message.createSuccessMessage(type, successMessage);
        response.addData("transactions", result);
        response.addData("count", result.size());
        return response;
    }

    private static Long parseId(Object rawId) {
        if (rawId instanceof Long l) return l;
        if (rawId instanceof String s) {
            try {
                return Long.valueOf(s);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
