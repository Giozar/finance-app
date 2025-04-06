package com.giozar04.transactions.application.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.giozar04.shared.utils.SharedUtils;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.PaymentMethod;

public class TransactionUtils {

    public static Map<String, Object> transactionToMap(Transaction t) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", t.getId());
        map.put("type", t.getType());
        map.put("paymentMethod", t.getPaymentMethod() != null ? t.getPaymentMethod().name() : "CASH");
        map.put("amount", t.getAmount());
        map.put("title", t.getTitle());
        map.put("category", t.getCategory());
        map.put("description", t.getDescription());
        map.put("comments", t.getComments());

        if (t.getDate() != null) {
            map.put("date", t.getDate().format(SharedUtils.getFormatter()));
        }

        map.put("tags", t.getTags());
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Transaction mapToTransaction(Map<String, Object> map) {
        Transaction transaction = new Transaction();

        transaction.setId(SharedUtils.parseLong(map.get("id")));
        transaction.setType((String) map.getOrDefault("type", ""));

        Object method = map.get("paymentMethod");
        try {
            transaction.setPaymentMethod(PaymentMethod.valueOf(method.toString()));
        } catch (Exception e) {
            transaction.setPaymentMethod(PaymentMethod.CASH);
        }

        transaction.setAmount(SharedUtils.parseDouble(map.get("amount")));
        transaction.setTitle((String) map.getOrDefault("title", ""));
        transaction.setCategory((String) map.getOrDefault("category", ""));
        transaction.setDescription((String) map.getOrDefault("description", ""));
        transaction.setComments((String) map.getOrDefault("comments", ""));
        transaction.setDate(SharedUtils.parseZonedDateTime(map.get("date")));

        Object tagsObj = map.get("tags");
        if (tagsObj instanceof List) {
            transaction.setTags((List<String>) tagsObj);
        }

        return transaction;
    }
}
