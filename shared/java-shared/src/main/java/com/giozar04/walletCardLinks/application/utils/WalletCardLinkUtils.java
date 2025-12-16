package com.giozar04.walletCardLinks.application.utils;

import java.util.HashMap;
import java.util.Map;

import com.giozar04.shared.utils.SharedUtils;
import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;

public class WalletCardLinkUtils {

    public static Map<String, Object> toMap(WalletCardLink link) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", link.getId());
        map.put("walletAccountId", link.getWalletAccountId());
        map.put("cardId", link.getCardId());

        if (link.getCreatedAt() != null) {
            map.put("createdAt", link.getCreatedAt().format(SharedUtils.getFormatter()));
        }

        if (link.getUpdatedAt() != null) {
            map.put("updatedAt", link.getUpdatedAt().format(SharedUtils.getFormatter()));
        }

        return map;
    }

    public static WalletCardLink fromMap(Map<String, Object> map) {
        WalletCardLink link = new WalletCardLink();
        link.setId(SharedUtils.parseLong(map.get("id")));
        link.setWalletAccountId(SharedUtils.parseLong(map.get("walletAccountId")));
        link.setCardId(SharedUtils.parseLong(map.get("cardId")));
        link.setCreatedAt(SharedUtils.parseZonedDateTime(map.get("createdAt")));
        link.setUpdatedAt(SharedUtils.parseZonedDateTime(map.get("updatedAt")));
        return link;
    }
}
