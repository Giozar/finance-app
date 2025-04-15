package com.giozar04.cards.presentation.views;

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

import com.giozar04.card.domain.entities.Card;
import com.giozar04.cards.infrastructure.services.CardService;
import com.giozar04.cards.presentation.components.CardFormPanel;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.MainContentPanel;
import com.giozar04.shared.components.table.ColumnDefinition;
import com.giozar04.shared.components.table.GenericTablePanel;
import com.giozar04.shared.components.table.OptionsCellEditor;
import com.giozar04.shared.components.table.OptionsCellRenderer;
import com.giozar04.shared.components.table.PopupMenuActionHandler;
import com.giozar04.shared.utils.DialogUtil;

public class CardsView extends JPanel implements PopupMenuActionHandler {

    private final CardService cardService;
    private JTextField searchField;
    private GenericTablePanel<Card> tablePanel;

    public CardsView() {
        cardService = CardService.getInstance();

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        initTablePanel();
    }

    private JPanel createTopPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("Gestión de Tarjetas");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        JButton newButton = new JButton("Nueva Tarjeta");
        newButton.addActionListener(this::handleNewCard);
        headerPanel.add(newButton, BorderLayout.EAST);

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
        List<ColumnDefinition<Card>> columns = Arrays.asList(
                new ColumnDefinition<>("Nombre", Card::getName),
                new ColumnDefinition<>("Tipo", c -> c.getCardType().getLabel()),
                new ColumnDefinition<>("Últimos 4 dígitos", Card::getCardNumber),
                new ColumnDefinition<>("Expira", c -> c.getExpirationDate() != null ? c.getExpirationDate().toLocalDate().toString() : "—"),
                new ColumnDefinition<>("Opciones", c -> "···")
        );

        columns.get(3).setRenderer(centerAlign());
        columns.get(4).setRenderer(new OptionsCellRenderer());
        columns.get(4).setEditor(new OptionsCellEditor(this));

        try {
            List<Card> cards = cardService.getAllCards();
            tablePanel = new GenericTablePanel<>(columns, cards);
            add(tablePanel, BorderLayout.CENTER);
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al cargar las tarjetas.");
        }
    }

    private DefaultTableCellRenderer centerAlign() {
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        return center;
    }

    private void loadCards() {
        try {
            List<Card> cards = cardService.getAllCards();
            tablePanel.setData(cards);
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al recargar las tarjetas.");
        }
    }

    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();
        try {
            List<Card> cards = cardService.getAllCards();
            List<Card> filtered = cards.stream()
                    .filter(c -> c.getName().toLowerCase().contains(query))
                    .collect(Collectors.toList());
            tablePanel.setData(filtered);
        } catch (ClientOperationException e) {
            DialogUtil.showError(this, "Error al buscar: " + e.getMessage());
        }
    }

    private void handleNewCard(ActionEvent e) {
        JButton btn = (JButton) e.getSource();
        btn.setEnabled(false);
        MainContentPanel mainPanel = getMainContentPanel();
        if (mainPanel != null) {
            mainPanel.setView(new CreateCardView());
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
        Card card = tablePanel.getItemAt(rowIndex);
        CardFormPanel form = new CardFormPanel();
        form.loadCard(card);
        MainContentPanel main = getMainContentPanel();
        if (main != null) main.setView(form);
    }

    @Override
    public void onDelete(int rowIndex) {
        Card card = tablePanel.getItemAt(rowIndex);
        boolean confirm = DialogUtil.showConfirm(this, "¿Desea eliminar esta tarjeta?", "Confirmar eliminación");
        if (confirm) {
            try {
                cardService.deleteCardById(card.getId());
                JOptionPane.showMessageDialog(this, "Tarjeta eliminada.");
                loadCards();
            } catch (ClientOperationException | HeadlessException ex) {
                DialogUtil.showError(this, "No se pudo eliminar: " + ex.getMessage());
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
        loadCards();
    }
}
