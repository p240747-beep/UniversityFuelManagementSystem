package com.university.fms.service;

import com.university.fms.model.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Central in-memory data store for all system entities.
 * Implements the REPOSITORY pattern — all CRUD operations go through here.
 * This makes the service layer testable and decoupled from storage concerns.
 */
public class DataStore {
    private static DataStore instance;

    private final Map<String, Person> users = new LinkedHashMap<>();
    private final Map<String, Vehicle> vehicles = new LinkedHashMap<>();
    private final Map<String, FuelRequest> fuelRequests = new LinkedHashMap<>();
    private final Map<String, TripLog> tripLogs = new LinkedHashMap<>();
    private final List<AuditEntry> auditTrail = new ArrayList<>();
    private FuelInventory inventory;

    private int requestCounter = 1000;
    private int tripCounter = 2000;
    private int auditCounter = 3000;

    private DataStore() {
        seedData();
    }

    // SINGLETON pattern
    public static DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    private void seedData() {
        // Seed inventory: 5000L capacity, 3000L initial, PKR 280/L
        inventory = new FuelInventory(5000, 3000, 280.0);

        // Seed users
        users.put("U001", new Administrator("U001", "Ahmad Raza", "admin@uni.edu.pk", "admin123", "Transport Dept"));
        users.put("U002", new Driver("U002", "Bilal Hussain", "bilal@uni.edu.pk", "pass123", "LHR-DL-001"));
        users.put("U003", new Driver("U003", "Kamran Ali", "kamran@uni.edu.pk", "pass123", "LHR-DL-002"));
        users.put("U004", new FleetManager("U004", "Sara Ahmed", "sara@uni.edu.pk", "pass123", "FM-001"));
        users.put("U005", new FinanceOfficer("U005", "Nadia Khan", "nadia@uni.edu.pk", "pass123", 500000));
        users.put("U006", new PumpOperator("U006", "Hamid Iqbal", "hamid@uni.edu.pk", "pass123", "PUMP-A1"));

        // Seed vehicles
        Vehicle v1 = new Vehicle("V001", "LHR-1234", "Toyota Coaster", Vehicle.VehicleType.BUS, 80);
        Vehicle v2 = new Vehicle("V002", "LHR-5678", "Toyota Corolla", Vehicle.VehicleType.CAR, 45);
        Vehicle v3 = new Vehicle("V003", "LHR-9012", "Suzuki Bolan", Vehicle.VehicleType.VAN, 35);
        Vehicle v4 = new Vehicle("V004", "LHR-3456", "Honda CG-125", Vehicle.VehicleType.MOTORCYCLE, 12);
        v1.setAssignedDriverID("U002");
        v2.setAssignedDriverID("U003");
        ((Driver) users.get("U002")).setAssignedVehicleID("V001");
        ((Driver) users.get("U003")).setAssignedVehicleID("V002");
        vehicles.put("V001", v1);
        vehicles.put("V002", v2);
        vehicles.put("V003", v3);
        vehicles.put("V004", v4);

        // Seed some fuel requests
        FuelRequest r1 = new FuelRequest("REQ-1001", "U002", "Bilal Hussain",
            "V001", "LHR-1234", 40, "Monthly route bus fuel");
        r1.approve("U001");
        fuelRequests.put(r1.getRequestID(), r1);

        FuelRequest r2 = new FuelRequest("REQ-1002", "U003", "Kamran Ali",
            "V002", "LHR-5678", 20, "Official trip to city campus");
        fuelRequests.put(r2.getRequestID(), r2);

        FuelRequest r3 = new FuelRequest("REQ-1003", "U004", "Sara Ahmed",
            "V003", "LHR-9012", 15, "Van maintenance trip");
        r3.approve("U001");
        fuelRequests.put(r3.getRequestID(), r3);

        // Seed trip logs
        TripLog t1 = new TripLog("TRIP-2001", "U002", "Bilal Hussain",
            "V001", "LHR-1234", "Model Town", "Student Transport",
            25, 8.5, LocalDate.now().minusDays(3));
        TripLog t2 = new TripLog("TRIP-2002", "U003", "Kamran Ali",
            "V002", "LHR-5678", "Gulberg", "Official Meeting",
            12, 3.2, LocalDate.now().minusDays(1));
        tripLogs.put(t1.getTripID(), t1);
        tripLogs.put(t2.getTripID(), t2);

        // Seed audit entries
        audit("U001", "Ahmad Raza", "SYSTEM_INIT", "System initialized with seed data");
    }

    // -------- Users --------
    public Optional<Person> authenticate(String email, String password) {
        return users.values().stream()
            .filter(p -> p.getEmail().equalsIgnoreCase(email)
                      && p.getPassword().equals(password))
            .findFirst();
    }

    public Collection<Person> getAllUsers() { return users.values(); }
    public Person getUser(String id) { return users.get(id); }
    public void addUser(Person p) { users.put(p.getPersonID(), p); }
    public String nextUserID() { return "U" + String.format("%03d", users.size() + 1); }

    // -------- Vehicles --------
    public Collection<Vehicle> getAllVehicles() { return vehicles.values(); }
    public Vehicle getVehicle(String id) { return vehicles.get(id); }
    public void addVehicle(Vehicle v) { vehicles.put(v.getVehicleID(), v); }
    public String nextVehicleID() { return "V" + String.format("%03d", vehicles.size() + 1); }

    // -------- Fuel Requests --------
    public Collection<FuelRequest> getAllRequests() { return fuelRequests.values(); }
    public FuelRequest getRequest(String id) { return fuelRequests.get(id); }
    public void addRequest(FuelRequest r) { fuelRequests.put(r.getRequestID(), r); }
    public String nextRequestID() { return "REQ-" + (++requestCounter); }

    // -------- Trip Logs --------
    public Collection<TripLog> getAllTripLogs() { return tripLogs.values(); }
    public void addTripLog(TripLog t) { tripLogs.put(t.getTripID(), t); }
    public String nextTripID() { return "TRIP-" + (++tripCounter); }

    // -------- Inventory --------
    public FuelInventory getInventory() { return inventory; }

    // -------- Audit Trail --------
    public List<AuditEntry> getAuditTrail() { return Collections.unmodifiableList(auditTrail); }
    public void audit(String userID, String userName, String action, String details) {
        auditTrail.add(new AuditEntry("AUD-" + (++auditCounter),
            userID, userName, action, details));
    }
}
