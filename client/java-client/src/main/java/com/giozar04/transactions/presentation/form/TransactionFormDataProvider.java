package com.giozar04.transactions.presentation.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.giozar04.accountCashbackSettings.domain.entities.AccountCashbackSetting;
import com.giozar04.accountCashbackSettings.application.ports.input.AccountCashbackSettingOperations;
import com.giozar04.bootstrap.ClientUseCases;
import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.accounts.application.ports.input.AccountOperations;
import com.giozar04.bootstrap.ClientUseCases;
import com.giozar04.card.domain.entities.Card;
import com.giozar04.card.domain.enums.CardTypes;
import com.giozar04.cards.application.ports.input.CardOperations;
import com.giozar04.bootstrap.ClientUseCases;
import com.giozar04.categories.domain.entities.Category;
import com.giozar04.categories.domain.enums.CategoryTypes;
import com.giozar04.categories.application.ports.input.CategoryOperations;
import com.giozar04.bootstrap.ClientUseCases;
import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.externalEntities.application.ports.input.ExternalEntityOperations;
import com.giozar04.bootstrap.ClientUseCases;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.tags.domain.entities.Tag;
import com.giozar04.tags.application.ports.input.TagOperations;
import com.giozar04.bootstrap.ClientUseCases;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;
import com.giozar04.walletCardLinks.application.ports.input.WalletCardLinkOperations;
import com.giozar04.bootstrap.ClientUseCases;

/**
 * Datos del formulario de transacciones (SRP): carga y cachea, por usuario, los catálogos que usan las
 * secciones. Solo usa los servicios del cliente; no conoce Swing.
 *
 * <ul>
 *   <li>Cuentas, categorías, etiquetas y entidades externas del usuario ({@code *_BY_USER}).</li>
 *   <li>Tarjetas de una cuenta ({@code GET_CARDS_BY_ACCOUNT}), opcionalmente por tipo (física/digital).</li>
 *   <li>Tarjetas vinculadas a una wallet ({@code WalletCardLinkOperations} → tarjetas).</li>
 *   <li>Configuración de cashback de una wallet ({@code AccountCashbackSettingOperations}).</li>
 *   <li>Categorías compatibles con una operación (mismo tipo o {@code BOTH}).</li>
 * </ul>
 *
 * <p>Los errores de carga se notifican al manejador de {@link #setOnError(Consumer)} y el método devuelve
 * una lista vacía, para que la UI decida cómo mostrarlos.</p>
 */
public class TransactionFormDataProvider {

    private static final ConsoleLogger logger = ConsoleLogger.getInstance();

    private Long userId;
    private List<Account> accounts = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private List<Tag> tags = new ArrayList<>();
    private List<ExternalEntity> externalEntities = new ArrayList<>();

    private final Map<Long, List<Card>> cardsByAccount = new HashMap<>();
    private final Map<Long, List<Card>> linkedCardsByWallet = new HashMap<>();
    private final Map<Long, Optional<AccountCashbackSetting>> cashbackByWallet = new HashMap<>();
    private Map<Long, Card> allCardsById;

    private Consumer<String> onError = message -> logger.error(message);

    /** Manejador de errores de carga (p. ej. un diálogo). */
    public void setOnError(Consumer<String> onError) {
        if (onError != null) {
            this.onError = onError;
        }
    }

    // ------------------------------------------------------------------
    // Usuario
    // ------------------------------------------------------------------

    /**
     * Carga los catálogos del usuario. Si es el mismo usuario ya cargado no hace nada;
     * con {@code null} vacía las cachés.
     */
    public void loadForUser(Long newUserId) {
        if (java.util.Objects.equals(userId, newUserId)) {
            return;
        }
        clearCaches();
        userId = newUserId;
        if (newUserId == null) {
            return;
        }
        try {
            accounts = new ArrayList<>(ClientUseCases.get(AccountOperations.class).getAccountsByUserId(newUserId));
        } catch (ClientOperationException | RuntimeException e) {
            reportError("Error al cargar las cuentas del usuario: " + e.getMessage());
        }
        try {
            categories = new ArrayList<>(ClientUseCases.get(CategoryOperations.class).getCategoriesByUserId(newUserId));
        } catch (ClientOperationException | RuntimeException e) {
            reportError("Error al cargar las categorías del usuario: " + e.getMessage());
        }
        try {
            tags = new ArrayList<>(ClientUseCases.get(TagOperations.class).getTagsByUserId(newUserId));
        } catch (ClientOperationException | RuntimeException e) {
            reportError("Error al cargar las etiquetas del usuario: " + e.getMessage());
        }
        try {
            externalEntities = new ArrayList<>(ClientUseCases.get(ExternalEntityOperations.class).getExternalEntitiesByUserId(newUserId));
        } catch (ClientOperationException | RuntimeException e) {
            reportError("Error al cargar las entidades externas del usuario: " + e.getMessage());
        }
    }

    public Long getUserId() {
        return userId;
    }

    // ------------------------------------------------------------------
    // Catálogos del usuario
    // ------------------------------------------------------------------

    public List<Account> getAccounts() {
        return new ArrayList<>(accounts);
    }

    /** Cuentas del usuario de un tipo; con {@code null} devuelve todas. */
    public List<Account> getAccounts(AccountTypes type) {
        if (type == null) {
            return getAccounts();
        }
        return accounts.stream().filter(a -> a.getType() == type).toList();
    }

    public Account findAccount(Long accountId) {
        if (accountId == null) {
            return null;
        }
        return accounts.stream().filter(a -> a.getId() == accountId).findFirst().orElse(null);
    }

