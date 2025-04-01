package com.giozar04.bootstrap;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.giozar04.configs.ServerConnectionConfig;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.shared.layouts.AppLayout;
import com.giozar04.transactions.infrastructure.services.TransactionService;
import com.giozar04.users.infrastructure.services.UserService;

public class ApplicationInitializer {

    private ServerConnectionService connectionService;
    private TransactionService transactionService;
    private UserService userService;

    public void start() {
        if (initializeConnection() && initializeServices()) {
            launchUI();
        } else {
            System.err.println("❌ La aplicación no se pudo iniciar por fallas de conexión.");
        }
    }

    private boolean initializeConnection() {
        try {
            ServerConnectionConfig config = new ServerConnectionConfig();
            connectionService = ServerConnectionService.getInstance(config.getHost(), config.getPort());
            connectionService.connect();
            System.out.println("✅ Conexión establecida con el servidor.");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error al conectar con el servidor: " + e.getMessage());
            return false;
        }
    }

    private boolean initializeServices() {
        try {
            this.transactionService = TransactionService.connectService(connectionService);
            System.out.println("✅ Servicio de transacciones conectado correctamente.");

            this.userService = UserService.connectService(connectionService);
            System.out.println("✅ Servicio de usuarios conectado correctamente.");

            
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error al inicializar servicios: " + e.getMessage());
            return false;
        }
    }

    private void launchUI() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Mi App de Finanzas");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new AppLayout());
            frame.setVisible(true);
        });
    }
}
