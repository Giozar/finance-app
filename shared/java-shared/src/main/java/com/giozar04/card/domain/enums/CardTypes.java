package com.giozar04.card.domain.enums;

public enum CardTypes {
    PHYSICAL("physical", "Física"),
    Digital("digital", "Digital");

    private final String value;
    private final String label;

    CardTypes(String value, String label) {
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

    public static CardTypes fromValue(String value) {
        for (CardTypes type: values()) {
            if(type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw  new IllegalArgumentException("Tipo de tarjeta no  valido: " + value);
    }

    
}
