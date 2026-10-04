package com.giozar04.cardTransactionDetails.infrastructure.serialization;

import java.util.HashMap;
import java.util.Map;

import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;
import com.giozar04.shared.infrastructure.serialization.ValueParser;

public class CardTransactionDetailMapper {

    public static Map<String, Object> toMap(CardTransactionDetail detail) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", detail.getId());
        map.put("transactionId", detail.getTransactionId());
        map.put("cardId", detail.getCardId());
        map.put("amount", detail.getAmount());
        map.put("installmentMonths", detail.getInstallmentMonths());
        map.put("interestFree", detail.isInterestFree());

        if (detail.getCreatedAt() != null)
            map.put("createdAt", detail.getCreatedAt().format(ValueParser.getFormatter()));

        if (detail.getUpdatedAt() != null)
            map.put("updatedAt", detail.getUpdatedAt().format(ValueParser.getFormatter()));

        return map;
    }

    public static CardTransactionDetail fromMap(Map<String, Object> map) {
        CardTransactionDetail detail = new CardTransactionDetail();
        detail.setId(ValueParser.parseLong(map.get("id")));
        detail.setTransactionId(ValueParser.parseLong(map.get("transactionId")));
        detail.setCardId(ValueParser.parseLong(map.get("cardId")));
        detail.setAmount(ValueParser.parseBigDecimal(map.get("amount")));
        detail.setInstallmentMonths(ValueParser.parseNullableInt(map.get("installmentMonths")));
        Object interestFreeObj = map.get("interestFree");
        detail.setInterestFree(interestFreeObj != null && Boolean.parseBoolean(interestFreeObj.toString()));
        detail.setCreatedAt(ValueParser.parseZonedDateTime(map.get("createdAt")));
        detail.setUpdatedAt(ValueParser.parseZonedDateTime(map.get("updatedAt")));
        return detail;
    }
}
