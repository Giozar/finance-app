package com.giozar04.cards.application.ports.output;

import java.util.List;
import com.giozar04.card.domain.entities.Card;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;

public interface CardGateway {
    Card createCard(Card card) throws ClientOperationException;
    Card updateCardById(Long id, Card card) throws ClientOperationException;
    void deleteCardById(Long id) throws ClientOperationException;
    List<Card> getAllCards() throws ClientOperationException;
    List<Card> getCardsByAccountId(long accountId) throws ClientOperationException;
}
