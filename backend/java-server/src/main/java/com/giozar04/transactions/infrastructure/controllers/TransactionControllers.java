package com.giozar04.transactions.infrastructure.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.logging.CustomLogger;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.servers.domain.handlers.MessageHandler;
import com.giozar04.servers.domain.models.ClientConnection;
import com.giozar04.transactions.application.services.TransactionService;
import com.giozar04.transactions.application.utils.TransactionUtils;
import com.giozar04.transactions.domain.entities.Transaction;

public class TransactionControllers {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    public static final class MessageTypes {
        public static final String CREATE = "CREATE_TRANSACTION";
        public static final String GET = "GET_TRANSACTION";
        public static final String UPDATE = "UPDATE_TRANSACTION";
        public static final String DELETE = "DELETE_TRANSACTION";
        public static final String GET_ALL = "GET_ALL_TRANSACTIONS";
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler createTransactionController(TransactionService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Creando transacción...");

            Map<String, Object> data = (Map<String, Object>) message.getData("transaction");
            if (data == null) {
                return Message.createErrorMessage(MessageTypes.CREATE, "Datos no proporcionados");
            }

            Transaction tx = TransactionUtils.fromMap(data);
            Transaction created = service.createTransaction(tx);

            Message response = Message.createSuccessMessage(MessageTypes.CREATE, "Transacción creada");
            response.addData("transaction", TransactionUtils.toMap(created));
            return response;
        };
    }

    public static MessageHandler getTransactionController(TransactionService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Obteniendo transacción por ID...");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(MessageTypes.GET, "ID inválido");
            }

            Transaction tx = service.getTransactionById(id);
            Message response = Message.createSuccessMessage(MessageTypes.GET, "Transacción obtenida");
            response.addData("transaction", TransactionUtils.toMap(tx));
            return response;
        };
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler updateTransactionController(TransactionService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Actualizando transacción...");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(MessageTypes.UPDATE, "ID inválido");
            }

            Map<String, Object> data = (Map<String, Object>) message.getData("transaction");
            if (data == null) {
                return Message.createErrorMessage(MessageTypes.UPDATE, "Datos no proporcionados");
            }

            Transaction updated = service.updateTransactionById(id, TransactionUtils.fromMap(data));

            Message response = Message.createSuccessMessage(MessageTypes.UPDATE, "Transacción actualizada");
            response.addData("transaction", TransactionUtils.toMap(updated));
            return response;
        };
    }

    public static MessageHandler deleteTransactionController(TransactionService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Eliminando transacción...");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(MessageTypes.DELETE, "ID inválido");
            }

            service.deleteTransactionById(id);
            return Message.createSuccessMessage(MessageTypes.DELETE, "Transacción eliminada");
        };
    }

    public static MessageHandler getAllTransactionsController(TransactionService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Obteniendo todas las transacciones...");

            List<Transaction> txList = service.getAllTransactions();
            List<Map<String, Object>> mapped = new ArrayList<>();
            for (Transaction tx : txList) {
                mapped.add(TransactionUtils.toMap(tx));
            }

            Message response = Message.createSuccessMessage(MessageTypes.GET_ALL, "Transacciones obtenidas");
            response.addData("transactions", mapped);
            response.addData("count", mapped.size());
            return response;
        };
    }

    private static Long parseId(Object raw) {
        if (raw instanceof Long l) return l;
        if (raw instanceof String s) {
            try { return Long.valueOf(s); } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
