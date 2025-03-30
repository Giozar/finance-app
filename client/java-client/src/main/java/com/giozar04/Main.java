package com.giozar04;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.giozar04.configs.ServerConnectionConfig;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.shared.layouts.AppLayout;
import com.giozar04.transactions.infrastructure.services.TransactionService;

public class Main {

    public static void main(String[] args) {
        TransactionService transactionService = initializeServices();

        if (transactionService != null) {
            launchUI();
        } else {
            System.err.println("La aplicación no se pudo iniciar debido a un error de conexión.");
        }
    }

    private static TransactionService initializeServices() {
        ServerConnectionConfig config = new ServerConnectionConfig();
        ServerConnectionService connectionService = ServerConnectionService.getInstance(
            config.getHost(), config.getPort()
        );

        try {
            connectionService.connect();
            System.out.println("✅ Conexión establecida exitosamente con el servidor.");

            TransactionService.connectService(connectionService);
            System.out.println("✅ Servicio de transacciones conectado correctamente.");

            return TransactionService.getInstance();
        } catch (IOException e) {
            System.err.println("Error al conectar con el servidor: " + e.getMessage());
            return null;
        }
    }

    private static void launchUI() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Mi App de Finanzas");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);

            AppLayout layout = new AppLayout();
            frame.setContentPane(layout);
            frame.setVisible(true);
        });
    }
}
