package com.giozar04.categories.application.ports.output;

import java.util.List;

import com.giozar04.categories.domain.entities.Category;

public interface CategoryRepository {
    Category createCategory(Category category);
    Category getCategoryById(long id);
    Category updateCategoryById(long id, Category category);
    void deleteCategoryById(long id);
    List<Category> getAllCategories();
    List<Category> getCategoriesByUserId(long userId);
}
