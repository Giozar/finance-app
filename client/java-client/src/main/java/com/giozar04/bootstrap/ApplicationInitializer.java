package com.giozar04.bootstrap;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.giozar04.accounts.infrastructure.services.AccountService;
import com.giozar04.bankClients.infrastructure.services.BankClientService;
import com.giozar04.cards.infrastructure.services.CardService;
import com.giozar04.categories.infrastructure.services.CategoryService;
import com.giozar04.configs.ServerConnectionConfig;
import com.giozar04.logging.CustomLogger;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.shared.layouts.AppLayout;
import com.giozar04.transactions.infrastructure.services.TransactionService;
import com.giozar04.users.infrastructure.services.UserService;

public class ApplicationInitializer {

    private final CustomLogger logger = CustomLogger.getInstance();

    private ServerConnectionService connectionService;
    private UserService userService;
    private BankClientService bankClientService;
    private AccountService accountService;
    private CardService cardService;
    private CategoryService categoryService;
    private TransactionService transactionService;

    public void start() {
        if (initializeConnection() && initializeServices()) {
            launchUI();
        } else {
            this.logger.error("❌ La aplicación no se pudo iniciar por fallas de conexión.");
        }
    }

    private boolean initializeConnection() {
        try {
            ServerConnectionConfig config = new ServerConnectionConfig();
            connectionService = ServerConnectionService.getInstance(config.getHost(), config.getPort());
            connectionService.connect();
            System.out.println("✅ Conexión establecida con el servidor.");
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error al conectar con el servidor: " + e.getMessage());
            return false;
        }
    }

    private boolean initializeServices() {
        try {

            this.userService = UserService.connectService(connectionService);
            System.out.println("✅ Servicio de usuarios conectado correctamente.");

            this.bankClientService = BankClientService.connectService(connectionService);
            System.out.println("✅ Servicio de clientes bancarios conectado correctamente.");

            this.accountService = AccountService.connectService(connectionService);
            System.out.println("Servicios de cuentas conectado correctamente");

            this.cardService = CardService.connectService(connectionService);
            System.out.println("✅ Servicio de tarjetas conectado correctamente.");

            this.categoryService = CategoryService.connectService(connectionService);
            System.out.println("Servicios de categorías conectados correctamente");
            
            this.transactionService = TransactionService.connectService(connectionService);
            System.out.println("✅ Servicio de transacciones conectado correctamente.");
            
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
