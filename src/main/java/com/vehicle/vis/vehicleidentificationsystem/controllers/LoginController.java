package com.vehicle.vis.vehicleidentificationsystem.controllers;

import com.vehicle.vis.vehicleidentificationsystem.MainApplication;
import com.vehicle.vis.vehicleidentificationsystem.dao.UserDAO;
import com.vehicle.vis.vehicleidentificationsystem.models.User;
import com.vehicle.vis.vehicleidentificationsystem.utils.AlertHelper;
import com.vehicle.vis.vehicleidentificationsystem.utils.AnimationUtils;
import com.vehicle.vis.vehicleidentificationsystem.utils.SessionManager;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button exitButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label statusLabel;
    @FXML private VBox loginCard;
    @FXML private StackPane rootPane;

    private UserDAO userDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDAO = new UserDAO();

        // Apply drop shadow effect to login button
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(10);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2);
        dropShadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.3));
        loginButton.setEffect(dropShadow);

        // Apply fade transition animation to exit button
        AnimationUtils.applyPulseAnimation(exitButton);

        // Setup enter key handler
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus();
            }
        });

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });

        // Animate login card entrance
        AnimationUtils.fadeIn(loginCard, 0.8);
        AnimationUtils.slideInFromLeft(loginCard, 0.6);

        // Set default test credentials
        usernameField.setText("moeti_leleka");
        passwordField.setText("password");
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            AlertHelper.showError("Login Error", "Please enter both username and password");
            AnimationUtils.shakeField(usernameField);
            return;
        }

        // Show loading indicator
        loadingIndicator.setVisible(true);
        loginButton.setDisable(true);
        statusLabel.setText("Authenticating...");
        statusLabel.setStyle("-fx-text-fill: #3182ce;");

        // Run login in background thread
        javafx.concurrent.Task<User> loginTask = new javafx.concurrent.Task<>() {
            @Override
            protected User call() throws Exception {
                return userDAO.authenticate(username, password);
            }
        };

        loginTask.setOnSucceeded(event -> {
            User user = loginTask.getValue();
            loadingIndicator.setVisible(false);

            if (user != null) {
                // Login successful
                SessionManager.getInstance().setCurrentUser(user);
                statusLabel.setText("Login successful! Redirecting...");
                statusLabel.setStyle("-fx-text-fill: #48bb78;");

                // Animate successful login
                AnimationUtils.fadeOut(loginCard, 0.3);

                PauseTransition redirect = new PauseTransition(Duration.seconds(0.5));
                redirect.setOnFinished(e -> {
                    try {
                        // *** CHANGE: Go to main dashboard with menu bar ***
                        MainApplication.changeScene("/views/dashboard.fxml",
                                "Vehicle Identification System - Dashboard");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        AlertHelper.showError("Navigation Error", "Could not load dashboard: " + ex.getMessage());
                        loadingIndicator.setVisible(false);
                        loginButton.setDisable(false);
                    }
                });
                redirect.play();
            } else {
                // Login failed
                statusLabel.setText("Invalid username or password!");
                statusLabel.setStyle("-fx-text-fill: #f56565;");
                AnimationUtils.shakeField(loginCard);
                passwordField.clear();
                passwordField.requestFocus();
                loginButton.setDisable(false);

                PauseTransition reset = new PauseTransition(Duration.seconds(2));
                reset.setOnFinished(e -> {
                    statusLabel.setText("");
                    statusLabel.setStyle("");
                });
                reset.play();
            }
        });

        loginTask.setOnFailed(event -> {
            loadingIndicator.setVisible(false);
            loginButton.setDisable(false);
            statusLabel.setText("Connection error!");
            statusLabel.setStyle("-fx-text-fill: #f56565;");
            loginTask.getException().printStackTrace();
            AlertHelper.showError("Error", "Database connection error: " + loginTask.getException().getMessage());
        });

        new Thread(loginTask).start();
    }

    @FXML
    private void handleExit() {
        AlertHelper.showConfirmation("Exit", "Are you sure you want to exit the application?", () -> {
            System.exit(0);
        });
    }
}