package com.giozar04.accounts.presentation.components.subpanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.shared.components.forms.FormComboBox;
import com.giozar04.shared.components.forms.FormDateField;
import com.giozar04.shared.components.forms.FormField;
import com.giozar04.shared.components.forms.PercentageField;
import com.giozar04.shared.utils.FormValidatorUtils;

/**
 * Subpanel para campos de {@code investment_details} (tipo INVESTMENT).
 *
 * <p>Sigue la interfaz pública de todos los subpaneles del sistema:
 * {@code validate(errors)}, {@code applyTo(account)}, {@code loadFrom(account)}, {@code clear()}.
 *
 * <h3>Decisiones de UX</h3>
 * <ul>
 *   <li><b>Tipo de instrumento:</b> selector desplegable con las opciones más comunes.</li>
 *   <li><b>Base de cálculo:</b> selector 360 / 365 con explicación contextual.</li>
 *   <li><b>Estado:</b> selector con ACTIVE seleccionado por defecto.</li>
 *   <li><b>Tasas:</b> {@link PercentageField} → el usuario ve "10.50 %", la BD recibe 0.105000.</li>
 *   <li><b>Fechas:</b> {@link FormDateField} con selectors Día/Mes/Año (no texto libre).</li>
 *   <li><b>Reinversión automática:</b> {@code JCheckBox} tipo switch semántico.</li>
 *   <li><b>Sección reinversión:</b> se oculta automáticamente si no hay reinversión.</li>
 * </ul>
 */
public class InvestmentDetailsSubPanel extends JPanel {

    // ── Instrumentos ────────────────────────────────────────────────────────
    private static final String[] INSTRUMENT_TYPES = {
        "CETES", "BONDDIA", "BONDESD", "BONOS", "UDIBONOS", "SOFIPOS", "OTRO"
    };

    // ── Estado de la inversión ───────────────────────────────────────────────
    private static final String[] STATUSES = { "ACTIVE", "MATURED", "CANCELLED" };

    // ── Base de cálculo ──────────────────────────────────────────────────────
    private static final String DAYS_360 = "360 días (CETES, mercado de dinero MX)";
    private static final String DAYS_365 = "365 días (bonos corporativos, renta variable)";

    // ── Campos del instrumento ───────────────────────────────────────────────
    private final FormComboBox<String> instrumentTypeCombo;
    private final FormField            termDaysField;
    private final FormField            principalAmountField;
    private final PercentageField      annualYieldField;
    private final FormComboBox<String> dayCountBasisCombo;

    // ── Fechas ───────────────────────────────────────────────────────────────
    private final FormDateField startDateField;
    private final FormDateField maturityDateField;

    // ── Ciclo de vida ────────────────────────────────────────────────────────
    private final FormComboBox<String> statusCombo;

    // ── Reinversión ──────────────────────────────────────────────────────────
    private final JCheckBox       autoReinvestCheck;
    private final JPanel          reinvestSection;
    private final FormField       reinvestTermDaysField;
    private final PercentageField reinvestYieldField;

    // ── Helpers de layout ────────────────────────────────────────────────────
    private static final int FIELD_W = 600;
    private static final int FIELD_H = 40;
    private static final int GAP     = 10;

    // ============================================================
    // Constructor
    // ============================================================

