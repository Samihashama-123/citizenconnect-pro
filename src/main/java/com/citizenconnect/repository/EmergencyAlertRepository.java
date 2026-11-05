
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

public class EmergencyAlertRepository {

    private static final String COLLECTION_NAME =
            "emergency_alerts";

    private MongoCollection<Document> getCollection() {
        return MongoDBConnection.getDatabase()
                .getCollection(COLLECTION_NAME);
    }

    private MongoCollection<Document> getUsersCollection() {
        return MongoDBConnection.getDatabase()
                .getCollection("users");
    }

    // =========================================
    // ADMIN VERIFICATION
    // =========================================

    private boolean isActiveAdmin(String adminId) {

        if (adminId == null || !ObjectId.isValid(adminId)) {
            return false;
        }

        Document admin = getUsersCollection().find(
                Filters.and(
                        Filters.eq("_id", new ObjectId(adminId)),
                        Filters.eq("role", "ADMIN"),
                        Filters.or(
                                Filters.eq("accountStatus", "ACTIVE"),
                                Filters.exists("accountStatus", false),
                                Filters.eq("accountStatus", null)
                        )
                )
        ).first();

        return admin != null;
    }

    private void requireAdmin(String adminId) {

        if (!isActiveAdmin(adminId)) {
            throw new SecurityException(
                    "Active admin access required."
            );
        }
    }

    // =========================================
    // INPUT VALIDATION
    // =========================================

