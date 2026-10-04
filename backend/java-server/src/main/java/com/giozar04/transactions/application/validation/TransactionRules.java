package com.giozar04.transactions.application.validation;

import java.util.EnumMap;
import java.util.List;

import com.giozar04.transactions.application.validation.rules.CardPaymentRule;
import com.giozar04.transactions.application.validation.rules.CommonFieldsRule;
import com.giozar04.transactions.application.validation.rules.ExpenseRule;
import com.giozar04.transactions.application.validation.rules.IncomeRule;
import com.giozar04.transactions.application.validation.rules.InternalPaymentRule;
import com.giozar04.transactions.application.validation.rules.NoDetailPaymentRule;
import com.giozar04.transactions.application.validation.rules.ReallocationRule;
import com.giozar04.transactions.application.validation.rules.WalletPaymentRule;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.domain.enums.PaymentMethod;

/**
 * Conjunto de reglas por defecto: comunes + una por tipo de operación + una por método de pago.
 * Para una regla nueva basta con añadirla aquí (o registrar otra entrada en el EnumMap).
 */
public final class TransactionRules {

    private TransactionRules() {}

    public static List<TransactionRule> defaultRules() {
        EnumMap<OperationTypes, TransactionRule> byOperation = new EnumMap<>(OperationTypes.class);
        byOperation.put(OperationTypes.INCOME, new IncomeRule());
        byOperation.put(OperationTypes.EXPENSE, new ExpenseRule());
        byOperation.put(OperationTypes.REALLOCATION, new ReallocationRule());

        TransactionRule noDetail = new NoDetailPaymentRule();
        EnumMap<PaymentMethod, TransactionRule> byPaymentMethod = new EnumMap<>(PaymentMethod.class);
        byPaymentMethod.put(PaymentMethod.CARD, new CardPaymentRule());
        byPaymentMethod.put(PaymentMethod.WALLET, new WalletPaymentRule());
        byPaymentMethod.put(PaymentMethod.INTERNAL, new InternalPaymentRule());
        byPaymentMethod.put(PaymentMethod.CASH, noDetail);
        byPaymentMethod.put(PaymentMethod.WIRE_TRANSFER, noDetail);
        byPaymentMethod.put(PaymentMethod.QR, noDetail);
        byPaymentMethod.put(PaymentMethod.CODI, noDetail);

        return List.of(
                new CommonFieldsRule(),
                new EnumDispatchRule<>(OperationTypes.class, Transaction::getOperationType, byOperation),
                new EnumDispatchRule<>(PaymentMethod.class, Transaction::getPaymentMethod, byPaymentMethod)
        );
    }
}
