package com.giozar04.contracts;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.giozar04.messages.infrastructure.serialization.MessageJsonCodec;
import com.giozar04.messages.infrastructure.transport.Message;
import com.giozar04.shared.infrastructure.serialization.ValueParser;

/** Caracterización del protocolo antes de migrar. La referencia se compara con un parser JSON independiente. */
public final class ContractProbe {
    private static final ZonedDateTime DATE = ZonedDateTime.parse("2026-01-15T13:45:30-06:00[America/Mexico_City]");
    private record Feature(String name, String entity, String mapper, String write, String read) {}

    private static final List<Feature> FEATURES = List.of(
        new Feature("tags", "com.giozar04.tags.domain.entities.Tag", "com.giozar04.tags.infrastructure.serialization.TagMapper", "toMap", "fromMap"),
        new Feature("users", "com.giozar04.users.domain.entities.User", "com.giozar04.users.infrastructure.serialization.UserMapper", "toMap", "fromMap"),
        new Feature("categories", "com.giozar04.categories.domain.entities.Category", "com.giozar04.categories.infrastructure.serialization.CategoryMapper", "toMap", "fromMap"),
        new Feature("externalEntities", "com.giozar04.externalEntities.domain.entities.ExternalEntity", "com.giozar04.externalEntities.infrastructure.serialization.ExternalEntityMapper", "toMap", "fromMap"),
        new Feature("bankClient", "com.giozar04.bankClient.domain.entities.BankClient", "com.giozar04.bankClient.infrastructure.serialization.BankClientMapper", "toMap", "fromMap"),
        new Feature("accounts", "com.giozar04.accounts.domain.entities.Account", "com.giozar04.accounts.infrastructure.serialization.AccountMapper", "toMap", "fromMap"),
        new Feature("card", "com.giozar04.card.domain.entities.Card", "com.giozar04.card.infrastructure.serialization.CardMapper", "toMap", "fromMap"),
        new Feature("accountCashbackSettings", "com.giozar04.accountCashbackSettings.domain.entities.AccountCashbackSetting", "com.giozar04.accountCashbackSettings.infrastructure.serialization.AccountCashbackSettingMapper", "toMap", "fromMap"),
        new Feature("walletCardLinks", "com.giozar04.walletCardLinks.domain.entities.WalletCardLink", "com.giozar04.walletCardLinks.infrastructure.serialization.WalletCardLinkMapper", "toMap", "fromMap"),
        new Feature("cardTransactionDetails", "com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail", "com.giozar04.cardTransactionDetails.infrastructure.serialization.CardTransactionDetailMapper", "toMap", "fromMap"),
        new Feature("walletTransactionDetails", "com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail", "com.giozar04.walletTransactionDetails.infrastructure.serialization.WalletTransactionDetailMapper", "toMap", "fromMap"),
        new Feature("transactions", "com.giozar04.transactions.domain.entities.Transaction", "com.giozar04.transactions.infrastructure.serialization.TransactionMapper", "toMap", "fromMap"),
        new Feature("accountReconciliations", "com.giozar04.accountReconciliations.domain.entities.AccountReconciliation", "com.giozar04.accountReconciliations.infrastructure.serialization.AccountReconciliationMapper", "toMap", "fromMap")
    );

