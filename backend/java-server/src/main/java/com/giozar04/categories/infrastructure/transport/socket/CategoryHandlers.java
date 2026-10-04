package com.giozar04.categories.infrastructure.transport.socket;

import com.giozar04.categories.application.ports.input.CategoryOperations;
import com.giozar04.categories.infrastructure.transport.socket.CategoryControllers;
import com.giozar04.servers.infrastructure.transport.socket.ServerService;
import com.giozar04.servers.infrastructure.transport.socket.ServerRegisterHandlers;

public class CategoryHandlers implements ServerRegisterHandlers {

    private final CategoryOperations categoryService;

    public CategoryHandlers(CategoryOperations categoryService) {
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
        server.registerHandler(
            CategoryControllers.CategoryMessageTypes.GET_CATEGORIES_BY_USER,
            CategoryControllers.getCategoriesByUserController(categoryService)
        );
    }
}
