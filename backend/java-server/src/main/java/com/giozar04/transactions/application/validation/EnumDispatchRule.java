package com.giozar04.transactions.application.validation;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.giozar04.transactions.domain.entities.Transaction;

/**
 * Despacha la validación a la regla registrada para un valor de enum de la transacción
 * (tipo de operación, método de pago...). Exige una regla por cada constante del enum:
 * si se añade un valor nuevo sin su regla, el servidor falla al arrancar (OCP con red de seguridad).
 * Si la transacción no trae el valor (null), no hace nada: lo reporta CommonFieldsRule.
 */
public class EnumDispatchRule<E extends Enum<E>> implements TransactionRule {

    private final Function<Transaction, E> keyExtractor;
    private final EnumMap<E, TransactionRule> rules;

    public EnumDispatchRule(Class<E> enumType, Function<Transaction, E> keyExtractor, Map<E, TransactionRule> rules) {
        this.keyExtractor = Objects.requireNonNull(keyExtractor, "El extractor no puede ser nulo");
        this.rules = new EnumMap<>(enumType);
        this.rules.putAll(Objects.requireNonNull(rules, "Las reglas no pueden ser nulas"));

        for (E value : enumType.getEnumConstants()) {
            if (!this.rules.containsKey(value)) {
                throw new IllegalStateException("Falta la regla de validación para " + enumType.getSimpleName() + "." + value.name());
            }
        }
    }

    @Override
    public void validate(Transaction tx, ValidationContext ctx, List<String> errors) {
        E key = keyExtractor.apply(tx);
        if (key == null) return;
        rules.get(key).validate(tx, ctx, errors);
    }
}
