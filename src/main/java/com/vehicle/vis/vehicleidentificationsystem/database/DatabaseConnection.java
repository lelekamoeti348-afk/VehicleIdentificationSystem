package com.vehicle.vis.vehicleidentificationsystem.database;

import com.vehicle.vis.vehicleidentificationsystem.config.DatabaseConfig;
import java.sql.*;

public class DatabaseConnection {
    private static DatabaseConnection instance;

    private DatabaseConnection() {}

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        try {
            Class.forName(DatabaseConfig.JDBC_DRIVER);
            return DriverManager.getConnection(
                    DatabaseConfig.DB_URL,
                    DatabaseConfig.DB_USER,
                    DatabaseConfig.DB_PASSWORD
            );
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found", e);
        }
    }

    /**
     * This method is kept for compatibility with existing code.
     * It simply closes the connection (since we no longer use a pool).
     */
    public void returnConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean testConnection() {
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.DB_URL,
                DatabaseConfig.DB_USER,
                DatabaseConfig.DB_PASSWORD)) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}