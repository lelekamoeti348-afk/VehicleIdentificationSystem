package com.vehicle.vis.vehicleidentificationsystem.dao;

import com.vehicle.vis.vehicleidentificationsystem.database.DatabaseConnection;
import com.vehicle.vis.vehicleidentificationsystem.models.Claim;
import com.vehicle.vis.vehicleidentificationsystem.models.InsurancePolicy;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class InsuranceDAO {

    public ObservableList<InsurancePolicy> getAllActivePolicies() {
        ObservableList<InsurancePolicy> policies = FXCollections.observableArrayList();
        String sql = "SELECT * FROM vw_active_policies ORDER BY end_date ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                InsurancePolicy policy = new InsurancePolicy();
                policy.setPolicyId(rs.getInt("policy_id"));
                policy.setPolicyNumber(rs.getString("policy_number"));
                policy.setRegistrationNumber(rs.getString("registration_number"));
                policy.setVehicleName(rs.getString("vehicle_name"));
                policy.setInsuranceCompany(rs.getString("insurance_company"));
                policy.setPolicyType(rs.getString("policy_type"));
                policy.setStartDate(rs.getDate("start_date").toLocalDate());
                policy.setEndDate(rs.getDate("end_date").toLocalDate());
                policy.setPremiumAmount(rs.getDouble("premium_amount"));
                policy.setCoverageAmount(rs.getDouble("coverage_amount"));
                policies.add(policy);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return policies;
    }

    public boolean addPolicy(InsurancePolicy policy) {
        String sql = "INSERT INTO insurance_policies (vehicle_id, policy_number, insurance_company, policy_type, " +
                "start_date, end_date, premium_amount, coverage_amount) " +
                "VALUES ((SELECT vehicle_id FROM vehicles WHERE registration_number = ?), ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, policy.getRegistrationNumber());
            pstmt.setString(2, policy.getPolicyNumber());
            pstmt.setString(3, policy.getInsuranceCompany());
            pstmt.setString(4, policy.getPolicyType());
            pstmt.setDate(5, Date.valueOf(policy.getStartDate()));
            pstmt.setDate(6, Date.valueOf(policy.getEndDate()));
            pstmt.setDouble(7, policy.getPremiumAmount());
            pstmt.setDouble(8, policy.getCoverageAmount());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ObservableList<Claim> getAllClaims() {
        ObservableList<Claim> claims = FXCollections.observableArrayList();
        String sql = "SELECT * FROM insurance_claims ORDER BY claim_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Claim claim = new Claim();
                claim.setClaimId(rs.getInt("claim_id"));
                claim.setClaimNumber(rs.getString("claim_number"));
                claim.setClaimDate(rs.getDate("claim_date").toLocalDate());
                claim.setClaimAmount(rs.getDouble("claim_amount"));
                claim.setStatus(rs.getString("status"));
                claim.setDescription(rs.getString("description"));
                claims.add(claim);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return claims;
    }

    public boolean updateClaimStatus(int claimId, String status) {
        String sql = "UPDATE insurance_claims SET status = ? WHERE claim_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, claimId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getActivePoliciesCount() {
        String sql = "SELECT COUNT(*) FROM insurance_policies WHERE status = 'ACTIVE' AND end_date >= CURRENT_DATE";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getPendingClaimsCount() {
        String sql = "SELECT COUNT(*) FROM insurance_claims WHERE status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}