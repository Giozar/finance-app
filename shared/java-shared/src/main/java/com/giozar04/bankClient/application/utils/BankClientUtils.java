package com.giozar04.bankClient.application.utils;

import java.util.HashMap;
import java.util.Map;

import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.shared.utils.SharedUtils;

public class BankClientUtils {
    public static Map<String, Object> bankClientToMap(BankClient client) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", client.getId());
        map.put("userId", client.getUserId());
        map.put("bankName", client.getBankName());
        map.put("clientNumber", client.getClientNumber());

        if (client.getCreatedAt() != null)
            map.put("createdAt", client.getCreatedAt().format(SharedUtils.getFormatter()));

        if (client.getUpdatedAt() != null)
            map.put("updatedAt", client.getUpdatedAt().format(SharedUtils.getFormatter()));

        return map;
    }

    public static BankClient mapToBankClient(Map<String, Object> map) {
        return new BankClient(
            SharedUtils.parseLong(map.get("id")),
            SharedUtils.parseLong(map.get("userId")),
            (String) map.getOrDefault("bankName", ""),
            (String) map.getOrDefault("clientNumber", ""),
            SharedUtils.parseZonedDateTime(map.get("createdAt")),
            SharedUtils.parseZonedDateTime(map.get("updatedAt"))
        );
    }
}
