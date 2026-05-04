package com.university.fms.ui;

import com.university.fms.model.*;
import com.university.fms.service.FuelManagementService;
import com.university.fms.util.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Vehicle management panel — UC-02: Vehicle Registration and Management.
 */
public class VehicleManagementPanel extends JPanel {
    private final FuelManagementService service;
    private final MainFrame mainFrame;
    private DefaultTableModel tableModel;
    private JTable table;

    public VehicleManagementPanel(FuelManagementService service, MainFrame mainFrame) {
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
        header.add(UIHelper.heading("🚗  Vehicle Management"), BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(UIConstants.BG_PANEL);
        if (user.getRole().equals("ADMIN") || user.getRole().equals("FLEET_MANAGER")) {
            JButton addBtn = UIHelper.primaryButton("+ Add Vehicle");
            addBtn.addActionListener(e -> showAddDialog());
            btns.add(addBtn);
        }
        JButton refreshBtn = UIHelper.successButton("↻ Refresh");
        refreshBtn.addActionListener(e -> refresh());
        btns.add(refreshBtn);
        header.add(btns, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"Vehicle ID", "Plate", "Model", "Type", "Capacity (L)", "Status", "Driver Assigned", "Fuel Consumed (L)"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = UIHelper.styledTable(cols);
        table.setModel(tableModel);
        UIHelper.applyAlternatingRows(table);
        add(UIHelper.scrollPane(table), BorderLayout.CENTER);

        if (user.getRole().equals("ADMIN")) {
            JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            actionBar.setBackground(UIConstants.BG_PANEL);
            JButton maintenanceBtn = UIHelper.dangerButton("Set Maintenance");
            JButton activeBtn2 = UIHelper.successButton("Set Active");
            maintenanceBtn.addActionListener(e -> setStatus(Vehicle.VehicleStatus.UNDER_MAINTENANCE));
            activeBtn2.addActionListener(e -> setStatus(Vehicle.VehicleStatus.ACTIVE));
            actionBar.add(maintenanceBtn);
            actionBar.add(activeBtn2);
            add(actionBar, BorderLayout.SOUTH);
        }
        refresh();
    }

    private void refresh() {
        tableModel.setRowCount(0);
        for (Vehicle v : service.getAllVehicles()) {
            String driverName = "-";
            if (v.getAssignedDriverID() != null) {
                var d = service.getAllUsers().stream()
                    .filter(u -> u.getPersonID().equals(v.getAssignedDriverID()))
                    .findFirst();
                driverName = d.map(Person::getName).orElse(v.getAssignedDriverID());
            }
            tableModel.addRow(new Object[]{
                v.getVehicleID(), v.getPlateNumber(), v.getModel(),
                v.getType(), v.getFuelCapacity(),
                v.getStatus(), driverName,
                String.format("%.1f", v.getTotalFuelConsumed())
            });
        }
    }

    private void setStatus(Vehicle.VehicleStatus status) {
        int sel = table.getSelectedRow();
        if (sel < 0) { UIHelper.showError(this, "Please select a vehicle."); return; }
        String vid = (String) tableModel.getValueAt(sel, 0);
        try {
            service.updateVehicleStatus(vid, status);
            refresh();
            UIHelper.showSuccess(this, "Vehicle status updated to " + status);
        } catch (Exception e) {
            UIHelper.showError(this, e.getMessage());
        }
    }

    private void showAddDialog() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
            "Add New Vehicle", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(400, 320);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField plateField = UIHelper.styledField();
        JTextField modelField = UIHelper.styledField();
        JComboBox<String> typeCb = UIHelper.styledCombo("BUS", "CAR", "VAN", "MOTORCYCLE");
        JTextField capacityField = UIHelper.styledField();
        capacityField.setText("45");

        addRow(form, gbc, 0, "Plate Number:", plateField);
        addRow(form, gbc, 1, "Model:", modelField);
        addRow(form, gbc, 2, "Type:", typeCb);
        addRow(form, gbc, 3, "Fuel Capacity (L):", capacityField);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(UIConstants.BG_WHITE);
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dlg.dispose());
        JButton save = UIHelper.primaryButton("Add Vehicle");
        save.addActionListener(e -> {
            try {
                String plate = plateField.getText().trim();
                String model = modelField.getText().trim();
                if (plate.isEmpty() || model.isEmpty()) {
                    UIHelper.showError(dlg, "Plate and model are required."); return;
                }
                Vehicle.VehicleType type = Vehicle.VehicleType.valueOf((String) typeCb.getSelectedItem());
                double cap = Double.parseDouble(capacityField.getText().trim());
                service.addVehicle(plate, model, type, cap);
                UIHelper.showSuccess(dlg, "Vehicle added successfully!");
                dlg.dispose();
                refresh();
            } catch (NumberFormatException ex) {
                UIHelper.showError(dlg, "Capacity must be a number.");
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

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        form.add(UIHelper.label(label), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(field, gbc);
    }
}
