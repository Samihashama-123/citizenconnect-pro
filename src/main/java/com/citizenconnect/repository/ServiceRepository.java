
package com.citizenconnect.repository;

import com.citizenconnect.config.MongoDBConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServiceRepository {

    private static final String COLLECTION_NAME = "services";

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
            throw new SecurityException("Active admin access required.");
        }
    }

    // =========================================
    // INPUT VALIDATION
    // =========================================

    private void validateServiceDetails(
            String serviceName,
            String category,
            String description,
            double applicationFee
    ) {

        if (serviceName == null || serviceName.isBlank()) {
            throw new IllegalArgumentException("Service name is required.");
        }

        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required.");
        }

        if (!Double.isFinite(applicationFee) || applicationFee < 0) {
            throw new IllegalArgumentException(
                    "Fee must be a valid non-negative amount."
            );
        }
    }

    private List<String> cleanRequirements(List<String> requirements) {

        List<String> cleaned = new ArrayList<>();

        if (requirements != null) {
            for (String requirement : requirements) {
                if (requirement != null && !requirement.isBlank()) {
                    cleaned.add(requirement.trim());
                }
            }
        }

        return cleaned;
    }

    // =========================================
    // ADD NEW SERVICE (ADMIN)
    // =========================================

    public ObjectId addService(
            String serviceName,
            String category,
            String description,
            List<String> requirements,
            double applicationFee,
            String processingTime,
            String officeName,
            String applicationInstructions,
            String createdBy
    ) {

        requireAdmin(createdBy);

        validateServiceDetails(
                serviceName, category, description, applicationFee
        );

        Date currentDate = new Date();

        Document service = new Document()
                .append("serviceName", serviceName.trim())
                .append("category", category.trim())
                .append("description", description.trim())
                .append("requirements", cleanRequirements(requirements))
                .append("applicationFee", applicationFee)
                .append("processingTime",
                        processingTime == null ? "" : processingTime.trim())
                .append("officeName",
                        officeName == null ? "" : officeName.trim())
                .append("applicationInstructions",
                        applicationInstructions == null
                                ? "" : applicationInstructions.trim())
                .append("status", "ACTIVE")
                .append("createdBy", new ObjectId(createdBy))
                .append("createdAt", currentDate)
                .append("updatedAt", currentDate);

        getCollection().insertOne(service);

        return service.getObjectId("_id");
    }

    // =========================================
    // GET ALL SERVICES (ADMIN)
    // =========================================

    public List<Document> getAllServices(String adminId) {

        requireAdmin(adminId);

        return getCollection()
                .find()
                .into(new ArrayList<>());
    }

    // Old method blocked for security.
    public List<Document> getAllServices() {
        throw new SecurityException(
                "Use getAllServices(adminId) instead."
        );
    }

    // =========================================
    // GET ACTIVE SERVICES (CITIZEN)
    // =========================================

    public List<Document> getActiveServices() {

        return getCollection()
                .find(Filters.eq("status", "ACTIVE"))
                .into(new ArrayList<>());
    }

    // =========================================
    // GET SERVICE BY ID (CITIZEN)
    // =========================================

    public Document getServiceById(String serviceId) {

        if (serviceId == null || !ObjectId.isValid(serviceId)) {
            return null;
        }

        return getCollection().find(
                Filters.and(
                        Filters.eq("_id", new ObjectId(serviceId)),
                        Filters.eq("status", "ACTIVE")
                )
        ).first();
    }

    // =========================================
    // GET SERVICE BY ID (ADMIN)
    // =========================================

    public Document getServiceById(
            String serviceId,
            String adminId
    ) {

        requireAdmin(adminId);

        if (serviceId == null || !ObjectId.isValid(serviceId)) {
            return null;
        }

        return getCollection().find(
                Filters.eq("_id", new ObjectId(serviceId))
        ).first();
    }

    // =========================================
    // UPDATE SERVICE DETAILS (ADMIN)
    // =========================================

    public boolean updateService(
            String serviceId,
            String serviceName,
            String category,
            String description,
            List<String> requirements,
            double applicationFee,
            String processingTime,
            String officeName,
            String applicationInstructions,
            String adminId
    ) {

        requireAdmin(adminId);

        if (serviceId == null || !ObjectId.isValid(serviceId)) {
            return false;
        }

        validateServiceDetails(
                serviceName, category, description, applicationFee
        );

        var result = getCollection().updateOne(
                Filters.eq("_id", new ObjectId(serviceId)),
                Updates.combine(
                        Updates.set("serviceName", serviceName.trim()),
                        Updates.set("category", category.trim()),
                        Updates.set("description", description.trim()),
                        Updates.set("requirements",
                                cleanRequirements(requirements)),
                        Updates.set("applicationFee", applicationFee),
                        Updates.set("processingTime",
                                processingTime == null
                                        ? "" : processingTime.trim()),
                        Updates.set("officeName",
                                officeName == null ? "" : officeName.trim()),
                        Updates.set("applicationInstructions",
                                applicationInstructions == null
                                        ? "" : applicationInstructions.trim()),
                        Updates.set("updatedAt", new Date())
                )
        );

        return result.getMatchedCount() > 0;
    }

    // Old method blocked for security.
    public boolean updateService(
            String serviceId,
            String serviceName,
            String category,
            String description,
            List<String> requirements,
            double applicationFee,
            String processingTime,
            String officeName,
            String applicationInstructions
    ) {
        throw new SecurityException(
                "Admin ID is required to update a service."
        );
    }

    // =========================================
    // CHANGE SERVICE STATUS (ADMIN)
    // =========================================

    public boolean updateServiceStatus(
            String serviceId,
            String status,
            String adminId
    ) {

        requireAdmin(adminId);

        if (serviceId == null || !ObjectId.isValid(serviceId)) {
            return false;
        }

        if (status == null) {
            throw new IllegalArgumentException("Status is required.");
        }

        String normalizedStatus = status.trim().toUpperCase();

        if (!normalizedStatus.equals("ACTIVE")
                && !normalizedStatus.equals("INACTIVE")) {
            throw new IllegalArgumentException("Invalid service status.");
        }

        var result = getCollection().updateOne(
                Filters.eq("_id", new ObjectId(serviceId)),
                Updates.combine(
                        Updates.set("status", normalizedStatus),
                        Updates.set("updatedAt", new Date())
                )
        );

        return result.getMatchedCount() > 0;
    }

    // Old method blocked for security.
    public boolean updateServiceStatus(
            String serviceId,
            String status
    ) {
        throw new SecurityException(
                "Admin ID is required to change service status."
        );
    }

    // =========================================
    // DELETE SERVICE (ADMIN)
    // =========================================

    public boolean deleteService(
            String serviceId,
            String adminId
    ) {

        requireAdmin(adminId);

        if (serviceId == null || !ObjectId.isValid(serviceId)) {
            return false;
        }

        ObjectId objectId = new ObjectId(serviceId);

        // Do not delete a service already used by applications.
        long applicationCount = MongoDBConnection.getDatabase()
                .getCollection("applications")
                .countDocuments(Filters.eq("serviceId", objectId));

        if (applicationCount > 0) {
            throw new IllegalStateException(
                    "This service has existing applications. " +
                    "Change its status to INACTIVE instead."
            );
        }

        var result = getCollection().deleteOne(
                Filters.eq("_id", objectId)
        );

        return result.getDeletedCount() > 0;
    }

    // Old method blocked for security.
    public boolean deleteService(String serviceId) {
        throw new SecurityException(
                "Admin ID is required to delete a service."
        );
    }
}
