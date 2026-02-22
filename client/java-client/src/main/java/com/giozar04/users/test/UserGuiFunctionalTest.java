package com.giozar04.users.test;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.giozar04.configs.ServerConnectionConfig;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.shared.components.MainContentPanel;
import com.giozar04.users.infrastructure.services.UserService;
import com.giozar04.users.presentation.views.UsersView;

public class UserGuiFunctionalTest {

    public static void main(String[] args) {
        System.out.println("=== PRUEBA FUNCIONAL GUI DE USUARIO (CLIENTE) ===");

        try {
            // 1. Inicializar conexión (Reutilizando lógica de ApplicationInitializer)
            ServerConnectionConfig config = new ServerConnectionConfig();
            ServerConnectionService connectionService = ServerConnectionService.getInstance(config.getHost(), config.getPort());
            connectionService.connect();
            System.out.println("✅ Conectado al servidor en " + config.getHost() + ":" + config.getPort());

            // 2. Inicializar servicios necesarios
            UserService.connectService(connectionService);
            System.out.println("✅ Servicio de usuarios inicializado.");

            // 3. Lanzar la interfaz en el EDT
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Prueba Funcional: Gestión de Usuarios");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1000, 700);
                frame.setLocationRelativeTo(null);

                // Reutilizamos MainContentPanel para permitir la navegación entre vistas
                MainContentPanel mainPanel = new MainContentPanel();
                mainPanel.setView(new UsersView());

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
