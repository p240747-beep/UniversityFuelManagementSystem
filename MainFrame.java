package com.university.fms.ui;

import com.university.fms.model.Person;
import com.university.fms.service.FuelManagementService;
import com.university.fms.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Main application frame with sidebar navigation and content area.
 * Uses STRATEGY pattern — content panel swapped based on selected panel class.
 */
public class MainFrame extends JFrame {
    private final FuelManagementService service;
    private JPanel contentArea;
    private JPanel sidebar;
    private JLabel userLabel;
    private JLabel roleLabel;
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private JButton activeBtn = null;

    public MainFrame(FuelManagementService service) {
        this.service = service;
        initUI();
        navigateTo("Dashboard");
    }

    private void initUI() {
        setTitle("University Fuel Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BG_PANEL);

        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);

        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(UIConstants.BG_PANEL);
        contentArea.setBorder(BorderFactory.createEmptyBorder(
            UIConstants.PADDING, UIConstants.PADDING,
            UIConstants.PADDING, UIConstants.PADDING));
        root.add(contentArea, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIConstants.PRIMARY);
        bar.setPreferredSize(new Dimension(0, UIConstants.TOPBAR_HEIGHT));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel appName = new JLabel("⛽  University Fuel Management System");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        appName.setForeground(Color.WHITE);
        bar.add(appName, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(UIConstants.PRIMARY);
        right.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        userLabel = new JLabel();
        userLabel.setFont(UIConstants.FONT_LABEL);
        userLabel.setForeground(Color.WHITE);

        roleLabel = new JLabel();
        roleLabel.setFont(UIConstants.FONT_SMALL);
        roleLabel.setForeground(new Color(180, 200, 230));

        JPanel userInfo = new JPanel();
        userInfo.setBackground(UIConstants.PRIMARY);
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.add(userLabel);
        userInfo.add(roleLabel);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(UIConstants.FONT_SMALL);
        logoutBtn.setBackground(UIConstants.DANGER);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> logout());

        right.add(userInfo);
        right.add(Box.createHorizontalStrut(10));
        right.add(logoutBtn);
        bar.add(right, BorderLayout.EAST);

        // Update user info
        Person user = service.getCurrentUser();
        if (user != null) {
            userLabel.setText(user.getName());
            roleLabel.setText(user.getDisplayRole());
        }
        return bar;
    }

    private JPanel buildSidebar() {
        sidebar = new JPanel();
        sidebar.setBackground(UIConstants.BG_DARK);
        sidebar.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        Person user = service.getCurrentUser();
        String role = user != null ? user.getRole() : "";

        // Define nav items per role
        addNavItem("Dashboard", "📊  Dashboard");
        addNavItem("FuelRequests", "📋  Fuel Requests");

        if (!role.equals("FINANCE_OFFICER")) {
            addNavItem("VehicleManagement", "🚗  Vehicles");
        }
        if (role.equals("DRIVER") || role.equals("FLEET_MANAGER") || role.equals("ADMIN")) {
            addNavItem("TripLogs", "🗺️  Trip Logs");
        }
        if (role.equals("PUMP_OPERATOR") || role.equals("ADMIN")) {
            addNavItem("FuelDispensing", "⛽  Fuel Dispensing");
        }
        if (role.equals("ADMIN") || role.equals("FLEET_MANAGER")) {
            addNavItem("DriverAssignment", "👤  Driver Assignment");
        }
        if (role.equals("ADMIN") || role.equals("FINANCE_OFFICER")) {
            addNavItem("Inventory", "🏭  Inventory");
            addNavItem("Reports", "📈  Reports");
        }
        if (role.equals("ADMIN")) {
            addNavItem("AuditTrail", "🔍  Audit Trail");
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private void addNavItem(String key, String label) {
        JButton btn = new JButton(label);
        btn.setFont(UIConstants.FONT_LABEL);
        btn.setForeground(new Color(180, 200, 230));
        btn.setBackground(UIConstants.BG_DARK);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 44));
        btn.setMinimumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 44));
        btn.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 44));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> navigateTo(key));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != activeBtn) btn.setBackground(new Color(35, 50, 80));
            }
            public void mouseExited(MouseEvent e) {
                if (btn != activeBtn) btn.setBackground(UIConstants.BG_DARK);
            }
        });
        navButtons.put(key, btn);
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(2));
    }

    public void navigateTo(String key) {
        // Update active button style
        if (activeBtn != null) {
            activeBtn.setBackground(UIConstants.BG_DARK);
            activeBtn.setForeground(new Color(180, 200, 230));
        }
        JButton btn = navButtons.get(key);
        if (btn != null) {
            btn.setBackground(UIConstants.PRIMARY);
            btn.setForeground(Color.WHITE);
            activeBtn = btn;
        }

        contentArea.removeAll();
        JPanel panel = createPanel(key);
        if (panel != null) contentArea.add(panel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    private JPanel createPanel(String key) {
        switch (key) {
            case "Dashboard":        return new DashboardPanel(service, this);
            case "FuelRequests":     return new FuelRequestPanel(service, this);
            case "VehicleManagement":return new VehicleManagementPanel(service, this);
            case "TripLogs":         return new TripLogPanel(service, this);
            case "FuelDispensing":   return new FuelDispensingPanel(service, this);
            case "DriverAssignment": return new DriverAssignmentPanel(service, this);
            case "Inventory":        return new InventoryPanel(service, this);
            case "Reports":          return new ReportsPanel(service, this);
            case "AuditTrail":       return new AuditTrailPanel(service, this);
            default:                 return new DashboardPanel(service, this);
        }
    }

    private void logout() {
        if (UIHelper.confirm(this, "Are you sure you want to logout?")) {
            service.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame(service).setVisible(true));
        }
    }
}
