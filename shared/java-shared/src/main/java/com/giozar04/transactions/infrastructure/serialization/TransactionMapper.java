package com.giozar04.transactions.infrastructure.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.giozar04.cardTransactionDetails.infrastructure.serialization.CardTransactionDetailMapper;
import com.giozar04.shared.infrastructure.serialization.ValueParser;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.domain.enums.PaymentMethod;
import com.giozar04.transactions.domain.enums.TransactionStatus;
import com.giozar04.walletTransactionDetails.infrastructure.serialization.WalletTransactionDetailMapper;

public class TransactionMapper {

    public static Map<String, Object> toMap(Transaction tx) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", tx.getId());
        map.put("userId", tx.getUserId());
        map.put("operationType", tx.getOperationType() != null ? tx.getOperationType().getValue() : null);
        map.put("paymentMethod", tx.getPaymentMethod() != null ? tx.getPaymentMethod().getValue() : null);
        map.put("status", tx.getStatus() != null ? tx.getStatus().getValue() : null);
        map.put("sourceAccountId", tx.getSourceAccountId());
        map.put("destinationAccountId", tx.getDestinationAccountId());
        map.put("externalEntityId", tx.getExternalEntityId());
        map.put("categoryId", tx.getCategoryId());
        map.put("parentTransactionId", tx.getParentTransactionId());
        map.put("amount", tx.getAmount());
        map.put("concept", tx.getConcept());
        map.put("description", tx.getDescription());
        map.put("comments", tx.getComments());
        map.put("receiptUrl", tx.getReceiptUrl());
        map.put("timezone", tx.getTimezone());
        map.put("tagIds", new ArrayList<>(tx.getTagIds()));

        if (tx.getCardDetail() != null)
            map.put("cardDetail", CardTransactionDetailMapper.toMap(tx.getCardDetail()));
        if (tx.getWalletDetail() != null)
            map.put("walletDetail", WalletTransactionDetailMapper.toMap(tx.getWalletDetail()));

        if (tx.getDate() != null)
            map.put("date", tx.getDate().format(ValueParser.getFormatter()));
        if (tx.getCreatedAt() != null)
            map.put("createdAt", tx.getCreatedAt().format(ValueParser.getFormatter()));
        if (tx.getUpdatedAt() != null)
            map.put("updatedAt", tx.getUpdatedAt().format(ValueParser.getFormatter()));

        return map;
    }

    public static Transaction fromMap(Map<String, Object> map) {
        Transaction tx = new Transaction();
        tx.setId(ValueParser.parseLong(map.get("id")));
        tx.setUserId(ValueParser.parseLong(map.get("userId")));

        String operationType = parseString(map.get("operationType"));
        if (operationType != null) tx.setOperationType(OperationTypes.fromValue(operationType));

        String paymentMethod = parseString(map.get("paymentMethod"));
        if (paymentMethod != null) tx.setPaymentMethod(PaymentMethod.fromValue(paymentMethod));

        String status = parseString(map.get("status"));
        tx.setStatus(status != null ? TransactionStatus.fromValue(status) : TransactionStatus.COMPLETED);

        tx.setSourceAccountId(ValueParser.parseNullableLong(map.get("sourceAccountId")));
        tx.setDestinationAccountId(ValueParser.parseNullableLong(map.get("destinationAccountId")));
        tx.setExternalEntityId(ValueParser.parseNullableLong(map.get("externalEntityId")));
        tx.setCategoryId(ValueParser.parseLong(map.get("categoryId")));
        tx.setParentTransactionId(ValueParser.parseNullableLong(map.get("parentTransactionId")));
        tx.setAmount(ValueParser.parseNullableBigDecimal(map.get("amount")));
        tx.setConcept(parseString(map.get("concept")));
        tx.setDescription(parseString(map.get("description")));
        tx.setComments(parseString(map.get("comments")));
        tx.setReceiptUrl(parseString(map.get("receiptUrl")));
        tx.setDate(ValueParser.parseZonedDateTime(map.get("date")));
        tx.setTimezone(parseString(map.get("timezone")));
        tx.setTagIds(parseTagIds(map.get("tagIds")));

        Map<String, Object> cardDetail = parseNestedMap(map.get("cardDetail"));
        if (cardDetail != null) tx.setCardDetail(CardTransactionDetailMapper.fromMap(cardDetail));

        Map<String, Object> walletDetail = parseNestedMap(map.get("walletDetail"));
        if (walletDetail != null) tx.setWalletDetail(WalletTransactionDetailMapper.fromMap(walletDetail));

        tx.setCreatedAt(ValueParser.parseZonedDateTime(map.get("createdAt")));
        tx.setUpdatedAt(ValueParser.parseZonedDateTime(map.get("updatedAt")));
        return tx;
    }

    // null o el literal "null" → null
    private static String parseString(Object value) {
        if (value == null) return null;
        String str = value.toString();
        return "null".equals(str) ? null : str;
    }

    // Acepta List<Object> con Strings o Numbers; ignora nulos e inválidos
    private static List<Long> parseTagIds(Object value) {
        List<Long> tagIds = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                Long tagId = ValueParser.parseNullableLong(item);
                if (tagId != null) tagIds.add(tagId);
            }
        }
        return tagIds;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseNestedMap(Object value) {
        return value instanceof Map<?, ?> nested ? (Map<String, Object>) nested : null;
    }
}
