package com.giozar04.accountCashbackSettings.application.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.giozar04.accountCashbackSettings.domain.entities.AccountCashbackSetting;
import com.giozar04.shared.utils.SharedUtils;

public class AccountCashbackSettingUtils {

    public static Map<String, Object> toMap(AccountCashbackSetting setting) {
        Map<String, Object> map = new HashMap<>();
        map.put("accountId", setting.getAccountId());
        map.put("cashbackEnabled", setting.isCashbackEnabled());
        map.put("defaultCashbackRate", setting.getDefaultCashbackRate() != null
                ? setting.getDefaultCashbackRate().toPlainString()
                : null);

        if (setting.getCreatedAt() != null) {
            map.put("createdAt", setting.getCreatedAt().format(SharedUtils.getFormatter()));
        }
        if (setting.getUpdatedAt() != null) {
            map.put("updatedAt", setting.getUpdatedAt().format(SharedUtils.getFormatter()));
        }

        return map;
    }

    public static AccountCashbackSetting fromMap(Map<String, Object> map) {
        AccountCashbackSetting setting = new AccountCashbackSetting();

        setting.setAccountId(SharedUtils.parseLong(map.get("accountId")));

        Object enabledObj = map.get("cashbackEnabled");
        if (enabledObj != null) {
            setting.setCashbackEnabled(Boolean.parseBoolean(enabledObj.toString()));
        }

        setting.setDefaultCashbackRate(SharedUtils.parseNullableBigDecimal(map.get("defaultCashbackRate")));
        setting.setCreatedAt(SharedUtils.parseZonedDateTime(map.get("createdAt")));
        setting.setUpdatedAt(SharedUtils.parseZonedDateTime(map.get("updatedAt")));

        return setting;
    }

    public static List<Map<String, Object>> toMapList(List<AccountCashbackSetting> settings) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (AccountCashbackSetting setting : settings) {
            list.add(toMap(setting));
        }
        return list;
    }

    public static List<AccountCashbackSetting> fromMapList(List<Map<String, Object>> maps) {
        List<AccountCashbackSetting> list = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            list.add(fromMap(map));
        }
        return list;
    }
}
