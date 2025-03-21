package com.giozar04.transactions.application.utils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.PaymentMethod;

public class TransactionUtils {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

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
            map.put("date", t.getDate().format(FORMATTER));
        }
        map.put("tags", t.getTags());
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Transaction mapToTransaction(Map<String, Object> map) {
        Transaction transaction = new Transaction();
        if (map.containsKey("id")) {
            Object idObj = map.get("id");
            long idVal = 0;
            if (idObj instanceof Number) {
                idVal = ((Number) idObj).longValue();
            } else if (idObj instanceof String) {
                try {
                    idVal = Long.parseLong((String) idObj);
                } catch (NumberFormatException e) {
                    // ignoring
                }
            }
            transaction.setId(idVal);
        }
        if (map.containsKey("type")) {
            transaction.setType((String) map.get("type"));
        }
        if (map.containsKey("paymentMethod")) {
            try {
                transaction.setPaymentMethod(PaymentMethod.valueOf((String) map.get("paymentMethod")));
            } catch (Exception e) {
                transaction.setPaymentMethod(PaymentMethod.CASH);
            }
        }
        if (map.containsKey("amount")) {
            Object amountObj = map.get("amount");
            double amt = 0.0;
            if (amountObj instanceof Number) {
                amt = ((Number) amountObj).doubleValue();
            } else if (amountObj instanceof String) {
                try {
                    amt = Double.parseDouble((String) amountObj);
                } catch (NumberFormatException e) {
                    // ignoring, se queda en 0
                }
            }
            transaction.setAmount(amt);
        }
        if (map.containsKey("title")) {
            transaction.setTitle((String) map.get("title"));
        }
        if (map.containsKey("category")) {
            transaction.setCategory((String) map.get("category"));
        }
        if (map.containsKey("description")) {
            transaction.setDescription((String) map.get("description"));
        }
        if (map.containsKey("comments")) {
            transaction.setComments((String) map.get("comments"));
        }
        if (map.containsKey("date")) {
            try {
                transaction.setDate(ZonedDateTime.parse((String) map.get("date"), FORMATTER));
            } catch (Exception e) {
                // ignoring parse error
            }
        }
        if (map.containsKey("tags")) {
            Object tagsObj = map.get("tags");
            if (tagsObj instanceof java.util.List) {
                transaction.setTags((java.util.List<String>) tagsObj);
            }
        }
        return transaction;
    }

    public static DateTimeFormatter getFormatter() {
        return FORMATTER;
    }
}
