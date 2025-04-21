package com.giozar04.tags.domain.entities;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class Tag implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private String color;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Tag() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return name;
    }
}
