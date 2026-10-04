package com.giozar04.transactions.application.services;

import java.util.List;
import java.util.Objects;

import com.giozar04.transactions.application.normalizers.TransactionNormalizer;
import com.giozar04.transactions.application.validation.TransactionValidator;
import com.giozar04.transactions.application.validation.ValidationContext;
import com.giozar04.transactions.application.validation.ValidationContextFactory;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.interfaces.TransactionRepositoryInterface;

/**
 * Orquesta las escrituras del agregado: normalizar → validar → repositorio.
 * Se normaliza primero porque el normalizador deriva datos (sourceAccountId en WALLET, montos de
 * los detalles, status por defecto) que las reglas deben comprobar tal y como se guardarán.
 * Ambos comparten un ValidationContext por petición (caché de consultas).
 * Las lecturas y el borrado se delegan sin más.
 */
public class TransactionService implements TransactionRepositoryInterface {

    private final TransactionRepositoryInterface repository;
    private final ValidationContextFactory contextFactory;
    private final TransactionNormalizer normalizer;
    private final TransactionValidator validator;

    public TransactionService(TransactionRepositoryInterface repository,
                              ValidationContextFactory contextFactory,
                              TransactionNormalizer normalizer,
                              TransactionValidator validator) {
        this.repository = Objects.requireNonNull(repository, "El repositorio de transacciones no puede ser nulo");
        this.contextFactory = Objects.requireNonNull(contextFactory, "La fábrica de contextos no puede ser nula");
        this.normalizer = Objects.requireNonNull(normalizer, "El normalizador no puede ser nulo");
        this.validator = Objects.requireNonNull(validator, "El validador no puede ser nulo");
    }

    @Override
    public Transaction createTransaction(Transaction tx) {
        prepare(tx);
        return repository.createTransaction(tx);
    }

    @Override
    public Transaction getTransactionById(long id) {
        return repository.getTransactionById(id);
    }

    @Override
    public Transaction updateTransactionById(long id, Transaction tx) {
        prepare(tx);
        return repository.updateTransactionById(id, tx);
    }

    @Override
    public void deleteTransactionById(long id) {
        repository.deleteTransactionById(id);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return repository.getAllTransactions();
    }

    @Override
    public List<Transaction> getTransactionsByUserId(long userId) {
        return repository.getTransactionsByUserId(userId);
    }

    private void prepare(Transaction tx) {
        ValidationContext ctx = contextFactory.create();
        normalizer.normalize(tx, ctx);
        validator.validate(tx, ctx);
    }
}
