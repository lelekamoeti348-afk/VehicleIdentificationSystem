package com.vehicle.vis.vehicleidentificationsystem.utils;

import com.vehicle.vis.vehicleidentificationsystem.models.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean hasRole(String role) {
        return currentUser != null && currentUser.getRole().equals(role);
    }
}