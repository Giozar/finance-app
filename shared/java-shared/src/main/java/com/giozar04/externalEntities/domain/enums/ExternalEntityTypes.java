package com.giozar04.externalEntities.domain.enums;

public enum ExternalEntityTypes {
    PERSON("PERSON", "Persona"),
    SERVICE("SERVICE", "Servicio"),
    STORE("STORE", "Tienda");

    private final String value;
    private final String label;

    ExternalEntityTypes(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() { return value; }
    public String getLabel() { return label; }

    @Override
    public String toString() {
        return label;
    }

    public static ExternalEntityTypes fromValue(String value) {
        for (ExternalEntityTypes type : values()) {
            if (type.value.equalsIgnoreCase(value)) return type;
        }
        throw new IllegalArgumentException("Tipo de entidad externa no válido: " + value);
    }
}
