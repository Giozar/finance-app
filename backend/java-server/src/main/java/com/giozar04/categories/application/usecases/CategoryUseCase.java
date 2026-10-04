package com.giozar04.categories.application.usecases;

import java.util.List;

import com.giozar04.categories.domain.entities.Category;
import com.giozar04.categories.application.ports.output.CategoryRepository;
import com.giozar04.categories.application.ports.input.CategoryOperations;

public class CategoryUseCase implements CategoryOperations {

    private final CategoryRepository categoryRepository;

    public CategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category createCategory(Category category) {
        return categoryRepository.createCategory(category);
    }

    @Override
    public Category getCategoryById(long id) {
        return categoryRepository.getCategoryById(id);
    }

    @Override
    public Category updateCategoryById(long id, Category category) {
        return categoryRepository.updateCategoryById(id, category);
    }

    @Override
    public void deleteCategoryById(long id) {
        categoryRepository.deleteCategoryById(id);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.getAllCategories();
    }

    @Override
    public List<Category> getCategoriesByUserId(long userId) {
        return categoryRepository.getCategoriesByUserId(userId);
    }
}
