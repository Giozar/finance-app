package com.giozar04.accounts.presentation.components.subpanels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.accounts.infrastructure.services.AccountService;
import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.bankClients.infrastructure.services.BankClientService;
import com.giozar04.card.domain.entities.Card;
import com.giozar04.card.domain.enums.CardTypes;
import com.giozar04.cards.infrastructure.services.CardService;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.shared.utils.DialogUtil;

/**
 * Subpanel para vincular tarjetas a una cuenta WALLET.
 * Carga tarjetas del usuario propietario que pertenecen a cuentas DEBIT, CREDIT o BENEFIT,
 * y permite filtrarlas dinámicamente.
 */
public class WalletCardLinksPanel extends JPanel {

    private static final int PANEL_W = 600;
    private static final int TABLE_H = 150;

    private final FormComboBox<BankClient> bankFilterCombo;
    private final FormComboBox<AccountTypes> accountTypeFilterCombo;
    private final FormComboBox<CardTypes> cardTypeFilterCombo;

    private final JTable table;
    private final CardTableModel tableModel;
    private final JScrollPane scrollPane;
    private final JLabel noCardsLabel;

    private final List<CardRow> allRows = new ArrayList<>();
    private final List<CardRow> filteredRows = new ArrayList<>();

    public WalletCardLinksPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Tarjetas Vinculadas",
                0,
                0,
                new Font("SansSerif", Font.BOLD, 12)
        ));

        // --- Panel de Filtros ---
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filtersPanel.setOpaque(false);

        bankFilterCombo = new FormComboBox<>("Banco:", 250, 30);
        bankFilterCombo.setPlaceholder("Todos los bancos");

        accountTypeFilterCombo = new FormComboBox<>("Tipo Cuenta:", 220, 30);
        accountTypeFilterCombo.setPlaceholder("Todos los tipos");
        accountTypeFilterCombo.setItems(List.of(AccountTypes.DEBIT, AccountTypes.CREDIT, AccountTypes.BENEFIT));

        cardTypeFilterCombo = new FormComboBox<>("Tipo Tarjeta:", 220, 30);
        cardTypeFilterCombo.setPlaceholder("Todos los tipos");
        cardTypeFilterCombo.setItems(List.of(CardTypes.PHYSICAL, CardTypes.DIGITAL));

        filtersPanel.add(bankFilterCombo);
        filtersPanel.add(accountTypeFilterCombo);
        filtersPanel.add(cardTypeFilterCombo);

        // --- Configuración de Tabla ---
        tableModel = new CardTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false);
        
        // Ajustar anchos de columnas
        table.getColumnModel().getColumn(0).setPreferredWidth(80);  // Checkbox
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Banco
        table.getColumnModel().getColumn(2).setPreferredWidth(140); // Cuenta
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Tipo Cuenta
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // Alias
        table.getColumnModel().getColumn(5).setPreferredWidth(90);  // Tipo Tarjeta
        table.getColumnModel().getColumn(6).setPreferredWidth(90);  // Dígitos

        scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(PANEL_W, TABLE_H));
        scrollPane.setMaximumSize(new Dimension(PANEL_W, TABLE_H));

        // --- Mensaje Informativo ---
        noCardsLabel = new JLabel("Este usuario no tiene tarjetas disponibles para vincular.");
        noCardsLabel.setForeground(Color.GRAY);
        noCardsLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        noCardsLabel.setAlignmentX(CENTER_ALIGNMENT);
        noCardsLabel.setVisible(false);

        // --- Agregar al Panel Principal ---
        add(filtersPanel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(scrollPane);
        add(noCardsLabel);

        // --- Registrar Eventos de Filtro ---
        bankFilterCombo.addActionListener(e -> applyFilters());
        accountTypeFilterCombo.addActionListener(e -> applyFilters());
        cardTypeFilterCombo.addActionListener(e -> applyFilters());
    }

    /**
     * Carga todas las tarjetas del usuario seleccionado y rellena la tabla.
     */
    public void loadCardsForUser(long userId) {
        try {
            // Guardar ids de tarjetas seleccionadas antes de la recarga
            Set<Long> previouslySelected = new HashSet<>(getSelectedCardIds());

            // 1. Obtener todas las cuentas del sistema
            List<Account> allAccounts = AccountService.getInstance().getAllAccounts();

            // Filtrar cuentas del usuario y que admitan tarjetas (DEBIT, CREDIT, BENEFIT)
            List<Account> userCardAccounts = new ArrayList<>();
            Map<Long, Account> accountMap = new HashMap<>();
            for (Account acc : allAccounts) {
                if (acc.getUserId() == userId && (acc.getType() == AccountTypes.DEBIT 
                        || acc.getType() == AccountTypes.CREDIT 
                        || acc.getType() == AccountTypes.BENEFIT)) {
                    userCardAccounts.add(acc);
                    accountMap.put(acc.getId(), acc);
                }
            }

            // 2. Obtener todas las tarjetas del sistema
            List<Card> allCards = CardService.getInstance().getAllCards();

            // Filtrar tarjetas asociadas a las cuentas del usuario
            List<Card> userCards = new ArrayList<>();
            for (Card card : allCards) {
                if (accountMap.containsKey(card.getAccountId())) {
                    userCards.add(card);
                }
            }

            // 3. Obtener clientes bancarios
            List<BankClient> bankClients = BankClientService.getInstance().getAllBankClients();
            Map<Long, BankClient> bankMap = new HashMap<>();
            for (BankClient bc : bankClients) {
                bankMap.put(bc.getId(), bc);
            }

            // Rellenar combo de bancos dinámicamente con los bancos presentes en las cuentas del usuario
            Set<BankClient> userBanks = new HashSet<>();
            for (Account acc : userCardAccounts) {
                if (acc.getBankClientId() != null) {
                    BankClient bc = bankMap.get(acc.getBankClientId());
                    if (bc != null) {
                        userBanks.add(bc);
                    }
                }
            }
            bankFilterCombo.setItems(new ArrayList<>(userBanks));

            // 4. Crear filas de la tabla
            allRows.clear();
            for (Card card : userCards) {
                Account acc = accountMap.get(card.getAccountId());
                String bankName = "Sin Banco";
                if (acc.getBankClientId() != null) {
                    BankClient bc = bankMap.get(acc.getBankClientId());
                    if (bc != null) {
                        bankName = bc.getBankName();
                    }
                }
                CardRow row = new CardRow(card, acc, bankName);
                if (previouslySelected.contains(card.getId())) {
                    row.setSelected(true);
                }
                allRows.add(row);
            }

            applyFilters();

        } catch (ClientOperationException ex) {
            DialogUtil.showError(this, "Error al cargar las tarjetas: " + ex.getMessage());
        }
    }

    /**
     * Aplica los filtros seleccionados a las filas.
     */
    private void applyFilters() {
        BankClient selectedBank = bankFilterCombo.getSelectedItem();
        AccountTypes selectedAccType = accountTypeFilterCombo.getSelectedItem();
        CardTypes selectedCardType = cardTypeFilterCombo.getSelectedItem();

        filteredRows.clear();
        for (CardRow row : allRows) {
            boolean matchesBank = (selectedBank == null) 
                    || (row.getAccount().getBankClientId() != null 
                        && row.getAccount().getBankClientId().equals(selectedBank.getId()));
            
            boolean matchesAccType = (selectedAccType == null) 
                    || (row.getAccount().getType() == selectedAccType);
            
            boolean matchesCardType = (selectedCardType == null) 
                    || (row.getCard().getCardType() == selectedCardType);

            if (matchesBank && matchesAccType && matchesCardType) {
                filteredRows.add(row);
            }
        }

        tableModel.setRows(filteredRows);

        if (allRows.isEmpty()) {
            noCardsLabel.setVisible(true);
            scrollPane.setVisible(false);
        } else {
            noCardsLabel.setVisible(false);
            scrollPane.setVisible(true);
        }
        revalidate();
        repaint();
    }

    /**
     * Obtiene la lista de IDs de tarjetas seleccionadas.
     */
    public List<Long> getSelectedCardIds() {
        List<Long> selectedIds = new ArrayList<>();
        for (CardRow row : allRows) {
            if (row.isSelected()) {
                selectedIds.add(row.getCard().getId());
            }
        }
        return selectedIds;
    }

    /**
     * Marca como seleccionadas las tarjetas cuyos IDs se indiquen.
     */
    public void setSelectedCardIds(List<Long> cardIds) {
        for (CardRow row : allRows) {
            row.setSelected(cardIds.contains(row.getCard().getId()));
        }
        tableModel.fireTableDataChanged();
    }

    /**
     * Restablece el panel de vínculos a su estado inicial.
     */
    public void clear() {
        allRows.clear();
        filteredRows.clear();
        tableModel.setRows(filteredRows);
        bankFilterCombo.clearSelection();
        accountTypeFilterCombo.clearSelection();
        cardTypeFilterCombo.clearSelection();
        noCardsLabel.setVisible(false);
        scrollPane.setVisible(true);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible) {
            clear();
        }
    }

    // ==================================================================
    // Modelos y Clases de Fila Internas
    // ==================================================================

    public static class CardRow {
        private final Card card;
        private final Account account;
        private final String bankName;
        private boolean selected;

        public CardRow(Card card, Account account, String bankName) {
            this.card = card;
            this.account = account;
            this.bankName = bankName;
            this.selected = false;
        }

        public Card getCard() { return card; }
        public Account getAccount() { return account; }
        public String getBankName() { return bankName; }
        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }
    }

    private class CardTableModel extends AbstractTableModel {
        private final List<CardRow> rows = new ArrayList<>();
        private final String[] columns = {
            "Vincular", "Banco", "Cuenta Origen", "Tipo Cuenta", "Alias Tarjeta", "Tipo Tarjeta", "Dígitos"
        };

        public void setRows(List<CardRow> newRows) {
            rows.clear();
            rows.addAll(newRows);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() { return rows.size(); }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public String getColumnName(int col) { return columns[col]; }

        @Override
        public Class<?> getColumnClass(int col) {
            if (col == 0) return Boolean.class;
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            CardRow row = rows.get(rowIndex);
            switch (columnIndex) {
                case 0: return row.isSelected();
                case 1: return row.getBankName();
                case 2: return row.getAccount().getName();
                case 3: return row.getAccount().getType().getLabel();
                case 4: return row.getCard().getName();
                case 5: return row.getCard().getCardType().getLabel();
                case 6: return row.getCard().getCardNumber();
                default: return null;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0 && aValue instanceof Boolean val) {
                rows.get(rowIndex).setSelected(val);
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }
}