    private void validateAlert(
            String title,
            String alertType,
            String description,
            String severity
    ) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Alert title is required."
            );
        }

        if (alertType == null || alertType.isBlank()) {
            throw new IllegalArgumentException(
                    "Alert type is required."
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Alert description is required."
            );
        }

        if (severity == null) {
            throw new IllegalArgumentException(
                    "Alert severity is required."
            );
        }

        String normalizedSeverity = severity.trim().toUpperCase();

        if (!List.of("LOW", "MEDIUM", "HIGH", "CRITICAL")
                .contains(normalizedSeverity)) {

            throw new IllegalArgumentException(
                    "Invalid alert severity."
            );
        }
    }

    private String cleanLocation(String location) {
        return location == null ? "" : location.trim();
    }

    // =========================================
    // CREATE EMERGENCY ALERT (ADMIN)
    // =========================================

    public ObjectId createAlert(
            String title,
            String alertType,
            String description,
            String location,
            String severity,
            String adminId
    ) {

        requireAdmin(adminId);

        validateAlert(title, alertType, description, severity);

        Date currentDate = new Date();

        Document alert = new Document()
                .append("title", title.trim())
                .append("alertType", alertType.trim())
                .append("description", description.trim())
                .append("location", cleanLocation(location))
                .append("severity", severity.trim().toUpperCase())
                .append("status", "ACTIVE")
                .append("createdBy", new ObjectId(adminId))
                .append("createdAt", currentDate)
                .append("updatedAt", currentDate);

        getCollection().insertOne(alert);

        return alert.getObjectId("_id");
    }

    // =========================================
    // GET ALL ALERTS (ADMIN)
    // =========================================

    public List<Document> getAllAlerts(String adminId) {

        requireAdmin(adminId);

        return getCollection()
                .find()
                .sort(Sorts.descending("createdAt"))
                .into(new ArrayList<>());
    }

    // Old method blocked for security.
    public List<Document> getAllAlerts() {

        throw new SecurityException(
                "Admin ID is required to view all alerts."
        );
    }

    // =========================================
    // GET ACTIVE ALERTS (CITIZEN)
    // =========================================

    public List<Document> getActiveAlerts() {

        return getCollection()
                .find(Filters.eq("status", "ACTIVE"))
                .sort(Sorts.descending("createdAt"))
                .into(new ArrayList<>());
    }

    // =========================================
    // GET ALERT BY ID (CITIZEN)
    // =========================================

    public Document getAlertById(String alertId) {

        if (alertId == null || !ObjectId.isValid(alertId)) {
            return null;
        }

        return getCollection()
                .find(Filters.and(
                        Filters.eq("_id", new ObjectId(alertId)),
                        Filters.eq("status", "ACTIVE")
                ))
                .first();
    }

    // =========================================
    // GET ALERT BY ID (ADMIN)
    // =========================================

    public Document getAlertById(
            String alertId,
            String adminId
    ) {

        requireAdmin(adminId);

        if (alertId == null || !ObjectId.isValid(alertId)) {
            return null;
        }

        return getCollection()
                .find(Filters.eq(
                        "_id",
                        new ObjectId(alertId)
                ))
                .first();
    }

    // =========================================
    // GET ACTIVE ALERTS BY SEVERITY (CITIZEN)
    // =========================================

    public List<Document> getAlertsBySeverity(String severity) {

        if (severity == null || severity.isBlank()) {
            return new ArrayList<>();
        }

        String normalizedSeverity = severity.trim().toUpperCase();

        if (!List.of("LOW", "MEDIUM", "HIGH", "CRITICAL")
                .contains(normalizedSeverity)) {
            return new ArrayList<>();
        }

        return getCollection()
                .find(Filters.and(
                        Filters.eq("severity", normalizedSeverity),
                        Filters.eq("status", "ACTIVE")
                ))
                .sort(Sorts.descending("createdAt"))
                .into(new ArrayList<>());
    }

    // =========================================
    // UPDATE ALERT DETAILS (ADMIN)
    // =========================================

    public boolean updateAlert(
            String alertId,
            String title,
            String alertType,
            String description,
            String location,
            String severity,
            String adminId
    ) {

        requireAdmin(adminId);

        if (alertId == null || !ObjectId.isValid(alertId)) {
            return false;
        }

        validateAlert(title, alertType, description, severity);

        var result = getCollection().updateOne(
                Filters.eq("_id", new ObjectId(alertId)),
                Updates.combine(
                        Updates.set("title", title.trim()),
                        Updates.set("alertType", alertType.trim()),
                        Updates.set("description", description.trim()),
                        Updates.set("location", cleanLocation(location)),
                        Updates.set("severity", severity.trim().toUpperCase()),
                        Updates.set("updatedAt", new Date())
                )
        );

        return result.getMatchedCount() > 0;
    }

    // Old method blocked for security.
    public boolean updateAlert(
            String alertId,
            String title,
            String alertType,
            String description,
            String location,
            String severity
    ) {

        throw new SecurityException(
                "Admin ID is required to update an alert."
        );
    }

    // =========================================
    // UPDATE ALERT STATUS (ADMIN)
    // =========================================

    public boolean updateAlertStatus(
            String alertId,
            String newStatus,
            String adminId
    ) {

        requireAdmin(adminId);

        if (alertId == null || !ObjectId.isValid(alertId)) {
            return false;
        }

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Alert status is required."
            );
        }

        String normalizedStatus = newStatus.trim().toUpperCase();

        if (!List.of("ACTIVE", "INACTIVE")
                .contains(normalizedStatus)) {

            throw new IllegalArgumentException(
                    "Invalid alert status."
            );
        }

        var result = getCollection().updateOne(
                Filters.eq("_id", new ObjectId(alertId)),
                Updates.combine(
                        Updates.set("status", normalizedStatus),
                        Updates.set("updatedAt", new Date())
                )
        );

        return result.getMatchedCount() > 0;
    }

    // Old method blocked for security.
    public boolean updateAlertStatus(
            String alertId,
            String newStatus
    ) {

        throw new SecurityException(
                "Admin ID is required to update alert status."
        );
    }

    // =========================================
    // DELETE ALERT (ADMIN)
    // =========================================

    public boolean deleteAlert(
            String alertId,
            String adminId
    ) {

        requireAdmin(adminId);

        if (alertId == null || !ObjectId.isValid(alertId)) {
            return false;
        }

        var result = getCollection().deleteOne(
                Filters.eq("_id", new ObjectId(alertId))
        );

        return result.getDeletedCount() > 0;
    }

    // Old method blocked for security.
    public boolean deleteAlert(String alertId) {

        throw new SecurityException(
                "Admin ID is required to delete an alert."
        );
    }
}
