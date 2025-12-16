package com.giozar04.transactions.application.utils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.giozar04.shared.utils.SharedUtils;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.domain.enums.PaymentMethod;

public class TransactionUtils {

    public static Map<String, Object> toMap(Transaction tx) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", tx.getId());
        map.put("operationType", tx.getOperationType().getValue());
        map.put("paymentMethod", tx.getPaymentMethod().getValue());
        map.put("sourceAccountId", tx.getSourceAccountId());
        map.put("destinationAccountId", tx.getDestinationAccountId());
        map.put("externalEntityId", tx.getExternalEntityId());
        map.put("amount", tx.getAmount());
        map.put("concept", tx.getConcept());
        map.put("category", tx.getCategory());
        map.put("description", tx.getDescription());
        map.put("comments", tx.getComments());
        map.put("date", tx.getDate().format(SharedUtils.getFormatter()));
        map.put("timezone", tx.getTimezone());
        map.put("tags", tx.getTags());

        if (tx.getCreatedAt() != null)
            map.put("createdAt", tx.getCreatedAt().format(SharedUtils.getFormatter()));
        if (tx.getUpdatedAt() != null)
            map.put("updatedAt", tx.getUpdatedAt().format(SharedUtils.getFormatter()));

        return map;
    }

    public static Transaction fromMap(Map<String, Object> map) {
        Transaction tx = new Transaction();
        tx.setId(SharedUtils.parseLong(map.get("id")));
        tx.setOperationType(OperationTypes.fromValue((String) map.get("operationType")));
        tx.setPaymentMethod(PaymentMethod.fromValue((String) map.get("paymentMethod")));
        tx.setSourceAccountId(SharedUtils.parseNullableLong(map.get("sourceAccountId")));
        tx.setDestinationAccountId(SharedUtils.parseNullableLong(map.get("destinationAccountId")));
        tx.setExternalEntityId(SharedUtils.parseNullableLong(map.get("externalEntityId")));
        tx.setAmount(new BigDecimal(map.get("amount").toString()));
        tx.setConcept((String) map.get("concept"));
        tx.setCategory((String) map.get("category"));
        tx.setDescription((String) map.get("description"));
        tx.setComments((String) map.get("comments"));
        tx.setDate(SharedUtils.parseZonedDateTime(map.get("date")));
        tx.setTimezone((String) map.get("timezone"));
        tx.setTags((String) map.get("tags"));
        tx.setCreatedAt(SharedUtils.parseZonedDateTime(map.get("createdAt")));
        tx.setUpdatedAt(SharedUtils.parseZonedDateTime(map.get("updatedAt")));
        return tx;
    }
}
