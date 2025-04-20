package com.giozar04.categories.presentation.views;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.giozar04.categories.presentation.components.CategoryFormPanel;

public class CreateCategoryView extends JPanel {

    private final CategoryFormPanel categoryFormPanel;

    public CreateCategoryView() {
        super(new BorderLayout());
        categoryFormPanel = new CategoryFormPanel();
        add(categoryFormPanel, BorderLayout.CENTER);
    }
}
