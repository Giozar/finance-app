package com.giozar04.accounts.domain.enums;

public enum AccountTypes {
    CASH("CASH", "Efectivo"),
    DEBIT("DEBIT", "Débito Bancario"),
    CREDIT("CREDIT", "Crédito"),
    WALLET("WALLET", "Billetera Virtual"),
    BENEFIT("BENEFIT", "Prestación/Vale"),
    SAVINGS("SAVINGS", "Ahorro con Rendimiento");

    private final String value;
    private final String label;

    AccountTypes(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static AccountTypes fromValue(String value) {
        for (AccountTypes type : values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Tipo de cuenta no válido: " + value);
    }
}
