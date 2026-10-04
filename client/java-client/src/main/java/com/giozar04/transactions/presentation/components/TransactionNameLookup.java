package com.giozar04.transactions.presentation.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.infrastructure.services.AccountService;
import com.giozar04.card.domain.entities.Card;
import com.giozar04.cards.infrastructure.services.CardService;
import com.giozar04.categories.domain.entities.Category;
import com.giozar04.categories.infrastructure.services.CategoryService;
import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.externalEntities.infrastructure.services.ExternalEntityService;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.tags.domain.entities.Tag;
import com.giozar04.tags.infrastructure.services.TagService;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;
import com.giozar04.walletTransactionDetails.domain.enums.WalletTransactionSourceType;

/**
 * Mapas id → nombre para mostrar transacciones (listado y resumen) sin repetir peticiones por fila.
 *
 * <p>Cuentas, entidades y categorías se cargan con {@link #load()}; etiquetas y tarjetas solo cuando
 * se piden (las usa el resumen de detalle).</p>
 */
public class TransactionNameLookup {

    private static final ConsoleLogger logger = ConsoleLogger.getInstance();

    private final Map<Long, Account> accounts = new HashMap<>();
    private final Map<Long, String> entityNames = new HashMap<>();
    private final Map<Long, String> categoryNames = new HashMap<>();
    private Map<Long, String> tagNames;
    private Map<Long, Card> cards;

    /** Carga cuentas, entidades externas y categorías. */
    public static TransactionNameLookup load() throws ClientOperationException {
        TransactionNameLookup lookup = new TransactionNameLookup();
        for (Account account : AccountService.getInstance().getAllAccounts()) {
            lookup.accounts.put(account.getId(), account);
        }
        for (ExternalEntity entity : ExternalEntityService.getInstance().getAllExternalEntities()) {
            lookup.entityNames.put(entity.getId(), entity.getName());
        }
        for (Category category : CategoryService.getInstance().getAllCategories()) {
            lookup.categoryNames.put(category.getId(), category.getName());
        }
        return lookup;
    }

    /** Lookup vacío (muestra ids) para cuando la carga falla. */
    public static TransactionNameLookup empty() {
        return new TransactionNameLookup();
    }

    public String accountName(Long id) {
        if (id == null) {
            return "-";
        }
        Account account = accounts.get(id);
        return account != null ? account.getName() : "Cuenta #" + id;
    }

    public String entityName(Long id) {
        if (id == null) {
            return "-";
        }
        return entityNames.getOrDefault(id, "Entidad #" + id);
    }

    public String categoryName(long id) {
        return categoryNames.getOrDefault(id, id > 0 ? "Categoría #" + id : "-");
    }

    /** Origen: la entidad en ingresos; la cuenta (o la wallet) en el resto. */
    public String originOf(Transaction tx) {
        if (tx.getOperationType() == OperationTypes.INCOME) {
            return entityName(tx.getExternalEntityId());
        }
        WalletTransactionDetail wallet = tx.getWalletDetail();
        if (wallet != null) {
            String walletName = accountName(wallet.getWalletAccountId());
            if (wallet.getSourceType() == WalletTransactionSourceType.LINKED_CARD) {
                return walletName + " (vía " + accountName(tx.getSourceAccountId()) + ")";
            }
            return walletName;
        }
        return accountName(tx.getSourceAccountId());
    }

    /** Destino: la entidad en egresos; la cuenta destino en el resto. */
    public String destinationOf(Transaction tx) {
        if (tx.getOperationType() == OperationTypes.EXPENSE) {
            return entityName(tx.getExternalEntityId());
        }
        return accountName(tx.getDestinationAccountId());
    }

    /** Nombres de las etiquetas, separados por comas (carga perezosa). */
    public String tagNames(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return "-";
        }
        if (tagNames == null) {
            tagNames = new HashMap<>();
            try {
                for (Tag tag : TagService.getInstance().getAllTags()) {
                    tagNames.put(tag.getId(), tag.getName());
                }
            } catch (ClientOperationException | RuntimeException e) {
                logger.error("No se pudieron cargar las etiquetas: " + e.getMessage());
            }
        }
        return tagIds.stream()
                .map(id -> tagNames.getOrDefault(id, "#" + id))
                .collect(Collectors.joining(", "));
    }

    /** Nombre de la tarjeta (carga perezosa). */
    public String cardName(Long cardId) {
        if (cardId == null) {
            return "-";
        }
        if (cards == null) {
            cards = new HashMap<>();
            try {
                for (Card card : CardService.getInstance().getAllCards()) {
                    cards.put(card.getId(), card);
                }
            } catch (ClientOperationException | RuntimeException e) {
                logger.error("No se pudieron cargar las tarjetas: " + e.getMessage());
            }
        }
        Card card = cards.get(cardId);
        return card != null ? card.toString() : "Tarjeta #" + cardId;
    }
}
