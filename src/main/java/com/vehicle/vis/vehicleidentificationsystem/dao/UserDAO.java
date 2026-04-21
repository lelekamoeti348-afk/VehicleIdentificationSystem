package com.vehicle.vis.vehicleidentificationsystem.dao;

import com.vehicle.vis.vehicleidentificationsystem.config.DatabaseConfig;
import com.vehicle.vis.vehicleidentificationsystem.database.DatabaseConnection;
import com.vehicle.vis.vehicleidentificationsystem.models.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;

public class UserDAO {

    public User authenticate(String username, String password) {
        String sql = DatabaseConfig.SELECT_USER_BY_USERNAME;
        Connection conn = null;

        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Username: " + username);

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                System.out.println("Stored hash: " + storedHash);

                if (BCrypt.checkpw(password, storedHash)) {
                    System.out.println("✅ Password matches!");

                    String role = rs.getString("role");
                    int userId = rs.getInt("user_id");
                    String fullName = rs.getString("full_name");
                    String email = rs.getString("email");
                    String phone = rs.getString("phone");

                    User user = createUserByRole(role, userId, username, fullName, email, phone);
                    if (user != null) {
                        loadRoleSpecificDetails(user, userId, role, conn);
                        updateLastLogin(userId, conn);
                        user.setLastLogin(LocalDateTime.now());
                        return user;
                    }
                } else {
                    System.out.println("❌ Password does NOT match!");
                }
            } else {
                System.out.println("❌ User not found: " + username);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DatabaseConnection.getInstance().returnConnection(conn);
            }
        }
        return null;
    }

    private User createUserByRole(String role, int userId, String username, String fullName, String email, String phone) {
        switch (role) {
            case "ADMIN":
                return new Admin(userId, username, fullName, email, phone);
            case "CUSTOMER":
                return new Customer(userId, username, fullName, email, phone);
            case "POLICE":
                return new PoliceOfficer(userId, username, fullName, email, phone);
            case "INSURANCE":
                return new InsuranceAgent(userId, username, fullName, email, phone);
            case "WORKSHOP":
                return new WorkshopStaff(userId, username, fullName, email, phone);
            default:
                return null;
        }
    }

    private void loadRoleSpecificDetails(User user, int userId, String role, Connection conn) {
        try {
            if (role.equals("CUSTOMER") && user instanceof Customer) {
                String sql = "SELECT * FROM customers WHERE customer_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    ((Customer) user).setIdNumber(rs.getString("id_number"));
                    ((Customer) user).setDriverLicense(rs.getString("driver_license"));
                    ((Customer) user).setDateOfBirth(rs.getString("date_of_birth"));
                }
                rs.close();
                stmt.close();
            } else if (role.equals("POLICE") && user instanceof PoliceOfficer) {
                String sql = "SELECT * FROM police_officers WHERE officer_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    ((PoliceOfficer) user).setBadgeNumber(rs.getString("badge_number"));
                    ((PoliceOfficer) user).setRank(rs.getString("rank"));
                    ((PoliceOfficer) user).setDepartment(rs.getString("department"));
                    ((PoliceOfficer) user).setStationName(rs.getString("station_name"));
                }
                rs.close();
                stmt.close();
            } else if (role.equals("INSURANCE") && user instanceof InsuranceAgent) {
                String sql = "SELECT * FROM insurance_agents WHERE agent_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    ((InsuranceAgent) user).setAgentCode(rs.getString("agent_code"));
                    ((InsuranceAgent) user).setCompanyName(rs.getString("company_name"));
                    ((InsuranceAgent) user).setLicenseNumber(rs.getString("license_number"));
                    ((InsuranceAgent) user).setCommissionRate(rs.getDouble("commission_rate"));
                }
                rs.close();
                stmt.close();
            } else if (role.equals("WORKSHOP") && user instanceof WorkshopStaff) {
                String sql = "SELECT * FROM workshop_staff WHERE staff_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    ((WorkshopStaff) user).setStaffCode(rs.getString("staff_code"));
                    ((WorkshopStaff) user).setPosition(rs.getString("position"));
                    ((WorkshopStaff) user).setSpecialization(rs.getString("specialization"));
                    ((WorkshopStaff) user).setHourlyRate(rs.getDouble("hourly_rate"));
                }
                rs.close();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateLastLogin(int userId, Connection conn) {
        try {
            PreparedStatement stmt = conn.prepareStatement(DatabaseConfig.UPDATE_LAST_LOGIN);
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}