    /** Categorías compatibles con la operación: mismo tipo o {@code BOTH}. Sin operación, ninguna. */
    public List<Category> getCategoriesFor(OperationTypes operation) {
        if (operation == null) {
            return List.of();
        }
        CategoryTypes type = toCategoryType(operation);
        return categories.stream()
                .filter(c -> c.getType() == type || c.getType() == CategoryTypes.BOTH)
                .toList();
    }

    public Category findCategory(long categoryId) {
        return categories.stream().filter(c -> c.getId() == categoryId).findFirst().orElse(null);
    }

    public List<Tag> getTags() {
        return new ArrayList<>(tags);
    }

    public List<Tag> findTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        return tags.stream().filter(t -> tagIds.contains(t.getId())).toList();
    }

    public List<ExternalEntity> getExternalEntities() {
        return new ArrayList<>(externalEntities);
    }

    public ExternalEntity findExternalEntity(Long entityId) {
        if (entityId == null) {
            return null;
        }
        return externalEntities.stream().filter(e -> e.getId() == entityId).findFirst().orElse(null);
    }

    /** Registra en la caché una categoría recién creada (alta rápida). */
    public void addCategory(Category category) {
        if (category != null && findCategory(category.getId()) == null) {
            categories.add(category);
        }
    }

    /** Registra en la caché una etiqueta recién creada (alta rápida). */
    public void addTag(Tag tag) {
        if (tag != null && tags.stream().noneMatch(t -> t.getId() == tag.getId())) {
            tags.add(tag);
        }
    }

    /** Registra en la caché una entidad externa recién creada (alta rápida). */
    public void addExternalEntity(ExternalEntity entity) {
        if (entity != null && findExternalEntity(entity.getId()) == null) {
            externalEntities.add(entity);
        }
    }

    /** Tipo de categoría equivalente a la operación. */
    public static CategoryTypes toCategoryType(OperationTypes operation) {
        return switch (operation) {
            case INCOME -> CategoryTypes.INCOME;
            case EXPENSE -> CategoryTypes.EXPENSE;
            case REALLOCATION -> CategoryTypes.REALLOCATION;
        };
    }

    // ------------------------------------------------------------------
    // Tarjetas
    // ------------------------------------------------------------------

    /** Tarjetas de una cuenta (cacheadas). */
    public List<Card> getCardsByAccount(long accountId) {
        List<Card> cached = cardsByAccount.get(accountId);
        if (cached == null) {
            try {
                cached = new ArrayList<>(ClientUseCases.get(CardOperations.class).getCardsByAccountId(accountId));
            } catch (ClientOperationException | RuntimeException e) {
                reportError("Error al cargar las tarjetas de la cuenta: " + e.getMessage());
                return List.of();
            }
            cardsByAccount.put(accountId, cached);
        }
        return new ArrayList<>(cached);
    }

    /** Tarjetas de una cuenta de un tipo (física o digital); con {@code null} devuelve todas. */
    public List<Card> getCardsByAccount(long accountId, CardTypes type) {
        List<Card> cards = getCardsByAccount(accountId);
        if (type == null) {
            return cards;
        }
        return cards.stream().filter(c -> c.getCardType() == type).toList();
    }

    /** Tarjetas vinculadas a una wallet ({@code wallet_card_links} → tarjetas), cacheadas. */
    public List<Card> getLinkedCards(long walletAccountId) {
        List<Card> cached = linkedCardsByWallet.get(walletAccountId);
        if (cached != null) {
            return new ArrayList<>(cached);
        }
        try {
            List<WalletCardLink> links = ClientUseCases.get(WalletCardLinkOperations.class).getAllByWalletId(walletAccountId);
            Map<Long, Card> cardsById = getAllCardsById();
            List<Card> cards = new ArrayList<>();
            for (WalletCardLink link : links) {
                Card card = cardsById.get(link.getCardId());
                if (card != null) {
                    cards.add(card);
                }
            }
            linkedCardsByWallet.put(walletAccountId, cards);
            return new ArrayList<>(cards);
        } catch (ClientOperationException | RuntimeException e) {
            reportError("Error al cargar las tarjetas vinculadas a la wallet: " + e.getMessage());
            return List.of();
        }
    }

    // ------------------------------------------------------------------
    // Cashback
    // ------------------------------------------------------------------

    /**
     * Configuración de cashback de una cuenta (cacheada). Vacío si la cuenta no tiene configuración
     * (el servicio lanza una excepción en ese caso, que aquí no es un error).
     */
    public Optional<AccountCashbackSetting> getCashbackSetting(long accountId) {
        return cashbackByWallet.computeIfAbsent(accountId, id -> {
            try {
                return Optional.ofNullable(ClientUseCases.get(AccountCashbackSettingOperations.class)
                        .getAccountCashbackSettingByAccountId(id));
            } catch (ClientOperationException | RuntimeException e) {
                logger.info("La cuenta " + id + " no tiene configuración de cashback: " + e.getMessage());
                return Optional.empty();
            }
        });
    }

    // ------------------------------------------------------------------
    // Interno
    // ------------------------------------------------------------------

    private Map<Long, Card> getAllCardsById() throws ClientOperationException {
        if (allCardsById == null) {
            Map<Long, Card> byId = new HashMap<>();
            for (Card card : ClientUseCases.get(CardOperations.class).getAllCards()) {
                byId.put(card.getId(), card);
            }
            allCardsById = byId;
        }
        return allCardsById;
    }

    private void clearCaches() {
        accounts = new ArrayList<>();
        categories = new ArrayList<>();
        tags = new ArrayList<>();
        externalEntities = new ArrayList<>();
        cardsByAccount.clear();
        linkedCardsByWallet.clear();
        cashbackByWallet.clear();
        allCardsById = null;
    }

    private void reportError(String message) {
        logger.error(message);
        onError.accept(message);
    }
}
