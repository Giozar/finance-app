package com.giozar04.cards.application.services;

import java.util.List;

import com.giozar04.card.domain.entities.Card;
import com.giozar04.cards.domain.interfaces.CardRepositoryInterface;

public class CardService implements CardRepositoryInterface {

    private final CardRepositoryInterface cardRepository;

    public CardService(CardRepositoryInterface cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    public Card createCard(Card card) {
        return cardRepository.createCard(card);
    }

    @Override
    public Card getCardById(long id) {
        return cardRepository.getCardById(id);
    }

    @Override
    public Card updateCardById(long id, Card card) {
        return cardRepository.updateCardById(id, card);
    }

    @Override
    public void deleteCardById(long id) {
        cardRepository.deleteCardById(id);
    }

    @Override
    public List<Card> getAllCards() {
        return cardRepository.getAllCards();
    }
}
