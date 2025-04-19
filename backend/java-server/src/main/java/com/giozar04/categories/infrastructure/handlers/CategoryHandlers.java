package com.giozar04.categories.infrastructure.handlers;

import com.giozar04.categories.application.services.CategoryService;
import com.giozar04.categories.infrastructure.controllers.CategoryControllers;
import com.giozar04.servers.application.services.ServerService;
import com.giozar04.servers.domain.interfaces.ServerRegisterHandlers;

public class CategoryHandlers implements ServerRegisterHandlers {

    private final CategoryService categoryService;

    public CategoryHandlers(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void register(ServerService server) {
        server.registerHandler(
            CategoryControllers.CategoryMessageTypes.CREATE_CATEGORY,
            CategoryControllers.createCategoryController(categoryService)
        );
        server.registerHandler(
            CategoryControllers.CategoryMessageTypes.GET_CATEGORY,
            CategoryControllers.getCategoryController(categoryService)
        );
        server.registerHandler(
            CategoryControllers.CategoryMessageTypes.UPDATE_CATEGORY,
            CategoryControllers.updateCategoryController(categoryService)
        );
        server.registerHandler(
            CategoryControllers.CategoryMessageTypes.DELETE_CATEGORY,
            CategoryControllers.deleteCategoryController(categoryService)
        );
        server.registerHandler(
            CategoryControllers.CategoryMessageTypes.GET_ALL_CATEGORIES,
            CategoryControllers.getAllCategoriesController(categoryService)
        );
    }
}
