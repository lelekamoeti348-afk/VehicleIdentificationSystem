package com.vehicle.vis.vehicleidentificationsystem.models;

public class WorkshopStaff extends User {
    private String staffCode;
    private String position;
    private String specialization;
    private double hourlyRate;

    public WorkshopStaff(int userId, String username, String fullName, String email, String phone) {
        super(userId, username, fullName, email, phone, "WORKSHOP");
    }

    public WorkshopStaff(int userId, String username, String fullName, String email, String phone,
                         String staffCode, String position, String specialization, double hourlyRate) {
        super(userId, username, fullName, email, phone, "WORKSHOP");
        this.staffCode = staffCode;
        this.position = position;
        this.specialization = specialization;
        this.hourlyRate = hourlyRate;
    }

    @Override
    public String getDashboardView() {
        return "/views/workshop/workshop_dashboard.fxml";
    }

    @Override
    public String getRoleDescription() {
        return "Workshop Staff - Manage service records and vehicle maintenance";
    }

    // Getters and Setters
    public String getStaffCode() { return staffCode; }
    public void setStaffCode(String staffCode) { this.staffCode = staffCode; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }
}