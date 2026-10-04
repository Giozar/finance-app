package com.giozar04.shared.components.forms;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import com.giozar04.shared.components.DatePickerComponent;

/**
 * Campo de fecha y hora: etiqueta + {@link DatePickerComponent} (día/mes/año) + {@code JSpinner} HH:mm.
 *
 * <p>Trabaja siempre en la zona horaria del sistema ({@link #getZoneId()}). Por defecto muestra la fecha
 * y hora actuales.</p>
 *
 * Uso:
 * <pre>
 *   FormDateTimeField dateTimeField = new FormDateTimeField("Fecha y hora:");
 *   ZonedDateTime when = dateTimeField.getDateTime();
 *   dateTimeField.setDateTime(transaction.getDate());
 *   dateTimeField.clearToNow();
 * </pre>
 */
public class FormDateTimeField extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_WIDTH = 620;
    private static final int DEFAULT_HEIGHT = 40;

    private final JLabel label;
    private final DatePickerComponent datePicker;
    private final JSpinner timeSpinner;
    private final ZoneId zoneId = ZoneId.systemDefault();

    public FormDateTimeField(String labelText) {
        this(labelText, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public FormDateTimeField(String labelText, int width, int height) {
        setLayout(new BorderLayout(5, 5));
        setOpaque(false);

        label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(150, 25));

        datePicker = new DatePickerComponent();
        datePicker.setOpaque(false);

        timeSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));
        timeSpinner.setPreferredSize(new Dimension(70, Math.max(25, height - 14)));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        row.setOpaque(false);
        row.add(datePicker);
        row.add(new JLabel("Hora:"));
        row.add(timeSpinner);

        add(label, BorderLayout.WEST);
        add(row, BorderLayout.CENTER);

        Dimension size = new Dimension(width, height);
        setMaximumSize(size);
        setPreferredSize(size);

        clearToNow();
    }

    // ── API pública ──────────────────────────────────────────────────────────

    /** Fecha y hora seleccionadas (segundos en cero) en la zona del sistema. */
    public ZonedDateTime getDateTime() {
        LocalDate date = datePicker.getDate().toLocalDate();
        LocalTime time = LocalDateTime.ofInstant(((Date) timeSpinner.getValue()).toInstant(), zoneId)
                .toLocalTime()
                .withSecond(0)
                .withNano(0);
        return ZonedDateTime.of(date, time, zoneId);
    }

    /**
     * Carga una fecha y hora. Se convierte a la zona del sistema conservando el instante;
     * con {@code null} se restablece a la fecha y hora actuales.
     */
    public void setDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) {
            clearToNow();
            return;
        }
        ZonedDateTime local = dateTime.withZoneSameInstant(zoneId);
        datePicker.setDate(local);
        setTime(local.toLocalTime());
    }

    /** Restablece a la fecha y hora actuales. */
    public void clearToNow() {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        datePicker.setDate(now);
        setTime(now.toLocalTime());
    }

    /** Zona horaria con la que trabaja el campo (la del sistema). */
    public ZoneId getZoneId() {
        return zoneId;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        label.setEnabled(enabled);
        datePicker.setEnabled(enabled);
        timeSpinner.setEnabled(enabled);
    }

    // ── Lógica interna ───────────────────────────────────────────────────────

    private void setTime(LocalTime time) {
        LocalTime minutes = time.withSecond(0).withNano(0);
        Date value = Date.from(LocalDate.now(zoneId).atTime(minutes).atZone(zoneId).toInstant());
        timeSpinner.setValue(value);
    }
}
