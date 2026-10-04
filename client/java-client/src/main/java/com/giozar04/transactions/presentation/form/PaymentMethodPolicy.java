package com.giozar04.transactions.presentation.form;

import java.util.List;

import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.domain.enums.PaymentMethod;

/**
 * Política de métodos de pago permitidos según la operación y el tipo de las cuentas.
 *
 * <p>Clase pura (sin Swing ni servicios): es el único lugar que hay que tocar para ajustar la tabla.</p>
 *
 * <pre>
 * INCOME        destino CASH                 → CASH
 * INCOME        otro destino                 → WIRE_TRANSFER, QR, CODI, CASH
 * EXPENSE       origen CASH                  → CASH
 * EXPENSE       origen DEBIT                 → CARD, WIRE_TRANSFER, QR, CODI
 * EXPENSE       origen CREDIT / BENEFIT      → CARD
 * EXPENSE       origen WALLET                → WALLET
 * EXPENSE       origen SAVINGS / INVESTMENT  → WIRE_TRANSFER
 * REALLOCATION  origen o destino CASH        → CASH, INTERNAL
 * REALLOCATION  otros                        → INTERNAL, WIRE_TRANSFER
 * </pre>
 *
 * <p>Mientras falte la cuenta relevante devuelve una lista vacía. Si solo queda un método,
 * el formulario lo autoselecciona y lo bloquea.</p>
 */
public class PaymentMethodPolicy {

    public List<PaymentMethod> allowedMethods(OperationTypes operation, AccountTypes source, AccountTypes destination) {
        if (operation == null) {
            return List.of();
        }
        return switch (operation) {
            case INCOME -> incomeMethods(destination);
            case EXPENSE -> expenseMethods(source);
            case REALLOCATION -> reallocationMethods(source, destination);
        };
    }

    private List<PaymentMethod> incomeMethods(AccountTypes destination) {
        if (destination == null) {
            return List.of();
        }
        if (destination == AccountTypes.CASH) {
            return List.of(PaymentMethod.CASH);
        }
        return List.of(PaymentMethod.WIRE_TRANSFER, PaymentMethod.QR, PaymentMethod.CODI, PaymentMethod.CASH);
    }

    private List<PaymentMethod> expenseMethods(AccountTypes source) {
        if (source == null) {
            return List.of();
        }
        return switch (source) {
            case CASH -> List.of(PaymentMethod.CASH);
            case DEBIT -> List.of(PaymentMethod.CARD, PaymentMethod.WIRE_TRANSFER, PaymentMethod.QR, PaymentMethod.CODI);
            case CREDIT, BENEFIT -> List.of(PaymentMethod.CARD);
            case WALLET -> List.of(PaymentMethod.WALLET);
            case SAVINGS, INVESTMENT -> List.of(PaymentMethod.WIRE_TRANSFER);
        };
    }

    private List<PaymentMethod> reallocationMethods(AccountTypes source, AccountTypes destination) {
        if (source == null || destination == null) {
            return List.of();
        }
        if (source == AccountTypes.CASH || destination == AccountTypes.CASH) {
            return List.of(PaymentMethod.CASH, PaymentMethod.INTERNAL);
        }
        return List.of(PaymentMethod.INTERNAL, PaymentMethod.WIRE_TRANSFER);
    }
}
