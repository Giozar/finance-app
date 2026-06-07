package com.giozar04.cards.test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.accounts.infrastructure.services.AccountService;
import com.giozar04.card.domain.entities.Card;
import com.giozar04.card.domain.enums.CardTypes;
import com.giozar04.cards.infrastructure.services.CardService;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.infrastructure.services.UserService;

public class CardCreationTest {

    public static void main(String[] args) throws IOException, InterruptedException, ClientOperationException {
        System.out.println("=== TEST: Creación de Tarjeta ===\n");

        ServerConnectionService conn = ServerConnectionService.getInstance("127.0.0.1", 8080);
        conn.connect();
        System.out.println("[OK] Conectado al servidor.");

        UserService    userService    = UserService.connectService(conn);
        AccountService accountService = AccountService.connectService(conn);
        CardService    cardService    = CardService.connectService(conn);

        // Obtener usuarios
        List<User> users = userService.getAllUsers();
        System.out.println("[OK] Usuarios: " + users.size());
        if (users.isEmpty()) { System.err.println("[ERROR] Sin usuarios."); return; }
        User user = users.get(0);
        System.out.println("     id=" + user.getId() + "  nombre=" + user.getName());

        // Obtener cuentas eligibles
        List<Account> allAccounts = accountService.getAllAccounts();
        System.out.println("[OK] Total cuentas: " + allAccounts.size());

        Account eligible = null;
        for (Account acc : allAccounts) {
            System.out.println("     Cuenta: id=" + acc.getId() + " userId=" + acc.getUserId()
                    + " tipo=" + acc.getType() + " nombre=" + acc.getName());
            if (eligible == null && acc.getUserId() == user.getId()
                    && (acc.getType() == AccountTypes.DEBIT
                        || acc.getType() == AccountTypes.CREDIT
                        || acc.getType() == AccountTypes.BENEFIT)) {
                eligible = acc;
            }
        }

        if (eligible == null) {
            System.err.println("[ERROR] El usuario no tiene cuentas DEBIT/CREDIT/BENEFIT.");
            return;
        }
        System.out.println("\n[OK] Cuenta elegida: id=" + eligible.getId()
                + " tipo=" + eligible.getType() + " nombre=" + eligible.getName());

        // Construir tarjeta igual que el formulario
        Card card = new Card();
        card.setName("Tarjeta Test");
        card.setCardType(CardTypes.PHYSICAL);
        card.setCardNumber("1234");
        card.setAccountId(eligible.getId());
        card.setExpirationDate(ZonedDateTime.now().plusYears(2));
        card.setCreatedAt(ZonedDateTime.now());
        card.setUpdatedAt(ZonedDateTime.now());

        System.out.println("\n[INFO] Enviando CREATE_CARD...");
        try {
            Card created = cardService.createCard(card);
            System.out.println("[EXITO] Tarjeta creada con ID: " + created.getId());
        } catch (Exception e) {
            System.err.println("[FALLO] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            Throwable cause = e.getCause();
            while (cause != null) {
                System.err.println("  Causado por: " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
                cause = cause.getCause();
            }
        }

        conn.disconnect();
    }
}
