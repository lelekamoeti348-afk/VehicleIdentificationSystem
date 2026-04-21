package com.vehicle.vis.vehicleidentificationsystem.controllers;


import com.vehicle.vis.vehicleidentificationsystem.MainApplication;
import com.vehicle.vis.vehicleidentificationsystem.database.DatabaseConnection;
import com.vehicle.vis.vehicleidentificationsystem.models.User;
import com.vehicle.vis.vehicleidentificationsystem.utils.AlertHelper;
import com.vehicle.vis.vehicleidentificationsystem.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML private Label usernameLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        loadUserInfo();
    }

    private void loadUserInfo() {
        usernameLabel.setText(currentUser.getUsername());
        fullNameLabel.setText(currentUser.getFullName());
        emailLabel.setText(currentUser.getEmail());
    }

    @FXML
    private void changePassword() {
        String currentPw = currentPasswordField.getText().trim();
        String newPw = newPasswordField.getText().trim();
        String confirmPw = confirmPasswordField.getText().trim();

        if (currentPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
            AlertHelper.showError("Validation Error", "Please fill all password fields.");
            return;
        }
        if (!newPw.equals(confirmPw)) {
            AlertHelper.showError("Password Mismatch", "New password and confirmation do not match.");
            return;
        }
        if (newPw.length() < 4) {
            AlertHelper.showError("Weak Password", "Password must be at least 4 characters.");
            return;
        }

        // Verify current password
        String sql = "SELECT password_hash FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (!BCrypt.checkpw(currentPw, storedHash)) {
                    AlertHelper.showError("Authentication Error", "Current password is incorrect.");
                    return;
                }
                // Hash new password
                String newHash = BCrypt.hashpw(newPw, BCrypt.gensalt());
                String updateSql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
                try (PreparedStatement upstmt = conn.prepareStatement(updateSql)) {
                    upstmt.setString(1, newHash);
                    upstmt.setInt(2, currentUser.getUserId());
                    upstmt.executeUpdate();
                    AlertHelper.showInfo("Success", "Password changed successfully. Please login again.");
                    // Logout and return to login screen
                    SessionManager.getInstance().logout();
                    MainApplication.changeScene("/views/login.fxml", "Vehicle Identification System - Login");
                }
            }
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Could not change password: " + e.getMessage());
        }
    }

    @FXML
    private void refreshSettings() {
        loadUserInfo();
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        AlertHelper.showInfo("Refreshed", "Account info refreshed.");
    }
}