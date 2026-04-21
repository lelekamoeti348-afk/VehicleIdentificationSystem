package com.vehicle.vis.vehicleidentificationsystem.dao;

import com.vehicle.vis.vehicleidentificationsystem.database.DatabaseConnection;
import com.vehicle.vis.vehicleidentificationsystem.models.CustomerQuery;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class QueryDAO {

    public boolean submitQuery(CustomerQuery query) {
        String sql = "INSERT INTO customer_queries (customer_id, vehicle_id, query_number, subject, query_text, query_type, priority) " +
                "VALUES (?, (SELECT vehicle_id FROM vehicles WHERE registration_number = ?), ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, query.getCustomerId());
            pstmt.setString(2, query.getRegistrationNumber());
            pstmt.setString(3, query.getQueryNumber());
            pstmt.setString(4, query.getSubject());
            pstmt.setString(5, query.getQueryText());
            pstmt.setString(6, query.getQueryType());
            pstmt.setString(7, query.getPriority());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ObservableList<CustomerQuery> getQueriesByCustomer(int customerId) {
        ObservableList<CustomerQuery> queries = FXCollections.observableArrayList();
        String sql = "SELECT * FROM customer_queries WHERE customer_id = ? ORDER BY query_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                CustomerQuery q = new CustomerQuery();
                q.setQueryId(rs.getInt("query_id"));
                q.setQueryNumber(rs.getString("query_number"));
                q.setQueryDate(rs.getTimestamp("query_date").toLocalDateTime());
                q.setSubject(rs.getString("subject"));
                q.setQueryText(rs.getString("query_text"));
                q.setResponseText(rs.getString("response_text"));
                q.setStatus(rs.getString("status"));
                queries.add(q);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return queries;
    }

    public boolean respondToQuery(int queryId, String response) {
        String sql = "UPDATE customer_queries SET response_text = ?, status = 'RESPONDED', responded_at = CURRENT_TIMESTAMP WHERE query_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, response);
            pstmt.setInt(2, queryId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}