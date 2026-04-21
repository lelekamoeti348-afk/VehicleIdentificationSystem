package com.vehicle.vis.vehicleidentificationsystem.controllers;

import com.vehicle.vis.vehicleidentificationsystem.MainApplication;
import com.vehicle.vis.vehicleidentificationsystem.database.DatabaseConnection;
import com.vehicle.vis.vehicleidentificationsystem.models.User;
import com.vehicle.vis.vehicleidentificationsystem.utils.AlertHelper;
import com.vehicle.vis.vehicleidentificationsystem.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class WorkshopController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label totalServicesLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label vehiclesServicedLabel;
    @FXML private Label avgServiceCostLabel;

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button refreshButton;
    @FXML private VBox vehicleInfoCard;
    @FXML private Label regNumberLabel;
    @FXML private Label ownerLabel;
    @FXML private Label makeModelLabel;
    @FXML private Label yearColorLabel;
    @FXML private Label mileageLabel;
    @FXML private Label statusLabel;

    @FXML private TableView<ServiceRecordItem> serviceHistoryTable;
    @FXML private TableColumn<ServiceRecordItem, Integer> colServiceId;
    @FXML private TableColumn<ServiceRecordItem, String> colServiceDate;
    @FXML private TableColumn<ServiceRecordItem, String> colServiceType;
    @FXML private TableColumn<ServiceRecordItem, String> colServiceDesc;
    @FXML private TableColumn<ServiceRecordItem, Double> colServiceCost;
    @FXML private TableColumn<ServiceRecordItem, Integer> colOdometer;
    @FXML private TableColumn<ServiceRecordItem, String> colVehicleReg;

    private User currentUser;
    private ObservableList<ServiceRecordItem> serviceRecords = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("========== WORKSHOP CONTROLLER LOADED ==========");

        currentUser = SessionManager.getInstance().getCurrentUser();
        welcomeLabel.setText("Welcome, " + currentUser.getFullName());

        setupTableColumns();
        loadAllServiceRecords();
        loadStatistics();

        searchButton.setTooltip(new Tooltip("Search by registration number"));
        refreshButton.setTooltip(new Tooltip("Refresh all data"));
    }

    private void setupTableColumns() {
        colServiceId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colServiceDate.setCellValueFactory(new PropertyValueFactory<>("serviceDate"));
        colServiceType.setCellValueFactory(new PropertyValueFactory<>("serviceType"));
        colServiceDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colServiceCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
        colOdometer.setCellValueFactory(new PropertyValueFactory<>("odometer"));
        colVehicleReg.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
    }

    private void loadAllServiceRecords() {
        serviceRecords.clear();
        String sql = "SELECT s.service_id, s.service_date, s.service_type, s.description, s.cost, s.odometer_reading, v.registration_number " +
                "FROM service_records s JOIN vehicles v ON s.vehicle_id = v.vehicle_id " +
                "ORDER BY s.service_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                serviceRecords.add(new ServiceRecordItem(
                        rs.getInt("service_id"),
                        rs.getDate("service_date").toString(),
                        rs.getString("service_type"),
                        rs.getString("description"),
                        rs.getDouble("cost"),
                        rs.getInt("odometer_reading"),
                        rs.getString("registration_number")
                ));
            }
            serviceHistoryTable.setItems(serviceRecords);
            System.out.println("Loaded " + serviceRecords.size() + " service records");
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Could not load service records: " + e.getMessage());
        }
    }

    private void loadStatistics() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) FROM service_records");
            if (rs1.next()) totalServicesLabel.setText(String.valueOf(rs1.getInt(1)));

            ResultSet rs2 = stmt.executeQuery("SELECT COALESCE(SUM(cost), 0) FROM service_records");
            if (rs2.next()) totalRevenueLabel.setText(String.format("M%.2f", rs2.getDouble(1)));

            ResultSet rs3 = stmt.executeQuery("SELECT COUNT(DISTINCT vehicle_id) FROM service_records");
            if (rs3.next()) vehiclesServicedLabel.setText(String.valueOf(rs3.getInt(1)));

            ResultSet rs4 = stmt.executeQuery("SELECT COALESCE(AVG(cost), 0) FROM service_records");
            if (rs4.next()) avgServiceCostLabel.setText(String.format("M%.2f", rs4.getDouble(1)));

        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Could not load statistics: " + e.getMessage());
        }
    }

    // ==================== LOGOUT ====================
    @FXML
    private void handleLogout() {
        AlertHelper.showConfirmation("Logout", "Are you sure you want to logout?", () -> {
            MainApplication.changeScene("/views/login.fxml", "Vehicle Identification System - Login");
        });
    }

    // ==================== SEARCH ====================
    @FXML
    private void searchVehicle() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            AlertHelper.showWarning("Search Error", "Please enter a registration number");
            return;
        }

        String vehicleSql = "SELECT v.registration_number, v.make, v.model, v.year, v.color, v.mileage, v.status, " +
                "COALESCE(u.full_name, 'Unknown') as owner_name " +
                "FROM vehicles v " +
                "LEFT JOIN customers c ON v.owner_id = c.customer_id " +
                "LEFT JOIN users u ON c.customer_id = u.user_id " +
                "WHERE v.registration_number ILIKE ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(vehicleSql)) {
            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                regNumberLabel.setText(rs.getString("registration_number"));
                ownerLabel.setText(rs.getString("owner_name"));
                makeModelLabel.setText(rs.getString("make") + " " + rs.getString("model"));
                yearColorLabel.setText(rs.getInt("year") + " | " + rs.getString("color"));
                mileageLabel.setText(rs.getInt("mileage") + " km");
                statusLabel.setText(rs.getString("status"));
                vehicleInfoCard.setVisible(true);
                vehicleInfoCard.setManaged(true);

                // Filter table to show only this vehicle's records
                ObservableList<ServiceRecordItem> filtered = FXCollections.observableArrayList();
                for (ServiceRecordItem record : serviceRecords) {
                    if (record.getRegistrationNumber().equalsIgnoreCase(rs.getString("registration_number"))) {
                        filtered.add(record);
                    }
                }
                serviceHistoryTable.setItems(filtered);
                AlertHelper.showInfo("Vehicle Found", "Showing " + filtered.size() + " records for: " + rs.getString("registration_number"));
            } else {
                AlertHelper.showInfo("Not Found", "No vehicle found with registration: " + searchTerm);
                vehicleInfoCard.setVisible(false);
                vehicleInfoCard.setManaged(false);
                serviceHistoryTable.setItems(serviceRecords);
            }
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Could not search: " + e.getMessage());
        }
    }

    @FXML
    private void refreshData() {
        loadAllServiceRecords();
        loadStatistics();
        searchField.clear();
        vehicleInfoCard.setVisible(false);
        vehicleInfoCard.setManaged(false);
        AlertHelper.showInfo("Refreshed", "All data has been refreshed!");
    }

    // ==================== ADD SERVICE RECORD ====================
    @FXML
    private void newService() {
        ObservableList<String> vehicleList = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT registration_number FROM vehicles ORDER BY registration_number")) {
            while (rs.next()) {
                vehicleList.add(rs.getString("registration_number"));
            }
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Could not load vehicles: " + e.getMessage());
            return;
        }

        if (vehicleList.isEmpty()) {
            AlertHelper.showWarning("No Vehicles", "Please add vehicles first.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Service Record");
        dialog.setHeaderText("Enter service details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> regCombo = new ComboBox<>();
        regCombo.setItems(vehicleList);
        regCombo.setPromptText("Select Vehicle Registration");
        regCombo.setValue(vehicleList.get(0));

        DatePicker datePicker = new DatePicker(LocalDate.now());
        ComboBox<String> serviceTypeCombo = new ComboBox<>();
        serviceTypeCombo.getItems().addAll("Oil Change", "Major Service", "Brake Service", "Tire Rotation",
                "Engine Repair", "Transmission Service", "AC Service", "Inspection");
        serviceTypeCombo.setValue("Oil Change");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Describe the service performed...");
        descriptionArea.setPrefRowCount(3);

        TextField costField = new TextField();
        costField.setPromptText("Cost (M)");

        TextField odometerField = new TextField();
        odometerField.setPromptText("Odometer reading (km)");

        grid.add(new Label("Vehicle Registration:*"), 0, 0);
        grid.add(regCombo, 1, 0);
        grid.add(new Label("Service Date:*"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Service Type:*"), 0, 2);
        grid.add(serviceTypeCombo, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(descriptionArea, 1, 3);
        grid.add(new Label("Cost (M):*"), 0, 4);
        grid.add(costField, 1, 4);
        grid.add(new Label("Odometer (km):*"), 0, 5);
        grid.add(odometerField, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String reg = regCombo.getValue();
            String costText = costField.getText().trim();
            String odometerText = odometerField.getText().trim();

            if (reg == null || costText.isEmpty() || odometerText.isEmpty()) {
                AlertHelper.showError("Validation Error", "Please fill all required fields (*)");
                return;
            }

            double cost;
            int odometer;
            try {
                cost = Double.parseDouble(costText);
                odometer = Integer.parseInt(odometerText);
            } catch (NumberFormatException e) {
                AlertHelper.showError("Invalid Input", "Cost must be a number and odometer must be an integer.");
                return;
            }

            String insertSql = "INSERT INTO service_records (vehicle_id, service_date, service_type, description, cost, odometer_reading) " +
                    "VALUES ((SELECT vehicle_id FROM vehicles WHERE registration_number = ?), ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, reg);
                pstmt.setDate(2, Date.valueOf(datePicker.getValue()));
                pstmt.setString(3, serviceTypeCombo.getValue());
                pstmt.setString(4, descriptionArea.getText());
                pstmt.setDouble(5, cost);
                pstmt.setInt(6, odometer);
                pstmt.executeUpdate();
                AlertHelper.showInfo("Success", "Service record added successfully!");
                refreshData();
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not add service record: " + e.getMessage());
            }
        }
    }

    // ==================== EDIT SERVICE RECORD ====================
    @FXML
    private void editServiceRecord() {
        ServiceRecordItem selected = serviceHistoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a service record to edit.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Service Record");
        dialog.setHeaderText("Update service details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Vehicle registration (read‑only)
        Label regLabel = new Label(selected.getRegistrationNumber());
        regLabel.setStyle("-fx-font-weight: bold;");

        DatePicker datePicker = new DatePicker(LocalDate.parse(selected.getServiceDate()));
        ComboBox<String> serviceTypeCombo = new ComboBox<>();
        serviceTypeCombo.getItems().addAll("Oil Change", "Major Service", "Brake Service", "Tire Rotation",
                "Engine Repair", "Transmission Service", "AC Service", "Inspection");
        serviceTypeCombo.setValue(selected.getServiceType());

        TextArea descriptionArea = new TextArea(selected.getDescription());
        descriptionArea.setPrefRowCount(3);

        TextField costField = new TextField(String.valueOf(selected.getCost()));
        TextField odometerField = new TextField(String.valueOf(selected.getOdometer()));

        grid.add(new Label("Vehicle Registration:"), 0, 0);
        grid.add(regLabel, 1, 0);
        grid.add(new Label("Service Date:*"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Service Type:*"), 0, 2);
        grid.add(serviceTypeCombo, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(descriptionArea, 1, 3);
        grid.add(new Label("Cost (M):*"), 0, 4);
        grid.add(costField, 1, 4);
        grid.add(new Label("Odometer (km):*"), 0, 5);
        grid.add(odometerField, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String costText = costField.getText().trim();
            String odometerText = odometerField.getText().trim();
            if (costText.isEmpty() || odometerText.isEmpty()) {
                AlertHelper.showError("Validation Error", "Cost and odometer are required.");
                return;
            }
            double cost;
            int odometer;
            try {
                cost = Double.parseDouble(costText);
                odometer = Integer.parseInt(odometerText);
            } catch (NumberFormatException e) {
                AlertHelper.showError("Invalid Input", "Cost must be a number, odometer an integer.");
                return;
            }

            String updateSql = "UPDATE service_records SET service_date=?, service_type=?, description=?, cost=?, odometer_reading=? WHERE service_id=?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setDate(1, Date.valueOf(datePicker.getValue()));
                pstmt.setString(2, serviceTypeCombo.getValue());
                pstmt.setString(3, descriptionArea.getText());
                pstmt.setDouble(4, cost);
                pstmt.setInt(5, odometer);
                pstmt.setInt(6, selected.getId());
                pstmt.executeUpdate();
                AlertHelper.showInfo("Success", "Service record updated.");
                refreshData();
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not update: " + e.getMessage());
            }
        }
    }

    // ==================== DELETE SERVICE RECORD ====================
    @FXML
    private void deleteServiceRecord() {
        ServiceRecordItem selected = serviceHistoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a service record to delete.");
            return;
        }
        AlertHelper.showConfirmation("Delete Record", "Delete service record for vehicle " + selected.getRegistrationNumber() + "?", () -> {
            String sql = "DELETE FROM service_records WHERE service_id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selected.getId());
                pstmt.executeUpdate();
                AlertHelper.showInfo("Success", "Service record deleted.");
                refreshData();
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not delete: " + e.getMessage());
            }
        });
    }

    // ==================== PRINT INVOICE ====================
    @FXML
    private void printInvoice() {
        ServiceRecordItem selected = serviceHistoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a service record first");
            return;
        }

        String invoice = "═══════════════════════════════════════════════\n" +
                "              WORKSHOP INVOICE\n" +
                "═══════════════════════════════════════════════\n\n" +
                "Service Date: " + selected.getServiceDate() + "\n" +
                "Service Type: " + selected.getServiceType() + "\n" +
                "Description: " + selected.getDescription() + "\n" +
                "Vehicle: " + selected.getRegistrationNumber() + "\n" +
                "Speedometer: " + selected.getOdometer() + " km\n" +
                "───────────────────────────────────────────────\n" +
                "Total Amount: M" + selected.getCost() + "\n" +
                "═══════════════════════════════════════════════\n" +
                "Thank you for choosing us!\n";

        AlertHelper.showInfo("Invoice", invoice);
    }

    // ==================== INNER CLASS ====================
    public static class ServiceRecordItem {
        private final int id;
        private final String serviceDate, serviceType, description, registrationNumber;
        private final double cost;
        private final int odometer;

        public ServiceRecordItem(int id, String serviceDate, String serviceType, String description,
                                 double cost, int odometer, String registrationNumber) {
            this.id = id;
            this.serviceDate = serviceDate;
            this.serviceType = serviceType;
            this.description = description;
            this.cost = cost;
            this.odometer = odometer;
            this.registrationNumber = registrationNumber;
        }

        public int getId() { return id; }
        public String getServiceDate() { return serviceDate; }
        public String getServiceType() { return serviceType; }
        public String getDescription() { return description; }
        public double getCost() { return cost; }
        public int getOdometer() { return odometer; }
        public String getRegistrationNumber() { return registrationNumber; }
    }
}