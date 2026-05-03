package com.university.fms;

import com.university.fms.service.FuelManagementService;
import com.university.fms.ui.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Use system look and feel for a native feel, fall back to Nimbus
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
            catch (Exception ignored) {}
        }

        SwingUtilities.invokeLater(() -> {
            FuelManagementService service = new FuelManagementService();
            LoginFrame frame = new LoginFrame(service);
            frame.setVisible(true);
        });
    }
}
