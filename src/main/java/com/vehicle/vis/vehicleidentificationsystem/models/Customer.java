package com.vehicle.vis.vehicleidentificationsystem.models;

public class Customer extends User {
    private String idNumber;
    private String driverLicense;
    private String dateOfBirth;
    private String address;

    public Customer(int userId, String username, String fullName, String email, String phone) {
        super(userId, username, fullName, email, phone, "CUSTOMER");
    }

    public Customer(int userId, String username, String fullName, String email, String phone,
                    String idNumber, String driverLicense, String address) {
        super(userId, username, fullName, email, phone, "CUSTOMER");
        this.idNumber = idNumber;
        this.driverLicense = driverLicense;
        this.address = address;
    }

    @Override
    public String getDashboardView() {
        return "/views/customer/customer_dashboard.fxml";
    }

    @Override
    public String getRoleDescription() {
        return "Customer - View vehicle information, service history, and submit queries";
    }

    // Getters and Setters
    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public String getDriverLicense() { return driverLicense; }
    public void setDriverLicense(String driverLicense) { this.driverLicense = driverLicense; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}