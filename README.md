# University Fuel Management System (UFMS)

**Course:** Software Design & Analysis  
**Group Members:** Danial Hassan (24P-0747) · Abdul Mueez (24P-0510) · M. Mohiuddin Fida (24P-0588)

---

## Overview

A fully functional Java Swing GUI application implementing the University Fuel Management System designed in previous assignments. The system digitizes fuel requests, approvals, dispensing, vehicle management, trip logging, and reporting.

---

## How to Run

### Prerequisites
- Java 11 or later (Java 21 recommended)
- No additional libraries required — pure Java Swing

### Option 1: Run the JAR directly
```bash
java -jar UniversityFMS.jar
```

### Option 2: Compile and run from source
```bash
# Compile
find src -name "*.java" > sources.txt
mkdir -p out
javac -d out @sources.txt

# Run
java -cp out com.university.fms.Main
```

---

## Demo Login Accounts

| Email | Password | Role |
|-------|----------|------|
| admin@uni.edu.pk | admin123 | Administrator (full access) |
| bilal@uni.edu.pk | pass123 | Driver |
| kamran@uni.edu.pk | pass123 | Driver |
| sara@uni.edu.pk | pass123 | Fleet Manager |
| nadia@uni.edu.pk | pass123 | Finance Officer |
| hamid@uni.edu.pk | pass123 | Pump Operator |

---

## Features Implemented (Use Cases)

| Use Case | Description | Who |
|----------|-------------|-----|
| UC-01 | Login / Logout | All roles |
| UC-02 | Vehicle Registration & Status Management | Admin, Fleet Manager |
| UC-03 | Submit Fuel Request | Driver, Fleet Manager |
| UC-04 | Approve / Reject Fuel Request | Admin, Fleet Manager |
| UC-05 | Record Fuel Dispensing (with token) | Pump Operator, Admin |
| UC-06 | Fuel Inventory Tracking & Replenishment | Admin |
| UC-07 | Log Trip Details | Driver, Fleet Manager |
| UC-08 | Driver-to-Vehicle Assignment | Admin, Fleet Manager |
| UC-13 | View Audit Trail | Admin only |
| UC-14 | Generate Fuel Consumption Reports | Admin, Finance Officer |
| UC-15 | Budget & Cost Monitoring | Admin, Finance Officer |
| UC-16 | Dashboard & KPI Visualization | All roles |
| UC-20 | Fuel Price Management | Admin |

---

## Project Structure

```
src/main/java/com/university/fms/
├── Main.java                        ← Entry point
├── model/                           ← Domain classes (from class diagram)
│   ├── Person.java                  ← Abstract base (inheritance root)
│   ├── Administrator.java
│   ├── Driver.java
│   ├── FleetManager.java
│   ├── FinanceOfficer.java
│   ├── PumpOperator.java
│   ├── Vehicle.java
│   ├── FuelRequest.java             ← State pattern (PENDING→APPROVED→DISPENSED)
│   ├── FuelInventory.java
│   ├── TripLog.java
│   └── AuditEntry.java
├── service/
│   ├── DataStore.java               ← Repository pattern (Singleton)
│   └── FuelManagementService.java   ← Facade pattern (all business logic)
├── ui/                              ← Swing GUI panels
│   ├── LoginFrame.java
│   ├── MainFrame.java               ← Strategy pattern (panel navigation)
│   ├── DashboardPanel.java
│   ├── FuelRequestPanel.java
│   ├── VehicleManagementPanel.java
│   ├── TripLogPanel.java
│   ├── FuelDispensingPanel.java
│   ├── DriverAssignmentPanel.java
│   ├── InventoryPanel.java
│   ├── ReportsPanel.java
│   └── AuditTrailPanel.java
└── util/
    ├── UIConstants.java             ← Centralized styling
    └── UIHelper.java                ← Factory methods for UI components
```

---

## Design Patterns Used (Part 2 — Technical Reflection)

### 1. Singleton — `DataStore`
**Problem it solves:** Ensures a single shared in-memory data store across all service calls. Without this, each panel would have its own disconnected copy of data.  
**Would code work without it?** No — fuel requests submitted in `FuelRequestPanel` would be invisible to `FuelDispensingPanel`.  
**Limitation:** In a multi-user server environment, a true database would replace this.

### 2. Repository — `DataStore`
**Problem it solves:** Centralizes all CRUD operations. The service layer never directly accesses raw collections; it always goes through the store's typed methods.  
**Would code work without it?** Yes, but every service method would contain scattered `HashMap` lookups, making testing impossible.  
**Limitation:** In-memory only — data resets on restart.

### 3. Facade — `FuelManagementService`
**Problem it solves:** Provides a single clean API to the UI layer. UI panels call `service.submitFuelRequest(...)` without knowing about `DataStore`, `FuelInventory`, or audit logging.  
**Would code work without it?** Yes, but UI panels would be tightly coupled to all model classes.  
**Limitation:** As features grow, the Facade becomes a "god class".

### 4. State — `FuelRequest`
**Problem it solves:** Fuel requests follow a strict lifecycle: `PENDING → APPROVED → DISPENSED` (or `REJECTED`). Invalid transitions throw exceptions.  
**Would code work without it?** Code would work but be unsafe — nothing would prevent dispensing a rejected request.  
**Limitation:** Adding new states (e.g., `CANCELLED`) requires modifying the enum and all transition methods.

### 5. Strategy — `MainFrame.navigateTo()`
**Problem it solves:** Swaps content panels dynamically based on navigation. Each role sees only their permitted panels without changing the frame.  
**Would code work without it?** Yes, but with deeply nested `if/else` blocks or a monolithic JFrame.  
**Limitation:** All panel classes are loaded at compile time; truly dynamic plugin-style loading is not supported.
