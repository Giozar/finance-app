package com.giozar04.users.presentation.validators;

import java.util.ArrayList;
import java.util.List;

public class UserValidator {

    private final List<String> errors;

    public UserValidator() {
        this.errors = new ArrayList<>();
    }

    public boolean validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            errors.add(fieldName + " es un campo requerido.");
            return false;
        }
        return true;
    }

    public boolean validateEmail(String value, String fieldName) {
        if (!validateRequired(value, fieldName)) return false;

        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!value.matches(regex)) {
            errors.add(fieldName + " debe tener un formato de correo válido.");
            return false;
        }
        return true;
    }

    public boolean validatePassword(String value, String fieldName) {
        if (!validateRequired(value, fieldName)) return false;

        if (value.length() < 6) {
            errors.add(fieldName + " debe tener al menos 6 caracteres.");
            return false;
        }
        return true;
    }

    public boolean validatePositiveNumber(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) return true; // opcional

        try {
            double number = Double.parseDouble(value.trim());
            if (number < 0) {
                errors.add(fieldName + " debe ser un valor positivo.");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            errors.add(fieldName + " debe ser un número válido.");
            return false;
        }
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    public String getErrorMessage() {
        StringBuilder sb = new StringBuilder("Por favor corrija los siguientes errores:\n\n");
        for (String error : errors) {
            sb.append("• ").append(error).append("\n");
        }
        return sb.toString();
    }

    public void clearErrors() {
        errors.clear();
    }
}
