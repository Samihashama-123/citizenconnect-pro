
package com.citizenconnect;

import com.citizenconnect.config.MongoDBConnection;
import com.citizenconnect.config.DatabaseInitializer;
import com.citizenconnect.ui.LoginFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class App {

    public static void main(String[] args) {

        try {

            // Test MongoDB connection
            MongoDBConnection.ping();

            // Initialize database collections and indexes
            DatabaseInitializer.initialize();

            System.out.println("Application is starting...");

            // Close MongoDB connection when application exits
            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> {
                        MongoDBConnection.close();
                    })
            );

            // Open Login Frame
            SwingUtilities.invokeLater(() -> {

                LoginFrame loginFrame = new LoginFrame();

                loginFrame.setVisible(true);

            });

        } catch (Exception e) {

            System.err.println("Application startup failed!");

            e.printStackTrace();

            SwingUtilities.invokeLater(() -> {

                JOptionPane.showMessageDialog(
                        null,
                        "Application could not start.\n\n"
                                + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE
                );

            });
        }
    }
}
