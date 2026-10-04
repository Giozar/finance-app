package com.giozar04.walletCardLinks.infrastructure.transport.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.messages.infrastructure.transport.Message;
import com.giozar04.servers.domain.handlers.MessageHandler;
import com.giozar04.servers.domain.models.ClientConnection;
import com.giozar04.walletCardLinks.application.ports.input.WalletCardLinkOperations;
import com.giozar04.walletCardLinks.infrastructure.serialization.WalletCardLinkMapper;
import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;

public class WalletCardLinkControllers {

    private static final ConsoleLogger LOGGER = ConsoleLogger.getInstance();

    public static final class WalletCardLinkMessageTypes {
        public static final String CREATE_LINK = "CREATE_WALLET_CARD_LINK";
        public static final String GET_LINK = "GET_WALLET_CARD_LINK";
        public static final String UPDATE_LINK = "UPDATE_WALLET_CARD_LINK";
        public static final String DELETE_LINK = "DELETE_WALLET_CARD_LINK";
        public static final String GET_ALL_LINKS = "GET_ALL_WALLET_CARD_LINKS";
        public static final String GET_LINKS_BY_WALLET = "GET_LINKS_BY_WALLET_ACCOUNT_ID";
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler createLinkController(WalletCardLinkOperations service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Creando vínculo wallet-tarjeta");

            Map<String, Object> data = (Map<String, Object>) message.getData("walletCardLink");
            if (data == null) {
                return Message.createErrorMessage(WalletCardLinkMessageTypes.CREATE_LINK, "Datos no proporcionados");
            }

            WalletCardLink link = WalletCardLinkMapper.fromMap(data);
            WalletCardLink created = service.createLink(link);

            Message response = Message.createSuccessMessage(WalletCardLinkMessageTypes.CREATE_LINK, "Vínculo creado");
            response.addData("walletCardLink", WalletCardLinkMapper.toMap(created));
            return response;
        };
    }

    public static MessageHandler getLinkController(WalletCardLinkOperations service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Obteniendo vínculo por ID");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(WalletCardLinkMessageTypes.GET_LINK, "ID inválido");
            }

            WalletCardLink link = service.getLinkById(id);
            Message response = Message.createSuccessMessage(WalletCardLinkMessageTypes.GET_LINK, "Vínculo obtenido");
            response.addData("walletCardLink", WalletCardLinkMapper.toMap(link));
            return response;
        };
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler updateLinkController(WalletCardLinkOperations service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Actualizando vínculo");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(WalletCardLinkMessageTypes.UPDATE_LINK, "ID inválido");
            }

            Map<String, Object> data = (Map<String, Object>) message.getData("walletCardLink");
            if (data == null) {
                return Message.createErrorMessage(WalletCardLinkMessageTypes.UPDATE_LINK, "Datos no proporcionados");
            }

            WalletCardLink updated = service.updateLinkById(id, WalletCardLinkMapper.fromMap(data));

            Message response = Message.createSuccessMessage(WalletCardLinkMessageTypes.UPDATE_LINK, "Vínculo actualizado");
            response.addData("walletCardLink", WalletCardLinkMapper.toMap(updated));
            return response;
        };
    }

    public static MessageHandler deleteLinkController(WalletCardLinkOperations service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Eliminando vínculo wallet-tarjeta");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(WalletCardLinkMessageTypes.DELETE_LINK, "ID inválido");
            }

            service.deleteLinkById(id);
            return Message.createSuccessMessage(WalletCardLinkMessageTypes.DELETE_LINK, "Vínculo eliminado");
        };
    }

    public static MessageHandler getAllLinksController(WalletCardLinkOperations service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Obteniendo todos los vínculos");

            List<WalletCardLink> links = service.getAllLinks();
            List<Map<String, Object>> result = new ArrayList<>();

            for (WalletCardLink link : links) {
                result.add(WalletCardLinkMapper.toMap(link));
            }

            Message response = Message.createSuccessMessage(WalletCardLinkMessageTypes.GET_ALL_LINKS, "Vínculos obtenidos");
            response.addData("walletCardLinks", result);
            response.addData("count", result.size());

            return response;
        };
    }

    public static MessageHandler getLinksByWalletController(WalletCardLinkOperations service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Obteniendo vínculos por walletAccountId");

            Long walletId = parseId(message.getData("walletAccountId"));
            if (walletId == null) {
                return Message.createErrorMessage(WalletCardLinkMessageTypes.GET_LINKS_BY_WALLET, "walletAccountId inválido");
            }

            List<WalletCardLink> links = service.getLinksByWalletAccountId(walletId);
            List<Map<String, Object>> result = new ArrayList<>();

            for (WalletCardLink link : links) {
                result.add(WalletCardLinkMapper.toMap(link));
            }

            Message response = Message.createSuccessMessage(WalletCardLinkMessageTypes.GET_LINKS_BY_WALLET, "Vínculos obtenidos");
            response.addData("walletCardLinks", result);
            response.addData("count", result.size());

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
