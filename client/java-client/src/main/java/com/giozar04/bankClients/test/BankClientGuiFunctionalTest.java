package com.giozar04.bankClients.test;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.giozar04.bankClients.infrastructure.services.BankClientService;
import com.giozar04.bankClients.presentation.views.BankClientsView;
import com.giozar04.configs.ServerConnectionConfig;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.shared.components.MainContentPanel;
import com.giozar04.users.infrastructure.services.UserService;

public class BankClientGuiFunctionalTest {

    public static void main(String[] args) {
        System.out.println("=== PRUEBA FUNCIONAL GUI DE BANK_CLIENT (CLIENTE) ===");

        try {
            // 1. Inicializar conexión
            ServerConnectionConfig config = new ServerConnectionConfig();
            ServerConnectionService connectionService = ServerConnectionService.getInstance(config.getHost(), config.getPort());
            connectionService.connect();
            System.out.println("✅ Conectado al servidor en " + config.getHost() + ":" + config.getPort());

            // 2. Inicializar servicios necesarios (BankClient depende de User para el combo)
            UserService.connectService(connectionService);
            BankClientService.connectService(connectionService);
            System.out.println("✅ Servicios inicializados.");

            // 3. Lanzar la interfaz en el EDT
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Prueba Funcional: Gestión de Clientes Bancarios");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1000, 700);
                frame.setLocationRelativeTo(null);

                MainContentPanel mainPanel = new MainContentPanel();
                mainPanel.setView(new BankClientsView());

                frame.add(mainPanel);
                frame.setVisible(true);
            });

        } catch (IOException e) {
            System.err.println("❌ Error de conexión: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
