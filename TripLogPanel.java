package com.university.fms.ui;

import com.university.fms.model.*;
import com.university.fms.service.FuelManagementService;
import com.university.fms.util.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Trip log panel — UC-07: Log Trip Details.
 */
public class TripLogPanel extends JPanel {
    private final FuelManagementService service;
    private final MainFrame mainFrame;
    private DefaultTableModel tableModel;

    public TripLogPanel(FuelManagementService service, MainFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;
        setBackground(UIConstants.BG_PANEL);
        setLayout(new BorderLayout(0, UIConstants.GAP));
        buildUI();
    }

    private void buildUI() {
        Person user = service.getCurrentUser();

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_PANEL);
        header.add(UIHelper.heading("🗺️  Trip Logs"), BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(UIConstants.BG_PANEL);
        JButton addBtn = UIHelper.primaryButton("+ Log Trip");
        addBtn.addActionListener(e -> showLogDialog());
        btns.add(addBtn);
        JButton refreshBtn = UIHelper.successButton("↻ Refresh");
        refreshBtn.addActionListener(e -> refresh());
        btns.add(refreshBtn);
        header.add(btns, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"Trip ID", "Driver", "Vehicle", "Destination", "Purpose",
                         "Distance (km)", "Fuel Used (L)", "Efficiency (km/L)", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = UIHelper.styledTable(cols);
        table.setModel(tableModel);
        UIHelper.applyAlternatingRows(table);
        add(UIHelper.scrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    private void refresh() {
        tableModel.setRowCount(0);
        Person user = service.getCurrentUser();
        List<TripLog> logs = user.getRole().equals("DRIVER")
            ? service.getTripsByDriver(user.getPersonID())
            : service.getAllTripLogs();
        for (TripLog t : logs) {
            tableModel.addRow(new Object[]{
                t.getTripID(), t.getDriverName(), t.getVehiclePlate(),
                t.getDestination(), t.getPurpose(),
                String.format("%.1f", t.getDistanceKm()),
                String.format("%.1f", t.getFuelUsedLitres()),
                String.format("%.2f", t.getFuelEfficiency()),
                t.getTripDate().toString()
            });
        }
    }

    private void showLogDialog() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
            "Log Trip Details", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(430, 400);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;

        List<Vehicle> vehicles = service.getAllVehicles();
        String[] vOptions = vehicles.stream()
            .filter(v -> v.getStatus() == Vehicle.VehicleStatus.ACTIVE)
            .map(v -> v.getVehicleID() + " — " + v.getPlateNumber())
            .toArray(String[]::new);

        JComboBox<String> vehicleCb = UIHelper.styledCombo(vOptions);
        JTextField destField = UIHelper.styledField();
        JTextField purposeField = UIHelper.styledField();
        JTextField distField = UIHelper.styledField();
        JTextField fuelField = UIHelper.styledField();
        JTextField dateField = UIHelper.styledField();
        dateField.setText(LocalDate.now().toString());

        int row = 0;
        addRow(form, gbc, row++, "Vehicle:", vehicleCb);
        addRow(form, gbc, row++, "Destination:", destField);
        addRow(form, gbc, row++, "Purpose:", purposeField);
        addRow(form, gbc, row++, "Distance (km):", distField);
        addRow(form, gbc, row++, "Fuel Used (L):", fuelField);
        addRow(form, gbc, row++, "Date (yyyy-MM-dd):", dateField);

        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(UIConstants.BG_WHITE);
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dlg.dispose());
        JButton save = UIHelper.primaryButton("Log Trip");
        save.addActionListener(e -> {
            try {
                String vSel = (String) vehicleCb.getSelectedItem();
                if (vSel == null) { UIHelper.showError(dlg, "No vehicle available."); return; }
                String vehicleID = vSel.split(" — ")[0];
                String dest = destField.getText().trim();
                String purpose = purposeField.getText().trim();
                if (dest.isEmpty() || purpose.isEmpty()) { UIHelper.showError(dlg, "All fields required."); return; }
                double dist = Double.parseDouble(distField.getText().trim());
                double fuel = Double.parseDouble(fuelField.getText().trim());
                LocalDate date = LocalDate.parse(dateField.getText().trim());
                service.logTrip(vehicleID, dest, purpose, dist, fuel, date);
                UIHelper.showSuccess(dlg, "Trip logged successfully!");
                dlg.dispose();
                refresh();
            } catch (Exception ex) {
                UIHelper.showError(dlg, "Error: " + ex.getMessage());
            }
        });
        btnRow.add(cancel);
        btnRow.add(save);
        form.add(btnRow, gbc);
        dlg.setContentPane(form);
        dlg.setVisible(true);
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        form.add(UIHelper.label(label), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(field, gbc);
    }
}
