
package com.citizenconnect.repository;

import com.citizenconnect.config.MongoDBConnection;
import com.mongodb.MongoWriteException;
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

public class VolunteerRepository {

    private static final String COLLECTION_NAME = "volunteers";

    private static final List<String> VOLUNTEER_STATUSES = List.of(
            "PENDING",
            "APPROVED",
            "REJECTED",
            "INACTIVE"
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
    // VOLUNTEER REGISTRATION (CITIZEN)
    // =========================================

    public ObjectId registerVolunteer(
            String userId,
            String fullName,
            String email,
            String phone,
            List<String> skills,
            String availability,
            String address
    ) {

        if (!ObjectId.isValid(userId)) {
            throw new IllegalArgumentException("Invalid user ID.");
        }

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required.");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number is required.");
        }

        ObjectId citizenObjectId = new ObjectId(userId);

        if (!isActiveCitizen(citizenObjectId)) {
            throw new SecurityException(
                    "Only an active citizen can register as a volunteer."
            );
        }

        Date currentDate = new Date();

        Document volunteer = new Document()
                .append("userId", citizenObjectId)
                .append("fullName", fullName.trim())
                .append(
                        "email",
                        email.trim().toLowerCase(Locale.ROOT)
                )
                .append("phone", phone.trim())
                .append(
                        "skills",
                        skills == null
                                ? new ArrayList<>()
                                : new ArrayList<>(skills)
                )
                .append(
                        "availability",
                        availability == null ? "" : availability.trim()
                )
                .append(
                        "address",
                        address == null ? "" : address.trim()
                )
                .append("status", "PENDING")
                .append("registeredAt", currentDate)
                .append("updatedAt", currentDate);

        try {

            getCollection().insertOne(volunteer);

        } catch (MongoWriteException e) {

            // MongoDB unique userId index prevents duplicate
            // registration, including simultaneous requests.
            if (e.getCode() == 11000) {
                throw new IllegalStateException(
                        "You have already registered as a volunteer.",
                        e
                );
            }

            throw e;
        }

