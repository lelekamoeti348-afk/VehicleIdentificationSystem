package com.vehicle.vis.vehicleidentificationsystem.controllers;

import com.vehicle.vis.vehicleidentificationsystem.database.DatabaseConnection;
import com.vehicle.vis.vehicleidentificationsystem.utils.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class VehicleController implements Initializable {

    @FXML private TableView<VehicleItem> vehicleTable;
    @FXML private TableColumn<VehicleItem, String> colRegNumber;
    @FXML private TableColumn<VehicleItem, String> colMake;
    @FXML private TableColumn<VehicleItem, String> colModel;
    @FXML private TableColumn<VehicleItem, Integer> colYear;
    @FXML private TableColumn<VehicleItem, String> colColor;
    @FXML private TableColumn<VehicleItem, String> colOwner;
    @FXML private TableColumn<VehicleItem, String> colStatus;
    @FXML private TextField searchField;

    private ObservableList<VehicleItem> vehicleList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadVehicles();
        searchField.textProperty().addListener((obs, old, newVal) -> filterVehicles(newVal));
    }

    private void setupTableColumns() {
        colRegNumber.setCellValueFactory(new PropertyValueFactory<>("registration"));
        colMake.setCellValueFactory(new PropertyValueFactory<>("make"));
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colColor.setCellValueFactory(new PropertyValueFactory<>("color"));
        colOwner.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadVehicles() {
        vehicleList.clear();
        String sql = "SELECT v.registration_number, v.make, v.model, v.year, v.color, v.status, COALESCE(u.full_name, 'No owner') as owner_name " +
                "FROM vehicles v LEFT JOIN customers c ON v.owner_id = c.customer_id " +
                "LEFT JOIN users u ON c.customer_id = u.user_id ORDER BY v.vehicle_id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                vehicleList.add(new VehicleItem(
                        rs.getString("registration_number"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getString("color"),
                        rs.getString("owner_name"),
                        rs.getString("status")
                ));
            }
            vehicleTable.setItems(vehicleList);
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Could not load vehicles: " + e.getMessage());
        }
    }

    private void filterVehicles(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            vehicleTable.setItems(vehicleList);
            return;
        }
        String lower = keyword.toLowerCase();
        ObservableList<VehicleItem> filtered = FXCollections.observableArrayList();
        for (VehicleItem v : vehicleList) {
            if (v.getRegistration().toLowerCase().contains(lower) ||
                    v.getMake().toLowerCase().contains(lower) ||
                    v.getModel().toLowerCase().contains(lower)) {
                filtered.add(v);
            }
        }
        vehicleTable.setItems(filtered);
    }

    @FXML
    private void refreshData() {
        loadVehicles();
        AlertHelper.showInfo("Refreshed", "Vehicle list updated.");
    }

    public static class VehicleItem {
        private final String registration, make, model, color, ownerName, status;
        private final int year;
        public VehicleItem(String reg, String mk, String md, int yr, String col, String own, String stat) {
            this.registration = reg; this.make = mk; this.model = md;
            this.year = yr; this.color = col; this.ownerName = own; this.status = stat;
        }
        public String getRegistration() { return registration; }
        public String getMake() { return make; }
        public String getModel() { return model; }
        public int getYear() { return year; }
        public String getColor() { return color; }
        public String getOwnerName() { return ownerName; }
        public String getStatus() { return status; }
    }
}