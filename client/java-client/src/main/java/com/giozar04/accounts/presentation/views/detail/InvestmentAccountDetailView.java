package com.giozar04.accounts.presentation.views.detail;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.giozar04.accounts.domain.entities.Account;

/**
 * Vista de detalle para cuentas de tipo <b>INVESTMENT (Inversión a Plazo)</b>.
 * Muestra:
 * <ul>
 *   <li>Instrumento y estado</li>
 *   <li>Montos (capital, tasa anual, base de días)</li>
 *   <li>Fechas (inicio, vencimiento)</li>
 *   <li>Reinversión automática (si aplica)</li>
 * </ul>
 */
public class InvestmentAccountDetailView extends BaseAccountDetailView {

    public InvestmentAccountDetailView(Account account) {
        super(account);
    }

    @Override
    protected JPanel buildContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // Instrumento
        panel.add(buildSection("Instrumento"));
        panel.add(buildRow("Tipo de instrumento", account.getInstrumentType()));
        panel.add(buildRow("Estado",              account.getInvestmentStatus()));

        String termStr = account.getTermDays() != null
                ? account.getTermDays() + " días"
                : "Sin plazo fijo";
        panel.add(buildRow("Plazo",               termStr));

        // Montos
        addGap(panel);
        panel.add(buildSection("Montos"));

        String principalStr = account.getPrincipalAmount() != null
                ? String.format("$%,.2f", account.getPrincipalAmount())
                : null;
        panel.add(buildRow("Capital invertido",   principalStr));

        String yieldStr = account.getInvestmentAnnualYield() != null
                ? String.format("%.2f%%", account.getInvestmentAnnualYield() * 100)
                : null;
        panel.add(buildRow("Tasa anual",          yieldStr));

        String basisStr = account.getDayCountBasis() != null
                ? account.getDayCountBasis() + " días"
                : null;
        panel.add(buildRow("Base días",           basisStr));

        // Fechas
        addGap(panel);
        panel.add(buildSection("Fechas"));
        panel.add(buildRow("Fecha de inicio",     account.getStartDate()));
        panel.add(buildRow("Fecha de vencimiento",account.getMaturityDate()));

        // Reinversión
        boolean autoReinvest = Boolean.TRUE.equals(account.getAutoReinvest());
        addGap(panel);
        panel.add(buildSection("Reinversión automática"));
        panel.add(buildRow("Reinvertir al vencer", autoReinvest ? "Sí" : "No"));

        if (autoReinvest) {
            String reinvestTerm = account.getReinvestTermDays() != null
                    ? account.getReinvestTermDays() + " días"
                    : "Mismo plazo";
            String reinvestYield = account.getReinvestAnnualYield() != null
                    ? String.format("%.2f%%", account.getReinvestAnnualYield() * 100)
                    : "Misma tasa";
            panel.add(buildRow("Plazo de reinversión", reinvestTerm));
            panel.add(buildRow("Tasa de reinversión",  reinvestYield));
        }

        return panel;
    }
}
