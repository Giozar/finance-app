package com.giozar04.tags.presentation.views;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.giozar04.tags.presentation.components.TagFormPanel;

public class CreateTagView extends JPanel {

    private final TagFormPanel tagFormPanel;

    public CreateTagView() {
        super(new BorderLayout());
        tagFormPanel = new TagFormPanel();
        add(tagFormPanel, BorderLayout.CENTER);
    }
}
