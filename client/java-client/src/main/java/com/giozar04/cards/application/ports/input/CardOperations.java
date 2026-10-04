package com.giozar04.cards.application.ports.input;

import java.util.List;
import com.giozar04.card.domain.entities.Card;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;

public interface CardOperations {
    Card createCard(Card card) throws ClientOperationException;
    Card updateCardById(Long id, Card card) throws ClientOperationException;
    void deleteCardById(Long id) throws ClientOperationException;
    List<Card> getAllCards() throws ClientOperationException;
    List<Card> getCardsByAccountId(long accountId) throws ClientOperationException;
}
