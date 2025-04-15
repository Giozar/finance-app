package com.giozar04.cards.presentation.views;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.giozar04.cards.presentation.components.CardFormPanel;

public class CreateCardView extends JPanel {

    private final CardFormPanel cardFormPanel;

    public CreateCardView() {
        super(new BorderLayout());
        cardFormPanel = new CardFormPanel();
        add(cardFormPanel, BorderLayout.CENTER);
    }
}
