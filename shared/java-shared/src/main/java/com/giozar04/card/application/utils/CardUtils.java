package com.giozar04.card.application.utils;

import java.util.HashMap;
import java.util.Map;

import com.giozar04.card.domain.entities.Card;
import com.giozar04.card.domain.enums.CardTypes;
import com.giozar04.shared.utils.SharedUtils;

public class CardUtils {

    public static Map<String, Object> cardToMap(Card card) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", card.getId());
        map.put("accountId", card.getAccountId());
        map.put("name", card.getName());
        map.put("cardType", card.getCardType() != null ? card.getCardType().getValue() : null);
        map.put("cardNumber", card.getCardNumber());
        map.put("status", card.getStatus());

        if (card.getExpirationDate() != null) {
            map.put("expirationDate", card.getExpirationDate().format(SharedUtils.getFormatter()));
        }
        if (card.getCreatedAt() != null) {
            map.put("createdAt", card.getCreatedAt().format(SharedUtils.getFormatter()));
        }
        if (card.getUpdatedAt() != null) {
            map.put("updatedAt", card.getUpdatedAt().format(SharedUtils.getFormatter()));
        }

        return map;
    }

    public static Card mapToCard(Map<String, Object> map) {
        Card card = new Card();

        card.setId(SharedUtils.parseLong(map.get("id")));
        card.setAccountId(SharedUtils.parseLong(map.get("accountId")));
        card.setName((String) map.getOrDefault("name", ""));
        card.setCardNumber((String) map.getOrDefault("cardNumber", ""));
        card.setExpirationDate(SharedUtils.parseZonedDateTime(map.get("expirationDate")));
        card.setStatus((String) map.getOrDefault("status", "ACTIVE"));
        card.setCreatedAt(SharedUtils.parseZonedDateTime(map.get("createdAt")));
        card.setUpdatedAt(SharedUtils.parseZonedDateTime(map.get("updatedAt")));

        Object typeObj = map.get("cardType");
        if (typeObj != null) {
            card.setCardType(CardTypes.fromValue(typeObj.toString()));
        }

        return card;
    }
}
