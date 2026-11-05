
package com.citizenconnect.config;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;

import java.util.HashSet;
import java.util.Set;

import static com.mongodb.client.model.Indexes.ascending;

public class DatabaseInitializer {

    private static final String[] COLLECTIONS = {
            "users",
            "services",
            "applications",
            "complaints",
            "important_information",
            "emergency_alerts",
            "volunteers",
            "feedback"
    };

    private DatabaseInitializer() {
    }

    public static void initialize() {

        MongoDatabase database = MongoDBConnection.getDatabase();

        Set<String> existingCollections = new HashSet<>();

        database.listCollectionNames().forEach(existingCollections::add);

        for (String name : COLLECTIONS) {

            if (!existingCollections.contains(name)) {
                database.createCollection(name);
                System.out.println("Created collection: " + name);
            }
        }

        createIndexes(database);

        System.out.println("Database initialization completed!");
    }

    /**
     * Ensures one volunteer document per citizen. If the old non-unique
     * userId_1 index exists, it is replaced only after checking for duplicates.
     */
    private static void ensureUniqueVolunteerUserIndex(
            MongoCollection<Document> volunteers
    ) {

        Document existingIndex = null;

        for (Document index : volunteers.listIndexes()) {
            if ("userId_1".equals(index.getString("name"))) {
                existingIndex = index;
                break;
            }
        }

        if (existingIndex != null &&
                !Boolean.TRUE.equals(existingIndex.getBoolean("unique"))) {

            Document duplicate = volunteers.aggregate(java.util.List.of(
                    new Document("$group",
                            new Document("_id", "$userId")
                                    .append("count", new Document("$sum", 1))),
                    new Document("$match",
                            new Document("count", new Document("$gt", 1)))
            )).first();

            if (duplicate != null) {
                throw new IllegalStateException(
                        "Cannot create unique volunteers.userId index: " +
                        "duplicate volunteer registrations exist for userId " +
                        duplicate.get("_id") +
                        ". Remove/reconcile duplicates in Atlas, then run initialization again."
                );
            }

            volunteers.dropIndex("userId_1");
        }

        volunteers.createIndex(
                ascending("userId"),
                new IndexOptions().unique(true)
        );
    }

    private static void createIndexes(MongoDatabase database) {

        // Users
        MongoCollection<Document> users =
                database.getCollection("users");

        users.createIndex(
                ascending("email"),
                new IndexOptions().unique(true)
        );

        // Services
        MongoCollection<Document> services =
                database.getCollection("services");

        services.createIndex(ascending("category"));
        services.createIndex(ascending("status"));

        // Applications
        MongoCollection<Document> applications =
                database.getCollection("applications");

        applications.createIndex(
                ascending("applicationNumber"),
                new IndexOptions().unique(true)
        );

        applications.createIndex(ascending("userId"));
        applications.createIndex(ascending("serviceId"));
        applications.createIndex(ascending("applicationStatus"));

        // Complaints
        MongoCollection<Document> complaints =
                database.getCollection("complaints");

        complaints.createIndex(
                ascending("complaintNumber"),
                new IndexOptions().unique(true)
        );

        complaints.createIndex(ascending("userId"));
        complaints.createIndex(ascending("status"));

        // Important Information
        MongoCollection<Document> information =
                database.getCollection("important_information");

        information.createIndex(ascending("status"));
        information.createIndex(ascending("publishedAt"));

        // Emergency Alerts
        MongoCollection<Document> alerts =
                database.getCollection("emergency_alerts");

        alerts.createIndex(ascending("status"));
        alerts.createIndex(ascending("severity"));

        // Volunteers
        MongoCollection<Document> volunteers =
                database.getCollection("volunteers");

        ensureUniqueVolunteerUserIndex(volunteers);
        volunteers.createIndex(ascending("status"));

        // Feedback
        MongoCollection<Document> feedback =
                database.getCollection("feedback");

        feedback.createIndex(ascending("userId"));
        feedback.createIndex(ascending("submittedAt"));
    }
}
