package com.giozar04.cards.application.usecases;

import java.util.List;

import com.giozar04.card.domain.entities.Card;
import com.giozar04.cards.application.ports.output.CardRepository;
import com.giozar04.cards.application.ports.input.CardOperations;

public class CardUseCase implements CardOperations {

    private final CardRepository cardRepository;

    public CardUseCase(CardRepository cardRepository) {
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

    @Override
    public List<Card> getCardsByAccountId(long accountId) {
        return cardRepository.getCardsByAccountId(accountId);
    }
}
