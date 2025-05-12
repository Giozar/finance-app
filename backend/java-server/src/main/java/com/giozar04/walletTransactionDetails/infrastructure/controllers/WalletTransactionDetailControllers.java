package com.giozar04.walletTransactionDetails.infrastructure.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.logging.CustomLogger;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.servers.domain.handlers.MessageHandler;
import com.giozar04.servers.domain.models.ClientConnection;
import com.giozar04.walletTransactionDetails.application.services.WalletTransactionDetailService;
import com.giozar04.walletTransactionDetails.application.utils.WalletTransactionDetailUtils;
import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;

public class WalletTransactionDetailControllers {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    public static final class MessageTypes {
        public static final String CREATE_DETAIL = "CREATE_WALLET_TRANSACTION_DETAIL";
        public static final String GET_DETAIL = "GET_WALLET_TRANSACTION_DETAIL";
        public static final String UPDATE_DETAIL = "UPDATE_WALLET_TRANSACTION_DETAIL";
        public static final String DELETE_DETAIL = "DELETE_WALLET_TRANSACTION_DETAIL";
        public static final String GET_ALL_DETAILS = "GET_ALL_WALLET_TRANSACTION_DETAILS";
        public static final String GET_DETAILS_BY_TRANSACTION = "GET_DETAILS_BY_TRANSACTION_ID";
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler createDetailController(WalletTransactionDetailService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Creando detalle de transacción wallet");

            Map<String, Object> data = (Map<String, Object>) message.getData("walletTransactionDetail");
            if (data == null) {
                return Message.createErrorMessage(MessageTypes.CREATE_DETAIL, "Datos no proporcionados");
            }

            WalletTransactionDetail detail = WalletTransactionDetailUtils.fromMap(data);
            WalletTransactionDetail created = service.createDetail(detail);

            Message response = Message.createSuccessMessage(MessageTypes.CREATE_DETAIL, "Detalle creado");
            response.addData("walletTransactionDetail", WalletTransactionDetailUtils.toMap(created));
            return response;
        };
    }

    public static MessageHandler getDetailController(WalletTransactionDetailService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Obteniendo detalle por ID");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(MessageTypes.GET_DETAIL, "ID inválido");
            }

            WalletTransactionDetail detail = service.getDetailById(id);
            Message response = Message.createSuccessMessage(MessageTypes.GET_DETAIL, "Detalle obtenido");
            response.addData("walletTransactionDetail", WalletTransactionDetailUtils.toMap(detail));
            return response;
        };
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler updateDetailController(WalletTransactionDetailService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Actualizando detalle");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(MessageTypes.UPDATE_DETAIL, "ID inválido");
            }

            Map<String, Object> data = (Map<String, Object>) message.getData("walletTransactionDetail");
            if (data == null) {
                return Message.createErrorMessage(MessageTypes.UPDATE_DETAIL, "Datos no proporcionados");
            }

            WalletTransactionDetail updated = service.updateDetailById(id, WalletTransactionDetailUtils.fromMap(data));

            Message response = Message.createSuccessMessage(MessageTypes.UPDATE_DETAIL, "Detalle actualizado");
            response.addData("walletTransactionDetail", WalletTransactionDetailUtils.toMap(updated));
            return response;
        };
    }

    public static MessageHandler deleteDetailController(WalletTransactionDetailService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Eliminando detalle");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(MessageTypes.DELETE_DETAIL, "ID inválido");
            }

            service.deleteDetailById(id);
            return Message.createSuccessMessage(MessageTypes.DELETE_DETAIL, "Detalle eliminado");
        };
    }

    public static MessageHandler getAllDetailsController(WalletTransactionDetailService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Obteniendo todos los detalles");

            List<WalletTransactionDetail> list = service.getAllDetails();
            List<Map<String, Object>> mapped = new ArrayList<>();
            for (WalletTransactionDetail d : list) {
                mapped.add(WalletTransactionDetailUtils.toMap(d));
            }

            Message response = Message.createSuccessMessage(MessageTypes.GET_ALL_DETAILS, "Detalles obtenidos");
            response.addData("walletTransactionDetails", mapped);
            response.addData("count", mapped.size());

            return response;
        };
    }

    public static MessageHandler getDetailsByTransactionController(WalletTransactionDetailService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Obteniendo detalles por ID de transacción");

            Long txId = parseId(message.getData("transactionId"));
            if (txId == null) {
                return Message.createErrorMessage(MessageTypes.GET_DETAILS_BY_TRANSACTION, "transactionId inválido");
            }

            List<WalletTransactionDetail> list = service.getDetailsByTransactionId(txId);
            List<Map<String, Object>> mapped = new ArrayList<>();
            for (WalletTransactionDetail d : list) {
                mapped.add(WalletTransactionDetailUtils.toMap(d));
            }

            Message response = Message.createSuccessMessage(MessageTypes.GET_DETAILS_BY_TRANSACTION, "Detalles obtenidos");
            response.addData("walletTransactionDetails", mapped);
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
