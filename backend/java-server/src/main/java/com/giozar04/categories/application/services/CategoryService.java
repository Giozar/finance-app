package com.giozar04.categories.application.services;

import java.util.List;

import com.giozar04.categories.domain.entities.Category;
import com.giozar04.categories.domain.interfaces.CategoryRepositoryInterface;

public class CategoryService implements CategoryRepositoryInterface {

    private final CategoryRepositoryInterface categoryRepository;

    public CategoryService(CategoryRepositoryInterface categoryRepository) {
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
}
