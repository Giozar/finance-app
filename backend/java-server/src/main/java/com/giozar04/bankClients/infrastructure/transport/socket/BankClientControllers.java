package com.giozar04.bankClients.infrastructure.transport.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.bankClient.infrastructure.serialization.BankClientMapper;
import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.bankClients.application.ports.input.BankClientOperations;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.messages.infrastructure.transport.Message;
import com.giozar04.servers.infrastructure.transport.socket.MessageHandler;
import com.giozar04.servers.infrastructure.transport.socket.ClientConnection;

public class BankClientControllers {

    private static final ConsoleLogger LOGGER = ConsoleLogger.getInstance();

    public static final class BankClientMessageTypes {
        public static final String CREATE_BANK_CLIENT = "CREATE_BANK_CLIENT";
        public static final String GET_BANK_CLIENT = "GET_BANK_CLIENT";
        public static final String GET_BANK_CLIENTS_BY_USER = "GET_BANK_CLIENTS_BY_USER";
        public static final String UPDATE_BANK_CLIENT = "UPDATE_BANK_CLIENT";
        public static final String DELETE_BANK_CLIENT = "DELETE_BANK_CLIENT";
        public static final String GET_ALL_BANK_CLIENTS = "GET_ALL_BANK_CLIENTS";
    }

    public static MessageHandler createBankClientController(BankClientOperations service) {
        return (ClientConnection conn, Message message) -> {
            LOGGER.info("Procesando creación de BankClient");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData("bankClient");
            BankClient client = BankClientMapper.fromMap(data);
            BankClient created = service.createBankClient(client);

            Message response = Message.createSuccessMessage(BankClientMessageTypes.CREATE_BANK_CLIENT,
                    "Cliente bancario creado exitosamente");
            response.addData("bankClient", BankClientMapper.toMap(created));
            return response;
        };
    }

    public static MessageHandler getBankClientController(BankClientOperations service) {
        return (ClientConnection conn, Message message) -> {
            Long id = parseId(message.getData("id"));
            BankClient client = service.getBankClientById(id);

            Message response = Message.createSuccessMessage(BankClientMessageTypes.GET_BANK_CLIENT,
                    "Cliente bancario obtenido");
            response.addData("bankClient", BankClientMapper.toMap(client));
            return response;
        };
    }

    public static MessageHandler getBankClientsByUserController(BankClientOperations service) {
        return (ClientConnection conn, Message message) -> {
            Long userId = parseId(message.getData("userId"));
            List<BankClient> clients = service.getBankClientsByUserId(userId);

            List<Map<String, Object>> mapped = new ArrayList<>();
            for (BankClient c : clients) {
                mapped.add(BankClientMapper.toMap(c));
            }

            Message response = Message.createSuccessMessage(BankClientMessageTypes.GET_BANK_CLIENTS_BY_USER,
                    "Clientes bancarios del usuario obtenidos");
            response.addData("bankClients", mapped);
            response.addData("count", mapped.size());
            return response;
        };
    }

    public static MessageHandler updateBankClientController(BankClientOperations service) {
        return (ClientConnection conn, Message message) -> {
            Long id = parseId(message.getData("id"));
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData("bankClient");
            BankClient updated = BankClientMapper.fromMap(data);
            BankClient result = service.updateBankClientById(id, updated);

            Message response = Message.createSuccessMessage(BankClientMessageTypes.UPDATE_BANK_CLIENT,
                    "Cliente bancario actualizado");
            response.addData("bankClient", BankClientMapper.toMap(result));
            return response;
        };
    }

    public static MessageHandler deleteBankClientController(BankClientOperations service) {
        return (ClientConnection conn, Message message) -> {
            Long id = parseId(message.getData("id"));
            service.deleteBankClientById(id);
            return Message.createSuccessMessage(BankClientMessageTypes.DELETE_BANK_CLIENT,
                    "Cliente bancario eliminado");
        };
    }

    public static MessageHandler getAllBankClientsController(BankClientOperations service) {
        return (ClientConnection conn, Message message) -> {
            List<BankClient> clients = service.getAllBankClients();
            List<Map<String, Object>> mapped = new ArrayList<>();

            for (BankClient c : clients) {
                mapped.add(BankClientMapper.toMap(c));
            }

            Message response = Message.createSuccessMessage(BankClientMessageTypes.GET_ALL_BANK_CLIENTS,
                    "Todos los clientes bancarios obtenidos");
            response.addData("bankClients", mapped);
            response.addData("count", mapped.size());
            return response;
        };
    }

    private static Long parseId(Object raw) {
        if (raw instanceof Number n) return n.longValue();
        if (raw instanceof String s) {
            try {
                return Long.valueOf(s);
            } catch (NumberFormatException ignored) {}
        }
        return 0L;
    }
}
