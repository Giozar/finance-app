package com.giozar04.categories.infrastructure.persistence.mysql;

import java.util.List;
import java.util.Objects;

import com.giozar04.categories.domain.entities.Category;
import com.giozar04.categories.domain.enums.CategoryTypes;
import com.giozar04.categories.application.ports.output.CategoryRepository;
import com.giozar04.categories.domain.policies.CategoryPolicy;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.logging.infrastructure.ConsoleLogger;

public abstract class AbstractCategoryJdbcRepository implements CategoryRepository {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final ConsoleLogger logger = ConsoleLogger.getInstance();

    protected AbstractCategoryJdbcRepository(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, "La conexión a base de datos no puede ser nula");
    }

    protected void validateCategory(Category category) {
        CategoryPolicy.validateCategory(category);
    }

    protected void validateId(long id) {
        CategoryPolicy.validateId(id);
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

    @Override
    public abstract List<Category> getCategoriesByUserId(long userId);
}
