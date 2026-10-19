
package com.citizenconnect.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBConnection {

    private static final String DATABASE_NAME = "citizenconnect_db";

    public static void main(String[] args) {

        String uri = System.getenv("MONGODB_URI");

        if (uri == null || uri.isBlank()) {
            System.err.println("MONGODB_URI environment variable is not set.");
            return;
        }

        try (MongoClient client = MongoClients.create(uri)) {

            MongoDatabase database = client.getDatabase(DATABASE_NAME);

            database.runCommand(new Document("ping", 1));

            System.out.println("MongoDB Atlas connected successfully!");
            System.out.println("Database: " + database.getName());

        } catch (Exception e) {
            System.err.println("MongoDB connection failed!");
            System.err.println(e.getMessage());
        }
    }
}
