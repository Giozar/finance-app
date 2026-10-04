package com.giozar04.transactions.presentation.components.sections;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.giozar04.accountCashbackSettings.domain.entities.AccountCashbackSetting;
import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.card.domain.entities.Card;
import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.shared.components.forms.FormHelpText;
import com.giozar04.shared.components.forms.FormLabel;
import com.giozar04.shared.components.forms.PercentageField;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.PaymentMethod;
import com.giozar04.transactions.presentation.form.TransactionFormContext;
import com.giozar04.transactions.presentation.form.TransactionFormDataProvider;
import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;
import com.giozar04.walletTransactionDetails.domain.enums.WalletTransactionSourceType;

/**
 * Sección 5 – Detalle de wallet (solo con método WALLET; la wallet es la cuenta origen elegida).
 *
 * <ul>
 *   <li>"Saldo de la wallet": se carga a la propia wallet.</li>
 *   <li>"Tarjeta vinculada": tarjetas vinculadas a esa wallet; se carga a la cuenta de la tarjeta
 *       (el servidor ajusta la cuenta origen).</li>
 *   <li>Cashback: solo se registra la tasa; se precarga desde la configuración de la wallet si está activo.</li>
 * </ul>
 */
public class WalletDetailsSection extends AbstractTransactionSection {

    private static final long serialVersionUID = 1L;

    private final JRadioButton balanceRadio;
    private final JRadioButton linkedCardRadio;
    private final FormComboBox<Card> linkedCardCombo;
    private final FormHelpText chargeHelp;
    private final PercentageField cashbackField;
    private final FormHelpText cashbackHelp;

    private Long lastWalletId;

    public WalletDetailsSection(TransactionFormContext context, TransactionFormDataProvider provider) {
        super("Detalle de wallet", context, provider);

        balanceRadio = new JRadioButton(labelOf(WalletTransactionSourceType.WALLET_BALANCE), true);
        linkedCardRadio = new JRadioButton(labelOf(WalletTransactionSourceType.LINKED_CARD));
        balanceRadio.setOpaque(false);
        linkedCardRadio.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        group.add(balanceRadio);
        group.add(linkedCardRadio);

        JPanel radios = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        radios.setOpaque(false);
        radios.add(balanceRadio);
        radios.add(linkedCardRadio);
        FormLabel sourceRow = new FormLabel("Pagar con:", radios);
        sourceRow.getComponent(0).setPreferredSize(new Dimension(150, 25));
        Dimension rowSize = new Dimension(FIELD_WIDTH, 30);
        sourceRow.setPreferredSize(rowSize);
        sourceRow.setMaximumSize(rowSize);

        linkedCardCombo = new FormComboBox<>("Tarjeta vinculada:", FIELD_WIDTH, FIELD_HEIGHT);
        linkedCardCombo.setPlaceholder("Seleccione una tarjeta...");
        chargeHelp = new FormHelpText("", FIELD_WIDTH);

        cashbackField = new PercentageField("Cashback:", FIELD_WIDTH, FIELD_HEIGHT);
        cashbackHelp = new FormHelpText("Solo se registra la tasa de cashback; no modifica saldos.", FIELD_WIDTH);

        addRow(sourceRow);
        addRow(linkedCardCombo);
        addRow(chargeHelp);
        addRow(cashbackField);
        addRow(cashbackHelp);

        balanceRadio.addActionListener(e -> updateSourceVisibility());
        linkedCardRadio.addActionListener(e -> updateSourceVisibility());
        linkedCardCombo.addActionListener(e -> updateChargeHelp());

        updateSourceVisibility();
        setVisible(false);
    }

    private static String labelOf(WalletTransactionSourceType type) {
        return type == WalletTransactionSourceType.WALLET_BALANCE ? "Saldo de la wallet" : "Tarjeta vinculada";
    }

    // ------------------------------------------------------------------
    // Contexto
    // ------------------------------------------------------------------

    private boolean isActive() {
        return context.getPaymentMethod() == PaymentMethod.WALLET;
    }

    private Account wallet() {
        Account source = context.getSourceAccount();
        return source != null && source.getType() == AccountTypes.WALLET ? source : null;
    }

    @Override
    public void onContextChanged(TransactionFormContext ctx) {
        if (ctx.getPaymentMethod() != PaymentMethod.WALLET) {
            if (isVisible()) {
                clear();
                setVisible(false);
            }
            lastWalletId = null;
            return;
        }
        setVisible(true);
        Account wallet = wallet();
        Long walletId = wallet != null ? wallet.getId() : null;
        if (!Objects.equals(lastWalletId, walletId)) {
            lastWalletId = walletId;
            loadWalletData();
        }
    }

