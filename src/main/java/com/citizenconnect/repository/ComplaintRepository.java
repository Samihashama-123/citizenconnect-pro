
package com.citizenconnect.repository;

import com.citizenconnect.config.MongoDBConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ComplaintRepository {

    private static final String COLLECTION_NAME = "complaints";

    private static final List<String> PRIORITIES = List.of(
            "LOW",
            "MEDIUM",
            "HIGH"
    );

    private static final List<String> COMPLAINT_STATUSES = List.of(
            "SUBMITTED",
            "UNDER_REVIEW",
            "IN_PROGRESS",
            "RESOLVED",
            "REJECTED"
    );

    private MongoCollection<Document> getCollection() {
        return MongoDBConnection.getDatabase()
                .getCollection(COLLECTION_NAME);
    }

    private MongoCollection<Document> getUsersCollection() {
        return MongoDBConnection.getDatabase()
                .getCollection("users");
    }

    // =========================================
    // CHECK ACTIVE CITIZEN
    // =========================================

    private boolean isActiveCitizen(ObjectId userId) {

        Document user = getUsersCollection().find(
                Filters.eq("_id", userId)
        ).first();

        if (user == null ||
                !"CITIZEN".equals(user.getString("role"))) {
            return false;
        }

        String accountStatus = user.getString("accountStatus");

        return accountStatus == null ||
                "ACTIVE".equals(accountStatus);
    }

    // =========================================
    // CHECK ACTIVE ADMIN
    // =========================================

    private boolean isActiveAdmin(ObjectId adminId) {

        Document admin = getUsersCollection().find(
                Filters.eq("_id", adminId)
        ).first();

        if (admin == null ||
                !"ADMIN".equals(admin.getString("role"))) {
            return false;
        }

        String accountStatus = admin.getString("accountStatus");

        return accountStatus == null ||
                "ACTIVE".equals(accountStatus);
    }

    // =========================================
    // SUBMIT COMPLAINT (CITIZEN)
    // =========================================

    public String submitComplaint(
            String userId,
            String subject,
            String category,
            String description,
            String location,
            List<Document> attachments,
            String priority
    ) {

        if (!ObjectId.isValid(userId)) {
            throw new IllegalArgumentException("Invalid user ID.");
        }

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "Complaint subject is required."
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Complaint description is required."
            );
        }

        if (priority == null || priority.isBlank()) {
            throw new IllegalArgumentException(
                    "Complaint priority is required."
            );
        }

        String normalizedPriority =
                priority.trim().toUpperCase(Locale.ROOT);

        if (!PRIORITIES.contains(normalizedPriority)) {
            throw new IllegalArgumentException(
                    "Invalid complaint priority."
            );
        }

        ObjectId citizenObjectId = new ObjectId(userId);

        if (!isActiveCitizen(citizenObjectId)) {
            throw new IllegalStateException(
                    "Active citizen account is required."
            );
        }

        Date currentDate = new Date();

        String complaintNumber =
                "CMP-" + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase(Locale.ROOT);

        Document historyEntry = new Document()
                .append("status", "SUBMITTED")
                .append("remarks", "Complaint submitted.")
                .append("changedBy", citizenObjectId)
                .append("changedAt", currentDate);

        Document complaint = new Document()
                .append("complaintNumber", complaintNumber)
                .append("userId", citizenObjectId)
                .append("subject", subject.trim())
                .append(
                        "category",
                        category == null ? "" : category.trim()
                )
                .append("description", description.trim())
                .append(
                        "location",
                        location == null ? "" : location.trim()
                )
                .append(
                        "attachments",
                        attachments == null
                                ? new ArrayList<>()
                                : new ArrayList<>(attachments)
                )
                .append("priority", normalizedPriority)
                .append("status", "SUBMITTED")
                .append("adminResponse", "")
                .append("submittedAt", currentDate)
                .append("updatedAt", currentDate)
                .append("statusHistory", List.of(historyEntry));

        getCollection().insertOne(complaint);

        return complaintNumber;
    }

    // =========================================
    // GET CITIZEN'S COMPLAINTS
    // =========================================

    public List<Document> getComplaintsByUserId(String userId) {

        if (!ObjectId.isValid(userId)) {
            return new ArrayList<>();
        }

        return getCollection()
                .find(Filters.eq(
                        "userId",
                        new ObjectId(userId)
                ))
                .sort(Sorts.descending("submittedAt"))
                .into(new ArrayList<>());
    }

    // =========================================
    // GET COMPLAINT BY NUMBER (ADMIN USE)
    // =========================================

    public Document getComplaintByNumber(String complaintNumber) {

        if (complaintNumber == null ||
                complaintNumber.isBlank()) {
            return null;
        }

        return getCollection()
                .find(Filters.eq(
                        "complaintNumber",
                        complaintNumber.trim().toUpperCase(Locale.ROOT)
                ))
                .first();
    }

    // =========================================
    // GET CITIZEN COMPLAINT BY NUMBER
    // =========================================

    public Document getComplaintByNumberForUser(
            String complaintNumber,
            String userId
    ) {

        if (complaintNumber == null ||
                complaintNumber.isBlank() ||
                !ObjectId.isValid(userId)) {
            return null;
        }

        return getCollection()
                .find(Filters.and(
                        Filters.eq(
                                "complaintNumber",
                                complaintNumber.trim().toUpperCase(Locale.ROOT)
                        ),
                        Filters.eq("userId", new ObjectId(userId))
                ))
                .first();
    }

    // =========================================
    // GET COMPLAINT BY ID (ADMIN USE)
    // =========================================

    public Document getComplaintById(String complaintId) {

        if (!ObjectId.isValid(complaintId)) {
            return null;
        }

        return getCollection()
                .find(Filters.eq(
                        "_id",
                        new ObjectId(complaintId)
                ))
                .first();
    }

    // =========================================
    // GET CITIZEN COMPLAINT BY ID
    // =========================================

    public Document getComplaintByIdForUser(
            String complaintId,
            String userId
    ) {

        if (!ObjectId.isValid(complaintId) ||
                !ObjectId.isValid(userId)) {
            return null;
        }

        return getCollection()
                .find(Filters.and(
                        Filters.eq(
                                "_id",
                                new ObjectId(complaintId)
                        ),
                        Filters.eq(
                                "userId",
                                new ObjectId(userId)
                        )
                ))
                .first();
    }

    // =========================================
    // GET ALL COMPLAINTS (ADMIN)
    // =========================================

    public List<Document> getAllComplaints() {

        return getCollection()
                .find()
                .sort(Sorts.descending("submittedAt"))
                .into(new ArrayList<>());
    }

    // =========================================
    // GET ALL COMPLAINTS (VERIFIED ADMIN)
    // =========================================

    public List<Document> getAllComplaints(String adminId) {

        if (!ObjectId.isValid(adminId) ||
                !isActiveAdmin(new ObjectId(adminId))) {
            throw new SecurityException(
                    "Only an active admin can view all complaints."
            );
        }

        return getAllComplaints();
    }

    // =========================================
    // GET COMPLAINTS BY STATUS (ADMIN)
    // =========================================

    public List<Document> getComplaintsByStatus(String status) {

        if (status == null || status.isBlank()) {
            return new ArrayList<>();
        }

        String normalizedStatus =
                status.trim().toUpperCase(Locale.ROOT);

        if (!COMPLAINT_STATUSES.contains(normalizedStatus)) {
            return new ArrayList<>();
        }

        return getCollection()
                .find(Filters.eq("status", normalizedStatus))
                .sort(Sorts.descending("submittedAt"))
                .into(new ArrayList<>());
    }

    // =========================================
    // GET COMPLAINTS BY STATUS (VERIFIED ADMIN)
    // =========================================

    public List<Document> getComplaintsByStatus(
            String status,
            String adminId
    ) {

        if (!ObjectId.isValid(adminId) ||
                !isActiveAdmin(new ObjectId(adminId))) {
            throw new SecurityException(
                    "Only an active admin can view complaints by status."
            );
        }

        return getComplaintsByStatus(status);
    }

    // =========================================
    // UPDATE COMPLAINT STATUS (ADMIN)
    // =========================================

    public boolean updateComplaintStatus(
            String complaintId,
            String newStatus,
            String adminId,
            String adminResponse
    ) {

        if (!ObjectId.isValid(complaintId) ||
                !ObjectId.isValid(adminId)) {
            return false;
        }

        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException(
                    "Complaint status is required."
            );
        }

        String normalizedStatus =
                newStatus.trim().toUpperCase(Locale.ROOT);

        if (!COMPLAINT_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException(
                    "Invalid complaint status."
            );
        }

        ObjectId adminObjectId = new ObjectId(adminId);

        if (!isActiveAdmin(adminObjectId)) {
            throw new SecurityException(
                    "Only an active admin can update complaint status."
            );
        }

        Date currentDate = new Date();

        String normalizedResponse =
                adminResponse == null ? "" : adminResponse.trim();

        Document historyEntry = new Document()
                .append("status", normalizedStatus)
                .append("remarks", normalizedResponse)
                .append("changedBy", adminObjectId)
                .append("changedAt", currentDate);

        var updates = Updates.combine(
                Updates.set("status", normalizedStatus),
                Updates.set("adminResponse", normalizedResponse),
                Updates.set("reviewedBy", adminObjectId),
                Updates.set("updatedAt", currentDate),
                Updates.push("statusHistory", historyEntry)
        );

        if ("RESOLVED".equals(normalizedStatus)) {

            updates = Updates.combine(
                    updates,
                    Updates.set("resolvedAt", currentDate)
            );

        } else {

            updates = Updates.combine(
                    updates,
                    Updates.unset("resolvedAt")
            );
        }

        var result = getCollection().updateOne(
                Filters.eq(
                        "_id",
                        new ObjectId(complaintId)
                ),
                updates
        );

        return result.getMatchedCount() > 0;
    }
}
