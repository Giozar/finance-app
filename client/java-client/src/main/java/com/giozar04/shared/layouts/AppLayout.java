package com.giozar04.shared.layouts;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.giozar04.dashboard.presentation.views.MainDashboardView;
import com.giozar04.shared.components.HeaderPanel;
import com.giozar04.shared.components.MainContentPanel;
import com.giozar04.shared.components.SidebarPanel;
import com.giozar04.transactions.presentation.views.TransactionsView;

public class AppLayout extends JPanel {

    private final MainContentPanel contentPanel;

    public AppLayout() {
        setLayout(new BorderLayout());

        HeaderPanel header = new HeaderPanel();
        contentPanel = new MainContentPanel();

        // Sidebar con navegación
        SidebarPanel sidebar = new SidebarPanel(this::navigate);

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.add(header, BorderLayout.NORTH);
        contentWrapper.add(contentPanel, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(contentWrapper, BorderLayout.CENTER);
    }

    private void navigate(String menu) {
        switch (menu) {
            case "Inicio" -> setContent(new MainDashboardView());
            case "Transacciones" -> setContent(new TransactionsView());
            default -> setContent(new JLabel("Vista no encontrada"));
        }
    }

    public void setContent(Component component) {
        contentPanel.setView(component);
    }
}