    public static void main(String[] args) throws Exception {
        Map<String, Object> scenarios = new TreeMap<>();
        for (Feature feature : FEATURES) {
            Class<?> entity = Class.forName(feature.entity());
            Class<?> mapper = Class.forName(feature.mapper());
            Method write = mapper.getMethod(feature.write(), entity);
            Method read = mapper.getMethod(feature.read(), Map.class);
            Object example = sample(entity, true);
            Map<String, Object> full = asMap(write.invoke(null, example));
            scenarios.put(feature.name() + ".full", wire(full));
            scenarios.put(feature.name() + ".roundtrip", wire(asMap(write.invoke(null, read.invoke(null, full)))));
            try {
                scenarios.put(feature.name() + ".defaults", wire(asMap(write.invoke(null, sample(entity, false)))));
            } catch (java.lang.reflect.InvocationTargetException error) {
                // Documentar también los rechazos actuales de entradas incompletas.
                scenarios.put(feature.name() + ".defaults", wire(Map.of("exception", error.getCause().getClass().getName())));
            }
            Message decoded = MessageJsonCodec.decode(wire(full));
            scenarios.put(feature.name() + ".jsonRoundtrip", wire(asMap(write.invoke(null, read.invoke(null, decoded.getData())))));

            // Cada valor de enum debe conservar sus códigos de intercambio.
            for (Method setter : setters(entity)) {
                Class<?> type = setter.getParameterTypes()[0];
                if (type.isEnum()) {
                    for (Object value : type.getEnumConstants()) {
                        setter.invoke(example, value);
                        Map<String, Object> data = asMap(write.invoke(null, example));
                        scenarios.put(feature.name() + "." + setter.getName() + "." + value,
                                wire(asMap(write.invoke(null, read.invoke(null, data)))));
                    }
                }
            }
            if (feature.name().equals("transactions")) {
                Map<String, Object> nullable = new HashMap<>(full);
                nullable.put("status", null);
                nullable.put("description", "null");
                nullable.put("tagIds", Arrays.asList("3", 5L, null, "invalid"));
                nullable.remove("cardDetail");
                nullable.remove("walletDetail");
                scenarios.put("transactions.nullable", wire(asMap(write.invoke(null, read.invoke(null, nullable)))));
            }
        }
        ZonedDateTime before = ZonedDateTime.now();
        ZonedDateTime fallback = ValueParser.parseZonedDateTime("invalid");
        if (fallback.isBefore(before) || fallback.isAfter(ZonedDateTime.now()))
            throw new AssertionError("Cambió el fallback de fechas");
        if (ValueParser.parseLong("invalid") != 0L || ValueParser.parseNullableLong("invalid") != null)
            throw new AssertionError("Cambió el fallback numérico");
        Message result = new Message("contracts", "Baseline de shared");
        result.setData(scenarios);
        System.out.println(MessageJsonCodec.encode(result));
    }

    private static List<Method> setters(Class<?> type) {
        return Arrays.stream(type.getMethods())
                .filter(m -> m.getName().startsWith("set") && m.getParameterCount() == 1)
                .sorted(java.util.Comparator.comparing(Method::getName)).toList();
    }

    private static Object sample(Class<?> type, boolean fill) throws Exception {
        Object instance = type.getConstructor().newInstance();
        for (Method setter : setters(type)) {
            Class<?> valueType = setter.getParameterTypes()[0];
            Object value = null;
            if (valueType == ZonedDateTime.class) value = DATE;
            else if (!fill) continue;
            else if (valueType == String.class) value = setter.getName().equals("setTimezone")
                    ? "America/Mexico_City" : "Texto español \\\"\n\r\t\u0001";
            else if (valueType == long.class || valueType == Long.class) value = 42L;
            else if (valueType == int.class || valueType == Integer.class) value = 6;
            else if (valueType == double.class || valueType == Double.class) value = 123.45;
            else if (valueType == boolean.class || valueType == Boolean.class) value = true;
            else if (valueType == BigDecimal.class) value = new BigDecimal("123.450000");
            else if (valueType.isEnum()) value = valueType.getEnumConstants()[0];
            else if (valueType == List.class) value = new ArrayList<>(List.of(3L, 5L));
            else if (valueType.getName().startsWith("com.giozar04.")) value = sample(valueType, true);
            else throw new AssertionError("Tipo de muestra no soportado: " + valueType);
            setter.invoke(instance, value);
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) { return (Map<String, Object>) value; }

    private static String wire(Map<String, Object> data) {
        Message message = new Message("CONTRACT", "Comillas: \"; barra: \\; salto: \n; tab: \t; unicode: á");
        message.setData(data);
        return MessageJsonCodec.encode(message);
    }
}
