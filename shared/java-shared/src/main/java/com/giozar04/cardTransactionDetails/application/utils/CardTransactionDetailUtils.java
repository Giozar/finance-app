package com.giozar04.cardTransactionDetails.application.utils;

import java.util.HashMap;
import java.util.Map;

import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;
import com.giozar04.shared.utils.SharedUtils;

public class CardTransactionDetailUtils {

    public static Map<String, Object> toMap(CardTransactionDetail detail) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", detail.getId());
        map.put("transactionId", detail.getTransactionId());
        map.put("cardId", detail.getCardId());
        map.put("amount", detail.getAmount());
        map.put("installmentMonths", detail.getInstallmentMonths());
        map.put("interestFree", detail.isInterestFree());

        if (detail.getCreatedAt() != null)
            map.put("createdAt", detail.getCreatedAt().format(SharedUtils.getFormatter()));

        if (detail.getUpdatedAt() != null)
            map.put("updatedAt", detail.getUpdatedAt().format(SharedUtils.getFormatter()));

        return map;
    }

    public static CardTransactionDetail fromMap(Map<String, Object> map) {
        CardTransactionDetail detail = new CardTransactionDetail();
        detail.setId(SharedUtils.parseLong(map.get("id")));
        detail.setTransactionId(SharedUtils.parseLong(map.get("transactionId")));
        detail.setCardId(SharedUtils.parseLong(map.get("cardId")));
        detail.setAmount(SharedUtils.parseBigDecimal(map.get("amount")));
        detail.setInstallmentMonths(SharedUtils.parseNullableInt(map.get("installmentMonths")));
        detail.setInterestFree(Boolean.parseBoolean(map.get("interestFree").toString()));
        detail.setCreatedAt(SharedUtils.parseZonedDateTime(map.get("createdAt")));
        detail.setUpdatedAt(SharedUtils.parseZonedDateTime(map.get("updatedAt")));
        return detail;
    }
}
