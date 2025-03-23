package com.giozar04;

import java.io.IOException;

import javax.swing.SwingUtilities;

import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.transactions.presentation.views.TransactionFormFrame;

public class Main {
    public static void main(String[] args) {
        // Inicializamos ServerConnectionService con host y puerto del servidor
        ServerConnectionService serverConnectionSocket = new ServerConnectionService("localhost", 8080);
        try {
            serverConnectionSocket.connect();
            System.out.println("Conexión establecida exitosamente con el servidor.");
        } catch (IOException e) {
            System.err.println("Error al establecer la conexión con el servidor: " + e.getMessage());
            return; // Si no se conecta, no se continúa
        }
        
        // Iniciar la UI en el hilo de Swing, pasando la instancia del servicio
        SwingUtilities.invokeLater(() -> {
            TransactionFormFrame frame = new TransactionFormFrame(serverConnectionSocket);
            frame.setVisible(true);
        });
    }
}
