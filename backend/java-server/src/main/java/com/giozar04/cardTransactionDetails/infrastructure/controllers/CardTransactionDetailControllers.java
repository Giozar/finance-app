package com.giozar04.cardTransactionDetails.infrastructure.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.cardTransactionDetails.application.services.CardTransactionDetailService;
import com.giozar04.cardTransactionDetails.application.utils.CardTransactionDetailUtils;
import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;
import com.giozar04.logging.CustomLogger;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.servers.domain.handlers.MessageHandler;
import com.giozar04.servers.domain.models.ClientConnection;

public class CardTransactionDetailControllers {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    public static final class MessageTypes {
        public static final String CREATE_DETAIL = "CREATE_CARD_TRANSACTION_DETAIL";
        public static final String GET_DETAIL = "GET_CARD_TRANSACTION_DETAIL";
        public static final String UPDATE_DETAIL = "UPDATE_CARD_TRANSACTION_DETAIL";
        public static final String DELETE_DETAIL = "DELETE_CARD_TRANSACTION_DETAIL";
        public static final String GET_ALL_DETAILS = "GET_ALL_CARD_TRANSACTION_DETAILS";
        public static final String GET_DETAILS_BY_TRANSACTION = "GET_CARD_TRANSACTION_DETAILS_BY_TRANSACTION_ID";
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler createDetailController(CardTransactionDetailService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Creando detalle de transacción con tarjeta");

            Map<String, Object> data = (Map<String, Object>) message.getData("cardTransactionDetail");
            if (data == null) {
                return Message.createErrorMessage(MessageTypes.CREATE_DETAIL, "Datos no proporcionados");
            }

            CardTransactionDetail detail = CardTransactionDetailUtils.fromMap(data);
            CardTransactionDetail created = service.createDetail(detail);

            Message response = Message.createSuccessMessage(MessageTypes.CREATE_DETAIL, "Detalle creado");
            response.addData("cardTransactionDetail", CardTransactionDetailUtils.toMap(created));
            return response;
        };
    }

    public static MessageHandler getDetailController(CardTransactionDetailService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Consultando detalle de transacción con tarjeta");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(MessageTypes.GET_DETAIL, "ID inválido");
            }

            CardTransactionDetail detail = service.getDetailById(id);
            Message response = Message.createSuccessMessage(MessageTypes.GET_DETAIL, "Detalle obtenido");
            response.addData("cardTransactionDetail", CardTransactionDetailUtils.toMap(detail));
            return response;
        };
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler updateDetailController(CardTransactionDetailService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Actualizando detalle");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(MessageTypes.UPDATE_DETAIL, "ID inválido");
            }

            Map<String, Object> data = (Map<String, Object>) message.getData("cardTransactionDetail");
            if (data == null) {
                return Message.createErrorMessage(MessageTypes.UPDATE_DETAIL, "Datos no proporcionados");
            }

            CardTransactionDetail updated = service.updateDetailById(id, CardTransactionDetailUtils.fromMap(data));

            Message response = Message.createSuccessMessage(MessageTypes.UPDATE_DETAIL, "Detalle actualizado");
            response.addData("cardTransactionDetail", CardTransactionDetailUtils.toMap(updated));
            return response;
        };
    }

    public static MessageHandler deleteDetailController(CardTransactionDetailService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Eliminando detalle de transacción con tarjeta");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(MessageTypes.DELETE_DETAIL, "ID inválido");
            }

            service.deleteDetailById(id);
            return Message.createSuccessMessage(MessageTypes.DELETE_DETAIL, "Detalle eliminado");
        };
    }

    public static MessageHandler getAllDetailsController(CardTransactionDetailService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Obteniendo todos los detalles de transacciones con tarjeta");

            List<CardTransactionDetail> list = service.getAllDetails();
            List<Map<String, Object>> mapped = new ArrayList<>();
            for (CardTransactionDetail detail : list) {
                mapped.add(CardTransactionDetailUtils.toMap(detail));
            }

            Message response = Message.createSuccessMessage(MessageTypes.GET_ALL_DETAILS, "Detalles obtenidos");
            response.addData("cardTransactionDetails", mapped);
            response.addData("count", mapped.size());
            return response;
        };
    }

    public static MessageHandler getDetailsByTransactionController(CardTransactionDetailService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Obteniendo detalles por ID de transacción");

            Long txId = parseId(message.getData("transactionId"));
            if (txId == null) {
                return Message.createErrorMessage(MessageTypes.GET_DETAILS_BY_TRANSACTION, "transactionId inválido");
            }

            List<CardTransactionDetail> list = service.getDetailsByTransactionId(txId);
            List<Map<String, Object>> mapped = new ArrayList<>();
            for (CardTransactionDetail d : list) {
                mapped.add(CardTransactionDetailUtils.toMap(d));
            }

            Message response = Message.createSuccessMessage(MessageTypes.GET_DETAILS_BY_TRANSACTION, "Detalles obtenidos");
            response.addData("cardTransactionDetails", mapped);
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
