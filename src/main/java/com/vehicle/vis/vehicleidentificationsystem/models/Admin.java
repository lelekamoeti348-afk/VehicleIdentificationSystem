package com.vehicle.vis.vehicleidentificationsystem.models;

public class Admin extends User {

    public Admin(int userId, String username, String fullName, String email, String phone) {
        super(userId, username, fullName, email, phone, "ADMIN");
    }

    @Override
    public String getDashboardView() {
        return "/views/admin/admin_dashboard.fxml";
    }

    @Override
    public String getRoleDescription() {
        return "System Administrator - Full system access and user management";
    }
}