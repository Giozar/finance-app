package com.giozar04.shared.utils;

import java.awt.Component;

import javax.swing.JOptionPane;

public class DialogUtil {

    public static void showError(Component parent, String message) {
        showDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showError(Component parent, String message, String title) {
        showDialog(parent, message, title != null ? title : "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showSuccess(Component parent, String message) {
        showDialog(parent, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showSuccess(Component parent, String message, String title) {
        showDialog(parent, message, title != null ? title : "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarning(Component parent, String message) {
        showDialog(parent, message, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    public static void showWarning(Component parent, String message, String title) {
        showDialog(parent, message, title != null ? title : "Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(
            parent,
            message,
            title != null ? title : "Confirmación",
            JOptionPane.YES_NO_OPTION
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    public static boolean showConfirm(Component parent, String message) {
        return showConfirm(parent, message, null);
    }
    

    private static void showDialog(Component parent, String message, String title, int messageType) {
        if (message == null || message.trim().isEmpty()) {
            message = switch (messageType) {
                case JOptionPane.ERROR_MESSAGE -> "Ha ocurrido un error inesperado.";
                case JOptionPane.INFORMATION_MESSAGE -> "Operación realizada con éxito.";
                case JOptionPane.WARNING_MESSAGE -> "Tenga precaución con esta acción.";
                default -> "Mensaje no especificado.";
            };
        }

        switch (messageType) {
            case JOptionPane.ERROR_MESSAGE -> System.err.println("Error: " + message);
            case JOptionPane.WARNING_MESSAGE -> System.out.println("Advertencia: " + message);
            case JOptionPane.INFORMATION_MESSAGE -> System.out.println("Éxito: " + message);
            default -> System.out.println("Mensaje: " + message);
        }

        JOptionPane.showMessageDialog(parent, message, title, messageType);
    }
}
