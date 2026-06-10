package com.giozar04.shared.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Componente visual de solo lectura que muestra el uso de crédito
 * de una cuenta de tipo CREDIT.
 *
 * <p>Responsabilidad única: renderizar la barra de progreso de crédito
 * con el porcentaje usado y el monto disponible. No edita nada.</p>
 *
 * <p>Uso:</p>
 * <pre>
 *   CreditUsagePanel panel = new CreditUsagePanel();
 *   panel.refresh(creditUsed, creditLimit);
 * </pre>
 */
public class CreditUsagePanel extends JPanel {

    private static final Color COLOR_OK    = new Color(46,  185, 110);
    private static final Color COLOR_OK2   = new Color(80,  220, 150);
    private static final Color COLOR_WARN  = new Color(240, 150,   0);
    private static final Color COLOR_WARN2 = new Color(255, 190,   0);
    private static final Color COLOR_DANGER  = new Color(220,  53,  53);
    private static final Color COLOR_DANGER2 = new Color(255,  90,  90);

    private final CreditBar    bar;
    private final JLabel       percentLabel;
    private final JLabel       availableLabel;
    private final JLabel       sectionLabel;

    public CreditUsagePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        sectionLabel = buildSectionLabel("CRÉDITO DISPONIBLE");
        sectionLabel.setAlignmentX(LEFT_ALIGNMENT);

        bar = new CreditBar();
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        bar.setPreferredSize(new Dimension(400, 18));
        bar.setAlignmentX(LEFT_ALIGNMENT);

        percentLabel = new JLabel("0%", SwingConstants.LEFT);
        percentLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        percentLabel.setForeground(new Color(100, 100, 110));
        percentLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
        percentLabel.setAlignmentX(LEFT_ALIGNMENT);

        availableLabel = new JLabel("Sin crédito registrado");
        availableLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        availableLabel.setForeground(new Color(80, 80, 90));
        availableLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        availableLabel.setAlignmentX(LEFT_ALIGNMENT);

        add(sectionLabel);
        add(Box.createRigidArea(new Dimension(0, 6)));
        add(bar);
        add(Box.createRigidArea(new Dimension(0, 4)));
        add(percentLabel);
        add(Box.createRigidArea(new Dimension(0, 2)));
        add(availableLabel);
    }

    /**
     * Actualiza el panel con los valores actuales de la cuenta.
     *
     * @param creditUsed  monto utilizado (puede ser null si la cuenta es nueva)
     * @param creditLimit límite de crédito configurado (puede ser null)
     */
    public void refresh(Double creditUsed, Double creditLimit) {
        if (creditUsed == null || creditLimit == null || creditLimit <= 0) {
            bar.setPercent(0.0);
            percentLabel.setText("0%");
            availableLabel.setText("Sin crédito registrado");
            percentLabel.setForeground(new Color(100, 100, 110));
            availableLabel.setForeground(new Color(80, 80, 90));
            repaint();
            return;
        }

        double used      = Math.max(0, creditUsed);
        double limit     = Math.max(0, creditLimit);
        double available = Math.max(0, limit - used);
        double pct       = Math.min(100.0, (used / limit) * 100.0);

        bar.setPercent(pct);
        percentLabel.setText(String.format("%.1f%% utilizado", pct));
        availableLabel.setText(String.format(
                "Disponible:  $%,.2f   /   Límite:  $%,.2f", available, limit));

        Color accent = pct >= 90 ? COLOR_DANGER
                     : pct >= 70 ? COLOR_WARN
                     : COLOR_OK;
        percentLabel.setForeground(accent);
        availableLabel.setForeground(accent.darker());

        revalidate();
        repaint();
    }

    /** Reinicia el panel a estado vacío. */
    public void clear() {
        refresh(null, null);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static JLabel buildSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(new Color(130, 130, 140));
        return lbl;
    }

    // =========================================================================
    // Barra de progreso custom con gradiente y bordes redondeados
    // =========================================================================

    private static class CreditBar extends JPanel {

        private double percent = 0.0;

        CreditBar() {
            setOpaque(false);
        }

        void setPercent(double percent) {
            this.percent = Math.max(0, Math.min(100, percent));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w   = getWidth();
            int h   = getHeight();
            int arc = h;

            // Track (fondo)
            g2.setColor(new Color(220, 220, 228));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // Relleno con gradiente
            int fillW = (int) ((w * percent) / 100.0);
            if (fillW > 0) {
                Color c1 = resolveStart();
                Color c2 = resolveEnd();
                g2.setPaint(new GradientPaint(0, 0, c1, fillW, 0, c2));
                g2.fillRoundRect(0, 0, Math.min(fillW, w), h, arc, arc);
            }

            // Borde sutil
            g2.setColor(new Color(190, 190, 200));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            g2.dispose();
        }

        private Color resolveStart() {
            if (percent >= 90) return COLOR_DANGER;
            if (percent >= 70) return COLOR_WARN;
            return COLOR_OK;
        }

        private Color resolveEnd() {
            if (percent >= 90) return COLOR_DANGER2;
            if (percent >= 70) return COLOR_WARN2;
            return COLOR_OK2;
        }
    }
}
