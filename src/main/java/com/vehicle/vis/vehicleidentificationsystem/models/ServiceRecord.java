package com.vehicle.vis.vehicleidentificationsystem.models;

import java.time.LocalDate;

public class ServiceRecord {
    private int serviceId;
    private LocalDate serviceDate;
    private String serviceType;
    private String description;
    private double cost;
    private int odometerReading;
    private String registrationNumber;
    private String servicedBy;
    private String paymentStatus;

    public ServiceRecord() {}

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }
    public LocalDate getServiceDate() { return serviceDate; }
    public void setServiceDate(LocalDate serviceDate) { this.serviceDate = serviceDate; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
    public int getOdometerReading() { return odometerReading; }
    public void setOdometerReading(int odometerReading) { this.odometerReading = odometerReading; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getServicedBy() { return servicedBy; }
    public void setServicedBy(String servicedBy) { this.servicedBy = servicedBy; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}