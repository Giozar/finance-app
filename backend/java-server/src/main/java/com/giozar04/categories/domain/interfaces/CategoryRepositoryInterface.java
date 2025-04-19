package com.giozar04.categories.domain.interfaces;

import java.util.List;

import com.giozar04.categories.domain.entities.Category;

public interface CategoryRepositoryInterface {
    Category createCategory(Category category);
    Category getCategoryById(long id);
    Category updateCategoryById(long id, Category category);
    void deleteCategoryById(long id);
    List<Category> getAllCategories();
}