        return volunteer.getObjectId("_id");
    }

    // =========================================
    // GET VOLUNTEER PROFILE BY USER ID
    // =========================================

    public Document getVolunteerByUserId(String userId) {

        if (!ObjectId.isValid(userId)) {
            return null;
        }

        return getCollection()
                .find(Filters.eq(
                        "userId",
                        new ObjectId(userId)
                ))
                .first();
    }

    // =========================================
    // GET VOLUNTEER BY ID (ADMIN)
    // =========================================

    public Document getVolunteerById(
            String volunteerId,
            String adminId
    ) {

        if (!ObjectId.isValid(volunteerId) ||
                !ObjectId.isValid(adminId)) {
            return null;
        }

        if (!isActiveAdmin(new ObjectId(adminId))) {
            throw new SecurityException(
                    "Only an active admin can view volunteer details."
            );
        }

        return getCollection()
                .find(Filters.eq(
                        "_id",
                        new ObjectId(volunteerId)
                ))
                .first();
    }

    // =========================================
    // LEGACY METHOD - USE ADMIN-VERIFIED VERSION
    // =========================================

    @Deprecated
    public Document getVolunteerById(String volunteerId) {

        throw new SecurityException(
                "Use getVolunteerById(volunteerId, adminId) instead."
        );
    }

    // =========================================
    // GET ALL VOLUNTEERS (ADMIN)
    // =========================================

    public List<Document> getAllVolunteers(String adminId) {

        if (!ObjectId.isValid(adminId) ||
                !isActiveAdmin(new ObjectId(adminId))) {
            throw new SecurityException(
                    "Only an active admin can view all volunteers."
            );
        }

        return getCollection()
                .find()
                .sort(Sorts.descending("registeredAt"))
                .into(new ArrayList<>());
    }

    // =========================================
    // LEGACY METHOD - USE ADMIN-VERIFIED VERSION
    // =========================================

    @Deprecated
    public List<Document> getAllVolunteers() {

        throw new SecurityException(
                "Use getAllVolunteers(adminId) instead."
        );
    }

    // =========================================
    // GET VOLUNTEERS BY STATUS (ADMIN)
    // =========================================

    public List<Document> getVolunteersByStatus(
            String status,
            String adminId
    ) {

        if (!ObjectId.isValid(adminId) ||
                !isActiveAdmin(new ObjectId(adminId))) {
            throw new SecurityException(
                    "Only an active admin can view volunteers by status."
            );
        }

        if (status == null || status.isBlank()) {
            return new ArrayList<>();
        }

        String normalizedStatus =
                status.trim().toUpperCase(Locale.ROOT);

        if (!VOLUNTEER_STATUSES.contains(normalizedStatus)) {
            return new ArrayList<>();
        }

        return getCollection()
                .find(Filters.eq("status", normalizedStatus))
                .sort(Sorts.descending("registeredAt"))
                .into(new ArrayList<>());
    }

    // =========================================
    // LEGACY METHOD - USE ADMIN-VERIFIED VERSION
    // =========================================

    @Deprecated
    public List<Document> getVolunteersByStatus(String status) {

        throw new SecurityException(
                "Use getVolunteersByStatus(status, adminId) instead."
        );
    }

    // =========================================
    // UPDATE VOLUNTEER PROFILE (CITIZEN)
    // =========================================

    public boolean updateVolunteerProfile(
            String userId,
            String fullName,
            String phone,
            List<String> skills,
            String availability,
            String address
    ) {

        if (!ObjectId.isValid(userId)) {
            return false;
        }

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException(
                    "Full name is required."
            );
        }

        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException(
                    "Phone number is required."
            );
        }

        ObjectId citizenObjectId = new ObjectId(userId);

        if (!isActiveCitizen(citizenObjectId)) {
            throw new SecurityException(
                    "Only an active citizen can update a volunteer profile."
            );
        }

        var result = getCollection().updateOne(
                Filters.eq("userId", citizenObjectId),
                Updates.combine(
                        Updates.set("fullName", fullName.trim()),
                        Updates.set("phone", phone.trim()),
                        Updates.set(
                                "skills",
                                skills == null
                                        ? new ArrayList<>()
                                        : new ArrayList<>(skills)
                        ),
                        Updates.set(
                                "availability",
                                availability == null ? "" : availability.trim()
                        ),
                        Updates.set(
                                "address",
                                address == null ? "" : address.trim()
                        ),
                        Updates.set("updatedAt", new Date())
                )
        );

        return result.getMatchedCount() > 0;
    }

    // =========================================
    // UPDATE VOLUNTEER STATUS (ADMIN)
    // =========================================

    public boolean updateVolunteerStatus(
            String volunteerId,
            String newStatus,
            String adminId
    ) {

        if (!ObjectId.isValid(volunteerId) ||
                !ObjectId.isValid(adminId)) {
            return false;
        }

        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException(
                    "Volunteer status is required."
            );
        }

        String normalizedStatus =
                newStatus.trim().toUpperCase(Locale.ROOT);

        if (!VOLUNTEER_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException(
                    "Invalid volunteer status."
            );
        }

        if (!isActiveAdmin(new ObjectId(adminId))) {
            throw new SecurityException(
                    "Only an active admin can update volunteer status."
            );
        }

        var result = getCollection().updateOne(
                Filters.eq(
                        "_id",
                        new ObjectId(volunteerId)
                ),
                Updates.combine(
                        Updates.set("status", normalizedStatus),
                        Updates.set("reviewedBy", new ObjectId(adminId)),
                        Updates.set("updatedAt", new Date())
                )
        );

        return result.getMatchedCount() > 0;
    }

    // =========================================
    // LEGACY METHOD - USE ADMIN-VERIFIED VERSION
    // =========================================

    @Deprecated
    public boolean updateVolunteerStatus(
            String volunteerId,
            String newStatus
    ) {

        throw new SecurityException(
                "Use updateVolunteerStatus(volunteerId, newStatus, adminId) instead."
        );
    }
}
