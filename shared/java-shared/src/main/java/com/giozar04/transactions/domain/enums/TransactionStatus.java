package com.giozar04.transactions.domain.enums;

public enum TransactionStatus {
    PENDING("PENDING", "Pendiente"),
    COMPLETED("COMPLETED", "Completada"),
    FAILED("FAILED", "Fallida"),
    CANCELLED("CANCELLED", "Cancelada");

    private final String value;
    private final String label;

    TransactionStatus(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() { return value; }
    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }

    public static TransactionStatus fromValue(String value) {
        for (TransactionStatus t : values()) {
            if (t.getValue().equalsIgnoreCase(value)) return t;
        }
        throw new IllegalArgumentException("Estado de transacción no válido: " + value);
    }
}
