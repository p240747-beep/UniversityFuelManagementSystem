package com.university.fms.ui;

import com.university.fms.model.*;
import com.university.fms.service.FuelManagementService;
import com.university.fms.util.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Driver Assignment panel — UC-08: Driver-to-Vehicle Assignment.
 */
public class DriverAssignmentPanel extends JPanel {
    private final FuelManagementService service;
    private final MainFrame mainFrame;
    private DefaultTableModel tableModel;

    public DriverAssignmentPanel(FuelManagementService service, MainFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;
        setBackground(UIConstants.BG_PANEL);
        setLayout(new BorderLayout(0, UIConstants.GAP));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_PANEL);
        header.add(UIHelper.heading("👤  Driver-Vehicle Assignment"), BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setBackground(UIConstants.BG_PANEL);
        JButton assignBtn = UIHelper.primaryButton("Assign Driver");
        assignBtn.addActionListener(e -> showAssignDialog());
        JButton refreshBtn = UIHelper.successButton("↻ Refresh");
        refreshBtn.addActionListener(e -> refresh());
        btns.add(assignBtn);
        btns.add(refreshBtn);
        header.add(btns, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"Driver ID", "Driver Name", "License No.", "Assigned Vehicle", "Vehicle Plate"};
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
        for (Driver d : service.getAllDrivers()) {
            String vehiclePlate = "-";
            String vehicleID = d.getAssignedVehicleID();
            if (vehicleID != null) {
                Vehicle v = service.getVehicle(vehicleID);
                vehiclePlate = v != null ? v.getPlateNumber() + " (" + v.getModel() + ")" : vehicleID;
            }
            tableModel.addRow(new Object[]{
                d.getPersonID(), d.getName(), d.getLicenseNumber(),
                vehicleID != null ? vehicleID : "-", vehiclePlate
            });
        }
    }

    private void showAssignDialog() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
            "Assign Driver to Vehicle", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(380, 230);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        List<Driver> drivers = service.getAllDrivers();
        String[] driverOptions = drivers.stream()
            .map(d -> d.getPersonID() + " — " + d.getName())
            .toArray(String[]::new);
        List<Vehicle> vehicles = service.getAllVehicles();
        String[] vehicleOptions = vehicles.stream()
            .filter(v -> v.getStatus() == Vehicle.VehicleStatus.ACTIVE)
            .map(v -> v.getVehicleID() + " — " + v.getPlateNumber() + " (" + v.getModel() + ")")
            .toArray(String[]::new);

        JComboBox<String> driverCb = UIHelper.styledCombo(driverOptions);
        JComboBox<String> vehicleCb = UIHelper.styledCombo(vehicleOptions);
        driverCb.setPreferredSize(new Dimension(250, UIConstants.FIELD_HEIGHT));
        vehicleCb.setPreferredSize(new Dimension(250, UIConstants.FIELD_HEIGHT));

        gbc.gridy = 0; gbc.gridx = 0; form.add(UIHelper.label("Driver:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; form.add(driverCb, gbc);
        gbc.gridy = 1; gbc.gridx = 0; gbc.fill = GridBagConstraints.NONE; form.add(UIHelper.label("Vehicle:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; form.add(vehicleCb, gbc);

        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(UIConstants.BG_WHITE);
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dlg.dispose());
        JButton save = UIHelper.primaryButton("Assign");
        save.addActionListener(e -> {
            try {
                String dSel = (String) driverCb.getSelectedItem();
                String vSel = (String) vehicleCb.getSelectedItem();
                if (dSel == null || vSel == null) { UIHelper.showError(dlg, "Select both driver and vehicle."); return; }
                String driverID = dSel.split(" — ")[0];
                String vehicleID = vSel.split(" — ")[0];
                service.assignDriverToVehicle(driverID, vehicleID);
                UIHelper.showSuccess(dlg, "Driver assigned successfully!");
                dlg.dispose();
                refresh();
            } catch (Exception ex) {
                UIHelper.showError(dlg, ex.getMessage());
            }
        });
        btnRow.add(cancel);
        btnRow.add(save);
        form.add(btnRow, gbc);
        dlg.setContentPane(form);
        dlg.setVisible(true);
    }
}
