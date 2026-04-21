package com.vehicle.vis.vehicleidentificationsystem.controllers;

import com.vehicle.vis.vehicleidentificationsystem.MainApplication;
import com.vehicle.vis.vehicleidentificationsystem.database.DatabaseConnection;
import com.vehicle.vis.vehicleidentificationsystem.utils.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    // ---------- User Table ----------
    @FXML private TableView<UserItem> userTable;
    @FXML private TableColumn<UserItem, Integer> colUserId;
    @FXML private TableColumn<UserItem, String> colUsername;
    @FXML private TableColumn<UserItem, String> colFullName;
    @FXML private TableColumn<UserItem, String> colEmail;
    @FXML private TableColumn<UserItem, String> colRole;
    @FXML private TableColumn<UserItem, String> colUserStatus;
    @FXML private TextField searchField;
    @FXML private Pagination userPagination;

    // ---------- Vehicle Table ----------
    @FXML private TableView<VehicleItem> vehicleTable;
    @FXML private TableColumn<VehicleItem, Integer> colVehId;
    @FXML private TableColumn<VehicleItem, String> colRegNumber;
    @FXML private TableColumn<VehicleItem, String> colMake;
    @FXML private TableColumn<VehicleItem, String> colModel;
    @FXML private TableColumn<VehicleItem, Integer> colYear;
    @FXML private TableColumn<VehicleItem, String> colColor;
    @FXML private TableColumn<VehicleItem, String> colOwner;
    @FXML private TableColumn<VehicleItem, String> colVehStatus;
    @FXML private TextField vehicleSearchField;
    @FXML private Pagination vehiclePagination;

    // ---------- Statistics ----------
    @FXML private Label totalUsersLabel;
    @FXML private Label totalVehiclesLabel;
    @FXML private Label activePoliciesLabel;
    @FXML private Label totalServicesLabel;

    private ObservableList<UserItem> userList = FXCollections.observableArrayList();
    private ObservableList<VehicleItem> vehicleList = FXCollections.observableArrayList();
    private int usersPerPage = 10;
    private int vehiclesPerPage = 10;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUserTableColumns();
        setupVehicleTableColumns();
        loadUsers();
        loadVehicles();
        loadStatistics();

        searchField.textProperty().addListener((obs, old, newVal) -> filterUsers(newVal));
        vehicleSearchField.textProperty().addListener((obs, old, newVal) -> filterVehicles(newVal));
    }

    // ==================== LOGOUT ====================
    @FXML
    private void handleLogout() {
        AlertHelper.showConfirmation("Logout", "Are you sure you want to logout?", () -> {
            MainApplication.changeScene("/views/login.fxml", "Vehicle Identification System - Login");
        });
    }

    // ==================== USER MANAGEMENT ====================
    private void setupUserTableColumns() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colUserStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadUsers() {
        userList.clear();
        String sql = "SELECT user_id, username, full_name, email, role, is_active FROM users ORDER BY user_id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                userList.add(new UserItem(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getBoolean("is_active") ? "Active" : "Inactive"
                ));
            }
            updateUserPagination();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateUserPagination() {
        int totalPages = (int) Math.ceil((double) userList.size() / usersPerPage);
        userPagination.setPageCount(totalPages == 0 ? 1 : totalPages);
        userPagination.setCurrentPageIndex(0);
        userPagination.setPageFactory(pageIndex -> {
            int from = pageIndex * usersPerPage;
            int to = Math.min(from + usersPerPage, userList.size());
            if (from >= userList.size()) return new VBox();
            userTable.setItems(FXCollections.observableArrayList(userList.subList(from, to)));
            return userTable;
        });
    }

    private void filterUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            updateUserPagination();
            return;
        }
        String lower = keyword.toLowerCase();
        ObservableList<UserItem> filtered = FXCollections.observableArrayList();
        for (UserItem u : userList) {
            if (u.getUsername().toLowerCase().contains(lower) ||
                    u.getFullName().toLowerCase().contains(lower) ||
                    u.getEmail().toLowerCase().contains(lower)) {
                filtered.add(u);
            }
        }
        userTable.setItems(filtered);
    }

    @FXML
    private void refreshUserTable() { loadUsers(); }

    // -------------------- Add New User (with password hashing) --------------------
    @FXML
    private void addNewUser() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Create a new system user");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("ADMIN", "CUSTOMER", "POLICE", "INSURANCE", "WORKSHOP");
        roleCombo.setValue("CUSTOMER");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        grid.add(new Label("Username:*"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Full Name:*"), 0, 1);
        grid.add(fullNameField, 1, 1);
        grid.add(new Label("Email:*"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Role:*"), 0, 4);
        grid.add(roleCombo, 1, 4);
        grid.add(new Label("Password:*"), 0, 5);
        grid.add(passwordField, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String username = usernameField.getText().trim();
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String role = roleCombo.getValue();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || role.isEmpty() || password.isEmpty()) {
                AlertHelper.showError("Validation Error", "Please fill all required fields (*)");
                return;
            }

            // Hash the password
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            String insertUserSql = "INSERT INTO users (username, password_hash, full_name, email, phone, role, is_active) VALUES (?, ?, ?, ?, ?, ?::user_role, true)";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, username);
                pstmt.setString(2, hashedPassword);
                pstmt.setString(3, fullName);
                pstmt.setString(4, email);
                pstmt.setString(5, phone.isEmpty() ? null : phone);
                pstmt.setString(6, role);
                pstmt.executeUpdate();

                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int newUserId = generatedKeys.getInt(1);
                    if (role.equals("CUSTOMER")) {
                        String insertCustomerSql = "INSERT INTO customers (customer_id, id_number, driver_license) VALUES (?, ?, ?)";
                        try (PreparedStatement pstmt2 = conn.prepareStatement(insertCustomerSql)) {
                            pstmt2.setInt(1, newUserId);
                            pstmt2.setString(2, "ID" + System.currentTimeMillis());
                            pstmt2.setString(3, "DL" + System.currentTimeMillis());
                            pstmt2.executeUpdate();
                        }
                    }
                    AlertHelper.showInfo("Success", "User " + username + " created successfully!");
                    refreshUserTable();
                    loadStatistics();
                }
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate key")) {
                    AlertHelper.showError("Duplicate", "Username or email already exists.");
                } else {
                    AlertHelper.showError("Database Error", "Could not create user: " + e.getMessage());
                }
            }
        }
    }

    // -------------------- Delete User --------------------
    @FXML
    private void deleteUser() {
        UserItem selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a user to delete.");
            return;
        }

        AlertHelper.showConfirmation("Delete User", "Are you sure you want to delete user: " + selected.getUsername() + "?", () -> {
            String sql = "DELETE FROM users WHERE user_id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selected.getId());
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    AlertHelper.showInfo("Success", "User deleted successfully.");
                    refreshUserTable();
                    loadStatistics();
                } else {
                    AlertHelper.showError("Error", "Could not delete user.");
                }
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Could not delete user: " + e.getMessage());
            }
        });
    }

    // ==================== VEHICLE MANAGEMENT ====================
    private void setupVehicleTableColumns() {
        colVehId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRegNumber.setCellValueFactory(new PropertyValueFactory<>("registration"));
        colMake.setCellValueFactory(new PropertyValueFactory<>("make"));
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colColor.setCellValueFactory(new PropertyValueFactory<>("color"));
        colOwner.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        colVehStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadVehicles() {
        vehicleList.clear();
        String sql = "SELECT v.vehicle_id, v.registration_number, v.make, v.model, v.year, v.color, v.status, " +
                "COALESCE(u.full_name, 'No owner') as owner_name " +
                "FROM vehicles v " +
                "LEFT JOIN customers c ON v.owner_id = c.customer_id " +
                "LEFT JOIN users u ON c.customer_id = u.user_id " +
                "ORDER BY v.vehicle_id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                vehicleList.add(new VehicleItem(
                        rs.getInt("vehicle_id"),
                        rs.getString("registration_number"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getString("color"),
                        rs.getString("owner_name"),
                        rs.getString("status")
                ));
            }
            updateVehiclePagination();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateVehiclePagination() {
        int totalPages = (int) Math.ceil((double) vehicleList.size() / vehiclesPerPage);
        vehiclePagination.setPageCount(totalPages == 0 ? 1 : totalPages);
        vehiclePagination.setCurrentPageIndex(0);
        vehiclePagination.setPageFactory(pageIndex -> {
            int from = pageIndex * vehiclesPerPage;
            int to = Math.min(from + vehiclesPerPage, vehicleList.size());
            if (from >= vehicleList.size()) return new VBox();
            vehicleTable.setItems(FXCollections.observableArrayList(vehicleList.subList(from, to)));
            return vehicleTable;
        });
    }

    private void filterVehicles(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            updateVehiclePagination();
            return;
        }
        String lower = keyword.toLowerCase();
        ObservableList<VehicleItem> filtered = FXCollections.observableArrayList();
        for (VehicleItem v : vehicleList) {
            if (v.getRegistration().toLowerCase().contains(lower) ||
                    v.getMake().toLowerCase().contains(lower) ||
                    v.getModel().toLowerCase().contains(lower) ||
                    v.getOwnerName().toLowerCase().contains(lower)) {
                filtered.add(v);
            }
        }
        vehicleTable.setItems(filtered);
    }

    @FXML
    private void refreshVehicleTable() { loadVehicles(); }

    @FXML
    private void addVehicle() {
        // Load customers for owner dropdown
        ObservableList<CustomerOwner> owners = FXCollections.observableArrayList();
        String sql = "SELECT u.user_id, u.full_name FROM customers c JOIN users u ON c.customer_id = u.user_id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                owners.add(new CustomerOwner(rs.getInt("user_id"), rs.getString("full_name")));
            }
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Could not load customers: " + e.getMessage());
            return;
        }
        if (owners.isEmpty()) {
            AlertHelper.showWarning("No Customers", "Please add a customer first.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Vehicle");
        dialog.setHeaderText("Enter vehicle details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField regField = new TextField();
        regField.setPromptText("e.g., B 9999 LS");
        TextField makeField = new TextField();
        makeField.setPromptText("e.g., Toyota");
        TextField modelField = new TextField();
        modelField.setPromptText("e.g., Hilux");
        TextField yearField = new TextField();
        yearField.setPromptText("e.g., 2022");
        TextField colorField = new TextField();
        colorField.setPromptText("e.g., White");
        ComboBox<CustomerOwner> ownerCombo = new ComboBox<>();
        ownerCombo.setItems(owners);
        ownerCombo.setPromptText("Select Owner");
        ownerCombo.setConverter(new javafx.util.StringConverter<CustomerOwner>() {
            @Override
            public String toString(CustomerOwner o) { return o == null ? "" : o.getName(); }
            @Override
            public CustomerOwner fromString(String s) { return null; }
        });

        grid.add(new Label("Registration Number:*"), 0, 0);
        grid.add(regField, 1, 0);
        grid.add(new Label("Make:*"), 0, 1);
        grid.add(makeField, 1, 1);
        grid.add(new Label("Model:*"), 0, 2);
        grid.add(modelField, 1, 2);
        grid.add(new Label("Year:*"), 0, 3);
        grid.add(yearField, 1, 3);
        grid.add(new Label("Color:"), 0, 4);
        grid.add(colorField, 1, 4);
        grid.add(new Label("Owner:*"), 0, 5);
        grid.add(ownerCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String reg = regField.getText().trim().toUpperCase();
            String make = makeField.getText().trim();
            String model = modelField.getText().trim();
            String yearStr = yearField.getText().trim();
            String color = colorField.getText().trim();
            CustomerOwner selectedOwner = ownerCombo.getValue();

            if (reg.isEmpty() || make.isEmpty() || model.isEmpty() || yearStr.isEmpty() || selectedOwner == null) {
                AlertHelper.showError("Validation Error", "Please fill all required fields (*)");
                return;
            }
            int year;
            try {
                year = Integer.parseInt(yearStr);
            } catch (NumberFormatException e) {
                AlertHelper.showError("Invalid Input", "Year must be a number");
                return;
            }

            String insertSql = "INSERT INTO vehicles (registration_number, make, model, year, color, owner_id) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, reg);
                pstmt.setString(2, make);
                pstmt.setString(3, model);
                pstmt.setInt(4, year);
                pstmt.setString(5, color);
                pstmt.setInt(6, selectedOwner.getId());
                pstmt.executeUpdate();
                AlertHelper.showInfo("Success", "Vehicle added successfully!");
                refreshVehicleTable();
                loadStatistics();
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate key")) {
                    AlertHelper.showError("Duplicate", "Registration number already exists.");
                } else {
                    AlertHelper.showError("Database Error", "Could not add vehicle: " + e.getMessage());
                }
            }
        }
    }

    // ==================== STATISTICS ====================
    private void loadStatistics() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next()) totalUsersLabel.setText(String.valueOf(rs.getInt(1)));
            rs = stmt.executeQuery("SELECT COUNT(*) FROM vehicles");
            if (rs.next()) totalVehiclesLabel.setText(String.valueOf(rs.getInt(1)));
            rs = stmt.executeQuery("SELECT COUNT(*) FROM insurance_policies WHERE status = 'ACTIVE'");
            if (rs.next()) activePoliciesLabel.setText(String.valueOf(rs.getInt(1)));
            rs = stmt.executeQuery("SELECT COUNT(*) FROM service_records");
            if (rs.next()) totalServicesLabel.setText(String.valueOf(rs.getInt(1)));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ==================== INNER CLASSES ====================
    public static class UserItem {
        private final int id;
        private final String username, fullName, email, role, status;
        public UserItem(int id, String un, String fn, String em, String r, String s) {
            this.id = id; this.username = un; this.fullName = fn; this.email = em; this.role = r; this.status = s;
        }
        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getStatus() { return status; }
    }

    public static class VehicleItem {
        private final int id;
        private final String registration, make, model, color, ownerName, status;
        private final int year;
        public VehicleItem(int id, String reg, String mk, String md, int yr, String col, String own, String stat) {
            this.id = id; this.registration = reg; this.make = mk; this.model = md;
            this.year = yr; this.color = col; this.ownerName = own; this.status = stat;
        }
        public int getId() { return id; }
        public String getRegistration() { return registration; }
        public String getMake() { return make; }
        public String getModel() { return model; }
        public int getYear() { return year; }
        public String getColor() { return color; }
        public String getOwnerName() { return ownerName; }
        public String getStatus() { return status; }
    }

    public static class CustomerOwner {
        private final int id;
        private final String name;
        public CustomerOwner(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        public String getName() { return name; }
    }
}