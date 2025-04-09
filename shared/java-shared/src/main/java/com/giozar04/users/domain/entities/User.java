package com.giozar04.users.domain.entities;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private String email;
    private String password;
    private double globalBalance;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public User() {}

    public User(long id, String name, String email, String password, double globalBalance,
                ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.globalBalance = globalBalance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters y setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public double getGlobalBalance() { return globalBalance; }
    public void setGlobalBalance(double globalBalance) { this.globalBalance = globalBalance; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return name + "( " + email + " )" ;
    }

    
}
