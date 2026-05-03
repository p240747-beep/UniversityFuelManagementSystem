package com.university.fms.model;

import java.time.LocalDate;

/**
 * Represents a trip logged by a driver.
 * Maps to FR7 - Log Trip Details.
 */
public class TripLog {
    private String tripID;
    private String driverID;
    private String driverName;
    private String vehicleID;
    private String vehiclePlate;
    private String destination;
    private String purpose;
    private double distanceKm;
    private double fuelUsedLitres;
    private LocalDate tripDate;

    public TripLog(String tripID, String driverID, String driverName,
                   String vehicleID, String vehiclePlate,
                   String destination, String purpose,
                   double distanceKm, double fuelUsedLitres, LocalDate tripDate) {
        this.tripID = tripID;
        this.driverID = driverID;
        this.driverName = driverName;
        this.vehicleID = vehicleID;
        this.vehiclePlate = vehiclePlate;
        this.destination = destination;
        this.purpose = purpose;
        this.distanceKm = distanceKm;
        this.fuelUsedLitres = fuelUsedLitres;
        this.tripDate = tripDate;
    }

    public String getTripID() { return tripID; }
    public String getDriverID() { return driverID; }
    public String getDriverName() { return driverName; }
    public String getVehicleID() { return vehicleID; }
    public String getVehiclePlate() { return vehiclePlate; }
    public String getDestination() { return destination; }
    public String getPurpose() { return purpose; }
    public double getDistanceKm() { return distanceKm; }
    public double getFuelUsedLitres() { return fuelUsedLitres; }
    public LocalDate getTripDate() { return tripDate; }
    public double getFuelEfficiency() {
        return fuelUsedLitres > 0 ? distanceKm / fuelUsedLitres : 0;
    }
}
