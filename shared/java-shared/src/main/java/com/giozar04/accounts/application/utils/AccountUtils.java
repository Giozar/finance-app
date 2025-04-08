package com.giozar04.accounts.application.utils;

import java.util.HashMap;
import java.util.Map;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.shared.utils.SharedUtils;

public class AccountUtils {

    public static Map<String, Object> accountToMap(Account account) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", account.getId());
        map.put("userId", account.getUserId());
        map.put("bankClientId", account.getBankClientId());
        map.put("name", account.getName());
        map.put("type", account.getType());
        map.put("currentBalance", account.getCurrentBalance());
        map.put("accountNumber", account.getAccountNumber());
        map.put("clabe", account.getClabe());
        map.put("creditLimit", account.getCreditLimit());
        map.put("cutoffDay", account.getCutoffDay());
        map.put("paymentDay", account.getPaymentDay());

        if (account.getCreatedAt() != null) {
            map.put("createdAt", account.getCreatedAt().format(SharedUtils.getFormatter()));
        }
        if (account.getUpdatedAt() != null) {
            map.put("updatedAt", account.getUpdatedAt().format(SharedUtils.getFormatter()));
        }

        return map;
    }

    public static Account mapToAccount(Map<String, Object> map) {
        Account account = new Account();

        account.setId(SharedUtils.parseLong(map.get("id")));
        account.setUserId(SharedUtils.parseLong(map.get("userId")));
        account.setBankClientId(SharedUtils.parseNullableLong(map.get("bankClientId")));
        account.setName((String) map.getOrDefault("name", ""));
        account.setType((String) map.getOrDefault("type", ""));
        account.setCurrentBalance(SharedUtils.parseDouble(map.get("currentBalance")));
        account.setAccountNumber((String) map.get("accountNumber"));
        account.setClabe((String) map.get("clabe"));
        account.setCreditLimit(SharedUtils.parseNullableDouble(map.get("creditLimit")));
        account.setCutoffDay(SharedUtils.parseNullableInt(map.get("cutoffDay")));
        account.setPaymentDay(SharedUtils.parseNullableInt(map.get("paymentDay")));
        account.setCreatedAt(SharedUtils.parseZonedDateTime(map.get("createdAt")));
        account.setUpdatedAt(SharedUtils.parseZonedDateTime(map.get("updatedAt")));

        return account;
    }
}
