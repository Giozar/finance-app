package com.giozar04.accounts.presentation.views.detail;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.presentation.views.AccountsView;
import com.giozar04.shared.components.MainContentPanel;

/**
 * Clase base abstracta para todas las vistas de detalle de cuentas.
 *
 * <p>Aplica el patrón <b>Template Method</b>:</p>
 * <ul>
 *   <li>El algoritmo de construcción de la vista (header → scroll(content) → footer)
 *       está definido aquí y es invariable.</li>
 *   <li>Cada subclase especializada solo implementa {@link #buildContent()},
 *       que es la parte variable por tipo de cuenta.</li>
 * </ul>
 *
 * <p>Principios SOLID:</p>
 * <ul>
 *   <li><b>SRP:</b> esta clase solo se ocupa del esqueleto visual compartido.</li>
 *   <li><b>OCP:</b> se extiende con nuevos tipos de cuenta sin tocar esta clase.</li>
 *   <li><b>LSP:</b> cualquier subclase puede sustituir a ésta de forma transparente.</li>
 * </ul>
 */
public abstract class BaseAccountDetailView extends JPanel {

    protected final Account account;

    protected BaseAccountDetailView(Account account) {
        this.account = account;
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 24, 16, 24));

        // Content wrapped in scroll pane for long views (investment, wallet…)
        JPanel contentWrapper = new JPanel();
        contentWrapper.setLayout(new BoxLayout(contentWrapper, BoxLayout.Y_AXIS));
        contentWrapper.setOpaque(false);
        contentWrapper.setBorder(new EmptyBorder(12, 0, 24, 0));

        JPanel specific = buildContent();
        specific.setAlignmentX(LEFT_ALIGNMENT);
        contentWrapper.add(specific);

        JScrollPane scroll = new JScrollPane(contentWrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(buildHeader(), BorderLayout.NORTH);
        add(scroll,        BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // =========================================================================
    // Template method — subclases implementan solo esto
    // =========================================================================

    /**
     * Construye el contenido específico de este tipo de cuenta.
     * El resultado se insertará dentro de un JScrollPane con BoxLayout Y_AXIS.
     */
    protected abstract JPanel buildContent();

    // =========================================================================
    // Header compartido: nombre + tipo + balance
    // =========================================================================

    private JPanel buildHeader() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel title = new JLabel(account.getName() != null ? account.getName() : "—");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setAlignmentX(LEFT_ALIGNMENT);

        String typeText = account.getType() != null ? account.getType().getLabel() : "—";
        JLabel typeLabel = new JLabel(typeText.toUpperCase());
        typeLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        typeLabel.setForeground(new Color(130, 100, 200));
        typeLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel balanceLabel = new JLabel(String.format("$%,.2f", account.getCurrentBalance()));
        balanceLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        balanceLabel.setForeground(new Color(30, 30, 50));
        balanceLabel.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(typeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));
        panel.add(balanceLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));

        return panel;
    }

    // =========================================================================
    // Footer compartido: botón regresar
    // =========================================================================

    private JPanel buildFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton backButton = new JButton("← Regresar");
        backButton.addActionListener(e -> navigateBack());
        panel.add(backButton, BorderLayout.WEST);
        return panel;
    }

    // =========================================================================
    // Helpers de UI reutilizables por todas las subclases
    // =========================================================================

    /**
     * Etiqueta de sección con separador horizontal.
     */
    protected JPanel buildSection(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lbl = new JLabel(title.toUpperCase());
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(new Color(130, 130, 145));
        lbl.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(lbl);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));
        panel.add(Box.createRigidArea(new Dimension(0, 6)));
        return panel;
    }

    /**
     * Fila "Etiqueta: Valor" con alineación fija de la columna izquierda.
     */
    protected JPanel buildRow(String label, String value) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0; left.gridy = 0;
        left.anchor = GridBagConstraints.WEST;
        left.insets = new Insets(2, 0, 2, 12);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1; right.gridy = 0;
        right.anchor = GridBagConstraints.WEST;
        right.weightx = 1.0;
        right.fill = GridBagConstraints.HORIZONTAL;

        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(new Color(90, 90, 105));
        lbl.setPreferredSize(new Dimension(170, 20));

        JLabel val = new JLabel(value != null && !value.isBlank() ? value : "—");
        val.setFont(new Font("SansSerif", Font.PLAIN, 12));
        val.setForeground(new Color(25, 25, 40));

        row.add(lbl, left);
        row.add(val, right);
        return row;
    }

    /** Gap vertical entre secciones. */
    protected void addGap(JPanel target) {
        target.add(Box.createRigidArea(new Dimension(0, 16)));
    }

    // =========================================================================
    // Navegación
    // =========================================================================

    protected void navigateBack() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof MainContentPanel)) {
            parent = parent.getParent();
        }
        if (parent instanceof MainContentPanel main) {
            main.setView(new AccountsView());
        }
    }
}
