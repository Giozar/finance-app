package com.giozar04.walletTransactionDetails.infrastructure.serialization;

import java.util.HashMap;
import java.util.Map;

import com.giozar04.shared.infrastructure.serialization.ValueParser;
import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;
import com.giozar04.walletTransactionDetails.domain.enums.WalletTransactionSourceType;

public class WalletTransactionDetailMapper {

    public static Map<String, Object> toMap(WalletTransactionDetail detail) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", detail.getId());
        map.put("transactionId", detail.getTransactionId());
        map.put("sourceType", detail.getSourceType().getValue());
        map.put("walletAccountId", detail.getWalletAccountId());
        map.put("cardId", detail.getCardId());
        map.put("amount", detail.getAmount());
        map.put("cashbackRate", detail.getCashbackRate());

        if (detail.getCreatedAt() != null) {
            map.put("createdAt", detail.getCreatedAt().format(ValueParser.getFormatter()));
        }

        if (detail.getUpdatedAt() != null) {
            map.put("updatedAt", detail.getUpdatedAt().format(ValueParser.getFormatter()));
        }

        return map;
    }

    public static WalletTransactionDetail fromMap(Map<String, Object> map) {
        WalletTransactionDetail detail = new WalletTransactionDetail();
        detail.setId(ValueParser.parseLong(map.get("id")));
        detail.setTransactionId(ValueParser.parseLong(map.get("transactionId")));
        detail.setSourceType(WalletTransactionSourceType.fromValue((String) map.get("sourceType")));
        detail.setWalletAccountId(ValueParser.parseLong(map.get("walletAccountId")));
        detail.setCardId(ValueParser.parseNullableLong(map.get("cardId")));
        detail.setAmount(ValueParser.parseBigDecimal(map.get("amount"))); // <- mejor que new BigDecimal(...)
        detail.setCashbackRate(ValueParser.parseNullableBigDecimal(map.get("cashbackRate")));
        detail.setCreatedAt(ValueParser.parseZonedDateTime(map.get("createdAt")));
        detail.setUpdatedAt(ValueParser.parseZonedDateTime(map.get("updatedAt")));
        return detail;
    }

}
