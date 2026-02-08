package com.giozar04.categories.domain.entities;

import java.io.Serializable;
import java.time.ZonedDateTime;

import com.giozar04.categories.domain.enums.CategoryTypes;

public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long userId;
    private String name;
    private CategoryTypes type; // Enum
    private String icon;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Category() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CategoryTypes getType() { return type; }
    public void setType(CategoryTypes type) { this.type = type; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
}
