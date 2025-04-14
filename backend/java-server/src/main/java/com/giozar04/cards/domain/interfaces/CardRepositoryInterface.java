package com.giozar04.cards.domain.interfaces;

import java.util.List;

import com.giozar04.card.domain.entities.Card;

public interface CardRepositoryInterface {
    Card createCard(Card card);
    Card getCardById(long id);
    Card updateCardById(long id, Card card);
    void deleteCardById(long id);
    List<Card> getAllCards();
}
