package com.giozar04.shared.components.forms;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalDate;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Campo de fecha reutilizable que envuelve un selector visual de Día/Mes/Año
 * y sigue el patrón de FormField/FormComboBox del sistema.
 *
 * Devuelve y acepta fechas en formato "yyyy-MM-dd" (compatible con la BD y con
 * {@code java.sql.Date.valueOf()}).
 *
 * Uso:
 *   FormDateField field = new FormDateField("Fecha de inicio:", 400, 40);
 *   String iso = field.getDateString();   // "2026-12-26"
 *   field.setDateString("2026-12-26");
 *   field.clearToToday();                 // restablece a la fecha actual
 *   field.clearToNull();                  // deja el campo sin valor (null)
 */
public class FormDateField extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final String[] MONTHS = {
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    private static final int[] DAYS_IN_MONTH = {
        31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };

    private final JLabel         label;
    private final JSpinner       daySpinner;
    private final JComboBox<String> monthCombo;
    private final JSpinner       yearSpinner;

    private boolean hasValue = false;

    /**
     * @param labelText texto de la etiqueta izquierda
     * @param width     ancho total del componente
     * @param height    alto del panel
     */
    public FormDateField(String labelText, int width, int height) {
        setLayout(new BorderLayout(5, 5));
        setOpaque(false);

        /* ── Etiqueta izquierda ── */
        label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(200, 25));
        add(label, BorderLayout.WEST);

        /* ── Controles de fecha ── */
        JPanel datePanel = new JPanel(new BorderLayout(4, 0));
        datePanel.setOpaque(false);

        LocalDate today = LocalDate.now();

        daySpinner = new JSpinner(new SpinnerNumberModel(today.getDayOfMonth(), 1, 31, 1));
        daySpinner.setEditor(new JSpinner.NumberEditor(daySpinner, "00"));
        daySpinner.setPreferredSize(new Dimension(55, height - 10));

        monthCombo = new JComboBox<>(MONTHS);
        monthCombo.setSelectedIndex(today.getMonthValue() - 1);
        monthCombo.setPrototypeDisplayValue("Septiembre");

        yearSpinner = new JSpinner(new SpinnerNumberModel(today.getYear(), 1900, 2200, 1));
        JSpinner.NumberEditor yearEditor = new JSpinner.NumberEditor(yearSpinner, "####");
        yearSpinner.setEditor(yearEditor);
        yearSpinner.setPreferredSize(new Dimension(70, height - 10));

        JPanel row = new JPanel();
        row.setOpaque(false);
        row.add(new JLabel("Día: "));
        row.add(daySpinner);
        row.add(new JLabel("  Mes: "));
        row.add(monthCombo);
        row.add(new JLabel("  Año: "));
        row.add(yearSpinner);

        datePanel.add(row, BorderLayout.WEST);
        add(datePanel, BorderLayout.CENTER);

        /* ── Actualizar máximo de días al cambiar mes/año ── */
        monthCombo.addActionListener(e -> adjustDays());
        yearSpinner.addChangeListener(e  -> adjustDays());

        /* ── Tamaño del panel ── */
        Dimension size = new Dimension(width, height);
        setMaximumSize(size);
        setPreferredSize(size);

        hasValue = true;
    }

    // ── Lógica interna ──────────────────────────────────────────────────────

    private void adjustDays() {
        int month = monthCombo.getSelectedIndex();
        int year  = (Integer) yearSpinner.getValue();
        int max   = DAYS_IN_MONTH[month];
        if (month == 1 && isLeapYear(year)) max = 29;

        SpinnerNumberModel m = (SpinnerNumberModel) daySpinner.getModel();
        m.setMaximum(max);
        int current = (Integer) daySpinner.getValue();
        if (current > max) daySpinner.setValue(max);
    }

    private boolean isLeapYear(int y) {
        return (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0);
    }

    // ── API pública ──────────────────────────────────────────────────────────

    /**
     * Devuelve la fecha seleccionada en formato "yyyy-MM-dd",
     * o {@code null} si el campo fue limpiado con {@link #clearToNull()}.
     */
    public String getDateString() {
        if (!hasValue) return null;
        int day   = (Integer) daySpinner.getValue();
        int month = monthCombo.getSelectedIndex() + 1;
        int year  = (Integer) yearSpinner.getValue();
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    /**
     * Carga una fecha en formato "yyyy-MM-dd".
     * Si el valor es null o vacío, no cambia el estado visible.
     */
    public void setDateString(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return;
        try {
            LocalDate d = LocalDate.parse(dateStr.trim());
            yearSpinner.setValue(d.getYear());
            monthCombo.setSelectedIndex(d.getMonthValue() - 1);
            daySpinner.setValue(d.getDayOfMonth());
            hasValue = true;
            adjustDays();
        } catch (Exception ignored) { /* fecha inválida: se ignora */ }
    }

    /** Restablece al día actual (uso típico en "Limpiar formulario"). */
    public void clearToToday() {
        LocalDate today = LocalDate.now();
        yearSpinner.setValue(today.getYear());
        monthCombo.setSelectedIndex(today.getMonthValue() - 1);
        daySpinner.setValue(today.getDayOfMonth());
        hasValue = true;
    }

    /** Devuelve true si hay un valor establecido. */
    public boolean hasDate() {
        return hasValue;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        label.setEnabled(enabled);
        daySpinner.setEnabled(enabled);
        monthCombo.setEnabled(enabled);
        yearSpinner.setEnabled(enabled);
    }
}
