package com.giozar04.walletCardLinks.infrastructure.serialization;

import java.util.HashMap;
import java.util.Map;

import com.giozar04.shared.infrastructure.serialization.ValueParser;
import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;

public class WalletCardLinkMapper {

    public static Map<String, Object> toMap(WalletCardLink link) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", link.getId());
        map.put("walletAccountId", link.getWalletAccountId());
        map.put("cardId", link.getCardId());

        if (link.getCreatedAt() != null) {
            map.put("createdAt", link.getCreatedAt().format(ValueParser.getFormatter()));
        }

        if (link.getUpdatedAt() != null) {
            map.put("updatedAt", link.getUpdatedAt().format(ValueParser.getFormatter()));
        }

        return map;
    }

    public static WalletCardLink fromMap(Map<String, Object> map) {
        WalletCardLink link = new WalletCardLink();
        link.setId(ValueParser.parseLong(map.get("id")));
        link.setWalletAccountId(ValueParser.parseLong(map.get("walletAccountId")));
        link.setCardId(ValueParser.parseLong(map.get("cardId")));
        link.setCreatedAt(ValueParser.parseZonedDateTime(map.get("createdAt")));
        link.setUpdatedAt(ValueParser.parseZonedDateTime(map.get("updatedAt")));
        return link;
    }
}
