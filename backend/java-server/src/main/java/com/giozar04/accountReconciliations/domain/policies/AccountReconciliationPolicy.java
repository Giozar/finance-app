package com.giozar04.accountReconciliations.domain.policies;

import java.util.List;
import java.util.Objects;
import com.giozar04.accountReconciliations.domain.entities.AccountReconciliation;

public final class AccountReconciliationPolicy {
    private AccountReconciliationPolicy() {}

    public static void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
    }
}
