package com.vehicle.vis.vehicleidentificationsystem.dao;

import com.vehicle.vis.vehicleidentificationsystem.database.DatabaseConnection;
import com.vehicle.vis.vehicleidentificationsystem.models.Vehicle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    public ObservableList<Vehicle> getAllVehicles() {
        ObservableList<Vehicle> vehicles = FXCollections.observableArrayList();
        String sql = "SELECT * FROM vw_vehicle_details ORDER BY vehicle_id DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vehicle v = new Vehicle();
                v.setVehicleId(rs.getInt("vehicle_id"));
                v.setRegistrationNumber(rs.getString("registration_number"));
                v.setMake(rs.getString("make"));
                v.setModel(rs.getString("model"));
                v.setYear(rs.getInt("year"));
                v.setColor(rs.getString("color"));
                v.setStatus(rs.getString("status"));
                v.setOwnerName(rs.getString("owner_name"));
                vehicles.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicles;
    }

    public boolean addVehicle(Vehicle vehicle) {
        String sql = "INSERT INTO vehicles (registration_number, make, model, year, color, owner_id, engine_number, chassis_number) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, vehicle.getRegistrationNumber());
            pstmt.setString(2, vehicle.getMake());
            pstmt.setString(3, vehicle.getModel());
            pstmt.setInt(4, vehicle.getYear());
            pstmt.setString(5, vehicle.getColor());
            pstmt.setInt(6, vehicle.getOwnerId());
            pstmt.setString(7, vehicle.getEngineNumber());
            pstmt.setString(8, vehicle.getChassisNumber());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateVehicle(Vehicle vehicle) {
        String sql = "UPDATE vehicles SET registration_number=?, make=?, model=?, year=?, color=?, status=? WHERE vehicle_id=?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, vehicle.getRegistrationNumber());
            pstmt.setString(2, vehicle.getMake());
            pstmt.setString(3, vehicle.getModel());
            pstmt.setInt(4, vehicle.getYear());
            pstmt.setString(5, vehicle.getColor());
            pstmt.setString(6, vehicle.getStatus());
            pstmt.setInt(7, vehicle.getVehicleId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteVehicle(int vehicleId) {
        String sql = "DELETE FROM vehicles WHERE vehicle_id=?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vehicleId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Vehicle searchByRegistration(String registration) {
        String sql = "SELECT * FROM vw_vehicle_details WHERE registration_number ILIKE ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + registration + "%");
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Vehicle v = new Vehicle();
                v.setVehicleId(rs.getInt("vehicle_id"));
                v.setRegistrationNumber(rs.getString("registration_number"));
                v.setMake(rs.getString("make"));
                v.setModel(rs.getString("model"));
                v.setYear(rs.getInt("year"));
                v.setColor(rs.getString("color"));
                v.setStatus(rs.getString("status"));
                v.setOwnerName(rs.getString("owner_name"));
                return v;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}