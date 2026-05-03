package com.university.fms.model;

import java.time.LocalDate;

/**
 * Represents a university vehicle in the fuel management system.
 * Maps to FR2 - Vehicle Registration and Management.
 */
public class Vehicle {
    public enum VehicleType { BUS, CAR, VAN, MOTORCYCLE }
    public enum VehicleStatus { ACTIVE, UNDER_MAINTENANCE, RETIRED }

    private String vehicleID;
    private String plateNumber;
    private String model;
    private VehicleType type;
    private VehicleStatus status;
    private String assignedDriverID;
    private double fuelCapacity; // litres
    private LocalDate lastMaintenanceDate;
    private double totalFuelConsumed;

    public Vehicle(String vehicleID, String plateNumber, String model,
                   VehicleType type, double fuelCapacity) {
        this.vehicleID = vehicleID;
        this.plateNumber = plateNumber;
        this.model = model;
        this.type = type;
        this.fuelCapacity = fuelCapacity;
        this.status = VehicleStatus.ACTIVE;
        this.totalFuelConsumed = 0;
        this.lastMaintenanceDate = LocalDate.now();
    }

    public String getVehicleID() { return vehicleID; }
    public String getPlateNumber() { return plateNumber; }
    public String getModel() { return model; }
    public VehicleType getType() { return type; }
    public VehicleStatus getStatus() { return status; }
    public String getAssignedDriverID() { return assignedDriverID; }
    public double getFuelCapacity() { return fuelCapacity; }
    public LocalDate getLastMaintenanceDate() { return lastMaintenanceDate; }
    public double getTotalFuelConsumed() { return totalFuelConsumed; }

    public void setStatus(VehicleStatus status) { this.status = status; }
    public void setAssignedDriverID(String driverID) { this.assignedDriverID = driverID; }
    public void setLastMaintenanceDate(LocalDate date) { this.lastMaintenanceDate = date; }
    public void addFuelConsumed(double litres) { this.totalFuelConsumed += litres; }

    @Override
    public String toString() {
        return plateNumber + " - " + model + " (" + type + ")";
    }
}
