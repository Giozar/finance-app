package com.giozar04.accountReconciliations.infrastructure.transport.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.accountReconciliations.application.ports.input.AccountReconciliationOperations;
import com.giozar04.accountReconciliations.infrastructure.serialization.AccountReconciliationMapper;
import com.giozar04.accountReconciliations.domain.entities.AccountReconciliation;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.messages.infrastructure.transport.Message;
import com.giozar04.servers.domain.handlers.MessageHandler;
import com.giozar04.servers.domain.models.ClientConnection;

public class AccountReconciliationControllers {

    private static final ConsoleLogger LOGGER = ConsoleLogger.getInstance();

    public static final class AccountReconciliationMessageTypes {
        public static final String GET_ALL_ACCOUNT_RECONCILIATIONS = "GET_ALL_ACCOUNT_RECONCILIATIONS";
        public static final String GET_ACCOUNT_RECONCILIATIONS_BY_USER = "GET_ACCOUNT_RECONCILIATIONS_BY_USER";
        public static final String GET_ACCOUNT_RECONCILIATION = "GET_ACCOUNT_RECONCILIATION";
        public static final String RECONCILE_ACCOUNT = "RECONCILE_ACCOUNT";
    }

    public static MessageHandler getAllAccountReconciliationsController(AccountReconciliationOperations service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando obtención de todas las reconciliaciones de cuentas");

            List<Map<String, Object>> result = toMapList(service.getAllAccountReconciliations());

            Message response = Message.createSuccessMessage(
                AccountReconciliationMessageTypes.GET_ALL_ACCOUNT_RECONCILIATIONS, "Reconciliaciones obtenidas exitosamente");
            response.addData("accountReconciliations", result);
            response.addData("count", result.size());
            return response;
        };
    }

    public static MessageHandler getAccountReconciliationsByUserController(AccountReconciliationOperations service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando obtención de reconciliaciones por usuario");

            Long userId = parseId(message.getData("userId"));
            if (userId == null) {
                return Message.createErrorMessage(AccountReconciliationMessageTypes.GET_ACCOUNT_RECONCILIATIONS_BY_USER, "ID de usuario inválido");
            }

            List<Map<String, Object>> result = toMapList(service.getAccountReconciliationsByUserId(userId));

            Message response = Message.createSuccessMessage(
                AccountReconciliationMessageTypes.GET_ACCOUNT_RECONCILIATIONS_BY_USER, "Reconciliaciones del usuario obtenidas exitosamente");
            response.addData("accountReconciliations", result);
            response.addData("count", result.size());
            return response;
        };
    }

    public static MessageHandler getAccountReconciliationController(AccountReconciliationOperations service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando obtención de reconciliación de cuenta");

            Long accountId = parseId(message.getData("accountId"));
            if (accountId == null) {
                return Message.createErrorMessage(AccountReconciliationMessageTypes.GET_ACCOUNT_RECONCILIATION, "ID de cuenta inválido");
            }

            AccountReconciliation reconciliation = service.getAccountReconciliationByAccountId(accountId);

            Message response = Message.createSuccessMessage(
                AccountReconciliationMessageTypes.GET_ACCOUNT_RECONCILIATION, "Reconciliación obtenida exitosamente");
            response.addData("accountReconciliation", AccountReconciliationMapper.toMap(reconciliation));
            return response;
        };
    }

    public static MessageHandler reconcileAccountController(AccountReconciliationOperations service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando reconciliación de cuenta");

            Long accountId = parseId(message.getData("accountId"));
            if (accountId == null) {
                return Message.createErrorMessage(AccountReconciliationMessageTypes.RECONCILE_ACCOUNT, "ID de cuenta inválido");
            }

            AccountReconciliation reconciliation = service.reconcileAccount(accountId);

            Message response = Message.createSuccessMessage(
                AccountReconciliationMessageTypes.RECONCILE_ACCOUNT, "Cuenta reconciliada exitosamente");
            response.addData("accountReconciliation", AccountReconciliationMapper.toMap(reconciliation));
            return response;
        };
    }

    private static List<Map<String, Object>> toMapList(List<AccountReconciliation> reconciliations) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (AccountReconciliation r : reconciliations) {
            result.add(AccountReconciliationMapper.toMap(r));
        }
        return result;
    }

    private static Long parseId(Object rawId) {
        if (rawId instanceof Long l) return l;
        if (rawId instanceof Number n) return n.longValue();
        if (rawId instanceof String s) {
            try {
                return Long.valueOf(s);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
