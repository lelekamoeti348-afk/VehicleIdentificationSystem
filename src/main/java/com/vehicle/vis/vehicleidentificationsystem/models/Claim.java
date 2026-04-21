package com.vehicle.vis.vehicleidentificationsystem.models;

import java.time.LocalDate;

public class Claim {
    private int claimId;
    private String claimNumber;
    private String policyNumber;
    private LocalDate claimDate;
    private double claimAmount;
    private String status;
    private String description;

    public Claim() {}

    public int getClaimId() { return claimId; }
    public void setClaimId(int claimId) { this.claimId = claimId; }
    public String getClaimNumber() { return claimNumber; }
    public void setClaimNumber(String claimNumber) { this.claimNumber = claimNumber; }
    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }
    public LocalDate getClaimDate() { return claimDate; }
    public void setClaimDate(LocalDate claimDate) { this.claimDate = claimDate; }
    public double getClaimAmount() { return claimAmount; }
    public void setClaimAmount(double claimAmount) { this.claimAmount = claimAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}