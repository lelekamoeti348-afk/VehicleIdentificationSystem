package com.vehicle.vis.vehicleidentificationsystem.models;

import java.time.LocalDate;

public class InsurancePolicy {
    private int policyId;
    private String policyNumber;
    private String registrationNumber;
    private String vehicleName;
    private String insuranceCompany;
    private String policyType;
    private LocalDate startDate;
    private LocalDate endDate;
    private double premiumAmount;
    private double coverageAmount;
    private String status;

    public InsurancePolicy() {}

    public int getPolicyId() { return policyId; }
    public void setPolicyId(int policyId) { this.policyId = policyId; }
    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }
    public String getInsuranceCompany() { return insuranceCompany; }
    public void setInsuranceCompany(String insuranceCompany) { this.insuranceCompany = insuranceCompany; }
    public String getPolicyType() { return policyType; }
    public void setPolicyType(String policyType) { this.policyType = policyType; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public double getPremiumAmount() { return premiumAmount; }
    public void setPremiumAmount(double premiumAmount) { this.premiumAmount = premiumAmount; }
    public double getCoverageAmount() { return coverageAmount; }
    public void setCoverageAmount(double coverageAmount) { this.coverageAmount = coverageAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}