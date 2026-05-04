package com.university.fms.model;

public class Driver extends Person {
    private String licenseNumber;
    private String assignedVehicleID;

    public Driver(String personID, String name, String email, String password, String licenseNumber) {
        super(personID, name, email, password, "DRIVER");
        this.licenseNumber = licenseNumber;
        this.assignedVehicleID = null;
    }

    public String getLicenseNumber() { return licenseNumber; }
    public String getAssignedVehicleID() { return assignedVehicleID; }
    public void setAssignedVehicleID(String vehicleID) { this.assignedVehicleID = vehicleID; }

    @Override
    public String getDisplayRole() { return "Driver"; }
}
