package com.giozar04.bankClients.presentation.views;

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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.bankClients.infrastructure.services.BankClientService;
import com.giozar04.bankClients.presentation.components.BankClientFormPanel;
import com.giozar04.bankClients.presentation.components.BankNameCellRenderer;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.MainContentPanel;
import com.giozar04.shared.components.table.ColumnDefinition;
import com.giozar04.shared.components.table.GenericTablePanel;
import com.giozar04.shared.components.table.OptionsCellEditor;
import com.giozar04.shared.components.table.OptionsCellRenderer;
import com.giozar04.shared.components.table.PopupMenuActionHandler;
import com.giozar04.shared.utils.DialogUtil;

public class BankClientsView extends JPanel implements PopupMenuActionHandler {

    private final BankClientService bankClientService;
    private JTextField searchField;
    private GenericTablePanel<BankClient> tablePanel;

    public BankClientsView() {
        this.bankClientService = BankClientService.getInstance();

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(createTopPanel(), BorderLayout.NORTH);
        initTablePanel();
    }

    private JPanel createTopPanel() {
        // Panel superior con título y botón
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Gestión de Clientes Bancarios");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton newClientButton = new JButton("Nuevo Cliente");
        newClientButton.addActionListener(this::handleNewClient);
        headerPanel.add(newClientButton, BorderLayout.EAST);

        // Panel de búsqueda
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
        List<ColumnDefinition<BankClient>> columns = Arrays.asList(
                new ColumnDefinition<>("Banco", BankClient::getBankName),
                new ColumnDefinition<>("Número de Cliente", BankClient::getClientNumber),
                new ColumnDefinition<>("Opciones", c -> "···")
        );

        // Render personalizado para banco
        columns.get(0).setRenderer(new BankNameCellRenderer());

        // Alineación a la derecha si se desea para columna 1 (no obligatorio)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        columns.get(1).setRenderer(centerRenderer);

        // Opciones (menú contextual)
        columns.get(2).setRenderer(new OptionsCellRenderer());
        columns.get(2).setEditor(new OptionsCellEditor(this));

        try {
            System.out.println("LLAMO A CLIENTES");
            List<BankClient> clients = bankClientService.getAllBankClients();
            tablePanel = new GenericTablePanel<>(columns, clients);
            add(tablePanel, BorderLayout.CENTER);
        } catch (ClientOperationException e) {
            System.out.println("Un error de acá");
            DialogUtil.showError(this, "Error al cargar los clientes: " + e.getMessage());
        }
    }

    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();
        try {
            List<BankClient> clients = bankClientService.getAllBankClients();
            List<BankClient> filtered = clients.stream()
                    .filter(c -> c.getBankName().toLowerCase().contains(query)
                              || c.getClientNumber().toLowerCase().contains(query))
                    .collect(Collectors.toList());
            tablePanel.setData(filtered);
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al buscar clientes: " + e.getMessage());
        }
    }

    private void loadClients() {
        try {
            List<BankClient> clients = bankClientService.getAllBankClients();
            tablePanel.setData(clients);
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al recargar clientes: " + e.getMessage());
        }
    }

    private void handleNewClient(ActionEvent e) {
        Object source = e.getSource();
        if (source instanceof JButton button) {
            button.setEnabled(false);
        }

        MainContentPanel main = getMainContentPanel();
        if (main != null) {
            main.setView(new CreateBankClientView());
        }
    }

    private MainContentPanel getMainContentPanel() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof MainContentPanel)) {
            parent = parent.getParent();
        }
        return (MainContentPanel) parent;
    }

    @Override
    public void onEdit(int rowIndex) {
        BankClient bankClient = tablePanel.getItemAt(rowIndex);
        MainContentPanel main = getMainContentPanel();
        if (main != null) {
            BankClientFormPanel formPanel = new BankClientFormPanel();
            formPanel.loadBankClient(bankClient);
            main.setView(formPanel);
        }
    }

    @Override
    public void onDelete(int rowIndex) {
        BankClient bankClient = tablePanel.getItemAt(rowIndex);
        boolean confirmed = DialogUtil.showConfirm(
                this,
                "¿Desea eliminar el cliente bancario seleccionado?",
                "Confirmar eliminación"
        );

        if (confirmed) {
            try {
                bankClientService.deleteBankClientById(bankClient.getId());
                DialogUtil.showSuccess(this, "Cliente bancario eliminado.");
                loadClients();
            } catch (ClientOperationException | HeadlessException e) {
                DialogUtil.showError(this, "Error al eliminar: " + e.getMessage());
            }
        }
    }

    @Override
    public void onViewDetails(int rowIndex) {
        DialogUtil.showWarning(this, "Función de ver detalles no implementada.");
    }

    @Override
    public void addNotify() {
        super.addNotify();
        loadClients();
    }
}
