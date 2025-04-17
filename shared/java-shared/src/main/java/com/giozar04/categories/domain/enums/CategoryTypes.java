package com.giozar04.categories.domain.enums;

public enum CategoryTypes {
    INCOME("income", "Ingreso"),
    EXPENSE("expense", "Gasto");

    private final String value;
    private final String label;

    CategoryTypes(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() { return value; }
    public String getLabel() { return label; }

    @Override
    public String toString() {
        return label;
    }

    public static CategoryTypes fromValue(String value) {
        for (CategoryTypes type : values()) {
            if (type.getValue().equalsIgnoreCase(value)) return type;
        }
        throw new IllegalArgumentException("Tipo de categoría no válido: " + value);
    }
}
