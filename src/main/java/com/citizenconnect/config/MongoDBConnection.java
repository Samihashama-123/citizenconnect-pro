
package com.citizenconnect.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBConnection {

    private static final String DATABASE_NAME = "citizenconnect_db";

    private static MongoClient mongoClient;

    private MongoDBConnection() {
        // Prevent object creation
    }

    // Get shared MongoClient
    public static synchronized MongoClient getClient() {

        if (mongoClient == null) {

            String uri = System.getenv("MONGODB_URI");

            if (uri == null || uri.isBlank()) {
                throw new IllegalStateException(
                        "MONGODB_URI environment variable is not set."
                );
            }

            mongoClient = MongoClients.create(uri);
        }

        return mongoClient;
    }

    // Get shared Database
    public static MongoDatabase getDatabase() {

        return getClient().getDatabase(DATABASE_NAME);
    }

    // Test MongoDB connection
    public static void ping() {

        getDatabase().runCommand(new Document("ping", 1));

        System.out.println("MongoDB Atlas connected successfully!");
        System.out.println("Database: " + DATABASE_NAME);
    }

    // Close shared MongoClient
    public static synchronized void close() {

        if (mongoClient != null) {

            mongoClient.close();
            mongoClient = null;

            System.out.println("MongoDB connection closed.");
        }
    }
}
