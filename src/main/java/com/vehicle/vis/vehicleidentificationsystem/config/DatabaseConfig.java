package com.vehicle.vis.vehicleidentificationsystem.config;

public class DatabaseConfig {
    // Database connection parameters - UPDATE THESE VALUES
    public static final String DB_HOST = "localhost";
    public static final String DB_PORT = "5432";
    public static final String DB_NAME = "vehicle_identification_system";
    public static final String DB_USER = "postgres";
    public static final String DB_PASSWORD = "123456789"; // Change this to your password

    public static final String DB_URL = String.format(
            "jdbc:postgresql://%s:%s/%s",
            DB_HOST, DB_PORT, DB_NAME
    );

    public static final String JDBC_DRIVER = "org.postgresql.Driver";

    // Connection pool settings
    public static final int MAX_POOL_SIZE = 10;
    public static final int CONNECTION_TIMEOUT = 30;

    // Query constants
    public static final String SELECT_USER_BY_USERNAME =
            "SELECT * FROM users WHERE username = ? AND is_active = true";

    public static final String SELECT_VEHICLE_BY_REGISTRATION =
            "SELECT * FROM vw_vehicle_details WHERE registration_number ILIKE ?";

    public static final String SELECT_VEHICLES_BY_OWNER =
            "SELECT * FROM vw_vehicle_details WHERE owner_name ILIKE ?";

    public static final String SELECT_SERVICE_HISTORY =
            "SELECT * FROM vw_service_history WHERE registration_number ILIKE ?";

    public static final String SELECT_ACTIVE_POLICIES =
            "SELECT * FROM vw_active_policies";

    public static final String SELECT_STOLEN_VEHICLES =
            "SELECT * FROM vw_stolen_vehicles";

    public static final String SELECT_VIOLATIONS_BY_VEHICLE =
            "SELECT * FROM vw_vehicle_violations WHERE registration_number ILIKE ?";

    public static final String INSERT_CUSTOMER_QUERY =
            "INSERT INTO customer_queries (customer_id, vehicle_id, query_number, subject, query_text, query_type, priority) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    public static final String UPDATE_LAST_LOGIN =
            "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";

    public static final String SEARCH_VEHICLE_FUNCTION =
            "SELECT * FROM fn_search_vehicle(?)";
}
