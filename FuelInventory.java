package com.university.fms.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tracks the fuel stock at the university pump station.
 * Maps to FR6 - Fuel Inventory Tracking.
 * Uses OBSERVER pattern: notifies listeners on low stock.
 */
public class FuelInventory {
    public static final double LOW_STOCK_THRESHOLD = 500.0; // litres

    private double currentStock;    // litres
    private double totalCapacity;
    private double pricePerLitre;   // PKR
    private List<String> auditLog;

    public FuelInventory(double totalCapacity, double initialStock, double pricePerLitre) {
        this.totalCapacity = totalCapacity;
        this.currentStock = initialStock;
        this.pricePerLitre = pricePerLitre;
        this.auditLog = new ArrayList<>();
        log("System initialized. Stock: " + initialStock + "L @ PKR " + pricePerLitre + "/L");
    }

    public boolean dispense(double litres, String requestID) {
        if (litres > currentStock) return false;
        currentStock -= litres;
        log("Dispensed " + litres + "L for request " + requestID +
            ". Remaining: " + currentStock + "L");
        return true;
    }

    public void replenish(double litres, String vendorName) {
        currentStock = Math.min(currentStock + litres, totalCapacity);
        log("Replenished " + litres + "L from " + vendorName +
            ". Total stock: " + currentStock + "L");
    }

    public boolean isLowStock() {
        return currentStock < LOW_STOCK_THRESHOLD;
    }

    public double calculateCost(double litres) {
        return litres * pricePerLitre;
    }

    private void log(String message) {
        auditLog.add("[" + LocalDateTime.now().toString().substring(0, 16) + "] " + message);
    }

    public double getCurrentStock() { return currentStock; }
    public double getTotalCapacity() { return totalCapacity; }
    public double getPricePerLitre() { return pricePerLitre; }
    public List<String> getAuditLog() { return new ArrayList<>(auditLog); }
    public double getStockPercentage() { return (currentStock / totalCapacity) * 100; }

    public void setPricePerLitre(double price) {
        this.pricePerLitre = price;
        log("Price updated to PKR " + price + "/L");
    }
}
