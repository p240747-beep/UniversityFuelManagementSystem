package com.university.fms.service;

import com.university.fms.model.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer containing all business logic for the Fuel Management System.
 * Uses FACADE pattern - provides a simple API over the complex subsystem.
 */
public class FuelManagementService {
    private final DataStore store;
    private Person currentUser;

    public FuelManagementService() {
        this.store = DataStore.getInstance();
    }

    // ============================================================
    // UC-01: Authentication
    // ============================================================
    public Optional<Person> login(String email, String password) {
        Optional<Person> user = store.authenticate(email, password);
        user.ifPresent(p -> {
            this.currentUser = p;
            store.audit(p.getPersonID(), p.getName(), "LOGIN",
                "User logged in as " + p.getDisplayRole());
        });
        return user;
    }

    public void logout() {
        if (currentUser != null) {
            store.audit(currentUser.getPersonID(), currentUser.getName(),
                "LOGOUT", "User logged out");
            currentUser = null;
        }
    }

    public Person getCurrentUser() { return currentUser; }

    // ============================================================
    // UC-02: Vehicle Management
    // ============================================================
    public List<Vehicle> getAllVehicles() {
        return new ArrayList<>(store.getAllVehicles());
    }

    public Vehicle getVehicle(String vehicleID) {
        return store.getVehicle(vehicleID);
    }

    public void addVehicle(String plateNumber, String model,
                           Vehicle.VehicleType type, double fuelCapacity) {
        String id = store.nextVehicleID();
        Vehicle v = new Vehicle(id, plateNumber, model, type, fuelCapacity);
        store.addVehicle(v);
        store.audit(currentUser.getPersonID(), currentUser.getName(),
            "ADD_VEHICLE", "Added vehicle: " + plateNumber + " (" + model + ")");
    }

    public void updateVehicleStatus(String vehicleID, Vehicle.VehicleStatus status) {
        Vehicle v = store.getVehicle(vehicleID);
        if (v == null) throw new IllegalArgumentException("Vehicle not found: " + vehicleID);
        v.setStatus(status);
        store.audit(currentUser.getPersonID(), currentUser.getName(),
            "UPDATE_VEHICLE_STATUS", "Vehicle " + v.getPlateNumber() + " → " + status);
    }

    // ============================================================
    // UC-03: Submit Fuel Request
    // ============================================================
    public FuelRequest submitFuelRequest(String vehicleID, double litres, String purpose) {
        Vehicle v = store.getVehicle(vehicleID);
        if (v == null) throw new IllegalArgumentException("Vehicle not found.");
        if (litres <= 0) throw new IllegalArgumentException("Litres must be > 0.");
        if (litres > v.getFuelCapacity())
            throw new IllegalArgumentException("Requested litres exceed vehicle capacity (" + v.getFuelCapacity() + "L).");

        String id = store.nextRequestID();
        FuelRequest req = new FuelRequest(id, currentUser.getPersonID(),
            currentUser.getName(), vehicleID, v.getPlateNumber(), litres, purpose);
        store.addRequest(req);
        store.audit(currentUser.getPersonID(), currentUser.getName(),
            "SUBMIT_REQUEST", "Submitted fuel request " + id + " for " + litres + "L");
        return req;
    }

    // ============================================================
    // UC-04: Approve / Reject Fuel Request
    // ============================================================
    public void approveRequest(String requestID) {
        FuelRequest req = store.getRequest(requestID);
        if (req == null) throw new IllegalArgumentException("Request not found.");
        req.approve(currentUser.getPersonID());
        store.audit(currentUser.getPersonID(), currentUser.getName(),
            "APPROVE_REQUEST", "Approved request " + requestID +
            " for " + req.getRequestedLitres() + "L. Token: " + req.getFuelToken());
    }

    public void rejectRequest(String requestID, String reason) {
        FuelRequest req = store.getRequest(requestID);
        if (req == null) throw new IllegalArgumentException("Request not found.");
        req.reject(currentUser.getPersonID(), reason);
        store.audit(currentUser.getPersonID(), currentUser.getName(),
            "REJECT_REQUEST", "Rejected request " + requestID + ". Reason: " + reason);
    }

    // ============================================================
    // UC-05: Record Fuel Dispensing
    // ============================================================
    public void dispenseFuel(String requestID) {
        FuelRequest req = store.getRequest(requestID);
        if (req == null) throw new IllegalArgumentException("Request not found.");
        if (req.getStatus() != FuelRequest.RequestStatus.APPROVED)
            throw new IllegalStateException("Request must be APPROVED before dispensing.");

        FuelInventory inv = store.getInventory();
        boolean ok = inv.dispense(req.getRequestedLitres(), requestID);
        if (!ok) throw new IllegalStateException("Insufficient fuel in stock (" +
            String.format("%.1f", inv.getCurrentStock()) + "L available).");

        req.markDispensed();
        Vehicle v = store.getVehicle(req.getVehicleID());
        if (v != null) v.addFuelConsumed(req.getRequestedLitres());

        store.audit(currentUser.getPersonID(), currentUser.getName(),
            "DISPENSE_FUEL", "Dispensed " + req.getRequestedLitres() +
            "L for request " + requestID);
    }

