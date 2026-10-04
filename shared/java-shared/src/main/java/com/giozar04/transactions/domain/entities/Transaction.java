package com.giozar04.transactions.domain.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;
import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.domain.enums.PaymentMethod;
import com.giozar04.transactions.domain.enums.TransactionStatus;
import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;

/**
 * Raíz de agregado: incluye los ids de etiquetas y los detalles de tarjeta/wallet anidados.
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long userId;
    private OperationTypes operationType;
    private PaymentMethod paymentMethod;
    private TransactionStatus status;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private Long externalEntityId;
    private long categoryId;
    private Long parentTransactionId;

    private BigDecimal amount;
    private String concept; // título o nombre
    private String description;
    private String comments;
    private String receiptUrl;
    private ZonedDateTime date;
    private String timezone;

    private List<Long> tagIds; // nunca null
    private CardTransactionDetail cardDetail; // null si no aplica
    private WalletTransactionDetail walletDetail; // null si no aplica

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Transaction() {
        this.status = TransactionStatus.COMPLETED;
        this.tagIds = new ArrayList<>();
    }

    // Getters y setters

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public OperationTypes getOperationType() { return operationType; }
    public void setOperationType(OperationTypes operationType) { this.operationType = operationType; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public Long getSourceAccountId() { return sourceAccountId; }
    public void setSourceAccountId(Long sourceAccountId) { this.sourceAccountId = sourceAccountId; }

    public Long getDestinationAccountId() { return destinationAccountId; }
    public void setDestinationAccountId(Long destinationAccountId) { this.destinationAccountId = destinationAccountId; }

    public Long getExternalEntityId() { return externalEntityId; }
    public void setExternalEntityId(Long externalEntityId) { this.externalEntityId = externalEntityId; }

    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }

    public Long getParentTransactionId() { return parentTransactionId; }
    public void setParentTransactionId(Long parentTransactionId) { this.parentTransactionId = parentTransactionId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getConcept() { return concept; }
    public void setConcept(String concept) { this.concept = concept; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }

    public ZonedDateTime getDate() { return date; }
    public void setDate(ZonedDateTime date) { this.date = date; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public List<Long> getTagIds() { return tagIds; }
    public void setTagIds(List<Long> tagIds) { this.tagIds = tagIds != null ? tagIds : new ArrayList<>(); }

    public CardTransactionDetail getCardDetail() { return cardDetail; }
    public void setCardDetail(CardTransactionDetail cardDetail) { this.cardDetail = cardDetail; }

    public WalletTransactionDetail getWalletDetail() { return walletDetail; }
    public void setWalletDetail(WalletTransactionDetail walletDetail) { this.walletDetail = walletDetail; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() { return concept; }
}
