package com.amalitech.smartecommerce.models;

import java.time.LocalDateTime;

/**
 * User model representing system users (customers, admins, managers).
 */
public class User {
    private int userId;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Role {
        ADMIN("admin"),
        MANAGER("manager"),
        CUSTOMER("customer");

        private final String value;

        Role(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Role fromString(String text) {
            for (Role role : Role.values()) {
                if (role.value.equalsIgnoreCase(text)) {
                    return role;
                }
            }
            return CUSTOMER;
        }
    }

    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.role = Role.CUSTOMER.getValue();
    }

    public User(int userId, String email, String passwordHash, String role) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public User(String email, String passwordHash, String role) {
        this();
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public User(String email, String passwordHash, String firstName, String lastName, String role) {
        this();
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public User(int userId, String email, String passwordHash, String firstName, String lastName,
                String role, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFullName() {
        if (firstName == null && lastName == null) {
            return email;
        }
        return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
    }

    public boolean isAdmin() {
        return Role.ADMIN.getValue().equalsIgnoreCase(role);
    }

    public boolean isManager() {
        return Role.MANAGER.getValue().equalsIgnoreCase(role);
    }

    public boolean isCustomer() {
        return Role.CUSTOMER.getValue().equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "User{id=" + userId + ", email='" + email + "', name='" + getFullName() + "', role='" + role + "'}";
    }
}
