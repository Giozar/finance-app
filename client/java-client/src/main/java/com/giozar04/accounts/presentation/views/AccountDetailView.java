package com.giozar04.accounts.presentation.views;

import javax.swing.JPanel;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.presentation.views.detail.BenefitAccountDetailView;
import com.giozar04.accounts.presentation.views.detail.CashAccountDetailView;
import com.giozar04.accounts.presentation.views.detail.CreditAccountDetailView;
import com.giozar04.accounts.presentation.views.detail.DebitAccountDetailView;
import com.giozar04.accounts.presentation.views.detail.InvestmentAccountDetailView;
import com.giozar04.accounts.presentation.views.detail.SavingsAccountDetailView;
import com.giozar04.accounts.presentation.views.detail.WalletAccountDetailView;

/**
 * Factory que selecciona la vista de detalle correcta según el tipo de cuenta.
 *
 * <p>Principios SOLID:</p>
 * <ul>
 *   <li><b>OCP:</b> añadir un nuevo tipo de cuenta solo requiere crear una nueva
 *       subclase de {@code BaseAccountDetailView} y agregar un caso aquí,
 *       sin modificar ninguna vista existente.</li>
 *   <li><b>DIP:</b> {@link AccountsView} depende de este factory (abstracción),
 *       no de las clases concretas de detalle.</li>
 * </ul>
 */
public final class AccountDetailView {

    private AccountDetailView() {
        // Utility class — no instantiation
    }

    /**
     * Crea y retorna la vista de detalle apropiada para el tipo de cuenta recibido.
     *
     * @param account cuenta a mostrar (ya hidratada con todos sus campos)
     * @return panel de detalle específico para el tipo de cuenta
     */
    public static JPanel create(Account account) {
        if (account == null || account.getType() == null) {
            return new CashAccountDetailView(account);
        }

        return switch (account.getType()) {
            case CASH       -> new CashAccountDetailView(account);
            case DEBIT      -> new DebitAccountDetailView(account);
            case CREDIT     -> new CreditAccountDetailView(account);
            case WALLET     -> new WalletAccountDetailView(account);
            case BENEFIT    -> new BenefitAccountDetailView(account);
            case SAVINGS    -> new SavingsAccountDetailView(account);
            case INVESTMENT -> new InvestmentAccountDetailView(account);
        };
    }
}
