package com.giozar04.transactions.domain.enums;

public enum PaymentMethod {
    CASH("cash", "Efectivo"),
    CARD("card", "Tarjeta"),
    TRANSFER("transfer", "Transferencia"),
    QR("qr", "Código QR"),
    CODI("codi", "CoDi"),
    WALLET("wallet", "Billetera");

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
