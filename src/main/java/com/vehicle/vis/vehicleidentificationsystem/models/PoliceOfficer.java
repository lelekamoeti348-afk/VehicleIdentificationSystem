package com.vehicle.vis.vehicleidentificationsystem.models;

public class PoliceOfficer extends User {
    private String badgeNumber;
    private String rank;
    private String department;
    private String stationName;

    public PoliceOfficer(int userId, String username, String fullName, String email, String phone) {
        super(userId, username, fullName, email, phone, "POLICE");
    }

    public PoliceOfficer(int userId, String username, String fullName, String email, String phone,
                         String badgeNumber, String rank, String department, String stationName) {
        super(userId, username, fullName, email, phone, "POLICE");
        this.badgeNumber = badgeNumber;
        this.rank = rank;
        this.department = department;
        this.stationName = stationName;
    }

    @Override
    public String getDashboardView() {
        return "/views/police/police_dashboard.fxml";
    }

    @Override
    public String getRoleDescription() {
        return "Police Officer - Access vehicle records, file reports, and track violations";
    }

    // Getters and Setters
    public String getBadgeNumber() { return badgeNumber; }
    public void setBadgeNumber(String badgeNumber) { this.badgeNumber = badgeNumber; }

    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
}