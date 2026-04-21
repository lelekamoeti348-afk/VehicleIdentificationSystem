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

import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class CustomerController implements Initializable {

    // Welcome label
    @FXML private Label welcomeLabel;

    // Statistics
    @FXML private Label vehicleCountLabel;
    @FXML private Label serviceCountLabel;
    @FXML private Label policyCountLabel;
    @FXML private Label queryCountLabel;

    // Tables
    @FXML private TableView<VehicleItem> vehiclesTable;
    @FXML private TableView<ServiceItem> serviceTable;
    @FXML private TableView<PolicyItem> policiesTable;
    @FXML private TableView<QueryItem> queriesTable;

    // Table columns for vehicles
    @FXML private TableColumn<VehicleItem, String> colRegNumber;
    @FXML private TableColumn<VehicleItem, String> colMake;
    @FXML private TableColumn<VehicleItem, String> colModel;
    @FXML private TableColumn<VehicleItem, Integer> colYear;
    @FXML private TableColumn<VehicleItem, String> colColor;
    @FXML private TableColumn<VehicleItem, String> colStatus;

    // Table columns for services
    @FXML private TableColumn<ServiceItem, String> colServiceDate;
    @FXML private TableColumn<ServiceItem, String> colServiceType;
    @FXML private TableColumn<ServiceItem, String> colServiceDesc;
    @FXML private TableColumn<ServiceItem, Double> colServiceCost;
    @FXML private TableColumn<ServiceItem, Integer> colOdometer;

    // Table columns for policies
    @FXML private TableColumn<PolicyItem, String> colPolicyNumber;
    @FXML private TableColumn<PolicyItem, String> colCompany;
    @FXML private TableColumn<PolicyItem, String> colStartDate;
    @FXML private TableColumn<PolicyItem, String> colEndDate;
    @FXML private TableColumn<PolicyItem, Double> colPremium;
    @FXML private TableColumn<PolicyItem, String> colPolicyStatus;

    // Table columns for queries
    @FXML private TableColumn<QueryItem, Integer> colQueryId;
    @FXML private TableColumn<QueryItem, String> colQueryDate;
    @FXML private TableColumn<QueryItem, String> colSubject;
    @FXML private TableColumn<QueryItem, String> colQueryText;
    @FXML private TableColumn<QueryItem, String> colResponse;
    @FXML private TableColumn<QueryItem, String> colQueryStatus;

    @FXML private Button refreshButton;
    @FXML private Button newQueryButton;

    private User currentUser;
    private int customerId;

    private ObservableList<VehicleItem> vehicleList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("========== CUSTOMER CONTROLLER LOADED ==========");
        currentUser = SessionManager.getInstance().getCurrentUser();
        customerId = currentUser.getUserId();
        welcomeLabel.setText("Welcome, " + currentUser.getFullName());

        setupTableColumns();
        loadVehicles();
        loadServiceHistory();
        loadInsurancePolicies();
        loadQueries();
        loadStatistics();

        refreshButton.setTooltip(new Tooltip("Refresh all data"));
        newQueryButton.setTooltip(new Tooltip("Submit a new query"));
    }

    // ==================== LOGOUT ====================
    @FXML
    private void handleLogout() {
        AlertHelper.showConfirmation("Logout", "Are you sure you want to logout?", () -> {
            MainApplication.changeScene("/views/login.fxml", "Vehicle Identification System - Login");
        });
    }

    // ==================== TABLE SETUP ====================
    private void setupTableColumns() {
        // Vehicles
        colRegNumber.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        colMake.setCellValueFactory(new PropertyValueFactory<>("make"));
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colColor.setCellValueFactory(new PropertyValueFactory<>("color"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Services
        colServiceDate.setCellValueFactory(new PropertyValueFactory<>("serviceDate"));
        colServiceType.setCellValueFactory(new PropertyValueFactory<>("serviceType"));
        colServiceDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colServiceCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
        colOdometer.setCellValueFactory(new PropertyValueFactory<>("odometer"));

        // Policies
        colPolicyNumber.setCellValueFactory(new PropertyValueFactory<>("policyNumber"));
        colCompany.setCellValueFactory(new PropertyValueFactory<>("company"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colPremium.setCellValueFactory(new PropertyValueFactory<>("premium"));
        colPolicyStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Queries (with ID column)
        colQueryId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colQueryDate.setCellValueFactory(new PropertyValueFactory<>("queryDate"));
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subject"));
        colQueryText.setCellValueFactory(new PropertyValueFactory<>("queryText"));
        colResponse.setCellValueFactory(new PropertyValueFactory<>("response"));
        colQueryStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    // ==================== LOAD DATA ====================
    private void loadVehicles() {
        vehicleList.clear();
        String sql = "SELECT registration_number, make, model, year, color, status FROM vehicles WHERE owner_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                vehicleList.add(new VehicleItem(
                        rs.getString("registration_number"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getString("color"),
                        rs.getString("status")
                ));
            }
            vehiclesTable.setItems(vehicleList);
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Could not load vehicles: " + e.getMessage());
        }
    }

    private void loadServiceHistory() {
        ObservableList<ServiceItem> services = FXCollections.observableArrayList();
        String sql = "SELECT s.service_date, s.service_type, s.description, s.cost, s.odometer_reading " +
                "FROM service_records s JOIN vehicles v ON s.vehicle_id = v.vehicle_id " +
                "WHERE v.owner_id = ? ORDER BY s.service_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                services.add(new ServiceItem(
                        rs.getDate("service_date").toString(),
                        rs.getString("service_type"),
                        rs.getString("description"),
                        rs.getDouble("cost"),
                        rs.getInt("odometer_reading")
                ));
            }
            serviceTable.setItems(services);
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Could not load service history: " + e.getMessage());
        }
    }

    private void loadInsurancePolicies() {
        ObservableList<PolicyItem> policies = FXCollections.observableArrayList();
        String sql = "SELECT p.policy_number, p.insurance_company, p.start_date, p.end_date, p.premium_amount, p.status " +
                "FROM insurance_policies p JOIN vehicles v ON p.vehicle_id = v.vehicle_id " +
                "WHERE v.owner_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                policies.add(new PolicyItem(
                        rs.getString("policy_number"),
                        rs.getString("insurance_company"),
                        rs.getDate("start_date").toString(),
                        rs.getDate("end_date").toString(),
                        rs.getDouble("premium_amount"),
                        rs.getString("status")
                ));
            }
            policiesTable.setItems(policies);
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Could not load insurance policies: " + e.getMessage());
        }
    }

    private void loadQueries() {
        ObservableList<QueryItem> queries = FXCollections.observableArrayList();
        String sql = "SELECT query_id, query_date, subject, query_text, response_text, status FROM customer_queries " +
                "WHERE customer_id = ? ORDER BY query_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String response = rs.getString("response_text");
                queries.add(new QueryItem(
                        rs.getInt("query_id"),
                        rs.getTimestamp("query_date").toString(),
                        rs.getString("subject"),
                        rs.getString("query_text"),
                        response != null ? response : "Awaiting response",
                        rs.getString("status")
                ));
            }
            queriesTable.setItems(queries);
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Could not load queries: " + e.getMessage());
        }
    }

    private void loadStatistics() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            // Vehicle count
            String sql1 = "SELECT COUNT(*) FROM vehicles WHERE owner_id = ?";
            PreparedStatement pstmt1 = conn.prepareStatement(sql1);
            pstmt1.setInt(1, customerId);
            ResultSet rs1 = pstmt1.executeQuery();
            if (rs1.next()) vehicleCountLabel.setText(String.valueOf(rs1.getInt(1)));

            // Service count
            String sql2 = "SELECT COUNT(*) FROM service_records s JOIN vehicles v ON s.vehicle_id = v.vehicle_id WHERE v.owner_id = ?";
            PreparedStatement pstmt2 = conn.prepareStatement(sql2);
            pstmt2.setInt(1, customerId);
            ResultSet rs2 = pstmt2.executeQuery();
            if (rs2.next()) serviceCountLabel.setText(String.valueOf(rs2.getInt(1)));

            // Active policies count
            String sql3 = "SELECT COUNT(*) FROM insurance_policies p JOIN vehicles v ON p.vehicle_id = v.vehicle_id WHERE v.owner_id = ? AND p.status = 'ACTIVE'";
            PreparedStatement pstmt3 = conn.prepareStatement(sql3);
            pstmt3.setInt(1, customerId);
            ResultSet rs3 = pstmt3.executeQuery();
            if (rs3.next()) policyCountLabel.setText(String.valueOf(rs3.getInt(1)));

            // Pending queries count
            String sql4 = "SELECT COUNT(*) FROM customer_queries WHERE customer_id = ? AND status = 'PENDING'";
            PreparedStatement pstmt4 = conn.prepareStatement(sql4);
            pstmt4.setInt(1, customerId);
            ResultSet rs4 = pstmt4.executeQuery();
            if (rs4.next()) queryCountLabel.setText(String.valueOf(rs4.getInt(1)));

        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Could not load statistics: " + e.getMessage());
        }
    }

    // ==================== REFRESH ====================
    @FXML
    private void refreshData() {
        loadVehicles();
        loadServiceHistory();
        loadInsurancePolicies();
        loadQueries();
        loadStatistics();
        AlertHelper.showInfo("Refreshed", "All data has been refreshed!");
    }

    // ==================== NEW QUERY ====================
    @FXML
    private void newQuery() {
        // Refresh vehicle list
        loadVehicles();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Submit New Query");
        dialog.setHeaderText("Please provide your query details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ObservableList<String> vehicleOptions = FXCollections.observableArrayList();
        vehicleOptions.add("Not vehicle specific");
        for (VehicleItem v : vehicleList) {
            vehicleOptions.add(v.getRegistrationNumber());
        }
        ComboBox<String> vehicleCombo = new ComboBox<>();
        vehicleCombo.setItems(vehicleOptions);
        vehicleCombo.setValue("Not vehicle specific");

        TextField subjectField = new TextField();
        subjectField.setPromptText("Query subject");

        TextArea queryArea = new TextArea();
        queryArea.setPromptText("Describe your query in detail...");
        queryArea.setPrefRowCount(5);

        grid.add(new Label("Vehicle:"), 0, 0);
        grid.add(vehicleCombo, 1, 0);
        grid.add(new Label("Subject:*"), 0, 1);
        grid.add(subjectField, 1, 1);
        grid.add(new Label("Query:*"), 0, 2);
        grid.add(queryArea, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String subject = subjectField.getText().trim();
            String queryText = queryArea.getText().trim();
            if (subject.isEmpty() || queryText.isEmpty()) {
                AlertHelper.showError("Validation Error", "Please fill all required fields (*)");
                return;
            }

            String selectedVehicle = vehicleCombo.getValue();
            String queryNumber = "QRY" + System.currentTimeMillis();

            String sql;
            if (selectedVehicle != null && !selectedVehicle.equals("Not vehicle specific")) {
                sql = "INSERT INTO customer_queries (customer_id, vehicle_id, query_number, subject, query_text, status) " +
                        "VALUES (?, (SELECT vehicle_id FROM vehicles WHERE registration_number = ?), ?, ?, ?, 'PENDING')";
            } else {
                sql = "INSERT INTO customer_queries (customer_id, vehicle_id, query_number, subject, query_text, status) " +
                        "VALUES (?, NULL, ?, ?, ?, 'PENDING')";
            }

            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, customerId);
                if (selectedVehicle != null && !selectedVehicle.equals("Not vehicle specific")) {
                    pstmt.setString(2, selectedVehicle);
                    pstmt.setString(3, queryNumber);
                    pstmt.setString(4, subject);
                    pstmt.setString(5, queryText);
                } else {
                    pstmt.setString(2, queryNumber);
                    pstmt.setString(3, subject);
                    pstmt.setString(4, queryText);
                }
                pstmt.executeUpdate();
                AlertHelper.showInfo("Success", "Your query has been submitted successfully!");
                refreshData();
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not submit query: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ==================== EDIT QUERY ====================
    @FXML
    private void editQuery() {
        QueryItem selected = queriesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a query to edit.");
            return;
        }
        if (!selected.getStatus().equals("PENDING")) {
            AlertHelper.showWarning("Cannot Edit", "Only pending queries can be edited.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Query");
        dialog.setHeaderText("Update your query");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField subjectField = new TextField(selected.getSubject());
        subjectField.setPromptText("Subject");

        TextArea queryArea = new TextArea(selected.getQueryText());
        queryArea.setPrefRowCount(5);

        grid.add(new Label("Subject:*"), 0, 0);
        grid.add(subjectField, 1, 0);
        grid.add(new Label("Query:*"), 0, 1);
        grid.add(queryArea, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String newSubject = subjectField.getText().trim();
            String newText = queryArea.getText().trim();
            if (newSubject.isEmpty() || newText.isEmpty()) {
                AlertHelper.showError("Validation Error", "Subject and query text cannot be empty.");
                return;
            }
            String updateSql = "UPDATE customer_queries SET subject = ?, query_text = ? WHERE query_id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, newSubject);
                pstmt.setString(2, newText);
                pstmt.setInt(3, selected.getId());
                pstmt.executeUpdate();
                AlertHelper.showInfo("Success", "Query updated.");
                refreshData();
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not update query: " + e.getMessage());
            }
        }
    }

    // ==================== DELETE QUERY ====================
    @FXML
    private void deleteQuery() {
        QueryItem selected = queriesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a query to delete.");
            return;
        }
        AlertHelper.showConfirmation("Delete Query", "Delete this query? This action cannot be undone.", () -> {
            String sql = "DELETE FROM customer_queries WHERE query_id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selected.getId());
                pstmt.executeUpdate();
                AlertHelper.showInfo("Success", "Query deleted.");
                refreshData();
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not delete query: " + e.getMessage());
            }
        });
    }

    // ==================== INNER CLASSES ====================
    public static class VehicleItem {
        private final String registrationNumber, make, model, color, status;
        private final int year;
        public VehicleItem(String reg, String make, String model, int year, String color, String status) {
            this.registrationNumber = reg; this.make = make; this.model = model;
            this.year = year; this.color = color; this.status = status;
        }
        public String getRegistrationNumber() { return registrationNumber; }
        public String getMake() { return make; }
        public String getModel() { return model; }
        public int getYear() { return year; }
        public String getColor() { return color; }
        public String getStatus() { return status; }
    }

    public static class ServiceItem {
        private final String serviceDate, serviceType, description;
        private final double cost;
        private final int odometer;
        public ServiceItem(String date, String type, String desc, double cost, int odometer) {
            this.serviceDate = date; this.serviceType = type; this.description = desc;
            this.cost = cost; this.odometer = odometer;
        }
        public String getServiceDate() { return serviceDate; }
        public String getServiceType() { return serviceType; }
        public String getDescription() { return description; }
        public double getCost() { return cost; }
        public int getOdometer() { return odometer; }
    }

    public static class PolicyItem {
        private final String policyNumber, company, startDate, endDate, status;
        private final double premium;
        public PolicyItem(String num, String co, String start, String end, double prem, String status) {
            this.policyNumber = num; this.company = co; this.startDate = start;
            this.endDate = end; this.premium = prem; this.status = status;
        }
        public String getPolicyNumber() { return policyNumber; }
        public String getCompany() { return company; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public double getPremium() { return premium; }
        public String getStatus() { return status; }
    }

    public static class QueryItem {
        private final int id;
        private final String queryDate, subject, queryText, response, status;
        public QueryItem(int id, String date, String subj, String text, String resp, String status) {
            this.id = id; this.queryDate = date; this.subject = subj; this.queryText = text;
            this.response = resp; this.status = status;
        }
        public int getId() { return id; }
        public String getQueryDate() { return queryDate; }
        public String getSubject() { return subject; }
        public String getQueryText() { return queryText; }
        public String getResponse() { return response; }
        public String getStatus() { return status; }
    }
}