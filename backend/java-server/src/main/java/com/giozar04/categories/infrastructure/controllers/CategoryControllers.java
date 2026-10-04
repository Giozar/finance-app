package com.giozar04.categories.infrastructure.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.categories.application.services.CategoryService;
import com.giozar04.categories.infrastructure.serialization.CategoryMapper;
import com.giozar04.categories.domain.entities.Category;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.messages.infrastructure.transport.Message;
import com.giozar04.servers.domain.handlers.MessageHandler;
import com.giozar04.servers.domain.models.ClientConnection;

public class CategoryControllers {

    private static final ConsoleLogger LOGGER = ConsoleLogger.getInstance();

    public static final class CategoryMessageTypes {
        public static final String CREATE_CATEGORY = "CREATE_CATEGORY";
        public static final String GET_CATEGORY = "GET_CATEGORY";
        public static final String UPDATE_CATEGORY = "UPDATE_CATEGORY";
        public static final String DELETE_CATEGORY = "DELETE_CATEGORY";
        public static final String GET_ALL_CATEGORIES = "GET_ALL_CATEGORIES";
        public static final String GET_CATEGORIES_BY_USER = "GET_CATEGORIES_BY_USER";
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler createCategoryController(CategoryService categoryService) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando creación de categoría");

            Map<String, Object> data = (Map<String, Object>) message.getData("category");
            if (data == null) {
                return Message.createErrorMessage(CategoryMessageTypes.CREATE_CATEGORY, "Datos no proporcionados");
            }

            Category category = CategoryMapper.fromMap(data);
            Category created = categoryService.createCategory(category);

            Message response = Message.createSuccessMessage(CategoryMessageTypes.CREATE_CATEGORY, "Categoría creada exitosamente");
            response.addData("category", CategoryMapper.toMap(created));
            return response;
        };
    }

    public static MessageHandler getCategoryController(CategoryService categoryService) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando obtención de categoría");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(CategoryMessageTypes.GET_CATEGORY, "ID inválido");
            }

            Category category = categoryService.getCategoryById(id);
            Message response = Message.createSuccessMessage(CategoryMessageTypes.GET_CATEGORY, "Categoría obtenida");
            response.addData("category", CategoryMapper.toMap(category));
            return response;
        };
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler updateCategoryController(CategoryService categoryService) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando actualización de categoría");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(CategoryMessageTypes.UPDATE_CATEGORY, "ID inválido");
            }

            Map<String, Object> data = (Map<String, Object>) message.getData("category");
            if (data == null) {
                return Message.createErrorMessage(CategoryMessageTypes.UPDATE_CATEGORY, "Datos no proporcionados");
            }

            Category updated = categoryService.updateCategoryById(id, CategoryMapper.fromMap(data));

            Message response = Message.createSuccessMessage(CategoryMessageTypes.UPDATE_CATEGORY, "Categoría actualizada exitosamente");
            response.addData("category", CategoryMapper.toMap(updated));
            return response;
        };
    }

    public static MessageHandler deleteCategoryController(CategoryService categoryService) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando eliminación de categoría");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(CategoryMessageTypes.DELETE_CATEGORY, "ID inválido");
            }

            categoryService.deleteCategoryById(id);
            return Message.createSuccessMessage(CategoryMessageTypes.DELETE_CATEGORY, "Categoría eliminada exitosamente");
        };
    }

    public static MessageHandler getAllCategoriesController(CategoryService categoryService) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando obtención de todas las categorías");

            List<Category> categories = categoryService.getAllCategories();
            List<Map<String, Object>> result = new ArrayList<>();

            for (Category c : categories) {
                result.add(CategoryMapper.toMap(c));
            }

            Message response = Message.createSuccessMessage(CategoryMessageTypes.GET_ALL_CATEGORIES, "Categorías obtenidas exitosamente");
            response.addData("categories", result);
            response.addData("count", result.size());

            return response;
        };
    }

    public static MessageHandler getCategoriesByUserController(CategoryService categoryService) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando obtención de categorías por usuario");

            Long userId = parseId(message.getData("userId"));
            if (userId == null) {
                return Message.createErrorMessage(CategoryMessageTypes.GET_CATEGORIES_BY_USER, "userId inválido");
            }

            List<Category> items = categoryService.getCategoriesByUserId(userId);
            List<Map<String, Object>> result = new ArrayList<>();

            for (Category item : items) {
                result.add(CategoryMapper.toMap(item));
            }

            Message response = Message.createSuccessMessage(CategoryMessageTypes.GET_CATEGORIES_BY_USER, "Categorías del usuario obtenidas exitosamente");
            response.addData("categories", result);
            response.addData("count", result.size());

            return response;
        };
    }

    private static Long parseId(Object rawId) {
        if (rawId instanceof Long l) return l;
        if (rawId instanceof String s) {
            try {
                return Long.valueOf(s);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
