package com.giozar04.categories.domain.models;

import java.util.List;
import java.util.Objects;

import com.giozar04.categories.domain.entities.Category;
import com.giozar04.categories.domain.enums.CategoryTypes;
import com.giozar04.categories.domain.interfaces.CategoryRepositoryInterface;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.logging.CustomLogger;

public abstract class CategoryRepositoryAbstract implements CategoryRepositoryInterface {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final CustomLogger logger = CustomLogger.getInstance();

    protected CategoryRepositoryAbstract(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, "La conexión a base de datos no puede ser nula");
    }

    protected void validateCategory(Category category) {
        Objects.requireNonNull(category, "La categoría no puede ser nula");

        if (category.getName() == null || category.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío");
        }

        if (category.getType() == null) {
            throw new IllegalArgumentException("El tipo de categoría es obligatorio");
        }

        try {
            CategoryTypes.valueOf(category.getType().name());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de categoría no válido: " + category.getType());
        }

        if (category.getIcon() == null || category.getIcon().isBlank()) {
            throw new IllegalArgumentException("El ícono de la categoría no puede estar vacío");
        }
    }

    protected void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
    }

    @Override
    public abstract Category createCategory(Category category);

    @Override
    public abstract Category getCategoryById(long id);

    @Override
    public abstract Category updateCategoryById(long id, Category category);

    @Override
    public abstract void deleteCategoryById(long id);

    @Override
    public abstract List<Category> getAllCategories();
}
