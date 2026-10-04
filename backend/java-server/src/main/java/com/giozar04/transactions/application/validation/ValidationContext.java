package com.giozar04.transactions.application.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.exceptions.AccountExceptions;
import com.giozar04.accounts.domain.interfaces.AccountRepositoryInterface;
import com.giozar04.card.domain.entities.Card;
import com.giozar04.card.domain.exceptions.CardExceptions;
import com.giozar04.cards.domain.interfaces.CardRepositoryInterface;
import com.giozar04.categories.domain.entities.Category;
import com.giozar04.categories.domain.exceptions.CategoryExceptions;
import com.giozar04.categories.domain.interfaces.CategoryRepositoryInterface;
import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.externalEntities.domain.exceptions.ExternalEntityExceptions;
import com.giozar04.externalEntities.domain.interfaces.ExternalEntityRepositoryInterface;
import com.giozar04.tags.domain.entities.Tag;
import com.giozar04.tags.application.exceptions.TagNotFoundException;
import com.giozar04.tags.domain.interfaces.TagRepositoryInterface;
import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;
import com.giozar04.walletCardLinks.domain.interfaces.WalletCardLinkRepositoryInterface;

/**
 * Datos de consulta para validar/normalizar UNA petición. Usa las interfaces de repositorio
 * existentes (DIP) y cachea cada búsqueda durante la vida del contexto: se crea uno nuevo por
 * petición con {@link ValidationContextFactory}, por lo que la caché nunca queda obsoleta.
 * Los métodos devuelven {@code null} si el registro no existe (los "no encontrado" no son errores aquí).
 */
public class ValidationContext {

    private final AccountRepositoryInterface accountRepository;
    private final CardRepositoryInterface cardRepository;
    private final WalletCardLinkRepositoryInterface walletCardLinkRepository;
    private final CategoryRepositoryInterface categoryRepository;
    private final ExternalEntityRepositoryInterface externalEntityRepository;
    private final TagRepositoryInterface tagRepository;

    private final Map<Long, Optional<Account>> accounts = new HashMap<>();
    private final Map<Long, Optional<Card>> cards = new HashMap<>();
    private final Map<Long, Set<Long>> linkedCardsByWallet = new HashMap<>();
    private final Map<Long, Optional<Category>> categories = new HashMap<>();
    private final Map<Long, Optional<ExternalEntity>> externalEntities = new HashMap<>();
    private final Map<Long, Optional<Tag>> tags = new HashMap<>();

    public ValidationContext(AccountRepositoryInterface accountRepository,
                             CardRepositoryInterface cardRepository,
                             WalletCardLinkRepositoryInterface walletCardLinkRepository,
                             CategoryRepositoryInterface categoryRepository,
                             ExternalEntityRepositoryInterface externalEntityRepository,
                             TagRepositoryInterface tagRepository) {
        this.accountRepository = Objects.requireNonNull(accountRepository, "El repositorio de cuentas no puede ser nulo");
        this.cardRepository = Objects.requireNonNull(cardRepository, "El repositorio de tarjetas no puede ser nulo");
        this.walletCardLinkRepository = Objects.requireNonNull(walletCardLinkRepository, "El repositorio de vínculos wallet-tarjeta no puede ser nulo");
        this.categoryRepository = Objects.requireNonNull(categoryRepository, "El repositorio de categorías no puede ser nulo");
        this.externalEntityRepository = Objects.requireNonNull(externalEntityRepository, "El repositorio de entidades externas no puede ser nulo");
        this.tagRepository = Objects.requireNonNull(tagRepository, "El repositorio de etiquetas no puede ser nulo");
    }

    public Account account(Long id) {
        if (id == null || id <= 0) return null;
        return accounts.computeIfAbsent(id, k -> find(() -> accountRepository.getAccountById(k),
                AccountExceptions.AccountNotFoundException.class)).orElse(null);
    }

    public Card card(Long id) {
        if (id == null || id <= 0) return null;
        return cards.computeIfAbsent(id, k -> find(() -> cardRepository.getCardById(k),
                CardExceptions.CardNotFoundException.class)).orElse(null);
    }

    public Category category(long id) {
        if (id <= 0) return null;
        return categories.computeIfAbsent(id, k -> find(() -> categoryRepository.getCategoryById(k),
                CategoryExceptions.CategoryNotFoundException.class)).orElse(null);
    }

    public ExternalEntity externalEntity(Long id) {
        if (id == null || id <= 0) return null;
        return externalEntities.computeIfAbsent(id, k -> find(() -> externalEntityRepository.getExternalEntityById(k),
                ExternalEntityExceptions.ExternalEntityNotFoundException.class)).orElse(null);
    }

    public Tag tag(Long id) {
        if (id == null || id <= 0) return null;
        return tags.computeIfAbsent(id, k -> find(() -> tagRepository.getTagById(k),
                TagNotFoundException.class)).orElse(null);
    }

    /** true si la tarjeta está en wallet_card_links para esa wallet. */
    public boolean isCardLinkedToWallet(long walletAccountId, long cardId) {
        Set<Long> linked = linkedCardsByWallet.computeIfAbsent(walletAccountId, k -> {
            List<WalletCardLink> links = walletCardLinkRepository.getLinksByWalletAccountId(k);
            return links.stream().map(WalletCardLink::getCardId).collect(Collectors.toSet());
        });
        return linked.contains(cardId);
    }

    /** Ejecuta la búsqueda y traduce solo la excepción "no encontrado" a vacío; el resto se propaga. */
    private static <T> Optional<T> find(Supplier<T> lookup, Class<? extends RuntimeException> notFoundType) {
        try {
            return Optional.ofNullable(lookup.get());
        } catch (RuntimeException e) {
            if (notFoundType.isInstance(e)) return Optional.empty();
            throw e;
        }
    }
}
