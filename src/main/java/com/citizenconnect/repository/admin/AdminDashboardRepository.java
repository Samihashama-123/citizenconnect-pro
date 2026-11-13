
package com.citizenconnect.repository.admin;

import com.citizenconnect.config.MongoDBConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardRepository {

    private final MongoDatabase database;

    public AdminDashboardRepository() {
        this.database = MongoDBConnection.getDatabase();
    }

    public record DashboardStats(
            long totalCitizens,
            long totalServices,
            long totalApplications,
            long pendingApplications,
            long totalComplaints,
            long unresolvedComplaints,
            long totalVolunteers
    ) {}

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
                    "Only an active Admin can access dashboard statistics."
            );
        }
    }

    public DashboardStats getDashboardStats(String adminId) {

        verifyAdmin(adminId);

        MongoCollection<Document> users =
                database.getCollection("users");

        MongoCollection<Document> services =
                database.getCollection("services");

        MongoCollection<Document> applications =
                database.getCollection("applications");

        MongoCollection<Document> complaints =
                database.getCollection("complaints");

        MongoCollection<Document> volunteers =
                database.getCollection("volunteers");

        long totalCitizens = users.countDocuments(
                Filters.eq("role", "CITIZEN")
        );

        long totalServices = services.countDocuments();

        long totalApplications = applications.countDocuments();

        long pendingApplications = applications.countDocuments(
                Filters.in(
                        "applicationStatus",
                        "SUBMITTED",
                        "UNDER_REVIEW",
                        "PENDING_DOCUMENTS"
                )
        );

        long totalComplaints = complaints.countDocuments();

        long unresolvedComplaints = complaints.countDocuments(
                Filters.in(
                        "status",
                        "SUBMITTED",
                        "UNDER_REVIEW",
                        "IN_PROGRESS"
                )
        );

        long totalVolunteers = volunteers.countDocuments();

        return new DashboardStats(
                totalCitizens,
                totalServices,
                totalApplications,
                pendingApplications,
                totalComplaints,
                unresolvedComplaints,
                totalVolunteers
        );
    }

    public List<Document> getRecentApplications(String adminId) {

        verifyAdmin(adminId);

        return database.getCollection("applications")
                .find()
                .sort(Sorts.descending("submittedAt"))
                .limit(5)
                .into(new ArrayList<>());
    }

    public List<Document> getRecentComplaints(String adminId) {

        verifyAdmin(adminId);

        return database.getCollection("complaints")
                .find()
                .sort(Sorts.descending("submittedAt"))
                .limit(5)
                .into(new ArrayList<>());
    }
}
