package com.giozar04.transactions.application.validation;

import java.util.Objects;

import com.giozar04.accounts.domain.interfaces.AccountRepositoryInterface;
import com.giozar04.cards.domain.interfaces.CardRepositoryInterface;
import com.giozar04.categories.domain.interfaces.CategoryRepositoryInterface;
import com.giozar04.externalEntities.domain.interfaces.ExternalEntityRepositoryInterface;
import com.giozar04.tags.domain.interfaces.TagRepositoryInterface;
import com.giozar04.walletCardLinks.domain.interfaces.WalletCardLinkRepositoryInterface;

/**
 * Crea un {@link ValidationContext} nuevo (caché vacía) por petición.
 */
public class ValidationContextFactory {

    private final AccountRepositoryInterface accountRepository;
    private final CardRepositoryInterface cardRepository;
    private final WalletCardLinkRepositoryInterface walletCardLinkRepository;
    private final CategoryRepositoryInterface categoryRepository;
    private final ExternalEntityRepositoryInterface externalEntityRepository;
    private final TagRepositoryInterface tagRepository;

    public ValidationContextFactory(AccountRepositoryInterface accountRepository,
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

    public ValidationContext create() {
        return new ValidationContext(accountRepository, cardRepository, walletCardLinkRepository,
                categoryRepository, externalEntityRepository, tagRepository);
    }
}
