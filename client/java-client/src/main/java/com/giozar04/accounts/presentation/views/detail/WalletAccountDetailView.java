package com.giozar04.accounts.presentation.views.detail;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;
import com.giozar04.walletCardLinks.infrastructure.services.WalletCardLinkService;

/**
 * Vista de detalle para cuentas de tipo <b>WALLET (Billetera Virtual)</b>.
 *
 * <p>Muestra:</p>
 * <ul>
 *   <li>Balance disponible</li>
 *   <li>Datos bancarios vinculados</li>
 *   <li>Tarjetas asociadas (cargadas desde {@link WalletCardLinkService})</li>
 * </ul>
 */
public class WalletAccountDetailView extends BaseAccountDetailView {

    public WalletAccountDetailView(Account account) {
        super(account);
    }

    @Override
    protected JPanel buildContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // Balance
        panel.add(buildSection("Información general"));
        panel.add(buildRow("Balance disponible", String.format("$%,.2f", account.getCurrentBalance())));

        // Datos bancarios
        addGap(panel);
        panel.add(buildSection("Datos bancarios"));
        panel.add(buildRow("Número de cuenta", account.getAccountNumber()));
        panel.add(buildRow("CLABE",            account.getClabe()));

        // Tarjetas vinculadas
        addGap(panel);
        panel.add(buildSection("Tarjetas vinculadas"));
        panel.add(buildLinkedCardsPanel());

        return panel;
    }

    /**
     * Carga los vínculos de tarjeta del servidor y los renderiza.
     * Maneja errores de red mostrando un mensaje apropiado.
     */
    private JPanel buildLinkedCardsPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setAlignmentX(LEFT_ALIGNMENT);

        try {
            List<WalletCardLink> links = WalletCardLinkService.getInstance()
                    .getAllByWalletId(account.getId());

            if (links == null || links.isEmpty()) {
                container.add(buildInfoLabel("Sin tarjetas vinculadas a esta billetera."));
            } else {
                for (WalletCardLink link : links) {
                    container.add(buildCardChip(link));
                    container.add(Box.createRigidArea(new Dimension(0, 6)));
                }
            }

        } catch (ClientOperationException e) {
            container.add(buildInfoLabel("No se pudieron cargar las tarjetas: " + e.getMessage()));
        }

        return container;
    }

    /**
     * "Chip" visual para cada tarjeta vinculada.
     */
    private JPanel buildCardChip(WalletCardLink link) {
        JPanel chip = new JPanel();
        chip.setLayout(new BoxLayout(chip, BoxLayout.X_AXIS));
        chip.setOpaque(true);
        chip.setBackground(new Color(240, 240, 248));
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 225), 1, true),
                new EmptyBorder(6, 12, 6, 12)));
        chip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        chip.setAlignmentX(LEFT_ALIGNMENT);

        JLabel icon = new JLabel("💳");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JLabel info = new JLabel(String.format("  Tarjeta ID: %d   (Vínculo #%d)",
                link.getCardId(), link.getId()));
        info.setFont(new Font("SansSerif", Font.PLAIN, 12));
        info.setForeground(new Color(40, 40, 60));

        chip.add(icon);
        chip.add(info);
        chip.add(Box.createHorizontalGlue());
        return chip;
    }

    private JLabel buildInfoLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lbl.setForeground(new Color(130, 130, 145));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }
}
