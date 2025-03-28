package com.giozar04.dashboard.presentation.views;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.giozar04.dashboard.presentation.components.HeaderPanel;
import com.giozar04.dashboard.presentation.components.MainContentPanel;
import com.giozar04.dashboard.presentation.components.SidebarPanel;
import com.giozar04.transactions.presentation.components.TransactionFormPanel;

public class MainDashboardView extends JFrame {

    private final MainContentPanel mainContentPanel;

    public MainDashboardView() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        // Layout principal
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Header
        HeaderPanel header = new HeaderPanel();

        // Contenido central dinámico
        mainContentPanel = new MainContentPanel();
        showHomeView(); // Vista inicial

        // Sidebar con evento
        SidebarPanel sidebar = new SidebarPanel(this::onMenuSelected);

        // Armar layout
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(header, BorderLayout.NORTH);
        contentPanel.add(mainContentPanel, BorderLayout.CENTER);

        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void onMenuSelected(String menu) {
        switch (menu) {
            case "Inicio" -> showHomeView();
            case "Transacciones" -> showTransactionsView();
            case "Clientes" -> showPlaceholder("Clientes");
            case "Cuentas" -> showPlaceholder("Cuentas");
            default -> showPlaceholder("Vista no encontrada");
        }
    }

    private void showHomeView() {
        JPanel homePanel = new JPanel();
        homePanel.setBackground(Color.LIGHT_GRAY);
        homePanel.add(new JLabel("Bienvenido al Dashboard 👋"));
        mainContentPanel.setView(homePanel);
    }

    private void showTransactionsView() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new TransactionFormPanel(), BorderLayout.CENTER);
        mainContentPanel.setView(panel);
    }

    private void showPlaceholder(String text) {
        JPanel placeholder = new JPanel();
        placeholder.setBackground(Color.LIGHT_GRAY);
        placeholder.add(new JLabel("Vista de " + text + " en construcción..."));
        mainContentPanel.setView(placeholder);
    }
}
