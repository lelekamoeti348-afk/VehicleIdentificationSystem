package com.vehicle.vis.vehicleidentificationsystem.models;

import java.time.LocalDateTime;

public class CustomerQuery {
    private int queryId;
    private String queryNumber;
    private int customerId;
    private String registrationNumber;
    private LocalDateTime queryDate;
    private String subject;
    private String queryText;
    private String responseText;
    private String status;
    private String queryType;
    private String priority;

    public CustomerQuery() {}

    public int getQueryId() { return queryId; }
    public void setQueryId(int queryId) { this.queryId = queryId; }
    public String getQueryNumber() { return queryNumber; }
    public void setQueryNumber(String queryNumber) { this.queryNumber = queryNumber; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public LocalDateTime getQueryDate() { return queryDate; }
    public void setQueryDate(LocalDateTime queryDate) { this.queryDate = queryDate; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getQueryText() { return queryText; }
    public void setQueryText(String queryText) { this.queryText = queryText; }
    public String getResponseText() { return responseText; }
    public void setResponseText(String responseText) { this.responseText = responseText; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getQueryType() { return queryType; }
    public void setQueryType(String queryType) { this.queryType = queryType; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}