package com.giozar04.shared.components.forms;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Campo de porcentaje reutilizable.
 * - Muestra al usuario el valor en porcentaje (0 % – 100 %).
 * - Incluye un slider visual sincronizado con el spinner.
 * - Internamente almacena y devuelve la fracción 0.0 – 1.0
 *   que requiere la base de datos (ej. 10.5 % → 0.105000).
 *
 * Uso:
 *   PercentageField field = new PercentageField("Tasa anual:", 400, 40);
 *   double fraction = field.getFraction();     // 0.105
 *   field.setFraction(0.105);                  // muestra 10.50 %
 */
public class PercentageField extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int   SLIDER_SCALE = 100;   // 1 tick = 0.01 % = fracción 0.0001
    private static final double MIN_PCT      = 0.0;
    private static final double MAX_PCT      = 100.0;

    private final JLabel      label;
    private final JSpinner    spinner;       // muestra % con 2 decimales
    private final JSlider     slider;        // rango 0 – 10 000 ticks (0.00 – 100.00 %)
    private final JLabel      pctLabel;     // símbolo "%"

    private boolean updating = false;        // evita bucle spinner ↔ slider

    /**
     * @param labelText texto de la etiqueta izquierda
     * @param width     ancho total del componente
     * @param height    alto del panel principal
     */
    public PercentageField(String labelText, int width, int height) {
        setLayout(new BorderLayout(5, 4));
        setOpaque(false);

        /* ── Etiqueta izquierda ── */
        label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(200, 25));
        add(label, BorderLayout.WEST);

        /* ── Panel central con spinner + "%" + slider ── */
        JPanel centerPanel = new JPanel(new BorderLayout(4, 2));
        centerPanel.setOpaque(false);

        // Spinner: 0.00 – 100.00, paso 0.01
        SpinnerNumberModel model = new SpinnerNumberModel(0.00, MIN_PCT, MAX_PCT, 0.01);
        spinner = new JSpinner(model);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0.00");
        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(80, height - 20));

        pctLabel = new JLabel(" %");

        JPanel spinnerRow = new JPanel(new BorderLayout(2, 0));
        spinnerRow.setOpaque(false);
        spinnerRow.add(spinner,  BorderLayout.CENTER);
        spinnerRow.add(pctLabel, BorderLayout.EAST);

        // Slider: 0 – 10 000 ticks (representa 0.00 % – 100.00 %)
        slider = new JSlider(0, SLIDER_SCALE * 100, 0);
        slider.setOpaque(false);
        slider.setPreferredSize(new Dimension(width - 320, 22));

        centerPanel.add(spinnerRow, BorderLayout.WEST);
        centerPanel.add(slider,    BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        /* ── Sincronización bidireccional ── */
        spinner.addChangeListener(e -> {
            if (updating) return;
            updating = true;
            double pct = ((Number) spinner.getValue()).doubleValue();
            slider.setValue((int) Math.round(pct * SLIDER_SCALE));
            updating = false;
        });

        slider.addChangeListener(e -> {
            if (updating) return;
            updating = true;
            double pct = slider.getValue() / (double) SLIDER_SCALE;
            // Redondear a 2 decimales para evitar artefactos de punto flotante
            spinner.setValue(Math.round(pct * 100.0) / 100.0);
            updating = false;
        });

        /* ── Tamaño del panel ── */
        Dimension size = new Dimension(width, height);
        setMaximumSize(size);
        setPreferredSize(size);
    }

    /** Devuelve la fracción interna (0.0 – 1.0). */
    public Double getFraction() {
        double pct = ((Number) spinner.getValue()).doubleValue();
        // Redondear a 6 decimales, igual que DECIMAL(9,6) de la BD
        return Math.round(pct * 10_000.0) / 1_000_000.0;
    }

    /**
     * Establece el valor a partir de la fracción almacenada en la BD
     * (ej. 0.105 → muestra 10.50 %).
     */
    public void setFraction(Double fraction) {
        if (fraction == null) {
            setPercentage(0.0);
            return;
        }
        setPercentage(fraction * 100.0);
    }

    /** Establece el valor directamente en porcentaje (ej. 10.5 → 10.50 %). */
    public void setPercentage(double pct) {
        double clamped = Math.max(MIN_PCT, Math.min(MAX_PCT, pct));
        double rounded = Math.round(clamped * 100.0) / 100.0;
        spinner.setValue(rounded);
        slider.setValue((int) Math.round(rounded * SLIDER_SCALE));
    }

    /** Devuelve el porcentaje como texto formateado (ej. "10.50 %"). */
    public String getFormattedPercentage() {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(((Number) spinner.getValue()).doubleValue()) + " %";
    }

    /** Restablece el campo a 0 %. */
    public void clear() {
        setPercentage(0.0);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        label.setEnabled(enabled);
        spinner.setEnabled(enabled);
        slider.setEnabled(enabled);
        pctLabel.setEnabled(enabled);
    }
}
