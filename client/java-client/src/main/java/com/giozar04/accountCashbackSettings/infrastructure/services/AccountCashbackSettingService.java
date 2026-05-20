package com.giozar04.accountCashbackSettings.infrastructure.services;

import java.util.Map;

import com.giozar04.accountCashbackSettings.application.utils.AccountCashbackSettingUtils;
import com.giozar04.accountCashbackSettings.domain.entities.AccountCashbackSetting;
import com.giozar04.logging.CustomLogger;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.serverConnection.application.validators.ServerResponseValidator;

public class AccountCashbackSettingService {

    private final ServerConnectionService serverConnectionService;
    private static final CustomLogger logger = CustomLogger.getInstance();
    private static AccountCashbackSettingService instance;

    private AccountCashbackSettingService(ServerConnectionService serverConnectionService) {
        this.serverConnectionService = serverConnectionService;
    }

    public static AccountCashbackSettingService connectService(ServerConnectionService serverConnectionService) {
        if (instance == null) {
            instance = new AccountCashbackSettingService(serverConnectionService);
        }
        return instance;
    }

    public static AccountCashbackSettingService getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public AccountCashbackSetting createAccountCashbackSetting(AccountCashbackSetting setting) throws ClientOperationException {
        Message message = new Message();
        message.setType("CREATE_ACCOUNT_CASHBACK_SETTING");
        message.addData("accountCashbackSetting", AccountCashbackSettingUtils.toMap(setting));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("CREATE_ACCOUNT_CASHBACK_SETTING");
            ServerResponseValidator.validateResponse(response);
            logger.info("Configuración de cashback creada exitosamente: " + response);
            return AccountCashbackSettingUtils.fromMap((Map<String, Object>) response.getData("accountCashbackSetting"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ClientOperationException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public AccountCashbackSetting getAccountCashbackSettingByAccountId(Long accountId) throws ClientOperationException {
        Message message = new Message();
        message.setType("GET_ACCOUNT_CASHBACK_SETTING");
        message.addData("accountId", accountId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_ACCOUNT_CASHBACK_SETTING");
            ServerResponseValidator.validateResponse(response);
            logger.info("Configuración de cashback obtenida correctamente: " + response);
            return AccountCashbackSettingUtils.fromMap((Map<String, Object>) response.getData("accountCashbackSetting"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ClientOperationException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public AccountCashbackSetting updateAccountCashbackSettingByAccountId(Long accountId, AccountCashbackSetting setting) throws ClientOperationException {
        Message message = new Message();
        message.setType("UPDATE_ACCOUNT_CASHBACK_SETTING");
        message.addData("accountId", accountId);
        message.addData("accountCashbackSetting", AccountCashbackSettingUtils.toMap(setting));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("UPDATE_ACCOUNT_CASHBACK_SETTING");
            ServerResponseValidator.validateResponse(response);
            logger.info("Configuración de cashback actualizada correctamente: " + response);
            return AccountCashbackSettingUtils.fromMap((Map<String, Object>) response.getData("accountCashbackSetting"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ClientOperationException("Error al esperar respuesta del servidor", e);
        }
    }

    public void deleteAccountCashbackSettingByAccountId(Long accountId) throws ClientOperationException {
        Message message = new Message();
        message.setType("DELETE_ACCOUNT_CASHBACK_SETTING");
        message.addData("accountId", accountId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("DELETE_ACCOUNT_CASHBACK_SETTING");
            ServerResponseValidator.validateResponse(response);
            logger.info("Configuración de cashback eliminada exitosamente: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ClientOperationException("Error al esperar respuesta del servidor", e);
        }
    }
}
