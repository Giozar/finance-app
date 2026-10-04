package com.giozar04.transactions.application.validation.rules;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.List;

import com.giozar04.categories.domain.entities.Category;
import com.giozar04.categories.domain.enums.CategoryTypes;
import com.giozar04.tags.domain.entities.Tag;
import com.giozar04.transactions.application.validation.TransactionRule;
import com.giozar04.transactions.application.validation.ValidationContext;
import com.giozar04.transactions.domain.entities.Transaction;

/**
 * Campos comunes a toda transacción: usuario, tipo, método, estado, monto, concepto, fecha,
 * zona horaria, categoría (del usuario y compatible con la operación) y etiquetas del usuario.
 */
public class CommonFieldsRule implements TransactionRule {

    private static final int MAX_CONCEPT_LENGTH = 100;
    private static final int MAX_RECEIPT_URL_LENGTH = 255;
    private static final int MAX_TIMEZONE_LENGTH = 50;

    @Override
    public void validate(Transaction tx, ValidationContext ctx, List<String> errors) {
        if (tx.getUserId() <= 0) errors.add("El usuario es obligatorio");
        if (tx.getOperationType() == null) errors.add("El tipo de operación es obligatorio");
        if (tx.getPaymentMethod() == null) errors.add("El método de pago es obligatorio");
        if (tx.getStatus() == null) errors.add("El estado es obligatorio");

        if (tx.getAmount() == null || tx.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("El monto debe ser mayor que cero");
        }

        if (tx.getConcept() == null || tx.getConcept().isBlank()) {
            errors.add("El concepto es obligatorio");
        } else if (tx.getConcept().length() > MAX_CONCEPT_LENGTH) {
            errors.add("El concepto no puede superar " + MAX_CONCEPT_LENGTH + " caracteres");
        }

        if (tx.getReceiptUrl() != null && tx.getReceiptUrl().length() > MAX_RECEIPT_URL_LENGTH) {
            errors.add("El comprobante no puede superar " + MAX_RECEIPT_URL_LENGTH + " caracteres");
        }

        if (tx.getDate() == null) errors.add("La fecha es obligatoria");
        validateTimezone(tx.getTimezone(), errors);

        if (tx.getParentTransactionId() != null && tx.getParentTransactionId() <= 0) {
            errors.add("La transacción padre es inválida");
        }

        validateCategory(tx, ctx, errors);
        validateTags(tx, ctx, errors);
    }

    private void validateTimezone(String timezone, List<String> errors) {
        if (timezone == null || timezone.isBlank()) {
            errors.add("La zona horaria es obligatoria");
            return;
        }
        if (timezone.length() > MAX_TIMEZONE_LENGTH) {
            errors.add("La zona horaria no puede superar " + MAX_TIMEZONE_LENGTH + " caracteres");
            return;
        }
        try {
            ZoneId.of(timezone);
        } catch (DateTimeException e) {
            errors.add("La zona horaria \"" + timezone + "\" no es válida");
        }
    }

    private void validateCategory(Transaction tx, ValidationContext ctx, List<String> errors) {
        if (tx.getCategoryId() <= 0) {
            errors.add("La categoría es obligatoria");
            return;
        }

        Category category = ctx.category(tx.getCategoryId());
        if (category == null) {
            errors.add("La categoría con ID " + tx.getCategoryId() + " no existe");
            return;
        }

        if (tx.getUserId() > 0 && category.getUserId() != tx.getUserId()) {
            errors.add("La categoría \"" + category.getName() + "\" no pertenece al usuario");
        }

        // Compatible si es BOTH o si su tipo coincide con la operación (INCOME, EXPENSE, REALLOCATION)
        if (tx.getOperationType() != null && category.getType() != null
                && category.getType() != CategoryTypes.BOTH
                && !category.getType().getValue().equals(tx.getOperationType().getValue())) {
            errors.add("La categoría \"" + category.getName() + "\" es de tipo \"" + category.getType().getLabel()
                    + "\" y no es compatible con la operación \"" + tx.getOperationType().getLabel() + "\"");
        }
    }

    private void validateTags(Transaction tx, ValidationContext ctx, List<String> errors) {
        for (Long tagId : tx.getTagIds()) {
            if (tagId == null || tagId <= 0) {
                errors.add("Hay una etiqueta con ID inválido");
                continue;
            }

            Tag tag = ctx.tag(tagId);
            if (tag == null) {
                errors.add("La etiqueta con ID " + tagId + " no existe");
            } else if (tx.getUserId() > 0 && tag.getUserId() != tx.getUserId()) {
                errors.add("La etiqueta \"" + tag.getName() + "\" no pertenece al usuario");
            }
        }
    }
}
