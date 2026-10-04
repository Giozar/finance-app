package com.giozar04.accountCashbackSettings.domain.policies;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import com.giozar04.accountCashbackSettings.domain.entities.AccountCashbackSetting;

public final class AccountCashbackSettingPolicy {
    private AccountCashbackSettingPolicy() {}

    public static void validateSetting(AccountCashbackSetting setting) {
        Objects.requireNonNull(setting, "La configuración de cashback no puede ser nula");

        if (setting.getAccountId() <= 0) {
            throw new IllegalArgumentException("El accountId debe ser mayor que cero");
        }

        BigDecimal rate = setting.getDefaultCashbackRate();
        if (rate != null) {
            if (rate.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("defaultCashbackRate no puede ser negativo");
            }
            if (rate.compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException("defaultCashbackRate no puede ser mayor a 1 (100%)");
            }
        }
    }

    public static void validateAccountId(long accountId) {
        if (accountId <= 0) {
            throw new IllegalArgumentException("El accountId debe ser mayor que cero");
        }
    }
}
