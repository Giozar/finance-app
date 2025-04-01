package com.giozar04.shared.utils;

import java.awt.Component;

import javax.swing.JOptionPane;

public class ErrorDialogUtil {

    public static void showError(Component parent, String message) {
        if (message == null || message.trim().isEmpty()) {
            message = "Ha ocurrido un error inesperado.";
        }
        System.err.println("Error: " + message);
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
