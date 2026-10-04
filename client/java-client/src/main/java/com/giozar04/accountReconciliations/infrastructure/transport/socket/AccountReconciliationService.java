package com.giozar04.accountReconciliations.infrastructure.transport.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.accountReconciliations.infrastructure.serialization.AccountReconciliationMapper;
import com.giozar04.accountReconciliations.domain.entities.AccountReconciliation;
import com.giozar04.accountReconciliations.application.exceptions.AccountReconciliationAdjustmentException;
import com.giozar04.accountReconciliations.application.exceptions.AccountReconciliationRetrievalException;
import com.giozar04.accountReconciliations.application.ports.output.AccountReconciliationGateway;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.messages.infrastructure.transport.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.serverConnection.application.validators.ServerResponseValidator;

public class AccountReconciliationService implements AccountReconciliationGateway {

    private final ServerConnectionService serverConnectionService;
    private static final ConsoleLogger logger = ConsoleLogger.getInstance();
    private static AccountReconciliationService instance;

    private AccountReconciliationService(ServerConnectionService serverConnectionService) {
        this.serverConnectionService = serverConnectionService;
    }

    public static AccountReconciliationService connectService(ServerConnectionService serverConnectionService) {
        if (instance == null) {
            instance = new AccountReconciliationService(serverConnectionService);
        }
        return instance;
    }

    public static AccountReconciliationService getInstance() {
        return instance;
    }

    public List<AccountReconciliation> getAllAccountReconciliations() throws ClientOperationException {
        logger.info("Solicitando la conciliación de todas las cuentas...");
        Message message = new Message();
        message.setType("GET_ALL_ACCOUNT_RECONCILIATIONS");

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_ALL_ACCOUNT_RECONCILIATIONS");
            ServerResponseValidator.validateResponse(response);
            List<AccountReconciliation> reconciliations = toReconciliationList(response.getData("accountReconciliations"));
            logger.info("Conciliaciones obtenidas correctamente. Total: " + reconciliations.size());
            return reconciliations;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AccountReconciliationRetrievalException("Error al esperar respuesta del servidor", e);
        }
    }

    public List<AccountReconciliation> getAccountReconciliationsByUserId(long userId) throws ClientOperationException {
        logger.info("Solicitando la conciliación de cuentas del usuario " + userId + "...");
        Message message = new Message();
        message.setType("GET_ACCOUNT_RECONCILIATIONS_BY_USER");
        message.addData("userId", userId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_ACCOUNT_RECONCILIATIONS_BY_USER");
            ServerResponseValidator.validateResponse(response);
            List<AccountReconciliation> reconciliations = toReconciliationList(response.getData("accountReconciliations"));
            logger.info("Conciliaciones del usuario obtenidas correctamente. Total: " + reconciliations.size());
            return reconciliations;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AccountReconciliationRetrievalException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public AccountReconciliation getAccountReconciliationByAccountId(long accountId) throws ClientOperationException {
        Message message = new Message();
        message.setType("GET_ACCOUNT_RECONCILIATION");
        message.addData("accountId", accountId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_ACCOUNT_RECONCILIATION");
            ServerResponseValidator.validateResponse(response);
            logger.info("Conciliación de la cuenta obtenida correctamente: " + response);
            return AccountReconciliationMapper.fromMap(
                    (Map<String, Object>) response.getData("accountReconciliation"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AccountReconciliationRetrievalException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public AccountReconciliation reconcileAccount(long accountId) throws ClientOperationException {
        Message message = new Message();
        message.setType("RECONCILE_ACCOUNT");
        message.addData("accountId", accountId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("RECONCILE_ACCOUNT");
            ServerResponseValidator.validateResponse(response);
            logger.info("Cuenta conciliada correctamente: " + response);
            return AccountReconciliationMapper.fromMap(
                    (Map<String, Object>) response.getData("accountReconciliation"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AccountReconciliationAdjustmentException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<AccountReconciliation> toReconciliationList(Object raw) {
        if (raw == null) {
            throw new AccountReconciliationRetrievalException("Lista de conciliaciones vacía", null);
        }
        if (!(raw instanceof List<?> rawList)) {
            throw new AccountReconciliationRetrievalException("Formato inesperado: " + raw.getClass().getName(), null);
        }
        List<AccountReconciliation> reconciliations = new ArrayList<>();
        for (Object item : rawList) {
            if (item instanceof Map<?, ?> map) {
                reconciliations.add(AccountReconciliationMapper.fromMap((Map<String, Object>) map));
            }
        }
        return reconciliations;
    }
}
