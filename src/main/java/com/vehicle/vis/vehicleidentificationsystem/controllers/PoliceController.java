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
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ResourceBundle;

public class PoliceController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label stolenCountLabel;
    @FXML private Label accidentCountLabel;
    @FXML private Label violationCountLabel;
    @FXML private Label openCasesLabel;

    @FXML private TextField searchField;
    @FXML private VBox vehicleInfoCard;
    @FXML private Label regNumberLabel;
    @FXML private Label ownerLabel;
    @FXML private Label makeModelLabel;
    @FXML private Label yearColorLabel;
    @FXML private Label statusLabel;

    @FXML private TableView<StolenItem> stolenTable;
    @FXML private TableView<ViolationItem> violationsTable;
    @FXML private TableView<AccidentItem> accidentsTable;
    @FXML private TableView<QueryItem> queriesTable;

    // Columns for stolen vehicles
    @FXML private TableColumn<StolenItem, Integer> colStolenId;
    @FXML private TableColumn<StolenItem, String> colStolenReg;
    @FXML private TableColumn<StolenItem, String> colStolenMake;
    @FXML private TableColumn<StolenItem, String> colStolenModel;
    @FXML private TableColumn<StolenItem, String> colStolenOwner;
    @FXML private TableColumn<StolenItem, String> colReportDate;
    @FXML private TableColumn<StolenItem, String> colCaseStatus;

    // Columns for violations
    @FXML private TableColumn<ViolationItem, Integer> colViolationId;
    @FXML private TableColumn<ViolationItem, String> colViolationReg;
    @FXML private TableColumn<ViolationItem, String> colViolationType;
    @FXML private TableColumn<ViolationItem, String> colViolationDate;
    @FXML private TableColumn<ViolationItem, Double> colFineAmount;
    @FXML private TableColumn<ViolationItem, String> colViolationStatus;

    // Columns for accidents
    @FXML private TableColumn<AccidentItem, Integer> colAccidentId;
    @FXML private TableColumn<AccidentItem, String> colAccidentReg;
    @FXML private TableColumn<AccidentItem, String> colLocation;
    @FXML private TableColumn<AccidentItem, String> colAccidentDate;
    @FXML private TableColumn<AccidentItem, String> colSeverity;
    @FXML private TableColumn<AccidentItem, String> colAccidentStatus;

    // Columns for customer queries
    @FXML private TableColumn<QueryItem, Integer> colQueryId;
    @FXML private TableColumn<QueryItem, String> colQueryCustomer;
    @FXML private TableColumn<QueryItem, String> colQuerySubject;
    @FXML private TableColumn<QueryItem, String> colQueryText;
    @FXML private TableColumn<QueryItem, String> colQueryDate;
    @FXML private TableColumn<QueryItem, String> colQueryStatus;

    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("========== POLICE CONTROLLER LOADED ==========");
        currentUser = SessionManager.getInstance().getCurrentUser();
        welcomeLabel.setText("Welcome, " + currentUser.getFullName());

        setupTableColumns();
        loadStolenVehicles();
        loadViolations();
        loadAccidents();
        loadCustomerQueries();
        loadStatistics();
    }

    private void setupTableColumns() {
        // Stolen vehicles
        colStolenId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStolenReg.setCellValueFactory(new PropertyValueFactory<>("registration"));
        colStolenMake.setCellValueFactory(new PropertyValueFactory<>("make"));
        colStolenModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        colStolenOwner.setCellValueFactory(new PropertyValueFactory<>("owner"));
        colReportDate.setCellValueFactory(new PropertyValueFactory<>("reportDate"));
        colCaseStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Violations
        colViolationId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colViolationReg.setCellValueFactory(new PropertyValueFactory<>("registration"));
        colViolationType.setCellValueFactory(new PropertyValueFactory<>("violationType"));
        colViolationDate.setCellValueFactory(new PropertyValueFactory<>("violationDate"));
        colFineAmount.setCellValueFactory(new PropertyValueFactory<>("fineAmount"));
        colViolationStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Accidents
        colAccidentId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAccidentReg.setCellValueFactory(new PropertyValueFactory<>("registration"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colAccidentDate.setCellValueFactory(new PropertyValueFactory<>("accidentDate"));
        colSeverity.setCellValueFactory(new PropertyValueFactory<>("severity"));
        colAccidentStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Customer queries
        colQueryId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colQueryCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colQuerySubject.setCellValueFactory(new PropertyValueFactory<>("subject"));
        colQueryText.setCellValueFactory(new PropertyValueFactory<>("queryText"));
        colQueryDate.setCellValueFactory(new PropertyValueFactory<>("queryDate"));
        colQueryStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadStolenVehicles() {
        ObservableList<StolenItem> items = FXCollections.observableArrayList();
        String sql = "SELECT pr.report_id, v.registration_number, v.make, v.model, COALESCE(u.full_name, 'Unknown') as owner_name, " +
                "pr.report_date, pr.case_status FROM police_reports pr " +
                "JOIN vehicles v ON pr.vehicle_id = v.vehicle_id " +
                "LEFT JOIN customers c ON v.owner_id = c.customer_id " +
                "LEFT JOIN users u ON c.customer_id = u.user_id " +
                "WHERE pr.report_type = 'THEFT' ORDER BY pr.report_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new StolenItem(
                        rs.getInt("report_id"),
                        rs.getString("registration_number"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getString("owner_name"),
                        rs.getTimestamp("report_date").toString(),
                        rs.getString("case_status")
                ));
            }
            stolenTable.setItems(items);
        } catch (SQLException e) {
            System.err.println("Could not load stolen vehicles: " + e.getMessage());
        }
    }

    private void loadViolations() {
        ObservableList<ViolationItem> items = FXCollections.observableArrayList();
        String sql = "SELECT vi.violation_id, v.registration_number, vi.violation_type, vi.violation_date, vi.fine_amount, vi.status " +
                "FROM violations vi JOIN vehicles v ON vi.vehicle_id = v.vehicle_id ORDER BY vi.violation_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new ViolationItem(
                        rs.getInt("violation_id"),
                        rs.getString("registration_number"),
                        rs.getString("violation_type"),
                        rs.getTimestamp("violation_date").toString(),
                        rs.getDouble("fine_amount"),
                        rs.getString("status")
                ));
            }
            violationsTable.setItems(items);
        } catch (SQLException e) {
            System.err.println("Could not load violations: " + e.getMessage());
        }
    }

    private void loadAccidents() {
        ObservableList<AccidentItem> items = FXCollections.observableArrayList();
        String sql = "SELECT pr.report_id, v.registration_number, pr.accident_location, pr.report_date, pr.case_status " +
                "FROM police_reports pr JOIN vehicles v ON pr.vehicle_id = v.vehicle_id " +
                "WHERE pr.report_type = 'ACCIDENT' ORDER BY pr.report_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new AccidentItem(
                        rs.getInt("report_id"),
                        rs.getString("registration_number"),
                        rs.getString("accident_location") != null ? rs.getString("accident_location") : "Unknown",
                        rs.getTimestamp("report_date").toString(),
                        "N/A",
                        rs.getString("case_status")
                ));
            }
            accidentsTable.setItems(items);
        } catch (SQLException e) {
            System.err.println("Could not load accidents: " + e.getMessage());
        }
    }

    private void loadCustomerQueries() {
        ObservableList<QueryItem> items = FXCollections.observableArrayList();
        String sql = "SELECT q.query_id, u.full_name as customer_name, q.subject, q.query_text, q.query_date, q.status " +
                "FROM customer_queries q JOIN users u ON q.customer_id = u.user_id " +
                "ORDER BY q.query_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new QueryItem(
                        rs.getInt("query_id"),
                        rs.getString("customer_name"),
                        rs.getString("subject"),
                        rs.getString("query_text"),
                        rs.getTimestamp("query_date").toString(),
                        rs.getString("status")
                ));
            }
            queriesTable.setItems(items);
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Could not load customer queries: " + e.getMessage());
        }
    }

    private void loadStatistics() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM police_reports WHERE report_type = 'THEFT' AND case_status != 'CLOSED'");
            stolenCountLabel.setText(rs.next() ? String.valueOf(rs.getInt(1)) : "0");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM police_reports WHERE report_type = 'ACCIDENT'");
            accidentCountLabel.setText(rs.next() ? String.valueOf(rs.getInt(1)) : "0");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM violations WHERE status = 'UNPAID'");
            violationCountLabel.setText(rs.next() ? String.valueOf(rs.getInt(1)) : "0");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM police_reports WHERE case_status != 'CLOSED'");
            openCasesLabel.setText(rs.next() ? String.valueOf(rs.getInt(1)) : "0");
        } catch (SQLException e) {
            System.err.println("Could not load statistics: " + e.getMessage());
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
        String reg = searchField.getText().trim();
        if (reg.isEmpty()) {
            AlertHelper.showWarning("Search Error", "Please enter a registration number");
            return;
        }
        String sql = "SELECT v.registration_number, v.make, v.model, v.year, v.color, v.status, COALESCE(u.full_name, 'Unknown') as owner_name " +
                "FROM vehicles v LEFT JOIN customers c ON v.owner_id = c.customer_id " +
                "LEFT JOIN users u ON c.customer_id = u.user_id WHERE v.registration_number ILIKE ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + reg + "%");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                regNumberLabel.setText(rs.getString("registration_number"));
                ownerLabel.setText(rs.getString("owner_name"));
                makeModelLabel.setText(rs.getString("make") + " " + rs.getString("model"));
                yearColorLabel.setText(rs.getInt("year") + " | " + rs.getString("color"));
                statusLabel.setText(rs.getString("status"));
                vehicleInfoCard.setVisible(true);
            } else {
                AlertHelper.showInfo("Not Found", "Vehicle not found");
                vehicleInfoCard.setVisible(false);
            }
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Could not search: " + e.getMessage());
        }
    }

    // ==================== FILE REPORT ====================
    @FXML
    private void fileReport() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("File Police Report");
        dialog.setHeaderText("File a new police report");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField regField = new TextField();
        regField.setPromptText("Vehicle Registration Number");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("ACCIDENT", "THEFT", "VANDALISM", "IMPOUND");
        typeCombo.setValue("ACCIDENT");
        TextField locationField = new TextField();
        locationField.setPromptText("Location");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description...");
        descriptionArea.setPrefRowCount(4);

        grid.add(new Label("Registration Number:*"), 0, 0);
        grid.add(regField, 1, 0);
        grid.add(new Label("Report Type:*"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Location:*"), 0, 2);
        grid.add(locationField, 1, 2);
        grid.add(new Label("Description:*"), 0, 3);
        grid.add(descriptionArea, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String reg = regField.getText().trim().toUpperCase();
            String type = typeCombo.getValue();
            String location = locationField.getText().trim();
            String description = descriptionArea.getText().trim();
            if (reg.isEmpty() || location.isEmpty() || description.isEmpty()) {
                AlertHelper.showError("Validation Error", "Please fill all required fields (*)");
                return;
            }
            String reportNumber = "RPT" + System.currentTimeMillis();
            String insertSql = "INSERT INTO police_reports (vehicle_id, report_number, report_type, description, accident_location, case_status) " +
                    "VALUES ((SELECT vehicle_id FROM vehicles WHERE registration_number = ?), ?, ?, ?, ?, 'OPEN')";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, reg);
                pstmt.setString(2, reportNumber);
                pstmt.setString(3, type);
                pstmt.setString(4, description);
                pstmt.setString(5, location);
                pstmt.executeUpdate();
                if ("THEFT".equals(type)) {
                    String updateSql = "UPDATE vehicles SET status = 'STOLEN' WHERE registration_number = ?";
                    try (PreparedStatement upstmt = conn.prepareStatement(updateSql)) {
                        upstmt.setString(1, reg);
                        upstmt.executeUpdate();
                    }
                }
                AlertHelper.showInfo("Success", "Report filed successfully!\nReport Number: " + reportNumber);
                refreshData();
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not file report: " + e.getMessage());
            }
        }
    }

    // ==================== ISSUE VIOLATION ====================
    @FXML
    private void issueViolation() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Issue Violation");
        dialog.setHeaderText("Issue a traffic violation ticket");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField regField = new TextField();
        regField.setPromptText("Vehicle Registration Number");
        TextField codeField = new TextField();
        codeField.setPromptText("Violation Code (e.g., SPEED01)");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Speeding", "Illegal Parking", "Seatbelt Violation", "Running Red Light",
                "Drunk Driving", "No License", "Expired License", "Other");
        typeCombo.setValue("Speeding");
        TextField locationField = new TextField();
        locationField.setPromptText("Location");
        TextField fineField = new TextField();
        fineField.setPromptText("Fine Amount (M)");
        TextField pointsField = new TextField();
        pointsField.setPromptText("Points");

        grid.add(new Label("Registration Number:*"), 0, 0);
        grid.add(regField, 1, 0);
        grid.add(new Label("Violation Code:*"), 0, 1);
        grid.add(codeField, 1, 1);
        grid.add(new Label("Violation Type:*"), 0, 2);
        grid.add(typeCombo, 1, 2);
        grid.add(new Label("Location:*"), 0, 3);
        grid.add(locationField, 1, 3);
        grid.add(new Label("Fine Amount:*"), 0, 4);
        grid.add(fineField, 1, 4);
        grid.add(new Label("Points:*"), 0, 5);
        grid.add(pointsField, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String reg = regField.getText().trim().toUpperCase();
            String code = codeField.getText().trim();
            String type = typeCombo.getValue();
            String location = locationField.getText().trim();
            String fineText = fineField.getText().trim();
            String pointsText = pointsField.getText().trim();
            if (reg.isEmpty() || code.isEmpty() || location.isEmpty() || fineText.isEmpty() || pointsText.isEmpty()) {
                AlertHelper.showError("Validation Error", "Please fill all required fields (*)");
                return;
            }
            double fine;
            int points;
            try {
                fine = Double.parseDouble(fineText);
                points = Integer.parseInt(pointsText);
            } catch (NumberFormatException e) {
                AlertHelper.showError("Invalid Input", "Fine must be a number, points an integer");
                return;
            }
            String insertSql = "INSERT INTO violations (vehicle_id, violation_code, violation_type, violation_location, fine_amount, points, status, violation_date) " +
                    "VALUES ((SELECT vehicle_id FROM vehicles WHERE registration_number = ?), ?, ?, ?, ?, ?, 'UNPAID', ?)";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, reg);
                pstmt.setString(2, code);
                pstmt.setString(3, type);
                pstmt.setString(4, location);
                pstmt.setDouble(5, fine);
                pstmt.setInt(6, points);
                pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.executeUpdate();
                AlertHelper.showInfo("Success", "Violation issued successfully!");
                refreshData();
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not issue violation: " + e.getMessage());
            }
        }
    }

    // ==================== EDIT VIOLATION ====================
    @FXML
    private void editViolation() {
        ViolationItem selected = violationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a violation to edit.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Violation");
        dialog.setHeaderText("Update violation details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField regField = new TextField(selected.getRegistration());
        regField.setEditable(false);
        regField.setStyle("-fx-opacity: 0.7;");

        TextField codeField = new TextField(selected.getViolationCode());
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Speeding", "Illegal Parking", "Seatbelt Violation", "Running Red Light",
                "Drunk Driving", "No License", "Expired License", "Other");
        typeCombo.setValue(selected.getViolationType());

        TextField locationField = new TextField(selected.getViolationLocation());
        TextField fineField = new TextField(String.valueOf(selected.getFineAmount()));
        TextField pointsField = new TextField(String.valueOf(selected.getPoints()));
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("UNPAID", "PAID", "DISPUTED");
        statusCombo.setValue(selected.getStatus());

        grid.add(new Label("Registration Number:"), 0, 0);
        grid.add(regField, 1, 0);
        grid.add(new Label("Violation Code:*"), 0, 1);
        grid.add(codeField, 1, 1);
        grid.add(new Label("Violation Type:*"), 0, 2);
        grid.add(typeCombo, 1, 2);
        grid.add(new Label("Location:*"), 0, 3);
        grid.add(locationField, 1, 3);
        grid.add(new Label("Fine Amount (M):*"), 0, 4);
        grid.add(fineField, 1, 4);
        grid.add(new Label("Points:*"), 0, 5);
        grid.add(pointsField, 1, 5);
        grid.add(new Label("Status:"), 0, 6);
        grid.add(statusCombo, 1, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                double fine = Double.parseDouble(fineField.getText().trim());
                int points = Integer.parseInt(pointsField.getText().trim());
                String updateSql = "UPDATE violations SET violation_code=?, violation_type=?, violation_location=?, fine_amount=?, points=?, status=? WHERE violation_id=?";
                try (Connection conn = DatabaseConnection.getInstance().getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setString(1, codeField.getText().trim());
                    pstmt.setString(2, typeCombo.getValue());
                    pstmt.setString(3, locationField.getText().trim());
                    pstmt.setDouble(4, fine);
                    pstmt.setInt(5, points);
                    pstmt.setString(6, statusCombo.getValue());
                    pstmt.setInt(7, selected.getId());
                    pstmt.executeUpdate();
                    AlertHelper.showInfo("Success", "Violation updated.");
                    refreshData();
                }
            } catch (NumberFormatException e) {
                AlertHelper.showError("Invalid Input", "Fine and points must be numbers.");
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not update violation: " + e.getMessage());
            }
        }
    }

    // ==================== DELETE VIOLATION ====================
    @FXML
    private void deleteViolation() {
        ViolationItem selected = violationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a violation to delete.");
            return;
        }
        AlertHelper.showConfirmation("Delete Violation", "Delete this violation?", () -> {
            String sql = "DELETE FROM violations WHERE violation_id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selected.getId());
                pstmt.executeUpdate();
                AlertHelper.showInfo("Success", "Violation deleted.");
                refreshData();
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not delete violation: " + e.getMessage());
            }
        });
    }

    // ==================== DELETE REPORT ====================
    @FXML
    private void deleteReport() {
        StolenItem selectedStolen = stolenTable.getSelectionModel().getSelectedItem();
        AccidentItem selectedAccident = accidentsTable.getSelectionModel().getSelectedItem();
        int reportId = -1;
        if (selectedStolen != null) reportId = selectedStolen.getId();
        else if (selectedAccident != null) reportId = selectedAccident.getId();
        else {
            AlertHelper.showWarning("No Selection", "Please select a stolen vehicle or accident report to delete.");
            return;
        }
        final int id = reportId;
        AlertHelper.showConfirmation("Delete Report", "Delete this report?", () -> {
            String sql = "DELETE FROM police_reports WHERE report_id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                AlertHelper.showInfo("Success", "Report deleted.");
                refreshData();
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not delete report: " + e.getMessage());
            }
        });
    }

    @FXML
    private void refreshData() {
        loadStolenVehicles();
        loadViolations();
        loadAccidents();
        loadCustomerQueries();
        loadStatistics();
        searchField.clear();
        vehicleInfoCard.setVisible(false);
        AlertHelper.showInfo("Refreshed", "All data has been refreshed!");
    }

    @FXML
    private void refreshQueries() {
        loadCustomerQueries();
    }

    @FXML
    private void respondToQuery() {
        QueryItem selected = queriesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a query to respond");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Respond to Query");
        dialog.setHeaderText("Customer: " + selected.getCustomerName() + "\nSubject: " + selected.getSubject());
        dialog.setContentText("Response:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String response = result.get().trim();
            String updateSql = "UPDATE customer_queries SET response_text = ?, status = 'RESPONDED', responded_at = CURRENT_TIMESTAMP WHERE query_id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, response);
                pstmt.setInt(2, selected.getId());
                pstmt.executeUpdate();
                AlertHelper.showInfo("Success", "Response saved.");
                refreshQueries();
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not save response: " + e.getMessage());
            }
        }
    }

    // ==================== INNER CLASSES ====================
    public static class StolenItem {
        private final int id;
        private final String registration, make, model, owner, reportDate, status;
        public StolenItem(int id, String reg, String make, String model, String owner, String date, String status) {
            this.id = id; this.registration = reg; this.make = make; this.model = model;
            this.owner = owner; this.reportDate = date; this.status = status;
        }
        public int getId() { return id; }
        public String getRegistration() { return registration; }
        public String getMake() { return make; }
        public String getModel() { return model; }
        public String getOwner() { return owner; }
        public String getReportDate() { return reportDate; }
        public String getStatus() { return status; }
    }

    public static class ViolationItem {
        private final int id;
        private final String registration, violationType, violationDate, status;
        private final double fineAmount;
        private final int points;
        private final String violationCode, violationLocation;
        public ViolationItem(int id, String reg, String type, String date, double fine, String status,
                             String code, String location, int points) {
            this.id = id; this.registration = reg; this.violationType = type;
            this.violationDate = date; this.fineAmount = fine; this.status = status;
            this.violationCode = code; this.violationLocation = location; this.points = points;
        }
        // simplified constructor for existing code
        public ViolationItem(int id, String reg, String type, String date, double fine, String status) {
            this(id, reg, type, date, fine, status, "", "", 0);
        }
        public int getId() { return id; }
        public String getRegistration() { return registration; }
        public String getViolationType() { return violationType; }
        public String getViolationDate() { return violationDate; }
        public double getFineAmount() { return fineAmount; }
        public String getStatus() { return status; }
        public String getViolationCode() { return violationCode; }
        public String getViolationLocation() { return violationLocation; }
        public int getPoints() { return points; }
    }

    public static class AccidentItem {
        private final int id;
        private final String registration, location, accidentDate, severity, status;
        public AccidentItem(int id, String reg, String loc, String date, String severity, String status) {
            this.id = id; this.registration = reg; this.location = loc;
            this.accidentDate = date; this.severity = severity; this.status = status;
        }
        public int getId() { return id; }
        public String getRegistration() { return registration; }
        public String getLocation() { return location; }
        public String getAccidentDate() { return accidentDate; }
        public String getSeverity() { return severity; }
        public String getStatus() { return status; }
    }

    public static class QueryItem {
        private final int id;
        private final String customerName, subject, queryText, queryDate, status;
        public QueryItem(int id, String customerName, String subject, String queryText, String queryDate, String status) {
            this.id = id; this.customerName = customerName; this.subject = subject;
            this.queryText = queryText; this.queryDate = queryDate; this.status = status;
        }
        public int getId() { return id; }
        public String getCustomerName() { return customerName; }
        public String getSubject() { return subject; }
        public String getQueryText() { return queryText; }
        public String getQueryDate() { return queryDate; }
        public String getStatus() { return status; }
    }
}