package com.giozar04.bankClient.infrastructure.serialization;

import java.util.HashMap;
import java.util.Map;

import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.shared.infrastructure.serialization.ValueParser;

public class BankClientMapper {
    public static Map<String, Object> toMap(BankClient client) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", client.getId());
        map.put("userId", client.getUserId());
        map.put("bankName", client.getBankName());
        map.put("clientNumber", client.getClientNumber());

        if (client.getCreatedAt() != null)
            map.put("createdAt", client.getCreatedAt().format(ValueParser.getFormatter()));

        if (client.getUpdatedAt() != null)
            map.put("updatedAt", client.getUpdatedAt().format(ValueParser.getFormatter()));

        return map;
    }

    public static BankClient fromMap(Map<String, Object> map) {
        return new BankClient(
            ValueParser.parseLong(map.get("id")),
            ValueParser.parseLong(map.get("userId")),
            (String) map.getOrDefault("bankName", ""),
            (String) map.getOrDefault("clientNumber", ""),
            ValueParser.parseZonedDateTime(map.get("createdAt")),
            ValueParser.parseZonedDateTime(map.get("updatedAt"))
        );
    }
}
