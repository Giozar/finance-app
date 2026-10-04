package com.giozar04.accountReconciliations.infrastructure.serialization;

import java.util.HashMap;
import java.util.Map;

import com.giozar04.accountReconciliations.domain.entities.AccountReconciliation;
import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.shared.infrastructure.serialization.ValueParser;

public class AccountReconciliationMapper {

    public static Map<String, Object> toMap(AccountReconciliation reconciliation) {
        Map<String, Object> map = new HashMap<>();
        map.put("accountId", reconciliation.getAccountId());
        map.put("userId", reconciliation.getUserId());
        map.put("accountName", reconciliation.getAccountName());
        map.put("accountType", reconciliation.getAccountType() != null ? reconciliation.getAccountType().getValue() : null);
        map.put("openingNet", reconciliation.getOpeningNet());
        map.put("totalInflows", reconciliation.getTotalInflows());
        map.put("totalOutflows", reconciliation.getTotalOutflows());
        map.put("expectedNet", reconciliation.getExpectedNet());
        map.put("actualNet", reconciliation.getActualNet());
        map.put("difference", reconciliation.getDifference());
        return map;
    }

    public static AccountReconciliation fromMap(Map<String, Object> map) {
        AccountReconciliation reconciliation = new AccountReconciliation();

        reconciliation.setAccountId(ValueParser.parseLong(map.get("accountId")));
        reconciliation.setUserId(ValueParser.parseLong(map.get("userId")));
        reconciliation.setAccountName((String) map.getOrDefault("accountName", ""));

        String typeStr = (String) map.get("accountType");
        if (typeStr != null && !typeStr.isEmpty() && !"null".equals(typeStr)) {
            reconciliation.setAccountType(AccountTypes.fromValue(typeStr));
        }

        reconciliation.setOpeningNet(ValueParser.parseNullableBigDecimal(map.get("openingNet")));
        reconciliation.setTotalInflows(ValueParser.parseNullableBigDecimal(map.get("totalInflows")));
        reconciliation.setTotalOutflows(ValueParser.parseNullableBigDecimal(map.get("totalOutflows")));
        reconciliation.setExpectedNet(ValueParser.parseNullableBigDecimal(map.get("expectedNet")));
        reconciliation.setActualNet(ValueParser.parseNullableBigDecimal(map.get("actualNet")));
        reconciliation.setDifference(ValueParser.parseNullableBigDecimal(map.get("difference")));

        return reconciliation;
    }
}
