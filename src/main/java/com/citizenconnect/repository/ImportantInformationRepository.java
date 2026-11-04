
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

public class ImportantInformationRepository {

    private static final String COLLECTION_NAME =
            "important_information";

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

    private void validateInformation(
            String title,
            String category,
            String description
    ) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Title is required."
            );
        }

        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException(
                    "Category is required."
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Description is required."
            );
        }
    }

    // =========================================
    // PUBLISH NEW INFORMATION (ADMIN)
    // =========================================

    public ObjectId publishInformation(
            String title,
            String category,
            String description,
            String adminId
    ) {

        requireAdmin(adminId);

        validateInformation(title, category, description);

        Date currentDate = new Date();

        Document information = new Document()
                .append("title", title.trim())
                .append("category", category.trim())
                .append("description", description.trim())
                .append("publishedBy", new ObjectId(adminId))
                .append("status", "PUBLISHED")
                .append("publishedAt", currentDate)
                .append("updatedAt", currentDate);

        getCollection().insertOne(information);

        return information.getObjectId("_id");
    }

    // =========================================
    // GET ALL INFORMATION (ADMIN)
    // =========================================

    public List<Document> getAllInformation(String adminId) {

        requireAdmin(adminId);

        return getCollection()
                .find()
                .sort(Sorts.descending("publishedAt"))
                .into(new ArrayList<>());
    }

    // Old method blocked for security.
    public List<Document> getAllInformation() {

        throw new SecurityException(
                "Admin ID is required to view all information."
        );
    }

    // =========================================
    // GET PUBLISHED INFORMATION (CITIZEN)
    // =========================================

    public List<Document> getPublishedInformation() {

        return getCollection()
                .find(Filters.eq("status", "PUBLISHED"))
                .sort(Sorts.descending("publishedAt"))
                .into(new ArrayList<>());
    }

    // =========================================
    // GET INFORMATION BY CATEGORY (CITIZEN)
    // =========================================

    public List<Document> getInformationByCategory(
            String category
    ) {

        if (category == null || category.isBlank()) {
            return new ArrayList<>();
        }

        return getCollection()
                .find(Filters.and(
                        Filters.eq("category", category.trim()),
                        Filters.eq("status", "PUBLISHED")
                ))
                .sort(Sorts.descending("publishedAt"))
                .into(new ArrayList<>());
    }

    // =========================================
    // GET INFORMATION BY ID (CITIZEN)
    // =========================================

    public Document getInformationById(String informationId) {

        if (informationId == null ||
                !ObjectId.isValid(informationId)) {
            return null;
        }

        return getCollection()
                .find(Filters.and(
                        Filters.eq(
                                "_id",
                                new ObjectId(informationId)
                        ),
                        Filters.eq("status", "PUBLISHED")
                ))
                .first();
    }

    // =========================================
    // GET INFORMATION BY ID (ADMIN)
    // =========================================

    public Document getInformationById(
            String informationId,
            String adminId
    ) {

        requireAdmin(adminId);

        if (informationId == null ||
                !ObjectId.isValid(informationId)) {
            return null;
        }

        return getCollection()
                .find(Filters.eq(
                        "_id",
                        new ObjectId(informationId)
                ))
                .first();
    }

    // =========================================
    // UPDATE INFORMATION (ADMIN)
    // =========================================

    public boolean updateInformation(
            String informationId,
            String title,
            String category,
            String description,
            String adminId
    ) {

        requireAdmin(adminId);

        if (informationId == null ||
                !ObjectId.isValid(informationId)) {
            return false;
        }

        validateInformation(title, category, description);

        var result = getCollection().updateOne(
                Filters.eq(
                        "_id",
                        new ObjectId(informationId)
                ),
                Updates.combine(
                        Updates.set("title", title.trim()),
                        Updates.set("category", category.trim()),
                        Updates.set("description", description.trim()),
                        Updates.set("updatedAt", new Date())
                )
        );

        return result.getMatchedCount() > 0;
    }

    // Old method blocked for security.
    public boolean updateInformation(
            String informationId,
            String title,
            String category,
            String description
    ) {

        throw new SecurityException(
                "Admin ID is required to update information."
        );
    }

    // =========================================
    // UPDATE INFORMATION STATUS (ADMIN)
    // =========================================

    public boolean updateInformationStatus(
            String informationId,
            String newStatus,
            String adminId
    ) {

        requireAdmin(adminId);

        if (informationId == null ||
                !ObjectId.isValid(informationId)) {
            return false;
        }

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Information status is required."
            );
        }

        String normalizedStatus =
                newStatus.trim().toUpperCase();

        if (!List.of("PUBLISHED", "UNPUBLISHED")
                .contains(normalizedStatus)) {

            throw new IllegalArgumentException(
                    "Invalid information status."
            );
        }

        var result = getCollection().updateOne(
                Filters.eq(
                        "_id",
                        new ObjectId(informationId)
                ),
                Updates.combine(
                        Updates.set("status", normalizedStatus),
                        Updates.set("updatedAt", new Date())
                )
        );

        return result.getMatchedCount() > 0;
    }

    // Old method blocked for security.
    public boolean updateInformationStatus(
            String informationId,
            String newStatus
    ) {

        throw new SecurityException(
                "Admin ID is required to update information status."
        );
    }

    // =========================================
    // DELETE INFORMATION (ADMIN)
    // =========================================

    public boolean deleteInformation(
            String informationId,
            String adminId
    ) {

        requireAdmin(adminId);

        if (informationId == null ||
                !ObjectId.isValid(informationId)) {
            return false;
        }

        var result = getCollection().deleteOne(
                Filters.eq(
                        "_id",
                        new ObjectId(informationId)
                )
        );

        return result.getDeletedCount() > 0;
    }

    // Old method blocked for security.
    public boolean deleteInformation(String informationId) {

        throw new SecurityException(
                "Admin ID is required to delete information."
        );
    }
}
