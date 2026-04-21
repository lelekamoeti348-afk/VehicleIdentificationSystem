package com.vehicle.vis.vehicleidentificationsystem.models;

import java.time.LocalDateTime;

// Base class demonstrating inheritance
public abstract class User {
    protected int userId;
    protected String username;
    protected String fullName;
    protected String email;
    protected String phone;
    protected String role;
    protected boolean isActive;
    protected LocalDateTime lastLogin;

    public User(int userId, String username, String fullName, String email, String phone, String role) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.isActive = true;
    }

    // Abstract methods - polymorphism
    public abstract String getDashboardView();
    public abstract String getRoleDescription();

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}