package com.giozar04.categories.infrastructure.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.categories.infrastructure.serialization.CategoryMapper;
import com.giozar04.categories.domain.entities.Category;
import com.giozar04.categories.application.exceptions.CategoryCreationException;
import com.giozar04.categories.application.exceptions.CategoryDeletionException;
import com.giozar04.categories.application.exceptions.CategoryRetrievalException;
import com.giozar04.categories.application.exceptions.CategoryUpdateException;
import com.giozar04.logging.CustomLogger;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.serverConnection.application.validators.ServerResponseValidator;

public class CategoryService {

    private final ServerConnectionService serverConnectionService;
    private static final CustomLogger logger = CustomLogger.getInstance();
    private static CategoryService instance;

    private CategoryService(ServerConnectionService serverConnectionService) {
        this.serverConnectionService = serverConnectionService;
    }

    public static CategoryService connectService(ServerConnectionService serverConnectionService) {
        if (instance == null) {
            instance = new CategoryService(serverConnectionService);
        }
        return instance;
    }

    public static CategoryService getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public Category createCategory(Category category) throws ClientOperationException {
        Message message = new Message();
        message.setType("CREATE_CATEGORY");
        message.addData("category", CategoryMapper.toMap(category));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("CREATE_CATEGORY");
            ServerResponseValidator.validateResponse(response);
            logger.info("Categoría creada exitosamente: " + response);
            return CategoryMapper.fromMap((Map<String, Object>) response.getData("category"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CategoryCreationException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Category updateCategoryById(Long id, Category category) throws ClientOperationException {
        Message message = new Message();
        message.setType("UPDATE_CATEGORY");
        message.addData("id", id);
        message.addData("category", CategoryMapper.toMap(category));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("UPDATE_CATEGORY");
            ServerResponseValidator.validateResponse(response);
            logger.info("Categoría actualizada correctamente: " + response);
            return CategoryMapper.fromMap((Map<String, Object>) response.getData("category"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CategoryUpdateException("Error al esperar respuesta del servidor", e);
        }
    }

    public void deleteCategoryById(Long id) throws ClientOperationException {
        Message message = new Message();
        message.setType("DELETE_CATEGORY");
        message.addData("id", id);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("DELETE_CATEGORY");
            ServerResponseValidator.validateResponse(response);
            logger.info("Categoría eliminada exitosamente: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CategoryDeletionException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Category> getAllCategories() throws ClientOperationException {
        logger.info("Solicitando todas las categorías...");
        Message message = new Message();
        message.setType("GET_ALL_CATEGORIES");

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_ALL_CATEGORIES");
            ServerResponseValidator.validateResponse(response);
            Object raw = response.getData("categories");

            if (raw == null) {
                throw new CategoryRetrievalException("Lista de categorías vacía", null);
            }

            if (raw instanceof List<?> rawList) {
                List<Category> categories = new ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> map) {
                        categories.add(CategoryMapper.fromMap((Map<String, Object>) map));
                    }
                }
                logger.info("Categorías obtenidas correctamente. Total: " + categories.size());
                return categories;
            } else {
                throw new CategoryRetrievalException("Formato inesperado: " + raw.getClass().getName(), null);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CategoryRetrievalException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Category> getCategoriesByUserId(long userId) throws ClientOperationException {
        logger.info("Solicitando categorías del usuario " + userId + "...");
        Message message = new Message();
        message.setType("GET_CATEGORIES_BY_USER");
        message.addData("userId", userId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_CATEGORIES_BY_USER");
            ServerResponseValidator.validateResponse(response);
            Object raw = response.getData("categories");

            if (raw == null) {
                throw new CategoryRetrievalException("Lista de categorías vacía", null);
            }

            if (raw instanceof List<?> rawList) {
                List<Category> categories = new ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> map) {
                        categories.add(CategoryMapper.fromMap((Map<String, Object>) map));
                    }
                }
                logger.info("Categorías del usuario obtenidas correctamente. Total: " + categories.size());
                return categories;
            } else {
                throw new CategoryRetrievalException("Formato inesperado: " + raw.getClass().getName(), null);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CategoryRetrievalException("Error al esperar respuesta del servidor", e);
        }
    }
}
