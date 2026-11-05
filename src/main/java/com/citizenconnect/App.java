
package com.citizenconnect;

import com.citizenconnect.config.MongoDBConnection;
import com.citizenconnect.config.DatabaseInitializer;

public class App {

    public static void main(String[] args) {

        try {

            // Test MongoDB connection
            MongoDBConnection.ping();

            // Initialize database collections and indexes
            DatabaseInitializer.initialize();

        } catch (Exception e) {

            System.err.println("Database initialization failed!");
            e.printStackTrace();

        } finally {

            // Close MongoDB connection
            MongoDBConnection.close();
        }
    }
}
