package com.giozar04.transactions.domain.enums;

public enum OperationTypes {
    INCOME("income", "Ingreso"),
    EXPENSE("expense", "Egreso");

    private final String value;
    private final String label;

    OperationTypes(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() { return value; }
    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }

    public static OperationTypes fromValue(String value) {
        for (OperationTypes t : values()) {
            if (t.getValue().equalsIgnoreCase(value)) return t;
        }
        throw new IllegalArgumentException("Tipo de operación no válido: " + value);
    }
}
