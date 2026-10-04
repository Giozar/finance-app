package com.giozar04.transactions.domain.enums;

public enum PaymentMethod {
    CASH("CASH", "Efectivo"),
    CARD("CARD", "Tarjeta"),
    WIRE_TRANSFER("WIRE_TRANSFER", "Transferencia (SPEI)"),
    INTERNAL("INTERNAL", "Movimiento interno"),
    QR("QR", "Código QR"),
    CODI("CODI", "CoDi"),
    WALLET("WALLET", "Billetera");

    private final String value;
    private final String label;

    PaymentMethod(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() { return value; }
    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }

    public static PaymentMethod fromValue(String value) {
        for (PaymentMethod m : values()) {
            if (m.getValue().equalsIgnoreCase(value)) return m;
        }
        throw new IllegalArgumentException("Método de pago no válido: " + value);
    }
}
