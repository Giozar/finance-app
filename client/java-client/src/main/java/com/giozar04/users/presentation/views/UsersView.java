package com.giozar04.users.presentation.views;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.MainContentPanel;
import com.giozar04.shared.components.table.ColumnDefinition;
import com.giozar04.shared.components.table.GenericTablePanel;
import com.giozar04.shared.components.table.OptionsCellEditor;
import com.giozar04.shared.components.table.OptionsCellRenderer;
import com.giozar04.shared.components.table.PopupMenuActionHandler;
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.infrastructure.services.UserService;
import com.giozar04.users.presentation.components.UserFormPanel;

public class UsersView extends JPanel implements PopupMenuActionHandler {

    private JTextField searchField;
    private GenericTablePanel<User> tablePanel;
    private final UserService userService;

    public UsersView() {
        userService = UserService.getInstance();

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Parte superior: título y búsqueda
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Tabla de usuarios
        initTablePanel();
    }

    private JPanel createTopPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("Gestión de Usuarios");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        JButton newUserButton = new JButton("Nuevo Usuario");
        newUserButton.addActionListener(this::handleNewUser);
        headerPanel.add(newUserButton, BorderLayout.EAST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Buscar:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        JButton searchButton = new JButton("Buscar");
        searchButton.addActionListener(e -> performSearch());
        searchPanel.add(searchButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        return topPanel;
    }

    private void initTablePanel() {
        List<ColumnDefinition<User>> columns = Arrays.asList(
                new ColumnDefinition<>("Nombre", User::getName),
                new ColumnDefinition<>("Correo", User::getEmail),
                new ColumnDefinition<>("Balance", u -> String.format("$%.2f", u.getGlobalBalance())),
                new ColumnDefinition<>("Opciones", u -> "···")
        );

        // Balance alineado a la derecha
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        columns.get(2).setRenderer(rightAlign);

        // Opciones con menú contextual
        columns.get(3).setRenderer(new OptionsCellRenderer());
        columns.get(3).setEditor(new OptionsCellEditor(this));

        try {
            List<User> users = userService.getAllUsers();
            tablePanel = new GenericTablePanel<>(columns, users);
            add(tablePanel, BorderLayout.CENTER);
        } catch (ClientOperationException e) {
            showError("Error al cargar los usuarios.");
        }
    }

    private void loadUsers() {
        try {
            List<User> users = userService.getAllUsers();
            tablePanel.setData(users);
        } catch (ClientOperationException e) {
            showError("Error al recargar los usuarios.");
        }
    }

    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();
        try {
            List<User> users = userService.getAllUsers();
            List<User> filtered = users.stream()
                    .filter(u -> u.getName().toLowerCase().contains(query) ||
                                 u.getEmail().toLowerCase().contains(query))
                    .collect(Collectors.toList());
            tablePanel.setData(filtered);
        } catch (ClientOperationException e) {
            showError("Error al buscar: " + e.getMessage());
        }
    }

    private void handleNewUser(ActionEvent e) {
        JButton btn = (JButton) e.getSource();
        btn.setEnabled(false);
        MainContentPanel mainPanel = getMainContentPanel();
        if (mainPanel != null) {
            mainPanel.setView(new CreateUserView());
        }
    }

    private MainContentPanel getMainContentPanel() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof MainContentPanel)) {
            parent = parent.getParent();
        }
        return (MainContentPanel) parent;
    }

    private void showError(String msg) {
        System.err.println("Error: " + msg);
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // CRUD desde menú contextual
    @Override
    public void onEdit(int rowIndex) {
        User user = tablePanel.getItemAt(rowIndex);
        UserFormPanel form = new UserFormPanel();
        form.loadUser(user);
        MainContentPanel main = getMainContentPanel();
        if (main != null) main.setView(form);
    }

    @Override
    public void onDelete(int rowIndex) {
        User user = tablePanel.getItemAt(rowIndex);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar usuario?",
                "Confirmación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                userService.deleteUserById(user.getId());
                JOptionPane.showMessageDialog(this, "Usuario eliminado.");
                loadUsers();
            } catch (ClientOperationException | HeadlessException ex) {
                showError("No se pudo eliminar: " + ex.getMessage());
            }
        }
    }

    @Override
    public void onViewDetails(int rowIndex) {
        JOptionPane.showMessageDialog(this, "Función no implementada.");
    }

    @Override
    public void addNotify() {
        super.addNotify();
        loadUsers();
    }
}
