package com.university.fms.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a fuel request submitted by a Driver or Fleet Manager.
 * Maps to FR3, FR4 - Submit and Approve/Reject Fuel Request.
 * Implements STATE PATTERN (pending → approved/rejected → dispensed).
 */
public class FuelRequest {
    public enum RequestStatus { PENDING, APPROVED, REJECTED, DISPENSED }

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String requestID;
    private String requesterID;
    private String requesterName;
    private String vehicleID;
    private String vehiclePlate;
    private double requestedLitres;
    private String purpose;
    private RequestStatus status;
    private LocalDateTime requestDate;
    private LocalDateTime processedDate;
    private String processedByID;
    private String remarks;
    private String fuelToken; // Generated when approved

    public FuelRequest(String requestID, String requesterID, String requesterName,
                       String vehicleID, String vehiclePlate,
                       double requestedLitres, String purpose) {
        this.requestID = requestID;
        this.requesterID = requesterID;
        this.requesterName = requesterName;
        this.vehicleID = vehicleID;
        this.vehiclePlate = vehiclePlate;
        this.requestedLitres = requestedLitres;
        this.purpose = purpose;
        this.status = RequestStatus.PENDING;
        this.requestDate = LocalDateTime.now();
    }

    // Approve transition
    public void approve(String approverID) {
        if (status != RequestStatus.PENDING)
            throw new IllegalStateException("Can only approve PENDING requests.");
        this.status = RequestStatus.APPROVED;
        this.processedByID = approverID;
        this.processedDate = LocalDateTime.now();
        this.fuelToken = "TKN-" + requestID + "-" + System.currentTimeMillis() % 10000;
    }

    // Reject transition
    public void reject(String approverID, String reason) {
        if (status != RequestStatus.PENDING)
            throw new IllegalStateException("Can only reject PENDING requests.");
        this.status = RequestStatus.REJECTED;
        this.processedByID = approverID;
        this.processedDate = LocalDateTime.now();
        this.remarks = reason;
    }

    // Dispense transition
    public void markDispensed() {
        if (status != RequestStatus.APPROVED)
            throw new IllegalStateException("Can only dispense APPROVED requests.");
        this.status = RequestStatus.DISPENSED;
    }

    public String getRequestID() { return requestID; }
    public String getRequesterID() { return requesterID; }
    public String getRequesterName() { return requesterName; }
    public String getVehicleID() { return vehicleID; }
    public String getVehiclePlate() { return vehiclePlate; }
    public double getRequestedLitres() { return requestedLitres; }
    public String getPurpose() { return purpose; }
    public RequestStatus getStatus() { return status; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public LocalDateTime getProcessedDate() { return processedDate; }
    public String getProcessedByID() { return processedByID; }
    public String getRemarks() { return remarks; }
    public String getFuelToken() { return fuelToken; }

    public String getRequestDateStr() {
        return requestDate != null ? requestDate.format(FORMATTER) : "-";
    }
    public String getProcessedDateStr() {
        return processedDate != null ? processedDate.format(FORMATTER) : "-";
    }
}
