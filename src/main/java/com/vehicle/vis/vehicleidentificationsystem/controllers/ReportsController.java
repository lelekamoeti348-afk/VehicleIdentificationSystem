package com.vehicle.vis.vehicleidentificationsystem.controllers;

import com.vehicle.vis.vehicleidentificationsystem.database.DatabaseConnection;
import com.vehicle.vis.vehicleidentificationsystem.utils.AlertHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ReportsController implements Initializable {

    @FXML private Label totalVehiclesLabel;
    @FXML private Label totalServicesLabel;
    @FXML private Label activePoliciesLabel;
    @FXML private Label totalUsersLabel;
    @FXML private BarChart<String, Number> vehicleBarChart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadStatistics();
        loadBarChart();
    }

    private void loadStatistics() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM vehicles");
            if (rs.next()) totalVehiclesLabel.setText(String.valueOf(rs.getInt(1)));
            rs = stmt.executeQuery("SELECT COUNT(*) FROM service_records");
            if (rs.next()) totalServicesLabel.setText(String.valueOf(rs.getInt(1)));
            rs = stmt.executeQuery("SELECT COUNT(*) FROM insurance_policies WHERE status = 'ACTIVE'");
            if (rs.next()) activePoliciesLabel.setText(String.valueOf(rs.getInt(1)));
            rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next()) totalUsersLabel.setText(String.valueOf(rs.getInt(1)));
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Could not load statistics: " + e.getMessage());
        }
    }

    private void loadBarChart() {
        Map<String, Integer> makeCount = new HashMap<>();
        String sql = "SELECT make, COUNT(*) as count FROM vehicles GROUP BY make ORDER BY count DESC LIMIT 5";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                makeCount.put(rs.getString("make"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Could not load chart data: " + e.getMessage());
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Number of Vehicles");
        for (Map.Entry<String, Integer> entry : makeCount.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        vehicleBarChart.getData().clear();
        vehicleBarChart.getData().add(series);
    }

    @FXML
    private void refreshReports() {
        loadStatistics();
        loadBarChart();
        AlertHelper.showInfo("Refreshed", "Reports updated.");
    }
}