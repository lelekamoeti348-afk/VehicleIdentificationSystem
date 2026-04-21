package com.vehicle.vis.vehicleidentificationsystem.controllers;

import com.vehicle.vis.vehicleidentificationsystem.MainApplication;
import com.vehicle.vis.vehicleidentificationsystem.models.User;
import com.vehicle.vis.vehicleidentificationsystem.utils.AlertHelper;
import com.vehicle.vis.vehicleidentificationsystem.utils.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Label dateTimeLabel;
    @FXML private StackPane contentArea;
    @FXML private VBox sidebar;
    @FXML private Button btnDashboard;
    @FXML private Button btnVehicles;
    @FXML private Button btnReports;
    @FXML private Button btnSettings;

    private User currentUser;
    private Timeline clockTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFullName());
            roleLabel.setText(currentUser.getRoleDescription());
        }

        startClock();

        // Load the user's default dashboard
        loadUserDashboard();

        // Style active button (optional)
        setActiveButton(btnDashboard);
    }

    private void startClock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm:ss");
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            dateTimeLabel.setText(LocalDateTime.now().format(formatter));
        }));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }

    private void loadUserDashboard() {
        if (currentUser != null) {
            loadView(currentUser.getDashboardView());
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showError("Navigation Error", "Could not load view: " + fxmlPath);
        }
    }

    private void setActiveButton(Button active) {
        // Reset all buttons to default style
        Button[] buttons = {btnDashboard, btnVehicles, btnReports, btnSettings};
        for (Button b : buttons) {
            b.setStyle("-fx-background-color: transparent; -fx-text-fill: #a0aec0;");
        }
        active.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
    }

    // ==================== MENU ACTIONS ====================
    @FXML
    private void handleExit() {
        AlertHelper.showConfirmation("Exit", "Are you sure you want to exit?", () -> Platform.exit());
    }

    @FXML
    private void showAbout() {
        AlertHelper.showInfo("About", "Vehicle Identification System\nVersion 1.0\n© 2026");
    }

    @FXML
    private void handleLogout() {
        AlertHelper.showConfirmation("Logout", "Are you sure you want to logout?", () -> {
            SessionManager.getInstance().logout();
            MainApplication.changeScene("/views/login.fxml", "Vehicle Identification System - Login");
        });
    }

    // ==================== SIDEBAR NAVIGATION ====================
    @FXML
    private void showDashboard() {
        loadUserDashboard();
        setActiveButton(btnDashboard);
    }

    @FXML
    private void showVehicles() {
        // Load a generic vehicle management view (works for all roles)
        loadView("/views/vehicles.fxml");
        setActiveButton(btnVehicles);
    }

    @FXML
    private void showReports() {
        loadView("/views/reports.fxml");
        setActiveButton(btnReports);
    }

    @FXML
    private void showSettings() {
        loadView("/views/settings.fxml");
        setActiveButton(btnSettings);
    }
}