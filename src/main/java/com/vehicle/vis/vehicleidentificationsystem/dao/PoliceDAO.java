package com.vehicle.vis.vehicleidentificationsystem.dao;

import com.vehicle.vis.vehicleidentificationsystem.database.DatabaseConnection;
import com.vehicle.vis.vehicleidentificationsystem.models.PoliceReport;
import com.vehicle.vis.vehicleidentificationsystem.models.Violation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;

public class PoliceDAO {

    public ObservableList<Violation> getAllViolations() {
        ObservableList<Violation> violations = FXCollections.observableArrayList();
        String sql = "SELECT violation_id, violation_code, violation_type, violation_date, " +
                "violation_location, fine_amount, points, status, registration_number " +
                "FROM vw_vehicle_violations ORDER BY violation_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Violation v = new Violation();
                v.setViolationId(rs.getInt("violation_id"));
                v.setViolationCode(rs.getString("violation_code"));
                v.setViolationType(rs.getString("violation_type"));
                v.setViolationDate(rs.getTimestamp("violation_date").toLocalDateTime());
                v.setViolationLocation(rs.getString("violation_location"));
                v.setFineAmount(rs.getDouble("fine_amount"));
                v.setPoints(rs.getInt("points"));
                v.setStatus(rs.getString("status"));
                v.setRegistrationNumber(rs.getString("registration_number"));
                violations.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return violations;
    }

    public ObservableList<PoliceReport> getStolenVehicles() {
        ObservableList<PoliceReport> reports = FXCollections.observableArrayList();
        String sql = "SELECT report_number, registration_number, make, model, owner_name, " +
                "report_date, theft_details, last_known_location, case_status " +
                "FROM vw_stolen_vehicles";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PoliceReport report = new PoliceReport();
                report.setReportNumber(rs.getString("report_number"));
                report.setRegistrationNumber(rs.getString("registration_number"));
                report.setMake(rs.getString("make"));
                report.setModel(rs.getString("model"));
                report.setOwnerName(rs.getString("owner_name"));
                report.setReportDate(rs.getTimestamp("report_date").toLocalDateTime());
                report.setTheftDetails(rs.getString("theft_details"));
                report.setLastKnownLocation(rs.getString("last_known_location"));
                report.setCaseStatus(rs.getString("case_status"));
                reports.add(report);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }
}