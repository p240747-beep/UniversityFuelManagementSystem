package com.university.fms.ui;

import com.university.fms.model.*;
import com.university.fms.service.FuelManagementService;
import com.university.fms.util.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dashboard panel showing key performance indicators.
 * Maps to FR16 - Dashboard and KPI Visualization.
 */
public class DashboardPanel extends JPanel {
    private final FuelManagementService service;
    private final MainFrame mainFrame;

    public DashboardPanel(FuelManagementService service, MainFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;
        setBackground(UIConstants.BG_PANEL);
        setLayout(new BorderLayout(0, UIConstants.GAP));
        buildUI();
    }

    private void buildUI() {
        Person user = service.getCurrentUser();

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_PANEL);
        JLabel title = new JLabel("Dashboard");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_MAIN);
        JLabel sub = UIHelper.label("Welcome back, " + user.getName() + " · " + user.getDisplayRole());
        sub.setForeground(UIConstants.TEXT_MUTED);
        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // KPI Cards
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 15, 0));
        kpiRow.setBackground(UIConstants.BG_PANEL);

        FuelInventory inv = service.getInventory();
        List<FuelRequest> allReqs = service.getAllRequests();
        long pending = allReqs.stream().filter(r -> r.getStatus() == FuelRequest.RequestStatus.PENDING).count();
        long approved = allReqs.stream().filter(r -> r.getStatus() == FuelRequest.RequestStatus.APPROVED).count();
        long dispensed = allReqs.stream().filter(r -> r.getStatus() == FuelRequest.RequestStatus.DISPENSED).count();
        double totalLitres = service.getTotalLitresDispensed();

        kpiRow.add(kpiCard("⛽ Stock Level",
            String.format("%.0f L", inv.getCurrentStock()),
            String.format("%.1f%% of capacity", inv.getStockPercentage()),
            inv.isLowStock() ? UIConstants.DANGER : UIConstants.SUCCESS));

        kpiRow.add(kpiCard("📋 Pending Requests",
            String.valueOf(pending),
            "Awaiting approval",
            pending > 0 ? UIConstants.WARNING : UIConstants.TEXT_MUTED));

        kpiRow.add(kpiCard("✅ Dispensed Today",
            String.valueOf(dispensed),
            String.format("%.1f L total dispensed", totalLitres),
            UIConstants.ACCENT));

        kpiRow.add(kpiCard("🚗 Total Vehicles",
            String.valueOf(service.getAllVehicles().size()),
            approved + " requests approved",
            UIConstants.PRIMARY));

        add(kpiRow, BorderLayout.CENTER);

        // Bottom section
        JPanel bottom = new JPanel(new GridLayout(1, 2, 15, 0));
        bottom.setBackground(UIConstants.BG_PANEL);
        bottom.setPreferredSize(new Dimension(0, 280));

        // Recent requests table
        JPanel recentCard = UIHelper.cardPanel("Recent Fuel Requests");
        String[] cols = {"Request ID", "Requester", "Vehicle", "Litres", "Status"};
        JTable table = UIHelper.styledTable(cols);
        UIHelper.applyAlternatingRows(table);
        var model = (javax.swing.table.DefaultTableModel) table.getModel();
        allReqs.stream()
            .sorted((a, b) -> b.getRequestDateStr().compareTo(a.getRequestDateStr()))
            .limit(6)
            .forEach(r -> model.addRow(new Object[]{
                r.getRequestID(), r.getRequesterName(), r.getVehiclePlate(),
                String.format("%.1f L", r.getRequestedLitres()), r.getStatus()
            }));
        recentCard.add(UIHelper.scrollPane(table), BorderLayout.CENTER);

        // Inventory bar
        JPanel invCard = UIHelper.cardPanel("Fuel Inventory Status");
        JPanel invContent = new JPanel();
        invContent.setLayout(new BoxLayout(invContent, BoxLayout.Y_AXIS));
        invContent.setBackground(UIConstants.BG_WHITE);

        addInventoryRow(invContent, "Current Stock", String.format("%.0f L", inv.getCurrentStock()), UIConstants.SUCCESS);
        addInventoryRow(invContent, "Total Capacity", String.format("%.0f L", inv.getTotalCapacity()), UIConstants.PRIMARY);
        addInventoryRow(invContent, "Price/Litre", String.format("PKR %.2f", inv.getPricePerLitre()), UIConstants.TEXT_MAIN);
        addInventoryRow(invContent, "Total Dispensed", String.format("%.1f L", totalLitres), UIConstants.ACCENT);
        addInventoryRow(invContent, "Est. Cost Dispensed",
            String.format("PKR %,.0f", service.getTotalFuelCostDispensed()), UIConstants.WARNING);

        // Visual progress bar
        invContent.add(Box.createVerticalStrut(15));
        JLabel pctLabel = UIHelper.label(String.format("Stock: %.1f%%", inv.getStockPercentage()));
        pctLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        invContent.add(pctLabel);
        invContent.add(Box.createVerticalStrut(6));

        JPanel barBg = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = (int) (getWidth() * inv.getStockPercentage() / 100.0);
                g2.setColor(UIConstants.BORDER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                Color barColor = inv.isLowStock() ? UIConstants.DANGER :
                    inv.getStockPercentage() < 50 ? UIConstants.WARNING : UIConstants.SUCCESS;
                g2.setColor(barColor);
                g2.fillRoundRect(0, 0, w, getHeight(), 8, 8);
            }
        };
        barBg.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        barBg.setAlignmentX(Component.LEFT_ALIGNMENT);
        barBg.setPreferredSize(new Dimension(100, 18));
        invContent.add(barBg);

        if (inv.isLowStock()) {
            invContent.add(Box.createVerticalStrut(10));
            JLabel warn = new JLabel("⚠ LOW STOCK ALERT — Replenish soon!");
            warn.setFont(UIConstants.FONT_SMALL);
            warn.setForeground(UIConstants.DANGER);
            warn.setAlignmentX(Component.LEFT_ALIGNMENT);
            invContent.add(warn);
        }

        invCard.add(invContent, BorderLayout.CENTER);

        bottom.add(recentCard);
        bottom.add(invCard);
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel kpiCard(String title, String value, String sub, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIConstants.BG_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel t = new JLabel(title);
        t.setFont(UIConstants.FONT_SMALL);
        t.setForeground(UIConstants.TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 28));
        v.setForeground(accent);

        JLabel s = new JLabel(sub);
        s.setFont(UIConstants.FONT_SMALL);
        s.setForeground(UIConstants.TEXT_MUTED);

        card.add(t);
        card.add(Box.createVerticalStrut(8));
        card.add(v);
        card.add(Box.createVerticalStrut(4));
        card.add(s);
        return card;
    }

    private void addInventoryRow(JPanel parent, String label, String value, Color color) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(UIConstants.BG_WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = UIHelper.label(label);
        JLabel v = new JLabel(value);
        v.setFont(UIConstants.FONT_LABEL);
        v.setForeground(color);
        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        parent.add(row);
        parent.add(Box.createVerticalStrut(4));
    }
}
