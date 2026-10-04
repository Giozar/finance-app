package com.giozar04.walletTransactionDetails.domain.enums;

public enum WalletTransactionSourceType {
    WALLET_BALANCE("WALLET_BALANCE", "Saldo Wallet"),
    LINKED_CARD("LINKED_CARD", "Tarjeta Asociada");

    private final String value;
    private final String label;

    WalletTransactionSourceType(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() { return value; }
    public String getLabel() { return label; }

    @Override
    public String toString() {
        return label;
    }

    public static WalletTransactionSourceType fromValue(String value) {
        for (WalletTransactionSourceType type : values()) {
            if (type.getValue().equalsIgnoreCase(value)) return type;
        }
        throw new IllegalArgumentException("Tipo de fuente de transacción wallet no válido: " + value);
    }
}
