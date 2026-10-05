package com.giozar04.card.domain.enums;

public enum CardStatus {
    ACTIVE("ACTIVE", "Activa"),
    BLOCKED("BLOCKED", "Bloqueada"),
    EXPIRED("EXPIRED", "Vencida");

    private final String value;
    private final String label;

    CardStatus(String value, String label) {
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

    public static CardStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (CardStatus status : CardStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estado de tarjeta inválido: " + value);
    }
}
