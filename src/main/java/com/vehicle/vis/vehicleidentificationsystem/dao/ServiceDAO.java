package com.vehicle.vis.vehicleidentificationsystem.dao;

import com.vehicle.vis.vehicleidentificationsystem.database.DatabaseConnection;
import com.vehicle.vis.vehicleidentificationsystem.models.ServiceRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class ServiceDAO {

    public ObservableList<ServiceRecord> getAllServiceRecords() {
        ObservableList<ServiceRecord> records = FXCollections.observableArrayList();
        String sql = "SELECT * FROM vw_service_history ORDER BY service_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ServiceRecord sr = new ServiceRecord();
                sr.setServiceId(rs.getInt("service_id"));
                sr.setServiceDate(rs.getDate("service_date").toLocalDate());
                sr.setServiceType(rs.getString("service_type"));
                sr.setDescription(rs.getString("description"));
                sr.setCost(rs.getDouble("cost"));
                sr.setOdometerReading(rs.getInt("odometer_reading"));
                sr.setRegistrationNumber(rs.getString("registration_number"));
                sr.setServicedBy(rs.getString("serviced_by"));
                records.add(sr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public boolean addServiceRecord(ServiceRecord record) {
        String sql = "INSERT INTO service_records (vehicle_id, service_date, service_type, description, cost, odometer_reading) " +
                "VALUES ((SELECT vehicle_id FROM vehicles WHERE registration_number = ?), ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, record.getRegistrationNumber());
            pstmt.setDate(2, Date.valueOf(record.getServiceDate()));
            pstmt.setString(3, record.getServiceType());
            pstmt.setString(4, record.getDescription());
            pstmt.setDouble(5, record.getCost());
            pstmt.setInt(6, record.getOdometerReading());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ObservableList<ServiceRecord> getRecordsByVehicle(String registration) {
        ObservableList<ServiceRecord> records = FXCollections.observableArrayList();
        String sql = "SELECT * FROM vw_service_history WHERE registration_number ILIKE ? ORDER BY service_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + registration + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ServiceRecord sr = new ServiceRecord();
                sr.setServiceId(rs.getInt("service_id"));
                sr.setServiceDate(rs.getDate("service_date").toLocalDate());
                sr.setServiceType(rs.getString("service_type"));
                sr.setDescription(rs.getString("description"));
                sr.setCost(rs.getDouble("cost"));
                sr.setOdometerReading(rs.getInt("odometer_reading"));
                sr.setRegistrationNumber(rs.getString("registration_number"));
                records.add(sr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(cost), 0) FROM service_records";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalServiceCount() {
        String sql = "SELECT COUNT(*) FROM service_records";

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