    /** Tarjetas vinculadas y cashback de la wallet actual; vuelve a "Saldo de la wallet". */
    private void loadWalletData() {
        balanceRadio.setSelected(true);
        linkedCardCombo.setItems(lastWalletId != null ? provider.getLinkedCards(lastWalletId) : List.of());

        Optional<AccountCashbackSetting> setting = lastWalletId != null
                ? provider.getCashbackSetting(lastWalletId) : Optional.empty();
        boolean cashbackEnabled = setting.map(AccountCashbackSetting::isCashbackEnabled).orElse(false);
        BigDecimal rate = setting.filter(AccountCashbackSetting::isCashbackEnabled)
                .map(AccountCashbackSetting::getDefaultCashbackRate).orElse(null);
        cashbackField.setFraction(rate != null ? rate.doubleValue() : null);
        setCashbackVisible(cashbackEnabled);

        updateSourceVisibility();
    }

    private void setCashbackVisible(boolean visible) {
        setRowVisible(cashbackField, visible);
        setRowVisible(cashbackHelp, visible);
    }

    private void updateSourceVisibility() {
        boolean linked = linkedCardRadio.isSelected();
        setRowVisible(linkedCardCombo, linked);
        if (!linked) {
            linkedCardCombo.clearSelection();
        }
        updateChargeHelp();
        refreshLayout();
    }

    /** "Se cargará a: ..." según la fuente elegida. */
    private void updateChargeHelp() {
        Account wallet = wallet();
        if (wallet == null) {
            chargeHelp.setText("Seleccione la wallet como cuenta origen.");
            return;
        }
        if (!linkedCardRadio.isSelected()) {
            chargeHelp.setText("Se cargará a: " + wallet.getName() + " (saldo de la wallet).");
            return;
        }
        if (linkedCardCombo.getItemCount() == 0) {
            chargeHelp.setText("Esta wallet no tiene tarjetas vinculadas. Vincúlelas desde Cuentas.");
            return;
        }
        Card card = linkedCardCombo.getSelectedItem();
        if (card == null) {
            chargeHelp.setText("Seleccione la tarjeta vinculada con la que se pagó.");
            return;
        }
        Account cardAccount = provider.findAccount(card.getAccountId());
        chargeHelp.setText("Se cargará a: " + (cardAccount != null ? cardAccount.toString()
                : "la cuenta #" + card.getAccountId()) + ".");
    }

    // ------------------------------------------------------------------
    // Contrato de sección
    // ------------------------------------------------------------------

    @Override
    public void validate(List<String> errors) {
        if (!isActive()) {
            return;
        }
        if (wallet() == null) {
            errors.add("Para pagar con wallet, la cuenta origen debe ser de tipo \"" + AccountTypes.WALLET.getLabel() + "\".");
        }
        if (linkedCardRadio.isSelected() && linkedCardCombo.getSelectedItem() == null) {
            errors.add("Debe seleccionar la tarjeta vinculada con la que se pagó.");
        }
    }

    @Override
    public void applyTo(Transaction tx) {
        if (!isActive()) {
            tx.setWalletDetail(null);
            return;
        }
        WalletTransactionDetail detail = new WalletTransactionDetail();
        detail.setWalletAccountId(wallet().getId());
        if (linkedCardRadio.isSelected()) {
            detail.setSourceType(WalletTransactionSourceType.LINKED_CARD);
            detail.setCardId(linkedCardCombo.getSelectedItem().getId());
        } else {
            detail.setSourceType(WalletTransactionSourceType.WALLET_BALANCE);
            detail.setCardId(null);
        }
        detail.setCashbackRate(cashbackField.isVisible() ? BigDecimal.valueOf(cashbackField.getFraction()) : null);
        tx.setWalletDetail(detail);
    }

    @Override
    public void loadFrom(Transaction tx) {
        WalletTransactionDetail detail = tx.getWalletDetail();
        if (detail == null || !isActive()) {
            return;
        }
        boolean linked = detail.getSourceType() == WalletTransactionSourceType.LINKED_CARD;
        linkedCardRadio.setSelected(linked);
        balanceRadio.setSelected(!linked);
        updateSourceVisibility();
        if (linked && detail.getCardId() != null) {
            long cardId = detail.getCardId();
            selectInCombo(linkedCardCombo, c -> c.getId() == cardId);
        }
        BigDecimal rate = detail.getCashbackRate();
        if (rate != null && rate.signum() > 0) {
            setCashbackVisible(true);
        }
        if (rate != null) {
            cashbackField.setFraction(rate.doubleValue());
        }
        updateChargeHelp();
    }

    @Override
    public void clear() {
        balanceRadio.setSelected(true);
        linkedCardCombo.clearSelection();
        cashbackField.clear();
        lastWalletId = null;
        updateSourceVisibility();
    }
}
