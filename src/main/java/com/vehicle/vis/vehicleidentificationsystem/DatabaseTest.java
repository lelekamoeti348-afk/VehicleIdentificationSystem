package com.vehicle.vis.vehicleidentificationsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/vehicle_identification_system";
        String user = "postgres";
        String password = "123456789";

        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Database connected successfully!");

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next()) {
                System.out.println("✅ Found " + rs.getInt(1) + " users in database");
            }

            conn.close();
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}