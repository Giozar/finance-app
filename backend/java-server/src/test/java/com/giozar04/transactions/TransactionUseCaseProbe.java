package com.giozar04.transactions;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;

import com.giozar04.accounts.application.ports.output.AccountRepository;
import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;
import com.giozar04.cards.application.ports.output.CardRepository;
import com.giozar04.categories.application.ports.output.CategoryRepository;
import com.giozar04.externalEntities.application.ports.output.ExternalEntityRepository;
import com.giozar04.tags.application.ports.output.TagRepository;
import com.giozar04.transactions.application.normalizers.TransactionNormalizer;
import com.giozar04.transactions.application.ports.output.TransactionRepository;
import com.giozar04.transactions.application.usecases.TransactionUseCase;
import com.giozar04.transactions.application.validation.TransactionValidator;
import com.giozar04.transactions.application.validation.ValidationContextFactory;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.PaymentMethod;
import com.giozar04.transactions.domain.enums.TransactionStatus;
import com.giozar04.transactions.domain.exceptions.TransactionValidationException;
import com.giozar04.walletCardLinks.application.ports.output.WalletCardLinkRepository;

/** Verifica el orden normalizar → validar → escribir sin MySQL. */
public final class TransactionUseCaseProbe {
    private static int writes;

    public static void main(String[] args) {
        ValidationContextFactory factory = new ValidationContextFactory(
            stub(AccountRepository.class), stub(CardRepository.class),
            stub(WalletCardLinkRepository.class), stub(CategoryRepository.class),
            stub(ExternalEntityRepository.class), stub(TagRepository.class));
        TransactionValidator validator = new TransactionValidator(List.of((tx, context, errors) -> {
            if (tx.getStatus() != TransactionStatus.COMPLETED) errors.add("Estado sin normalizar");
            if (tx.getCardDetail() != null) errors.add("Detalle de tarjeta indebido");
            if (!"Comida".equals(tx.getConcept())) errors.add("Concepto sin normalizar");
        }));
        TransactionRepository repository = (TransactionRepository) Proxy.newProxyInstance(
            TransactionRepository.class.getClassLoader(), new Class<?>[] {TransactionRepository.class},
            (proxy, method, values) -> {
                if (method.getName().equals("createTransaction")) {
                    writes++;
                    return values[0];
                }
                throw new AssertionError("Operación inesperada: " + method.getName());
            });
        TransactionUseCase useCase = new TransactionUseCase(repository, factory,
                new TransactionNormalizer(), validator);
        Transaction transaction = new Transaction();
        transaction.setPaymentMethod(PaymentMethod.CASH);
        transaction.setAmount(new BigDecimal("45.00"));
        transaction.setConcept("  Comida  ");
        transaction.setCardDetail(new CardTransactionDetail());
        if (useCase.createTransaction(transaction) != transaction || writes != 1
            || transaction.getStatus() != TransactionStatus.COMPLETED
            || transaction.getCardDetail() != null || !"Comida".equals(transaction.getConcept())) {
            throw new AssertionError("La transacción no siguió el flujo esperado");
        }
        transaction.setConcept("  Inválido  ");
        try {
            useCase.createTransaction(transaction);
            throw new AssertionError("Se escribió una transacción inválida");
        } catch (TransactionValidationException expected) {
            if (writes != 1) throw new AssertionError("Se invocó el repositorio antes de validar");
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T stub(Class<T> port) {
        return (T) Proxy.newProxyInstance(port.getClassLoader(), new Class<?>[] {port},
                (proxy, method, values) -> null);
    }
}
