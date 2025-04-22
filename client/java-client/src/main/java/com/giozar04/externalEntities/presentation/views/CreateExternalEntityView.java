package com.giozar04.externalEntities.presentation.views;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.giozar04.externalEntities.presentation.components.ExternalEntityFormPanel;

public class CreateExternalEntityView extends JPanel {

    private final ExternalEntityFormPanel externalEntityFormPanel;

    public CreateExternalEntityView() {
        super(new BorderLayout());
        externalEntityFormPanel = new ExternalEntityFormPanel();
        add(externalEntityFormPanel, BorderLayout.CENTER);
    }
}
