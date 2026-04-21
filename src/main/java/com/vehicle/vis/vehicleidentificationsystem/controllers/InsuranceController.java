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
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class InsuranceController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label activePoliciesLabel;
    @FXML private Label pendingClaimsLabel;
    @FXML private Label totalPremiumLabel;
    @FXML private Label expiringLabel;

    @FXML private TableView<PolicyItem> policiesTable;
    @FXML private TableView<ClaimItem> claimsTable;

    @FXML private TableColumn<PolicyItem, Integer> colPolicyId;
    @FXML private TableColumn<PolicyItem, String> colPolicyNumber;
    @FXML private TableColumn<PolicyItem, String> colVehicleReg;
    @FXML private TableColumn<PolicyItem, String> colCompany;
    @FXML private TableColumn<PolicyItem, String> colStartDate;
    @FXML private TableColumn<PolicyItem, String> colEndDate;
    @FXML private TableColumn<PolicyItem, Double> colPremium;

    @FXML private TableColumn<ClaimItem, Integer> colClaimId;
    @FXML private TableColumn<ClaimItem, String> colClaimNumber;
    @FXML private TableColumn<ClaimItem, String> colClaimPolicy;
    @FXML private TableColumn<ClaimItem, String> colClaimDate;
    @FXML private TableColumn<ClaimItem, Double> colClaimAmount;
    @FXML private TableColumn<ClaimItem, String> colClaimStatus;

    @FXML private Button refreshButton;
    @FXML private TextField searchField;

    private User currentUser;
    private ObservableList<PolicyItem> policyList = FXCollections.observableArrayList();
    private ObservableList<ClaimItem> claimList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("========== INSURANCE CONTROLLER LOADED ==========");

        currentUser = SessionManager.getInstance().getCurrentUser();
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        }

        setupTableColumns();
        loadPolicies();
        loadClaims();
        loadStatistics();

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterPolicies(newVal));
        }
    }

    // ==================== LOGOUT ====================
    @FXML
    private void handleLogout() {
        AlertHelper.showConfirmation("Logout", "Are you sure you want to logout?", () -> {
            MainApplication.changeScene("/views/login.fxml", "Vehicle Identification System - Login");
        });
    }

    private void setupTableColumns() {
        colPolicyId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPolicyNumber.setCellValueFactory(new PropertyValueFactory<>("policyNumber"));
        colVehicleReg.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        colCompany.setCellValueFactory(new PropertyValueFactory<>("company"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colPremium.setCellValueFactory(new PropertyValueFactory<>("premium"));

        colClaimId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClaimNumber.setCellValueFactory(new PropertyValueFactory<>("claimNumber"));
        colClaimPolicy.setCellValueFactory(new PropertyValueFactory<>("policyNumber"));
        colClaimDate.setCellValueFactory(new PropertyValueFactory<>("claimDate"));
        colClaimAmount.setCellValueFactory(new PropertyValueFactory<>("claimAmount"));
        colClaimStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadPolicies() {
        policyList.clear();
        String sql = "SELECT p.policy_id, p.policy_number, v.registration_number, p.insurance_company, " +
                "TO_CHAR(p.start_date, 'YYYY-MM-DD') as start_date, " +
                "TO_CHAR(p.end_date, 'YYYY-MM-DD') as end_date, " +
                "p.premium_amount " +
                "FROM insurance_policies p " +
                "JOIN vehicles v ON p.vehicle_id = v.vehicle_id " +
                "ORDER BY p.policy_id DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                policyList.add(new PolicyItem(
                        rs.getInt("policy_id"),
                        rs.getString("policy_number"),
                        rs.getString("registration_number"),
                        rs.getString("insurance_company"),
                        rs.getString("start_date"),
                        rs.getString("end_date"),
                        rs.getDouble("premium_amount")
                ));
            }
            policiesTable.setItems(policyList);
            System.out.println("Loaded " + policyList.size() + " policies");
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Could not load policies: " + e.getMessage());
        }
    }

    private void loadClaims() {
        claimList.clear();
        String sql = "SELECT c.claim_id, c.claim_number, p.policy_number, TO_CHAR(c.claim_date, 'YYYY-MM-DD') as claim_date, " +
                "c.claim_amount, c.status " +
                "FROM insurance_claims c " +
                "JOIN insurance_policies p ON c.policy_id = p.policy_id " +
                "ORDER BY c.claim_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                claimList.add(new ClaimItem(
                        rs.getInt("claim_id"),
                        rs.getString("claim_number"),
                        rs.getString("policy_number"),
                        rs.getString("claim_date"),
                        rs.getDouble("claim_amount"),
                        rs.getString("status")
                ));
            }
            claimsTable.setItems(claimList);
            System.out.println("Loaded " + claimList.size() + " claims");
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Could not load claims: " + e.getMessage());
        }
    }

    private void loadStatistics() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM insurance_policies WHERE status = 'ACTIVE'");
            if (rs.next()) activePoliciesLabel.setText(String.valueOf(rs.getInt(1)));
            rs = stmt.executeQuery("SELECT COUNT(*) FROM insurance_claims WHERE status = 'PENDING'");
            if (rs.next()) pendingClaimsLabel.setText(String.valueOf(rs.getInt(1)));
            rs = stmt.executeQuery("SELECT COALESCE(SUM(premium_amount), 0) FROM insurance_policies WHERE status = 'ACTIVE'");
            if (rs.next()) totalPremiumLabel.setText(String.format("M%.2f", rs.getDouble(1)));
            rs = stmt.executeQuery("SELECT COUNT(*) FROM insurance_policies WHERE end_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '30 days'");
            if (rs.next()) expiringLabel.setText(String.valueOf(rs.getInt(1)));
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Could not load statistics: " + e.getMessage());
        }
    }

    private void filterPolicies(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            policiesTable.setItems(policyList);
            return;
        }
        ObservableList<PolicyItem> filtered = FXCollections.observableArrayList();
        String lowerSearch = searchTerm.toLowerCase();
        for (PolicyItem policy : policyList) {
            if (policy.getRegistrationNumber().toLowerCase().contains(lowerSearch) ||
                    policy.getPolicyNumber().toLowerCase().contains(lowerSearch) ||
                    policy.getCompany().toLowerCase().contains(lowerSearch)) {
                filtered.add(policy);
            }
        }
        policiesTable.setItems(filtered);
    }

    // ==================== ADD POLICY ====================
    @FXML
    private void newPolicy() {
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
        dialog.setTitle("Add New Insurance Policy");
        dialog.setHeaderText("Enter policy details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> regCombo = new ComboBox<>();
        regCombo.setItems(vehicleList);
        regCombo.setValue(vehicleList.get(0));

        TextField policyNumberField = new TextField();
        TextField companyField = new TextField();
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("COMPREHENSIVE", "THIRD_PARTY", "COLLISION", "LIABILITY");
        typeCombo.setValue("COMPREHENSIVE");
        DatePicker startDate = new DatePicker(LocalDate.now());
        DatePicker endDate = new DatePicker(LocalDate.now().plusYears(1));
        TextField premiumField = new TextField();
        TextField coverageField = new TextField();

        grid.add(new Label("Vehicle Registration:*"), 0, 0);
        grid.add(regCombo, 1, 0);
        grid.add(new Label("Policy Number:*"), 0, 1);
        grid.add(policyNumberField, 1, 1);
        grid.add(new Label("Insurance Company:*"), 0, 2);
        grid.add(companyField, 1, 2);
        grid.add(new Label("Policy Type:*"), 0, 3);
        grid.add(typeCombo, 1, 3);
        grid.add(new Label("Start Date:*"), 0, 4);
        grid.add(startDate, 1, 4);
        grid.add(new Label("End Date:*"), 0, 5);
        grid.add(endDate, 1, 5);
        grid.add(new Label("Premium (M):*"), 0, 6);
        grid.add(premiumField, 1, 6);
        grid.add(new Label("Coverage (M):*"), 0, 7);
        grid.add(coverageField, 1, 7);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String reg = regCombo.getValue();
            String policyNum = policyNumberField.getText().trim();
            String company = companyField.getText().trim();
            String type = typeCombo.getValue();
            String premiumText = premiumField.getText().trim();
            String coverageText = coverageField.getText().trim();

            if (reg == null || policyNum.isEmpty() || company.isEmpty() || premiumText.isEmpty()) {
                AlertHelper.showError("Validation Error", "Please fill all required fields (*)");
                return;
            }
            double premium, coverage = 0;
            try {
                premium = Double.parseDouble(premiumText);
                if (!coverageText.isEmpty()) coverage = Double.parseDouble(coverageText);
            } catch (NumberFormatException e) {
                AlertHelper.showError("Invalid Input", "Premium must be a number.");
                return;
            }

            String insertSql = "INSERT INTO insurance_policies (vehicle_id, policy_number, insurance_company, policy_type, " +
                    "start_date, end_date, premium_amount, coverage_amount, status) " +
                    "VALUES ((SELECT vehicle_id FROM vehicles WHERE registration_number = ?), ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, reg);
                pstmt.setString(2, policyNum);
                pstmt.setString(3, company);
                pstmt.setString(4, type);
                pstmt.setDate(5, Date.valueOf(startDate.getValue()));
                pstmt.setDate(6, Date.valueOf(endDate.getValue()));
                pstmt.setDouble(7, premium);
                pstmt.setDouble(8, coverage);
                pstmt.executeUpdate();
                AlertHelper.showInfo("Success", "Policy added.");
                refreshData();
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not add policy: " + e.getMessage());
            }
        }
    }

    // ==================== EDIT POLICY ====================
    @FXML
    private void editPolicy() {
        PolicyItem selected = policiesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a policy to edit.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Policy");
        dialog.setHeaderText("Update policy details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField policyNumberField = new TextField(selected.getPolicyNumber());
        policyNumberField.setEditable(false);
        policyNumberField.setStyle("-fx-opacity: 0.7;");
        TextField companyField = new TextField(selected.getCompany());
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("COMPREHENSIVE", "THIRD_PARTY", "COLLISION", "LIABILITY");
        typeCombo.setValue(selected.getPolicyType() != null ? selected.getPolicyType() : "COMPREHENSIVE");

        // Create DatePicker and set value
        DatePicker startDate = new DatePicker();
        startDate.setValue(LocalDate.parse(selected.getStartDate()));
        DatePicker endDate = new DatePicker();
        endDate.setValue(LocalDate.parse(selected.getEndDate()));

        TextField premiumField = new TextField(String.valueOf(selected.getPremium()));

        grid.add(new Label("Policy Number:"), 0, 0);
        grid.add(policyNumberField, 1, 0);
        grid.add(new Label("Insurance Company:*"), 0, 1);
        grid.add(companyField, 1, 1);
        grid.add(new Label("Policy Type:*"), 0, 2);
        grid.add(typeCombo, 1, 2);
        grid.add(new Label("Start Date:*"), 0, 3);
        grid.add(startDate, 1, 3);
        grid.add(new Label("End Date:*"), 0, 4);
        grid.add(endDate, 1, 4);
        grid.add(new Label("Premium (M):*"), 0, 5);
        grid.add(premiumField, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String company = companyField.getText().trim();
            String type = typeCombo.getValue();
            double premium;
            try {
                premium = Double.parseDouble(premiumField.getText().trim());
            } catch (NumberFormatException e) {
                AlertHelper.showError("Invalid Input", "Premium must be a number.");
                return;
            }
            String updateSql = "UPDATE insurance_policies SET insurance_company=?, policy_type=?, start_date=?, end_date=?, premium_amount=? WHERE policy_id=?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, company);
                pstmt.setString(2, type);
                pstmt.setDate(3, Date.valueOf(startDate.getValue()));
                pstmt.setDate(4, Date.valueOf(endDate.getValue()));
                pstmt.setDouble(5, premium);
                pstmt.setInt(6, selected.getId());
                pstmt.executeUpdate();
                AlertHelper.showInfo("Success", "Policy updated.");
                refreshData();
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not update policy: " + e.getMessage());
            }
        }
    }

    // ==================== DELETE POLICY ====================
    @FXML
    private void deletePolicy() {
        PolicyItem selected = policiesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a policy to delete.");
            return;
        }
        AlertHelper.showConfirmation("Delete Policy", "Delete policy " + selected.getPolicyNumber() + "? All related claims will also be deleted.", () -> {
            String sql = "DELETE FROM insurance_policies WHERE policy_id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selected.getId());
                pstmt.executeUpdate();
                AlertHelper.showInfo("Success", "Policy deleted.");
                refreshData();
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not delete policy: " + e.getMessage());
            }
        });
    }

    // ==================== PROCESS CLAIM ====================
    @FXML
    private void processClaim() {
        PolicyItem selected = policiesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a policy first");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Process Claim");
        dialog.setHeaderText("File a claim for policy: " + selected.getPolicyNumber());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField claimNumberField = new TextField();
        claimNumberField.setPromptText("Claim Number");
        DatePicker claimDate = new DatePicker(LocalDate.now());
        TextField amountField = new TextField();
        amountField.setPromptText("Claim Amount");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description of claim...");
        descriptionArea.setPrefRowCount(3);

        grid.add(new Label("Claim Number:*"), 0, 0);
        grid.add(claimNumberField, 1, 0);
        grid.add(new Label("Claim Date:*"), 0, 1);
        grid.add(claimDate, 1, 1);
        grid.add(new Label("Claim Amount (M):*"), 0, 2);
        grid.add(amountField, 1, 2);
        grid.add(new Label("Description:*"), 0, 3);
        grid.add(descriptionArea, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String claimNum = claimNumberField.getText().trim();
            String amountText = amountField.getText().trim();
            if (claimNum.isEmpty() || amountText.isEmpty()) {
                AlertHelper.showError("Validation Error", "Please fill all required fields (*)");
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException e) {
                AlertHelper.showError("Invalid Input", "Claim amount must be a number.");
                return;
            }
            String insertSql = "INSERT INTO insurance_claims (policy_id, claim_number, claim_date, claim_amount, description, status) " +
                    "VALUES ((SELECT policy_id FROM insurance_policies WHERE policy_number = ?), ?, ?, ?, ?, 'PENDING')";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, selected.getPolicyNumber());
                pstmt.setString(2, claimNum);
                pstmt.setDate(3, Date.valueOf(claimDate.getValue()));
                pstmt.setDouble(4, amount);
                pstmt.setString(5, descriptionArea.getText());
                pstmt.executeUpdate();
                AlertHelper.showInfo("Success", "Claim filed successfully!");
                refreshData();
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not file claim: " + e.getMessage());
            }
        }
    }

    // ==================== EDIT CLAIM ====================
    @FXML
    private void editClaim() {
        ClaimItem selected = claimsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a claim to edit.");
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Claim");
        dialog.setHeaderText("Update claim details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField claimNumberField = new TextField(selected.getClaimNumber());
        claimNumberField.setEditable(false);
        claimNumberField.setStyle("-fx-opacity: 0.7;");

        // Create DatePicker and set value
        DatePicker claimDate = new DatePicker();
        claimDate.setValue(LocalDate.parse(selected.getClaimDate()));

        TextField amountField = new TextField(String.valueOf(selected.getClaimAmount()));
        TextArea descriptionArea = new TextArea(selected.getDescription());
        descriptionArea.setPrefRowCount(3);
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("PENDING", "APPROVED", "REJECTED", "SETTLED");
        statusCombo.setValue(selected.getStatus());

        grid.add(new Label("Claim Number:"), 0, 0);
        grid.add(claimNumberField, 1, 0);
        grid.add(new Label("Claim Date:*"), 0, 1);
        grid.add(claimDate, 1, 1);
        grid.add(new Label("Claim Amount (M):*"), 0, 2);
        grid.add(amountField, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(descriptionArea, 1, 3);
        grid.add(new Label("Status:"), 0, 4);
        grid.add(statusCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            double amount;
            try {
                amount = Double.parseDouble(amountField.getText().trim());
            } catch (NumberFormatException e) {
                AlertHelper.showError("Invalid Input", "Amount must be a number.");
                return;
            }
            String updateSql = "UPDATE insurance_claims SET claim_date=?, claim_amount=?, description=?, status=? WHERE claim_id=?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setDate(1, Date.valueOf(claimDate.getValue()));
                pstmt.setDouble(2, amount);
                pstmt.setString(3, descriptionArea.getText());
                pstmt.setString(4, statusCombo.getValue());
                pstmt.setInt(5, selected.getId());
                pstmt.executeUpdate();
                AlertHelper.showInfo("Success", "Claim updated.");
                refreshData();
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not update claim: " + e.getMessage());
            }
        }
    }

    // ==================== DELETE CLAIM ====================
    @FXML
    private void deleteClaim() {
        ClaimItem selected = claimsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a claim to delete.");
            return;
        }
        AlertHelper.showConfirmation("Delete Claim", "Delete claim " + selected.getClaimNumber() + "?", () -> {
            String sql = "DELETE FROM insurance_claims WHERE claim_id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selected.getId());
                pstmt.executeUpdate();
                AlertHelper.showInfo("Success", "Claim deleted.");
                refreshData();
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not delete claim: " + e.getMessage());
            }
        });
    }

    @FXML
    private void refreshData() {
        loadPolicies();
        loadClaims();
        loadStatistics();
        AlertHelper.showInfo("Refreshed", "All data has been refreshed!");
    }

    // ==================== INNER CLASSES ====================
    public static class PolicyItem {
        private final int id;
        private final String policyNumber, registrationNumber, company, startDate, endDate;
        private final double premium;
        private String policyType; // optional, for editing
        public PolicyItem(int id, String num, String reg, String co, String start, String end, double prem) {
            this.id = id; this.policyNumber = num; this.registrationNumber = reg; this.company = co;
            this.startDate = start; this.endDate = end; this.premium = prem;
        }
        public int getId() { return id; }
        public String getPolicyNumber() { return policyNumber; }
        public String getRegistrationNumber() { return registrationNumber; }
        public String getCompany() { return company; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public double getPremium() { return premium; }
        public String getPolicyType() { return policyType; }
        public void setPolicyType(String type) { this.policyType = type; }
    }

    public static class ClaimItem {
        private final int id;
        private final String claimNumber, policyNumber, claimDate, status;
        private final double claimAmount;
        private String description;
        public ClaimItem(int id, String num, String policy, String date, double amount, String status) {
            this.id = id; this.claimNumber = num; this.policyNumber = policy;
            this.claimDate = date; this.claimAmount = amount; this.status = status;
        }
        public int getId() { return id; }
        public String getClaimNumber() { return claimNumber; }
        public String getPolicyNumber() { return policyNumber; }
        public String getClaimDate() { return claimDate; }
        public double getClaimAmount() { return claimAmount; }
        public String getStatus() { return status; }
        public String getDescription() { return description; }
        public void setDescription(String desc) { this.description = desc; }
    }
}