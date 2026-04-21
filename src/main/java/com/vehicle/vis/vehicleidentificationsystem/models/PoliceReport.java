package com.vehicle.vis.vehicleidentificationsystem.models;

import java.time.LocalDateTime;

public class PoliceReport {
    private int reportId;
    private String reportNumber;
    private String registrationNumber;
    private String make;
    private String model;
    private String ownerName;
    private String ownerPhone;
    private LocalDateTime reportDate;
    private String reportType;
    private String description;
    private String accidentLocation;
    private String theftDetails;
    private String lastKnownLocation;
    private String caseStatus;

    public PoliceReport() {}

    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }
    public String getReportNumber() { return reportNumber; }
    public void setReportNumber(String reportNumber) { this.reportNumber = reportNumber; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }
    public LocalDateTime getReportDate() { return reportDate; }
    public void setReportDate(LocalDateTime reportDate) { this.reportDate = reportDate; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAccidentLocation() { return accidentLocation; }
    public void setAccidentLocation(String accidentLocation) { this.accidentLocation = accidentLocation; }
    public String getTheftDetails() { return theftDetails; }
    public void setTheftDetails(String theftDetails) { this.theftDetails = theftDetails; }
    public String getLastKnownLocation() { return lastKnownLocation; }
    public void setLastKnownLocation(String lastKnownLocation) { this.lastKnownLocation = lastKnownLocation; }
    public String getCaseStatus() { return caseStatus; }
    public void setCaseStatus(String caseStatus) { this.caseStatus = caseStatus; }
}