    // ============================================================
    // UC-06: Fuel Inventory
    // ============================================================
    public FuelInventory getInventory() { return store.getInventory(); }

    public void replenishFuel(double litres, String vendorName) {
        if (litres <= 0) throw new IllegalArgumentException("Litres must be > 0.");
        store.getInventory().replenish(litres, vendorName);
        store.audit(currentUser.getPersonID(), currentUser.getName(),
            "REPLENISH_FUEL", "Replenished " + litres + "L from " + vendorName);
    }

    public void updateFuelPrice(double price) {
        if (price <= 0) throw new IllegalArgumentException("Price must be > 0.");
        store.getInventory().setPricePerLitre(price);
        store.audit(currentUser.getPersonID(), currentUser.getName(),
            "UPDATE_PRICE", "Fuel price updated to PKR " + price + "/L");
    }

    // ============================================================
    // UC-07: Log Trip Details
    // ============================================================
    public void logTrip(String vehicleID, String destination, String purpose,
                        double distanceKm, double fuelUsedLitres, LocalDate date) {
        Vehicle v = store.getVehicle(vehicleID);
        if (v == null) throw new IllegalArgumentException("Vehicle not found.");
        if (distanceKm <= 0) throw new IllegalArgumentException("Distance must be > 0.");
        if (fuelUsedLitres < 0) throw new IllegalArgumentException("Fuel used cannot be negative.");

        String id = store.nextTripID();
        TripLog trip = new TripLog(id, currentUser.getPersonID(), currentUser.getName(),
            vehicleID, v.getPlateNumber(), destination, purpose,
            distanceKm, fuelUsedLitres, date);
        store.addTripLog(trip);
        store.audit(currentUser.getPersonID(), currentUser.getName(),
            "LOG_TRIP", "Logged trip " + id + " to " + destination);
    }

    // ============================================================
    // UC-08: Driver-Vehicle Assignment
    // ============================================================
    public void assignDriverToVehicle(String driverID, String vehicleID) {
        Person p = store.getUser(driverID);
        if (!(p instanceof Driver)) throw new IllegalArgumentException("User is not a driver.");
        Vehicle v = store.getVehicle(vehicleID);
        if (v == null) throw new IllegalArgumentException("Vehicle not found.");

        // Unassign from previous vehicle
        String prev = ((Driver) p).getAssignedVehicleID();
        if (prev != null) {
            Vehicle prevV = store.getVehicle(prev);
            if (prevV != null) prevV.setAssignedDriverID(null);
        }
        ((Driver) p).setAssignedVehicleID(vehicleID);
        v.setAssignedDriverID(driverID);
        store.audit(currentUser.getPersonID(), currentUser.getName(),
            "ASSIGN_DRIVER", "Assigned " + p.getName() + " to " + v.getPlateNumber());
    }

    // ============================================================
    // UC-13: Audit Trail
    // ============================================================
    public List<AuditEntry> getAuditTrail() {
        return store.getAuditTrail();
    }

    // ============================================================
    // UC-14: Reporting helpers
    // ============================================================
    public List<FuelRequest> getAllRequests() {
        return new ArrayList<>(store.getAllRequests());
    }

    public List<FuelRequest> getRequestsByStatus(FuelRequest.RequestStatus status) {
        return store.getAllRequests().stream()
            .filter(r -> r.getStatus() == status)
            .collect(Collectors.toList());
    }

    public List<FuelRequest> getRequestsByUser(String userID) {
        return store.getAllRequests().stream()
            .filter(r -> r.getRequesterID().equals(userID))
            .collect(Collectors.toList());
    }

    public List<TripLog> getAllTripLogs() {
        return new ArrayList<>(store.getAllTripLogs());
    }

    public List<TripLog> getTripsByDriver(String driverID) {
        return store.getAllTripLogs().stream()
            .filter(t -> t.getDriverID().equals(driverID))
            .collect(Collectors.toList());
    }

    public List<Person> getAllUsers() {
        return new ArrayList<>(store.getAllUsers());
    }

    public List<Driver> getAllDrivers() {
        return store.getAllUsers().stream()
            .filter(p -> p instanceof Driver)
            .map(p -> (Driver) p)
            .collect(Collectors.toList());
    }

    // ============================================================
    // UC-15: Budget / Cost helpers
    // ============================================================
    public double getTotalFuelCostDispensed() {
        double totalLitres = store.getAllRequests().stream()
            .filter(r -> r.getStatus() == FuelRequest.RequestStatus.DISPENSED)
            .mapToDouble(FuelRequest::getRequestedLitres)
            .sum();
        return store.getInventory().calculateCost(totalLitres);
    }

    public double getTotalLitresDispensed() {
        return store.getAllRequests().stream()
            .filter(r -> r.getStatus() == FuelRequest.RequestStatus.DISPENSED)
            .mapToDouble(FuelRequest::getRequestedLitres)
            .sum();
    }
}
