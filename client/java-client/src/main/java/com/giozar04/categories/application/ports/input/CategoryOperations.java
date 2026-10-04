package com.giozar04.categories.application.ports.input;

import java.util.List;
import com.giozar04.categories.domain.entities.Category;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;

public interface CategoryOperations {
    Category createCategory(Category category) throws ClientOperationException;
    Category updateCategoryById(Long id, Category category) throws ClientOperationException;
    void deleteCategoryById(Long id) throws ClientOperationException;
    List<Category> getAllCategories() throws ClientOperationException;
    List<Category> getCategoriesByUserId(long userId) throws ClientOperationException;
}
