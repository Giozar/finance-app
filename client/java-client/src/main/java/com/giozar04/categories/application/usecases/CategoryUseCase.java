package com.giozar04.categories.application.usecases;

import java.util.List;
import com.giozar04.categories.domain.entities.Category;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.categories.application.ports.input.CategoryOperations;
import com.giozar04.categories.application.ports.output.CategoryGateway;

public final class CategoryUseCase implements CategoryOperations {
    private final CategoryGateway gateway;

    public CategoryUseCase(CategoryGateway gateway) {
        this.gateway = java.util.Objects.requireNonNull(gateway);
    }

    @Override
    public Category createCategory(Category category) throws ClientOperationException {
        return gateway.createCategory(category);
    }

    @Override
    public Category updateCategoryById(Long id, Category category) throws ClientOperationException {
        return gateway.updateCategoryById(id, category);
    }

    @Override
    public void deleteCategoryById(Long id) throws ClientOperationException {
        gateway.deleteCategoryById(id);
    }

    @Override
    public List<Category> getAllCategories() throws ClientOperationException {
        return gateway.getAllCategories();
    }

    @Override
    public List<Category> getCategoriesByUserId(long userId) throws ClientOperationException {
        return gateway.getCategoriesByUserId(userId);
    }
}
