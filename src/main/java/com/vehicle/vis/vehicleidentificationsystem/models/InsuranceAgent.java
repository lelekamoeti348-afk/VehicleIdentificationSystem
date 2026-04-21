package com.vehicle.vis.vehicleidentificationsystem.models;

public class InsuranceAgent extends User {
    private String agentCode;
    private String companyName;
    private String licenseNumber;
    private double commissionRate;

    public InsuranceAgent(int userId, String username, String fullName, String email, String phone) {
        super(userId, username, fullName, email, phone, "INSURANCE");
    }

    public InsuranceAgent(int userId, String username, String fullName, String email, String phone,
                          String agentCode, String companyName, String licenseNumber, double commissionRate) {
        super(userId, username, fullName, email, phone, "INSURANCE");
        this.agentCode = agentCode;
        this.companyName = companyName;
        this.licenseNumber = licenseNumber;
        this.commissionRate = commissionRate;
    }

    @Override
    public String getDashboardView() {
        return "/views/insurance/insurance_dashboard.fxml";
    }

    @Override
    public String getRoleDescription() {
        return "Insurance Agent - Manage insurance policies and process claims";
    }

    // Getters and Setters
    public String getAgentCode() { return agentCode; }
    public void setAgentCode(String agentCode) { this.agentCode = agentCode; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public double getCommissionRate() { return commissionRate; }
    public void setCommissionRate(double commissionRate) { this.commissionRate = commissionRate; }
}