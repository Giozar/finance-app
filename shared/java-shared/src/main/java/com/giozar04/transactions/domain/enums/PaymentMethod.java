package com.giozar04.transactions.domain.enums;

public enum PaymentMethod {
    CASH,       // Pago en efectivo (solo cuentas tipo cash)
    CARD,       // Pago con tarjeta (física o digital; requiere selección de tarjeta)
    TRANSFER,   // Transferencia bancaria o interbancaria
    QR,         // Pago vía QR
    CODI,       // Pago con CoDi (México)
    WALLET      // Pago mediante Wallet como Cashi, PayPal, etc.
}