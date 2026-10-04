package com.giozar04.transactions.presentation.components.sections;

import java.util.List;
import java.util.Objects;

import com.giozar04.categories.domain.entities.Category;
import com.giozar04.categories.presentation.components.CategoryFormPanel;
import com.giozar04.shared.components.QuickCreateDialog;
import com.giozar04.shared.components.forms.FormMultiSelectField;
import com.giozar04.shared.utils.DialogUtil;
import com.giozar04.tags.domain.entities.Tag;
import com.giozar04.tags.presentation.components.TagFormPanel;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.presentation.components.CreatableSearchField;
import com.giozar04.transactions.presentation.form.TransactionFormContext;
import com.giozar04.transactions.presentation.form.TransactionFormDataProvider;

/**
 * Sección 6 – Clasificación: categoría compatible con la operación (mismo tipo o "Ambos") y etiquetas.
 * Ambas admiten alta rápida ("+ Nueva") con el usuario del formulario bloqueado.
 */
public class ClassificationSection extends AbstractTransactionSection {

    private static final long serialVersionUID = 1L;

    private final CreatableSearchField<Category> categoryField;
    private final FormMultiSelectField<Tag> tagsField;

    private Long lastUserId;
    private OperationTypes lastOperation;

    public ClassificationSection(TransactionFormContext context, TransactionFormDataProvider provider) {
        super("Clasificación", context, provider);

        categoryField = new CreatableSearchField<>("Categoría:", FIELD_WIDTH, FIELD_HEIGHT);
        categoryField.getCombo().setIdentityFunction(Category::getId);
        categoryField.getCombo().setDisplayFunction(ClassificationSection::categoryDisplay);
        categoryField.setOnCreateNew(this::quickCreateCategory);

        tagsField = new FormMultiSelectField<>("Etiquetas:", FIELD_WIDTH, FIELD_HEIGHT);
        tagsField.setPlaceholder("Busque una etiqueta...");
        tagsField.setIdentityFunction(Tag::getId);
        tagsField.setDisplayFunction(Tag::getName);
        tagsField.setOnCreateNew(this::quickCreateTag);

        addRow(categoryField);
        addRow(tagsField);

        updateEnabled(null, null);
    }

    private static String categoryDisplay(Category category) {
        String icon = category.getIcon();
        return (icon != null && !icon.isBlank() ? icon.trim() + " " : "") + category.getName();
    }

    // ------------------------------------------------------------------
    // Contexto
    // ------------------------------------------------------------------

    @Override
    public void onContextChanged(TransactionFormContext ctx) {
        boolean userChanged = !Objects.equals(lastUserId, ctx.getUserId());
        boolean operationChanged = lastOperation != ctx.getOperation();
        lastUserId = ctx.getUserId();
        lastOperation = ctx.getOperation();

        if (userChanged) {
            tagsField.clear();
            tagsField.setItems(provider.getTags());
        }
        if (userChanged || operationChanged) {
            reloadCategories();
        }
        updateEnabled(ctx.getUserId(), ctx.getOperation());
    }

    /** Categorías de la operación actual; conserva la elegida si sigue siendo compatible. */
    private void reloadCategories() {
        Category previous = categoryField.getCombo().getSelectedItem();
        categoryField.getCombo().setItems(provider.getCategoriesFor(lastOperation));
        categoryField.getCombo().setSelectedItem(previous);
    }

    private void updateEnabled(Long userId, OperationTypes operation) {
        boolean ready = userId != null && operation != null;
        categoryField.getCombo().setPlaceholder(ready ? "Busque una categoría..."
                : "Seleccione el usuario y el tipo de operación...");
        categoryField.getCombo().setEnabled(ready);
        categoryField.setCreateEnabled(ready);
        tagsField.setEnabled(userId != null);
    }

    private void quickCreateCategory() {
        if (context.getUser() == null || context.getOperation() == null) {
            return;
        }
        CategoryFormPanel form = new CategoryFormPanel();
        form.presetUser(context.getUser(), true);
        form.presetType(TransactionFormDataProvider.toCategoryType(context.getOperation()));
        QuickCreateDialog.<Category>show(this, "Nueva categoría", form, form::setOnSaved)
                .ifPresent(category -> {
                    provider.addCategory(category);
                    reloadCategories();
                    categoryField.getCombo().setSelectedItem(category);
                    if (categoryField.getCombo().getSelectedItem() == null) {
                        DialogUtil.showWarning(this, "La categoría se creó, pero su tipo no corresponde a la operación "
                                + "seleccionada, por lo que no se puede usar en esta transacción.");
                    }
                });
    }

    private void quickCreateTag() {
        if (context.getUser() == null) {
            return;
        }
        TagFormPanel form = new TagFormPanel();
        form.presetUser(context.getUser(), true);
        QuickCreateDialog.<Tag>show(this, "Nueva etiqueta", form, form::setOnSaved)
                .ifPresent(tag -> {
                    provider.addTag(tag);
                    tagsField.addSelected(tag);
                });
    }

    // ------------------------------------------------------------------
    // Contrato de sección
    // ------------------------------------------------------------------

    @Override
    public void validate(List<String> errors) {
        if (context.getOperation() != null && categoryField.getCombo().getSelectedItem() == null) {
            errors.add("Debe seleccionar una categoría.");
        }
    }

    @Override
    public void applyTo(Transaction tx) {
        tx.setCategoryId(categoryField.getCombo().getSelectedItem().getId());
        tx.setTagIds(tagsField.getSelectedItems().stream().map(Tag::getId).toList());
    }

    @Override
    public void loadFrom(Transaction tx) {
        categoryField.getCombo().setSelectedItem(provider.findCategory(tx.getCategoryId()));
        tagsField.setSelectedItems(provider.findTags(tx.getTagIds()));
    }

    @Override
    public void clear() {
        categoryField.getCombo().clearSelection();
        tagsField.clear();
    }
}
