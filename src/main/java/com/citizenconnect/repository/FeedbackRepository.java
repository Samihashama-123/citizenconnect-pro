
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

public class FeedbackRepository {

    private static final String COLLECTION_NAME = "feedback";

    private static final List<String> FEEDBACK_STATUSES = List.of(
            "NEW",
            "REVIEWED",
            "RESOLVED"
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
    // SUBMIT FEEDBACK (CITIZEN)
    // =========================================

    public ObjectId submitFeedback(
            String userId,
            int rating,
            String subject,
            String message,
            String category
    ) {

        if (!ObjectId.isValid(userId)) {
            throw new IllegalArgumentException("Invalid user ID.");
        }

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5."
            );
        }

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Subject is required.");
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Feedback message is required."
            );
        }

        ObjectId citizenObjectId = new ObjectId(userId);

        if (!isActiveCitizen(citizenObjectId)) {
            throw new SecurityException(
                    "Only an active citizen can submit feedback."
            );
        }

        Date currentDate = new Date();

        Document feedback = new Document()
                .append("userId", citizenObjectId)
                .append("rating", rating)
                .append("subject", subject.trim())
                .append("message", message.trim())
                .append(
                        "category",
                        category == null ? "" : category.trim()
                )
                .append("status", "NEW")
                .append("submittedAt", currentDate)
                .append("updatedAt", currentDate);

        getCollection().insertOne(feedback);

        return feedback.getObjectId("_id");
    }

    // =========================================
    // GET CITIZEN'S FEEDBACK
    // =========================================

    public List<Document> getFeedbackByUserId(String userId) {

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
    // GET FEEDBACK BY ID (ADMIN)
    // =========================================

    public Document getFeedbackById(
            String feedbackId,
            String adminId
    ) {

        if (!ObjectId.isValid(feedbackId) ||
                !ObjectId.isValid(adminId)) {
            return null;
        }

        if (!isActiveAdmin(new ObjectId(adminId))) {
            throw new SecurityException(
                    "Only an active admin can view feedback details."
            );
        }

        return getCollection()
                .find(Filters.eq(
                        "_id",
                        new ObjectId(feedbackId)
                ))
                .first();
    }

    // =========================================
    // GET CITIZEN'S FEEDBACK BY ID
    // =========================================

    public Document getFeedbackByIdForUser(
            String feedbackId,
            String userId
    ) {

        if (!ObjectId.isValid(feedbackId) ||
                !ObjectId.isValid(userId)) {
            return null;
        }

        return getCollection()
                .find(Filters.and(
                        Filters.eq(
                                "_id",
                                new ObjectId(feedbackId)
                        ),
                        Filters.eq(
                                "userId",
                                new ObjectId(userId)
                        )
                ))
                .first();
    }

    // =========================================
    // LEGACY METHOD - USE VERIFIED VERSION
    // =========================================

    @Deprecated
    public Document getFeedbackById(String feedbackId) {

        throw new SecurityException(
                "Use getFeedbackById(feedbackId, adminId) " +
                "or getFeedbackByIdForUser(feedbackId, userId)."
        );
    }

    // =========================================
    // GET ALL FEEDBACK (ADMIN)
    // =========================================

    public List<Document> getAllFeedback(String adminId) {

        if (!ObjectId.isValid(adminId) ||
                !isActiveAdmin(new ObjectId(adminId))) {
            throw new SecurityException(
                    "Only an active admin can view all feedback."
            );
        }

        return getCollection()
                .find()
                .sort(Sorts.descending("submittedAt"))
                .into(new ArrayList<>());
    }

    // =========================================
    // LEGACY METHOD - USE VERIFIED VERSION
    // =========================================

    @Deprecated
    public List<Document> getAllFeedback() {

        throw new SecurityException(
                "Use getAllFeedback(adminId) instead."
        );
    }

    // =========================================
    // GET FEEDBACK BY RATING (ADMIN)
    // =========================================

    public List<Document> getFeedbackByRating(
            int rating,
            String adminId
    ) {

        if (!ObjectId.isValid(adminId) ||
                !isActiveAdmin(new ObjectId(adminId))) {
            throw new SecurityException(
                    "Only an active admin can filter feedback."
            );
        }

        if (rating < 1 || rating > 5) {
            return new ArrayList<>();
        }

        return getCollection()
                .find(Filters.eq("rating", rating))
                .sort(Sorts.descending("submittedAt"))
                .into(new ArrayList<>());
    }

    // =========================================
    // LEGACY METHOD - USE VERIFIED VERSION
    // =========================================

    @Deprecated
    public List<Document> getFeedbackByRating(int rating) {

        throw new SecurityException(
                "Use getFeedbackByRating(rating, adminId) instead."
        );
    }

    // =========================================
    // GET FEEDBACK BY STATUS (ADMIN)
    // =========================================

    public List<Document> getFeedbackByStatus(
            String status,
            String adminId
    ) {

        if (!ObjectId.isValid(adminId) ||
                !isActiveAdmin(new ObjectId(adminId))) {
            throw new SecurityException(
                    "Only an active admin can filter feedback."
            );
        }

        if (status == null || status.isBlank()) {
            return new ArrayList<>();
        }

        String normalizedStatus =
                status.trim().toUpperCase(Locale.ROOT);

        if (!FEEDBACK_STATUSES.contains(normalizedStatus)) {
            return new ArrayList<>();
        }

        return getCollection()
                .find(Filters.eq("status", normalizedStatus))
                .sort(Sorts.descending("submittedAt"))
                .into(new ArrayList<>());
    }

    // =========================================
    // LEGACY METHOD - USE VERIFIED VERSION
    // =========================================

    @Deprecated
    public List<Document> getFeedbackByStatus(String status) {

        throw new SecurityException(
                "Use getFeedbackByStatus(status, adminId) instead."
        );
    }

    // =========================================
    // UPDATE FEEDBACK STATUS (ADMIN)
    // =========================================

    public boolean updateFeedbackStatus(
            String feedbackId,
            String newStatus,
            String adminId
    ) {

        if (!ObjectId.isValid(feedbackId) ||
                !ObjectId.isValid(adminId)) {
            return false;
        }

        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException(
                    "Feedback status is required."
            );
        }

        String normalizedStatus =
                newStatus.trim().toUpperCase(Locale.ROOT);

        if (!FEEDBACK_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException(
                    "Invalid feedback status."
            );
        }

        ObjectId adminObjectId = new ObjectId(adminId);

        if (!isActiveAdmin(adminObjectId)) {
            throw new SecurityException(
                    "Only an active admin can update feedback status."
            );
        }

        var result = getCollection().updateOne(
                Filters.eq(
                        "_id",
                        new ObjectId(feedbackId)
                ),
                Updates.combine(
                        Updates.set("status", normalizedStatus),
                        Updates.set("reviewedBy", adminObjectId),
                        Updates.set("updatedAt", new Date())
                )
        );

        return result.getMatchedCount() > 0;
    }

    // =========================================
    // LEGACY METHOD - USE VERIFIED VERSION
    // =========================================

    @Deprecated
    public boolean updateFeedbackStatus(
            String feedbackId,
            String newStatus
    ) {

        throw new SecurityException(
                "Use updateFeedbackStatus(feedbackId, newStatus, adminId) instead."
        );
    }
}