    public InvestmentDetailsSubPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(180, 180, 200), 1, true),
                        " 📈 Detalles de la Inversión",
                        0, 0,
                        new Font("SansSerif", Font.BOLD, 13),
                        new Color(70, 100, 160)),
                BorderFactory.createEmptyBorder(8, 12, 12, 12)));

        // ── Instrumento ─────────────────────────────────────────
        addSectionTitle("Instrumento");

        instrumentTypeCombo = new FormComboBox<>("Tipo de instrumento:", FIELD_W, FIELD_H);
        instrumentTypeCombo.setItems(List.of(INSTRUMENT_TYPES));
        instrumentTypeCombo.setSelectedItem("CETES");
        add(instrumentTypeCombo);
        addHint("CETES: certificados gubernamentales. BONDDIA: fondo de dinero diario. Selecciona el que corresponda.");

        addGap();
        termDaysField = new FormField("Plazo (días):", false, FIELD_W, FIELD_H);
        add(termDaysField);
        addHint("Ingresa el plazo del instrumento en días (ej. 28, 91, 182, 364). Deja vacío para instrumentos sin plazo fijo como BONDDIA.");

        // ── Capital y rendimiento ────────────────────────────────
        addGap();
        addSectionTitle("Capital y Rendimiento");

        principalAmountField = new FormField("Capital invertido ($):", false, FIELD_W, FIELD_H);
        add(principalAmountField);
        addHint("Monto fijo que colocas en esta inversión. No cambia durante el plazo (ej. 10000.00).");

        addGap();
        annualYieldField = new PercentageField("Tasa anual bruta:", FIELD_W, FIELD_H + 10);
        add(annualYieldField);
        addHint("Tasa de rendimiento anual. Mueve el slider o escribe el porcentaje (ej. 10.50 % → BD guarda 0.105000).");

        addGap();
        dayCountBasisCombo = new FormComboBox<>("Base de cálculo:", FIELD_W, FIELD_H);
        dayCountBasisCombo.setItems(List.of(DAYS_360, DAYS_365));
        dayCountBasisCombo.setSelectedItem(DAYS_360);
        add(dayCountBasisCombo);
        addHint("Define el divisor para calcular el interés diario. CETES usa 360; instrumentos corporativos suelen usar 365.");

        // ── Fechas del plazo ────────────────────────────────────
        addGap();
        addSectionTitle("Plazo del instrumento");

        startDateField = new FormDateField("Fecha de inicio:", FIELD_W, FIELD_H + 10);
        add(startDateField);
        addHint("Fecha en la que la inversión entra en vigor.");

        addGap();
        maturityDateField = new FormDateField("Fecha de vencimiento:", FIELD_W, FIELD_H + 10);
        add(maturityDateField);
        addHint("Fecha en que la inversión vence y el capital queda disponible.");

        // ── Ciclo de vida ────────────────────────────────────────
        addGap();
        addSectionTitle("Estado y ciclo de vida");

        statusCombo = new FormComboBox<>("Estado:", FIELD_W, FIELD_H);
        statusCombo.setItems(List.of(STATUSES));
        statusCombo.setSelectedItem("ACTIVE");
        add(statusCombo);
        addHint("ACTIVE → vigente. MATURED → venció y fue liquidada. CANCELLED → cancelada antes de vencer.");

        // ── Reinversión automática ───────────────────────────────
        addGap();
        addSectionTitle("Reinversión automática");

        autoReinvestCheck = new JCheckBox("Activar reinversión automática al vencimiento");
        autoReinvestCheck.setOpaque(false);
        autoReinvestCheck.setFont(new Font("SansSerif", Font.PLAIN, 13));
        autoReinvestCheck.setAlignmentX(LEFT_ALIGNMENT);
        add(autoReinvestCheck);
        addHint("Si está activo, al vencimiento se reinvierte automáticamente en las mismas condiciones o en las indicadas abajo.");

        // Sección de parámetros de reinversión (visible solo si el check está activo)
        reinvestSection = new JPanel();
        reinvestSection.setLayout(new BoxLayout(reinvestSection, BoxLayout.Y_AXIS));
        reinvestSection.setOpaque(false);
        reinvestSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(100, 149, 237)),
                BorderFactory.createEmptyBorder(4, 12, 4, 0)));

        reinvestTermDaysField = new FormField("Plazo de reinversión (días):", false, FIELD_W - 20, FIELD_H);
        reinvestSection.add(reinvestTermDaysField);
        addHintTo(reinvestSection, "Plazo en días para la nueva posición. Deja vacío para usar el mismo plazo.");

        reinvestSection.add(Box.createRigidArea(new Dimension(0, GAP)));

        reinvestYieldField = new PercentageField("Tasa de reinversión anual:", FIELD_W - 20, FIELD_H + 10);
        reinvestSection.add(reinvestYieldField);
        addHintTo(reinvestSection, "Tasa anual para la reinversión (ej. 9.50 %). Deja en 0 % para usar la tasa original.");

        add(reinvestSection);

        // Controlar visibilidad de la sección de reinversión
        reinvestSection.setVisible(false);
        autoReinvestCheck.addActionListener(e -> reinvestSection.setVisible(autoReinvestCheck.isSelected()));
    }

    // ============================================================
    // Helpers de layout
    // ============================================================

    private void addSectionTitle(String title) {
        add(Box.createRigidArea(new Dimension(0, GAP)));

        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setMaximumSize(new Dimension(FIELD_W, 2));
        sep.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(new Color(70, 100, 160));
        lbl.setAlignmentX(LEFT_ALIGNMENT);

        add(lbl);
        add(sep);
        add(Box.createRigidArea(new Dimension(0, 4)));
    }

    private void addHint(String text) {
        addHintTo(this, text);
    }

    private void addHintTo(JPanel target, String text) {
        JPanel hintPanel = new JPanel(new BorderLayout());
        hintPanel.setOpaque(false);
        hintPanel.setMaximumSize(new Dimension(FIELD_W, 20));
        hintPanel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel hint = new JLabel("  ⓘ " + text);
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(new Color(130, 130, 150));
        hintPanel.add(hint, BorderLayout.WEST);

        target.add(hintPanel);
    }

    private void addGap() {
        add(Box.createRigidArea(new Dimension(0, GAP)));
    }

    // ============================================================
    // Contrato público (igual que los demás subpaneles)
    // ============================================================

    /** Valida los campos y añade errores a la lista. */
    public void validate(List<String> errors) {
        // Capital
        String principalStr = principalAmountField.getValue().trim();
        FormValidatorUtils.isRequired(principalStr, "Capital invertido", errors);
        if (!principalStr.isEmpty()) {
            try {
                double p = Double.parseDouble(principalStr);
                if (p <= 0) errors.add("El capital invertido debe ser mayor a cero.");
            } catch (NumberFormatException e) {
                errors.add("El capital invertido debe ser un número válido (ej. 10000.00).");
            }
        }

        // Tasa anual
        double fraction = annualYieldField.getFraction();
        if (fraction < 0 || fraction > 1) {
            errors.add("La tasa anual debe estar entre 0 % y 100 %.");
        }

        // Fechas
        String sd = startDateField.getDateString();
        String md = maturityDateField.getDateString();
        if (sd == null) errors.add("La fecha de inicio es obligatoria.");
        if (md == null) errors.add("La fecha de vencimiento es obligatoria.");
        if (sd != null && md != null && !(md.compareTo(sd) > 0)) {
            errors.add("La fecha de vencimiento debe ser posterior a la de inicio.");
        }

        // Plazo (opcional)
        String termStr = termDaysField.getValue().trim();
        if (!termStr.isEmpty()) {
            try {
                int t = Integer.parseInt(termStr);
                if (t <= 0) errors.add("El plazo debe ser mayor a cero.");
            } catch (NumberFormatException e) {
                errors.add("El plazo debe ser un número entero (ej. 28, 91, 182).");
            }
        }

        // Reinversión
        if (autoReinvestCheck.isSelected()) {
            String rTermStr = reinvestTermDaysField.getValue().trim();
            if (!rTermStr.isEmpty()) {
                try {
                    int rt = Integer.parseInt(rTermStr);
                    if (rt <= 0) errors.add("El plazo de reinversión debe ser mayor a cero.");
                } catch (NumberFormatException e) {
                    errors.add("El plazo de reinversión debe ser un número entero.");
                }
            }
            double rYield = reinvestYieldField.getFraction();
            if (rYield < 0 || rYield > 1) {
                errors.add("La tasa de reinversión debe estar entre 0 % y 100 %.");
            }
        }
    }

    /** Escribe los valores del panel en la entidad {@code Account}. */
    public void applyTo(Account account) {
        // Instrumento
        account.setInstrumentType(instrumentTypeCombo.getSelectedItem());

        String termStr = termDaysField.getValue().trim();
        account.setTermDays(termStr.isEmpty() ? null : Integer.valueOf(termStr));

        account.setPrincipalAmount(Double.valueOf(principalAmountField.getValue().trim()));
        account.setInvestmentAnnualYield(annualYieldField.getFraction());

        String basis = dayCountBasisCombo.getSelectedItem();
        account.setDayCountBasis(basis != null && basis.startsWith("365") ? 365 : 360);

        // Fechas
        account.setStartDate(startDateField.getDateString());
        account.setMaturityDate(maturityDateField.getDateString());

        // Estado
        account.setInvestmentStatus(statusCombo.getSelectedItem() != null
                ? statusCombo.getSelectedItem() : "ACTIVE");

        // Reinversión
        boolean autoReinvest = autoReinvestCheck.isSelected();
        account.setAutoReinvest(autoReinvest);

        if (autoReinvest) {
            String rTermStr = reinvestTermDaysField.getValue().trim();
            account.setReinvestTermDays(rTermStr.isEmpty() ? null : Integer.valueOf(rTermStr));
            double rYield = reinvestYieldField.getFraction();
            account.setReinvestAnnualYield(rYield > 0 ? rYield : null);
        } else {
            account.setReinvestTermDays(null);
            account.setReinvestAnnualYield(null);
        }
    }

    /** Carga los valores de la entidad {@code Account} en el panel (para edición). */
    public void loadFrom(Account account) {
        // Instrumento
        if (account.getInstrumentType() != null) {
            instrumentTypeCombo.setSelectedItem(account.getInstrumentType());
        }
        termDaysField.setValue(account.getTermDays() != null ? account.getTermDays().toString() : "");
        principalAmountField.setValue(account.getPrincipalAmount() != null
                ? account.getPrincipalAmount().toString() : "");
        annualYieldField.setFraction(account.getInvestmentAnnualYield());

        if (account.getDayCountBasis() != null && account.getDayCountBasis() == 365) {
            dayCountBasisCombo.setSelectedItem(DAYS_365);
        } else {
            dayCountBasisCombo.setSelectedItem(DAYS_360);
        }

        // Fechas
        if (account.getStartDate() != null) startDateField.setDateString(account.getStartDate());
        if (account.getMaturityDate() != null) maturityDateField.setDateString(account.getMaturityDate());

        // Estado
        if (account.getInvestmentStatus() != null) {
            statusCombo.setSelectedItem(account.getInvestmentStatus());
        } else {
            statusCombo.setSelectedItem("ACTIVE");
        }

        // Reinversión
        Boolean autoReinvest = account.getAutoReinvest();
        autoReinvestCheck.setSelected(Boolean.TRUE.equals(autoReinvest));
        reinvestSection.setVisible(Boolean.TRUE.equals(autoReinvest));

        reinvestTermDaysField.setValue(account.getReinvestTermDays() != null
                ? account.getReinvestTermDays().toString() : "");
        reinvestYieldField.setFraction(account.getReinvestAnnualYield());
    }

    /** Limpia todos los campos y restablece los valores por defecto. */
    public void clear() {
        instrumentTypeCombo.setSelectedItem("CETES");
        termDaysField.clear();
        principalAmountField.clear();
        annualYieldField.clear();
        dayCountBasisCombo.setSelectedItem(DAYS_360);
        startDateField.clearToToday();
        maturityDateField.clearToToday();
        statusCombo.setSelectedItem("ACTIVE");
        autoReinvestCheck.setSelected(false);
        reinvestSection.setVisible(false);
        reinvestTermDaysField.clear();
        reinvestYieldField.clear();
    }
}
