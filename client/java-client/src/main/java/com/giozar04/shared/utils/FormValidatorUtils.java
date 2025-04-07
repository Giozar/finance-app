package com.giozar04.shared.utils;

import java.util.List;

/**
 * Utilidad para validación de formularios reutilizable en todo el proyecto.
 */
public class FormValidatorUtils {

    /**
     * Valida que un campo no esté vacío.
     *
     * @param value     valor del campo
     * @param fieldName nombre visible del campo
     * @param errors    lista de errores donde se añadirá si falla
     */
    public static void isRequired(String value, String fieldName, List<String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.add(fieldName + " es un campo obligatorio.");
        }
    }

    /**
     * Valida que un campo tenga un formato de correo válido.
     *
     * @param value     valor del campo
     * @param fieldName nombre visible del campo
     * @param errors    lista de errores
     */
    public static void isEmail(String value, String fieldName, List<String> errors) {
        isRequired(value, fieldName, errors);
        if (value != null && !value.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            errors.add(fieldName + " debe tener un formato de correo válido.");
        }
    }

    /**
     * Valida que una contraseña cumpla un mínimo de caracteres.
     *
     * @param value     contraseña
     * @param fieldName nombre del campo
     * @param minLength longitud mínima permitida
     * @param errors    lista de errores
     */
    public static void isPassword(String value, String fieldName, int minLength, List<String> errors) {
        isRequired(value, fieldName, errors);
        if (value != null && value.length() < minLength) {
            errors.add(fieldName + " debe tener al menos " + minLength + " caracteres.");
        }
    }

    /**
     * Valida que un campo sea numérico.
     *
     * @param value     valor del campo
     * @param fieldName nombre del campo
     * @param errors    lista de errores
     */
    public static void isNumeric(String value, String fieldName, List<String> errors) {
        if (value == null || value.trim().isEmpty()) return;

        try {
            Double.valueOf(value.trim());
        } catch (NumberFormatException e) {
            errors.add(fieldName + " debe ser un número válido.");
        }
    }

    /**
     * Valida que un número sea positivo.
     *
     * @param value     valor numérico
     * @param fieldName nombre del campo
     * @param errors    lista de errores
     */
    public static void isPositiveNumber(String value, String fieldName, List<String> errors) {
        if (value == null || value.trim().isEmpty()) return;

        try {
            double number = Double.parseDouble(value.trim());
            if (number < 0) {
                errors.add(fieldName + " debe ser un número positivo.");
            }
        } catch (NumberFormatException e) {
            // Si ya se validó en isNumeric, no repetir
        }
    }

    public static boolean isLongPositive(String value, String fieldName, List<String> errors) {
        try {
            long parsed = Long.parseLong(value.trim());
            if (parsed <= 0) {
                errors.add(fieldName + " debe ser mayor que cero.");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            errors.add(fieldName + " debe ser un número entero válido.");
            return false;
        }
    }

    /**
     * Verifica si la lista de errores contiene mensajes.
     *
     * @param errors lista de errores
     * @return true si hay errores, false si está vacía
     */
    public static boolean hasErrors(List<String> errors) {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Formatea una lista de errores en un único mensaje.
     *
     * @param errors lista de errores
     * @return texto con todos los errores en formato legible
     */
    public static String formatErrorMessage(List<String> errors) {
        StringBuilder sb = new StringBuilder("Corrige los siguientes errores:\n\n");
        for (String error : errors) {
            sb.append("• ").append(error).append("\n");
        }
        return sb.toString();
    }
}
