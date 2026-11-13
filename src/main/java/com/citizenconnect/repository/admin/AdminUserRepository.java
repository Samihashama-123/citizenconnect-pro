
package com.citizenconnect.repository.admin;

import com.citizenconnect.config.MongoDBConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class AdminUserRepository {

    private final MongoDatabase database;

    public AdminUserRepository() {
        this.database = MongoDBConnection.getDatabase();
    }

    // Safe user information for Admin Panel.
    // Password hash and salt are never returned.
    public record UserSummary(
            String id,
            String fullName,
            String email,
            String phone,
            String role,
            String accountStatus,
            Date createdAt
    ) {}

    // Verify active Admin account.
    private void verifyAdmin(String adminId) {

        if (adminId == null || !ObjectId.isValid(adminId)) {
            throw new SecurityException("Invalid Admin account.");
        }

        MongoCollection<Document> users =
                database.getCollection("users");

        Document admin = users.find(
                Filters.and(
                        Filters.eq("_id", new ObjectId(adminId)),
                        Filters.eq("role", "ADMIN"),
                        Filters.or(
                                Filters.eq("accountStatus", "ACTIVE"),
                                Filters.eq("accountStatus", null)
                        )
                )
        ).first();

        if (admin == null) {
            throw new SecurityException(
                    "Only an active Admin can manage users."
            );
        }
    }

    // Get all users or search by name/email.
    public List<UserSummary> getAllUsers(
            String adminId,
            String searchText
    ) {

        verifyAdmin(adminId);

        MongoCollection<Document> users =
                database.getCollection("users");

        List<Document> documents;

        // If search is empty, get all users.
        if (searchText == null || searchText.isBlank()) {

            documents = users.find()
                    .sort(Sorts.descending("createdAt"))
                    .into(new ArrayList<>());

        } else {

            // Search by full name or email.
            String search = Pattern.quote(searchText.trim());

            documents = users.find(
                    Filters.or(
                            Filters.regex("fullName", search, "i"),
                            Filters.regex("email", search, "i")
                    )
            )
            .sort(Sorts.descending("createdAt"))
            .into(new ArrayList<>());
        }

        List<UserSummary> result = new ArrayList<>();

        // Convert MongoDB documents into safe user records.
        for (Document user : documents) {

            ObjectId id = user.getObjectId("_id");

            String status = user.getString("accountStatus");

            // Older accounts without status are treated as ACTIVE.
            if (status == null || status.isBlank()) {
                status = "ACTIVE";
            }

            result.add(
                    new UserSummary(
                            id == null ? "" : id.toHexString(),
                            user.getString("fullName"),
                            user.getString("email"),
                            user.getString("phone"),
                            user.getString("role"),
                            status,
                            user.getDate("createdAt")
                    )
            );
        }

        return result;
    }

    // Activate or deactivate a Citizen account.
    public boolean updateCitizenStatus(
            String userId,
            String newStatus,
            String adminId
    ) {

        verifyAdmin(adminId);

        if (userId == null || !ObjectId.isValid(userId)) {
            throw new IllegalArgumentException("Invalid user ID.");
        }

        if (!"ACTIVE".equals(newStatus)
                && !"INACTIVE".equals(newStatus)) {

            throw new IllegalArgumentException(
                    "Status must be ACTIVE or INACTIVE."
            );
        }

        // Admin cannot change their own account status.
        if (userId.equals(adminId)) {
            throw new SecurityException(
                    "You cannot change your own Admin account status."
            );
        }

        MongoCollection<Document> users =
                database.getCollection("users");

        var result = users.updateOne(
                Filters.and(
                        Filters.eq("_id", new ObjectId(userId)),
                        Filters.eq("role", "CITIZEN")
                ),
                Updates.combine(
                        Updates.set("accountStatus", newStatus),
                        Updates.set("updatedAt", new Date())
                )
        );

        return result.getModifiedCount() > 0;
    }
}
