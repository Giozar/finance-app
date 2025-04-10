package com.giozar04.bankClients.infrastructure.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.bankClient.application.utils.BankClientUtils;
import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.bankClient.domain.exceptions.BankClientExceptions;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.serverConnection.application.validators.ServerResponseValidator;
import com.giozar04.shared.utils.CustomLogger;

public class BankClientService {

    private final ServerConnectionService serverConnectionService;
    private static final CustomLogger logger = CustomLogger.getInstance();
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
        message.addData("bankClient", BankClientUtils.bankClientToMap(bankClient));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("CREATE_BANK_CLIENT");
            ServerResponseValidator.validateResponse(response);
            logger.info("Cliente creado exitosamente: " + response);
            return BankClientUtils.mapToBankClient((Map<String, Object>) response.getData("bankClient"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BankClientExceptions.BankClientCreationException("Error al esperar la respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public BankClient updateBankClientById(Long id, BankClient bankClient) throws ClientOperationException {
        Message message = new Message();
        message.setType("UPDATE_BANK_CLIENT");
        message.addData("id", id);
        message.addData("bankClient", BankClientUtils.bankClientToMap(bankClient));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("UPDATE_BANK_CLIENT");
            ServerResponseValidator.validateResponse(response);
            logger.info("Cliente actualizado correctamente: " + response);
            return BankClientUtils.mapToBankClient((Map<String, Object>) response.getData("bankClient"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BankClientExceptions.BankClientUpdateException("Error al esperar la respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public BankClient deleteBankClientById(Long id) throws ClientOperationException {
        Message message = new Message();
        message.setType("DELETE_BANK_CLIENT");
        message.addData("id", id);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("DELETE_BANK_CLIENT");
            ServerResponseValidator.validateResponse(response);
            logger.info("Cliente eliminado exitosamente: " + response);
            return BankClientUtils.mapToBankClient((Map<String, Object>) response.getData("bankClient"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BankClientExceptions.BankClientDeletionException("Error al esperar la respuesta del servidor", e);
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
            return BankClientUtils.mapToBankClient((Map<String, Object>) response.getData("bankClient"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BankClientExceptions.BankClientRetrievalException("Error al esperar la respuesta del servidor", e);
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
                throw new BankClientExceptions.BankClientRetrievalException("Respuesta vacía del servidor", null);
            }

            if (raw instanceof List<?> list) {
                List<BankClient> clients = new ArrayList<>();
                for (Object obj : list) {
                    if (obj instanceof Map<?, ?> map) {
                        clients.add(BankClientUtils.mapToBankClient((Map<String, Object>) map));
                    }
                }
                logger.info("Lista de clientes obtenida correctamente. Total: " + clients.size());
                return clients;
            } else {
                throw new BankClientExceptions.BankClientParsingException(
                    "Formato inesperado del servidor: " + raw.getClass().getName(), null
                );
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BankClientExceptions.BankClientRetrievalException("Error al esperar la respuesta del servidor", e);
        }
    }
}
