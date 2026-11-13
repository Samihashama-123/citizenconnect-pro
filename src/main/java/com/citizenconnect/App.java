
package com.citizenconnect;

import com.citizenconnect.config.MongoDBConnection;
import com.citizenconnect.config.DatabaseInitializer;
import com.citizenconnect.ui.LoginFrame;

import javax.swing.SwingUtilities;

public class App {

    public static void main(String[] args) {

        try {

            // Test MongoDB connection
            MongoDBConnection.ping();

            // Initialize database collections and indexes
            DatabaseInitializer.initialize();

            System.out.println("Application is starting...");

        } catch (Exception e) {

            System.err.println("Database initialization failed!");
            e.printStackTrace();

            return;
        }

        // Close MongoDB connection when the application exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MongoDBConnection.close();
        }));

        // Launch Login window
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
