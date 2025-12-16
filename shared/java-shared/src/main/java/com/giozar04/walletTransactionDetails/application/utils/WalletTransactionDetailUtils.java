package com.giozar04.walletTransactionDetails.application.utils;

import java.util.HashMap;
import java.util.Map;

import com.giozar04.shared.utils.SharedUtils;
import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;
import com.giozar04.walletTransactionDetails.domain.enums.WalletTransactionSourceType;

public class WalletTransactionDetailUtils {

    public static Map<String, Object> toMap(WalletTransactionDetail detail) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", detail.getId());
        map.put("transactionId", detail.getTransactionId());
        map.put("sourceType", detail.getSourceType().getValue());
        map.put("walletAccountId", detail.getWalletAccountId());
        map.put("cardId", detail.getCardId());
        map.put("amount", detail.getAmount());
        map.put("cashbackPercentage", detail.getCashbackPercentage());

        if (detail.getCreatedAt() != null) {
            map.put("createdAt", detail.getCreatedAt().format(SharedUtils.getFormatter()));
        }

        if (detail.getUpdatedAt() != null) {
            map.put("updatedAt", detail.getUpdatedAt().format(SharedUtils.getFormatter()));
        }

        return map;
    }

    public static WalletTransactionDetail fromMap(Map<String, Object> map) {
        WalletTransactionDetail detail = new WalletTransactionDetail();
        detail.setId(SharedUtils.parseLong(map.get("id")));
        detail.setTransactionId(SharedUtils.parseLong(map.get("transactionId")));
        detail.setSourceType(WalletTransactionSourceType.fromValue((String) map.get("sourceType")));
        detail.setWalletAccountId(SharedUtils.parseLong(map.get("walletAccountId")));
        detail.setCardId(SharedUtils.parseNullableLong(map.get("cardId")));
        detail.setAmount(SharedUtils.parseBigDecimal(map.get("amount"))); // <- mejor que new BigDecimal(...)
        detail.setCashbackPercentage(SharedUtils.parseNullableBigDecimal(map.get("cashbackPercentage")));
        detail.setCreatedAt(SharedUtils.parseZonedDateTime(map.get("createdAt")));
        detail.setUpdatedAt(SharedUtils.parseZonedDateTime(map.get("updatedAt")));
        return detail;
    }

}
