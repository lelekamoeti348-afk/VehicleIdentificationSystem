package com.vehicle.vis.vehicleidentificationsystem.models;

import java.time.LocalDateTime;

public class Violation {
    private int violationId;
    private String violationCode;
    private String violationType;
    private LocalDateTime violationDate;
    private String violationLocation;
    private double fineAmount;
    private int points;
    private String status;
    private String registrationNumber;
    private String vehicleName;
    private String ownerName;

    public Violation() {}

    public int getViolationId() { return violationId; }
    public void setViolationId(int violationId) { this.violationId = violationId; }
    public String getViolationCode() { return violationCode; }
    public void setViolationCode(String violationCode) { this.violationCode = violationCode; }
    public String getViolationType() { return violationType; }
    public void setViolationType(String violationType) { this.violationType = violationType; }
    public LocalDateTime getViolationDate() { return violationDate; }
    public void setViolationDate(LocalDateTime violationDate) { this.violationDate = violationDate; }
    public String getViolationLocation() { return violationLocation; }
    public void setViolationLocation(String violationLocation) { this.violationLocation = violationLocation; }
    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
}