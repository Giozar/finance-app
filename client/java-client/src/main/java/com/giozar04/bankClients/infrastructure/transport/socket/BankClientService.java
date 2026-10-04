package com.giozar04.bankClients.infrastructure.transport.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.bankClient.infrastructure.serialization.BankClientMapper;
import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.bankClient.application.exceptions.BankClientCreationException;
import com.giozar04.bankClient.application.exceptions.BankClientDeletionException;
import com.giozar04.bankClient.infrastructure.serialization.BankClientParsingException;
import com.giozar04.bankClient.application.exceptions.BankClientRetrievalException;
import com.giozar04.bankClient.application.exceptions.BankClientUpdateException;
import com.giozar04.bankClients.application.ports.output.BankClientGateway;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.messages.infrastructure.transport.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.infrastructure.transport.socket.ServerConnectionService;
import com.giozar04.serverConnection.infrastructure.transport.socket.ServerResponseValidator;


public class BankClientService implements BankClientGateway {

    private final ServerConnectionService serverConnectionService;
    private static final ConsoleLogger logger = ConsoleLogger.getInstance();
    private static BankClientService instance;

    private BankClientService(ServerConnectionService serverConnectionService) {
        this.serverConnectionService = serverConnectionService;
    }

    public static BankClientService connectService(ServerConnectionService serverConnectionService) {
        if (instance == null) {
            instance = new BankClientService(serverConnectionService);
        }
        return instance;
    }

    public static BankClientService getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public BankClient createBankClient(BankClient bankClient) throws ClientOperationException {
        Message message = new Message();
        message.setType("CREATE_BANK_CLIENT");
        message.addData("bankClient", BankClientMapper.toMap(bankClient));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("CREATE_BANK_CLIENT");
            ServerResponseValidator.validateResponse(response);
            logger.info("Cliente creado exitosamente: " + response);
            return BankClientMapper.fromMap((Map<String, Object>) response.getData("bankClient"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BankClientCreationException("Error al esperar la respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public BankClient updateBankClientById(Long id, BankClient bankClient) throws ClientOperationException {
        Message message = new Message();
        message.setType("UPDATE_BANK_CLIENT");
        message.addData("id", id);
        message.addData("bankClient", BankClientMapper.toMap(bankClient));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("UPDATE_BANK_CLIENT");
            ServerResponseValidator.validateResponse(response);
            logger.info("Cliente actualizado correctamente: " + response);
            return BankClientMapper.fromMap((Map<String, Object>) response.getData("bankClient"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BankClientUpdateException("Error al esperar la respuesta del servidor", e);
        }
    }

    public void deleteBankClientById(Long id) throws ClientOperationException {
        Message message = new Message();
        message.setType("DELETE_BANK_CLIENT");
        message.addData("id", id);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("DELETE_BANK_CLIENT");
            ServerResponseValidator.validateResponse(response);
            logger.info("Cliente eliminado exitosamente: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BankClientDeletionException("Error al esperar la respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public BankClient getBankClientById(Long id) throws ClientOperationException {
        Message message = new Message();
        message.setType("GET_BANK_CLIENT");
        message.addData("id", id);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_BANK_CLIENT");
            ServerResponseValidator.validateResponse(response);
            logger.info("Cliente obtenido correctamente: " + response);
            return BankClientMapper.fromMap((Map<String, Object>) response.getData("bankClient"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BankClientRetrievalException("Error al esperar la respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<BankClient> getAllBankClients() throws ClientOperationException {
        logger.info("Solicitando todos los clientes...");
        Message message = new Message();
        message.setType("GET_ALL_BANK_CLIENTS");

        serverConnectionService.sendMessage(message);

        try {
            Message response = serverConnectionService.waitForMessage("GET_ALL_BANK_CLIENTS");
            ServerResponseValidator.validateResponse(response);
            Object raw = response.getData("bankClients");

            if (raw == null) {
                throw new BankClientRetrievalException("Respuesta vacía del servidor", null);
            }

            if (raw instanceof List<?> list) {
                List<BankClient> clients = new ArrayList<>();
                for (Object obj : list) {
                    if (obj instanceof Map<?, ?> map) {
                        clients.add(BankClientMapper.fromMap((Map<String, Object>) map));
                    }
                }
                logger.info("Lista de clientes obtenida correctamente. Total: " + clients.size());
                return clients;
            } else {
                throw new BankClientParsingException(
                    "Formato inesperado del servidor: " + raw.getClass().getName(), null
                );
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BankClientRetrievalException("Error al esperar la respuesta del servidor", e);
        }
    }
}
