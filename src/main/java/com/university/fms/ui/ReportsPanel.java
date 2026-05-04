package com.university.fms.ui;

import com.university.fms.model.*;
import com.university.fms.service.FuelManagementService;
import com.university.fms.util.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reports panel — UC-14: Generate Fuel Consumption Report, UC-15: Budget Monitoring.
 */
public class ReportsPanel extends JPanel {
    private final FuelManagementService service;
    private final MainFrame mainFrame;

    public ReportsPanel(FuelManagementService service, MainFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;
        setBackground(UIConstants.BG_PANEL);
        setLayout(new BorderLayout(0, UIConstants.GAP));
        buildUI();
    }

    private void buildUI() {
        add(UIHelper.heading("📈  Fuel Consumption & Budget Reports"), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIConstants.FONT_LABEL);
        tabs.setBackground(UIConstants.BG_PANEL);

        tabs.addTab("Summary Report", buildSummaryTab());
        tabs.addTab("By Vehicle", buildByVehicleTab());
        tabs.addTab("By Driver", buildByDriverTab());
        tabs.addTab("Cost Analysis", buildCostTab());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildSummaryTab() {
        JPanel p = new JPanel(new GridLayout(2, 2, 15, 15));
        p.setBackground(UIConstants.BG_PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        FuelInventory inv = service.getInventory();
        List<FuelRequest> allReqs = service.getAllRequests();
        long total = allReqs.size();
        long pending = allReqs.stream().filter(r -> r.getStatus() == FuelRequest.RequestStatus.PENDING).count();
        long approved = allReqs.stream().filter(r -> r.getStatus() == FuelRequest.RequestStatus.APPROVED).count();
        long dispensed = allReqs.stream().filter(r -> r.getStatus() == FuelRequest.RequestStatus.DISPENSED).count();
        long rejected = allReqs.stream().filter(r -> r.getStatus() == FuelRequest.RequestStatus.REJECTED).count();
        double totalLitres = service.getTotalLitresDispensed();
        double totalCost = service.getTotalFuelCostDispensed();

        p.add(summaryCard("Total Requests", total + " requests",
            "Pending: " + pending + " | Approved: " + approved,
            UIConstants.PRIMARY));
        p.add(summaryCard("Dispensed", dispensed + " requests",
            String.format("%.1f L total dispensed", totalLitres),
            UIConstants.SUCCESS));
        p.add(summaryCard("Fuel Cost", String.format("PKR %,.0f", totalCost),
            String.format("@ PKR %.2f/L", inv.getPricePerLitre()),
            UIConstants.WARNING));
        p.add(summaryCard("Rejected", rejected + " requests",
            String.format("%.0f L remaining in stock", inv.getCurrentStock()),
            UIConstants.DANGER));
        return p;
    }

    private JPanel summaryCard(String title, String value, String sub, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIConstants.BG_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, color),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        JLabel t = new JLabel(title); t.setFont(UIConstants.FONT_SMALL); t.setForeground(UIConstants.TEXT_MUTED);
        JLabel v = new JLabel(value); v.setFont(new Font("Segoe UI", Font.BOLD, 24)); v.setForeground(color);
        JLabel s = new JLabel(sub); s.setFont(UIConstants.FONT_SMALL); s.setForeground(UIConstants.TEXT_MUTED);
        card.add(t); card.add(Box.createVerticalStrut(8)); card.add(v);
        card.add(Box.createVerticalStrut(4)); card.add(s);
        return card;
    }

    private JPanel buildByVehicleTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIConstants.BG_PANEL);
        String[] cols = {"Vehicle ID", "Plate", "Model", "Type", "Total Fuel Consumed (L)", "Est. Cost (PKR)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = UIHelper.styledTable(cols);
        table.setModel(model);
        UIHelper.applyAlternatingRows(table);
        double price = service.getInventory().getPricePerLitre();
        for (Vehicle v : service.getAllVehicles()) {
            model.addRow(new Object[]{
                v.getVehicleID(), v.getPlateNumber(), v.getModel(), v.getType(),
                String.format("%.1f", v.getTotalFuelConsumed()),
                String.format("PKR %,.0f", v.getTotalFuelConsumed() * price)
            });
        }
        p.add(UIHelper.scrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildByDriverTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIConstants.BG_PANEL);
        String[] cols = {"Driver ID", "Driver Name", "Total Trips", "Total Distance (km)", "Total Fuel (L)", "Avg Efficiency (km/L)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = UIHelper.styledTable(cols);
        table.setModel(model);
        UIHelper.applyAlternatingRows(table);

        Map<String, List<TripLog>> byDriver = service.getAllTripLogs().stream()
            .collect(Collectors.groupingBy(TripLog::getDriverID));
        for (Driver d : service.getAllDrivers()) {
            List<TripLog> trips = byDriver.getOrDefault(d.getPersonID(), Collections.emptyList());
            double totalDist = trips.stream().mapToDouble(TripLog::getDistanceKm).sum();
            double totalFuel = trips.stream().mapToDouble(TripLog::getFuelUsedLitres).sum();
            double avgEff = totalFuel > 0 ? totalDist / totalFuel : 0;
            model.addRow(new Object[]{
                d.getPersonID(), d.getName(), trips.size(),
                String.format("%.1f", totalDist),
                String.format("%.1f", totalFuel),
                String.format("%.2f", avgEff)
            });
        }
        p.add(UIHelper.scrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildCostTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIConstants.BG_PANEL);

        JTextArea report = new JTextArea();
        report.setEditable(false);
        report.setFont(new Font("Monospaced", Font.PLAIN, 12));
        report.setBackground(new Color(248, 249, 252));
        report.setMargin(new Insets(15, 15, 15, 15));

        FuelInventory inv = service.getInventory();
        double totalLitres = service.getTotalLitresDispensed();
        double totalCost = service.getTotalFuelCostDispensed();
        List<FuelRequest> all = service.getAllRequests();
        long total = all.size();
        long dispensedCount = all.stream().filter(r -> r.getStatus() == FuelRequest.RequestStatus.DISPENSED).count();

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(55)).append("\n");
        sb.append("  UNIVERSITY FUEL MANAGEMENT SYSTEM — COST REPORT\n");
        sb.append("=".repeat(55)).append("\n\n");
        sb.append(String.format("  Current Stock:         %.0f L%n", inv.getCurrentStock()));
        sb.append(String.format("  Total Capacity:        %.0f L%n", inv.getTotalCapacity()));
        sb.append(String.format("  Fuel Price:            PKR %.2f / L%n", inv.getPricePerLitre()));
        sb.append("\n");
        sb.append(String.format("  Total Requests:        %d%n", total));
        sb.append(String.format("  Dispensed Requests:    %d%n", dispensedCount));
        sb.append(String.format("  Total Litres Dispensed:%.1f L%n", totalLitres));
        sb.append(String.format("  Total Fuel Cost:       PKR %,.2f%n", totalCost));
        sb.append("\n");
        sb.append(String.format("  Remaining Stock Value: PKR %,.2f%n",
            inv.calculateCost(inv.getCurrentStock())));
        sb.append("\n");
        sb.append("-".repeat(55)).append("\n");
        sb.append("  Vehicle Fuel Summary:\n");
        for (Vehicle v : service.getAllVehicles()) {
            if (v.getTotalFuelConsumed() > 0)
                sb.append(String.format("  %-18s  %6.1f L  PKR %,.0f%n",
                    v.getPlateNumber(), v.getTotalFuelConsumed(),
                    v.getTotalFuelConsumed() * inv.getPricePerLitre()));
        }
        sb.append("=".repeat(55)).append("\n");

        report.setText(sb.toString());
        report.setCaretPosition(0);
        p.add(UIHelper.scrollPane(report), BorderLayout.CENTER);
        return p;
    }
}
