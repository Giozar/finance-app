package com.giozar04.cards.application.usecases;

import java.util.List;
import com.giozar04.card.domain.entities.Card;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.cards.application.ports.input.CardOperations;
import com.giozar04.cards.application.ports.output.CardGateway;

public final class CardUseCase implements CardOperations {
    private final CardGateway gateway;

    public CardUseCase(CardGateway gateway) {
        this.gateway = java.util.Objects.requireNonNull(gateway);
    }

    @Override
    public Card createCard(Card card) throws ClientOperationException {
        return gateway.createCard(card);
    }

    @Override
    public Card updateCardById(Long id, Card card) throws ClientOperationException {
        return gateway.updateCardById(id, card);
    }

    @Override
    public void deleteCardById(Long id) throws ClientOperationException {
        gateway.deleteCardById(id);
    }

    @Override
    public List<Card> getAllCards() throws ClientOperationException {
        return gateway.getAllCards();
    }

    @Override
    public List<Card> getCardsByAccountId(long accountId) throws ClientOperationException {
        return gateway.getCardsByAccountId(accountId);
    }
}
