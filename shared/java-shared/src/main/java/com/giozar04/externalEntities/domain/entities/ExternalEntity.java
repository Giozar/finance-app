package com.giozar04.externalEntities.domain.entities;

import java.io.Serializable;
import java.time.ZonedDateTime;

import com.giozar04.externalEntities.domain.enums.ExternalEntityTypes;

public class ExternalEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private ExternalEntityTypes type;
    private String contact;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public ExternalEntity() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ExternalEntityTypes getType() { return type; }
    public void setType(ExternalEntityTypes type) { this.type = type; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return name + "( " +  type.getLabel() + " )";
    }
}
