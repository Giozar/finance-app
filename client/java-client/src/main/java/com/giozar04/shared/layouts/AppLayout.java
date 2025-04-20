package com.giozar04.shared.layouts;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.giozar04.accounts.presentation.views.AccountsView;
import com.giozar04.bankClients.presentation.views.BankClientsView;
import com.giozar04.cards.presentation.views.CardsView;
import com.giozar04.categories.presentation.views.CategoriesView;
import com.giozar04.dashboard.presentation.views.MainDashboardView;
import com.giozar04.shared.components.HeaderPanel;
import com.giozar04.shared.components.MainContentPanel;
import com.giozar04.shared.components.SidebarPanel;
import com.giozar04.transactions.presentation.views.TransactionsView;
import com.giozar04.users.presentation.views.UsersView;

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
            case "Usuarios" -> setContent(new UsersView());
            case "Clientes" -> setContent(new BankClientsView());
            case "Cuentas" -> setContent(new AccountsView());
            case "Tarjetas" -> setContent(new CardsView());
            case "Categorías" -> setContent(new CategoriesView());
            case "Transacciones" -> setContent(new TransactionsView());
            default -> setContent(new JLabel("Vista no encontrada"));
        }
    }

    public void setContent(Component component) {
        contentPanel.setView(component);
    }
}