package com.giozar04.transactions.domain.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

import com.giozar04.transactions.domain.enums.OperationTypes;
import com.giozar04.transactions.domain.enums.PaymentMethod;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private OperationTypes operationType;
    private PaymentMethod paymentMethod;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private Long externalEntityId;

    private BigDecimal amount;
    private String concept; // título o nombre
    private String category; // puede ser FK o string denormalizado
    private String description;
    private String comments;
    private ZonedDateTime date;
    private String timezone;
    private String tags; // CSV o tabla M-N

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Transaction() {}

    // Getters y setters

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public OperationTypes getOperationType() { return operationType; }
    public void setOperationType(OperationTypes operationType) { this.operationType = operationType; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public Long getSourceAccountId() { return sourceAccountId; }
    public void setSourceAccountId(Long sourceAccountId) { this.sourceAccountId = sourceAccountId; }

    public Long getDestinationAccountId() { return destinationAccountId; }
    public void setDestinationAccountId(Long destinationAccountId) { this.destinationAccountId = destinationAccountId; }

    public Long getExternalEntityId() { return externalEntityId; }
    public void setExternalEntityId(Long externalEntityId) { this.externalEntityId = externalEntityId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getConcept() { return concept; }
    public void setConcept(String concept) { this.concept = concept; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public ZonedDateTime getDate() { return date; }
    public void setDate(ZonedDateTime date) { this.date = date; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
}
