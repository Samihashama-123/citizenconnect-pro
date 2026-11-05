
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

public class ApplicationRepository {

    private static final String COLLECTION_NAME = "applications";

    private static final List<String> APPLICATION_STATUSES = List.of(
            "SUBMITTED",
            "UNDER_REVIEW",
            "PENDING_DOCUMENTS",
            "APPROVED",
            "REJECTED"
    );

    private static final List<String> PAYMENT_STATUSES = List.of(
            "UNPAID",
            "PAID",
            "FAILED"
    );

    private MongoCollection<Document> getCollection() {
        return MongoDBConnection.getDatabase()
                .getCollection(COLLECTION_NAME);
    }

    private MongoCollection<Document> getUsersCollection() {
        return MongoDBConnection.getDatabase()
                .getCollection("users");
    }

    private MongoCollection<Document> getServicesCollection() {
        return MongoDBConnection.getDatabase()
                .getCollection("services");
    }

    // =========================================
    // CHECK ACTIVE CITIZEN
    // =========================================

    private boolean isActiveCitizen(ObjectId userId) {

        Document user = getUsersCollection().find(
                Filters.eq("_id", userId)
        ).first();

        if (user == null) {
            return false;
        }

        if (!"CITIZEN".equals(user.getString("role"))) {
            return false;
        }

        String status = user.getString("accountStatus");

        return status == null || "ACTIVE".equals(status);
    }

    // =========================================
    // CHECK ACTIVE ADMIN
    // =========================================

    private boolean isActiveAdmin(ObjectId adminId) {

        Document admin = getUsersCollection().find(
                Filters.eq("_id", adminId)
        ).first();

        if (admin == null) {
            return false;
        }

        if (!"ADMIN".equals(admin.getString("role"))) {
            return false;
        }

        String status = admin.getString("accountStatus");

        return status == null || "ACTIVE".equals(status);
    }

    // =========================================
    // SUBMIT NEW APPLICATION (CITIZEN)
    // =========================================

    public String submitApplication(
            String userId,
            String serviceId,
            String applicantName,
            String applicantEmail,
            String applicantPhone,
            Document formData,
            List<Document> documents,
            double feeAmount
    ) {

        if (!ObjectId.isValid(userId) ||
                !ObjectId.isValid(serviceId)) {
            throw new IllegalArgumentException(
                    "Invalid user or service ID."
            );
        }

        if (applicantName == null || applicantName.isBlank()) {
            throw new IllegalArgumentException(
                    "Applicant name is required."
            );
        }

        if (applicantEmail == null || applicantEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Applicant email is required."
            );
        }

        if (applicantPhone == null || applicantPhone.isBlank()) {
            throw new IllegalArgumentException(
                    "Applicant phone is required."
            );
        }

        if (feeAmount < 0) {
            throw new IllegalArgumentException(
                    "Fee cannot be negative."
            );
        }

        ObjectId citizenObjectId = new ObjectId(userId);
        ObjectId serviceObjectId = new ObjectId(serviceId);

        // Verify citizen account
        if (!isActiveCitizen(citizenObjectId)) {
            throw new IllegalStateException(
                    "Active citizen account is required."
            );
        }

        // Verify service exists and is active
        Document service = getServicesCollection().find(
                Filters.and(
                        Filters.eq("_id", serviceObjectId),
                        Filters.eq("status", "ACTIVE")
                )
        ).first();

        if (service == null) {
            throw new IllegalStateException(
                    "Service does not exist or is inactive."
            );
        }

        // Always use the actual fee stored in MongoDB.
        // Do not trust the fee sent by the UI.
        Object feeValue = service.get("applicationFee");

        if (!(feeValue instanceof Number)) {
            throw new IllegalStateException(
                    "Service application fee is missing or invalid."
            );
        }

        double actualFee = ((Number) feeValue).doubleValue();

        if (!Double.isFinite(actualFee) || actualFee < 0) {
            throw new IllegalStateException(
                    "Invalid service application fee."
            );
        }

        Date currentDate = new Date();

        String applicationNumber =
                "APP-" + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase(Locale.ROOT);

        Document statusEntry = new Document()
                .append("status", "SUBMITTED")
                .append("remarks", "Application submitted.")
                .append("changedBy", citizenObjectId)
                .append("changedAt", currentDate);

        Document application = new Document()
                .append("applicationNumber", applicationNumber)
                .append("userId", citizenObjectId)
                .append("serviceId", serviceObjectId)
                .append("applicantName", applicantName.trim())
                .append(
                        "applicantEmail",
                        applicantEmail.trim().toLowerCase(Locale.ROOT)
                )
                .append("applicantPhone", applicantPhone.trim())
                .append(
                        "formData",
                        formData == null
                                ? new Document()
                                : new Document(formData)
                )
                .append(
                        "documents",
                        documents == null
                                ? new ArrayList<>()
                                : new ArrayList<>(documents)
                )
                .append("feeAmount", actualFee)
                .append("paymentStatus", "UNPAID")
                .append("applicationStatus", "SUBMITTED")
                .append("submittedAt", currentDate)
                .append("updatedAt", currentDate)
                .append("statusHistory", List.of(statusEntry));

        getCollection().insertOne(application);

        return applicationNumber;
    }

    // =========================================
    // GET CITIZEN'S APPLICATIONS
    // =========================================

    public List<Document> getApplicationsByUserId(String userId) {

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
    // GET APPLICATION BY NUMBER
    // ADMIN USE
    // =========================================

    public Document getApplicationByNumber(String applicationNumber) {

        if (applicationNumber == null ||
                applicationNumber.isBlank()) {
            return null;
        }

        return getCollection()
                .find(Filters.eq(
                        "applicationNumber",
                        applicationNumber.trim().toUpperCase(Locale.ROOT)
                ))
                .first();
    }

    // =========================================
    // GET CITIZEN APPLICATION BY NUMBER
    // =========================================

    public Document getApplicationByNumberForUser(
            String applicationNumber,
            String userId
    ) {

        if (applicationNumber == null ||
                applicationNumber.isBlank() ||
                !ObjectId.isValid(userId)) {
            return null;
        }

        return getCollection()
                .find(Filters.and(
                        Filters.eq(
                                "applicationNumber",
                                applicationNumber.trim().toUpperCase(Locale.ROOT)
                        ),
                        Filters.eq("userId", new ObjectId(userId))
                ))
                .first();
    }

    // =========================================
    // GET APPLICATION BY ID
    // ADMIN USE
    // =========================================

    public Document getApplicationById(String applicationId) {

        if (!ObjectId.isValid(applicationId)) {
            return null;
        }

        return getCollection()
                .find(Filters.eq(
                        "_id",
                        new ObjectId(applicationId)
                ))
                .first();
    }

    // =========================================
    // GET CITIZEN APPLICATION BY ID
    // =========================================

    public Document getApplicationByIdForUser(
            String applicationId,
            String userId
    ) {

        if (!ObjectId.isValid(applicationId) ||
                !ObjectId.isValid(userId)) {
            return null;
        }

        return getCollection()
                .find(Filters.and(
                        Filters.eq(
                                "_id",
                                new ObjectId(applicationId)
                        ),
                        Filters.eq(
                                "userId",
                                new ObjectId(userId)
                        )
                ))
                .first();
    }

    // =========================================
    // GET ALL APPLICATIONS (ADMIN)
    // =========================================

    public List<Document> getAllApplications() {

        return getCollection()
                .find()
                .sort(Sorts.descending("submittedAt"))
                .into(new ArrayList<>());
    }

    // =========================================
    // GET APPLICATIONS BY STATUS (ADMIN)
    // =========================================

    public List<Document> getApplicationsByStatus(String status) {

        if (status == null || status.isBlank()) {
            return new ArrayList<>();
        }

        String normalizedStatus =
                status.trim().toUpperCase(Locale.ROOT);

        if (!APPLICATION_STATUSES.contains(normalizedStatus)) {
            return new ArrayList<>();
        }

        return getCollection()
                .find(Filters.eq(
                        "applicationStatus",
                        normalizedStatus
                ))
                .sort(Sorts.descending("submittedAt"))
                .into(new ArrayList<>());
    }

    // =========================================
    // UPDATE APPLICATION STATUS (ADMIN)
    // =========================================

    public boolean updateApplicationStatus(
            String applicationId,
            String newStatus,
            String adminId,
            String remarks
    ) {

        if (!ObjectId.isValid(applicationId) ||
                !ObjectId.isValid(adminId)) {
            return false;
        }

        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException(
                    "Application status is required."
            );
        }

        String normalizedStatus =
                newStatus.trim().toUpperCase(Locale.ROOT);

        if (!APPLICATION_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException(
                    "Invalid application status."
            );
        }

        ObjectId adminObjectId = new ObjectId(adminId);

        if (!isActiveAdmin(adminObjectId)) {
            throw new SecurityException(
                    "Only an active admin can update application status."
            );
        }

        Date currentDate = new Date();

        Document historyEntry = new Document()
                .append("status", normalizedStatus)
                .append("remarks", remarks == null ? "" : remarks.trim())
                .append("changedBy", adminObjectId)
                .append("changedAt", currentDate);

        var result = getCollection().updateOne(
                Filters.eq(
                        "_id",
                        new ObjectId(applicationId)
                ),
                Updates.combine(
                        Updates.set(
                                "applicationStatus",
                                normalizedStatus
                        ),
                        Updates.set("updatedAt", currentDate),
                        Updates.set("reviewedBy", adminObjectId),
                        Updates.set(
                                "adminRemarks",
                                remarks == null ? "" : remarks.trim()
                        ),
                        Updates.push("statusHistory", historyEntry)
                )
        );

        return result.getMatchedCount() > 0;
    }

    // =========================================
    // UPDATE PAYMENT STATUS
    // =========================================

    public boolean updatePaymentStatus(
            String applicationId,
            String paymentStatus
    ) {

        if (!ObjectId.isValid(applicationId)) {
            return false;
        }

        if (paymentStatus == null || paymentStatus.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment status is required."
            );
        }

        String normalizedStatus =
                paymentStatus.trim().toUpperCase(Locale.ROOT);

        if (!PAYMENT_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException(
                    "Invalid payment status."
            );
        }

        var result = getCollection().updateOne(
                Filters.eq(
                        "_id",
                        new ObjectId(applicationId)
                ),
                Updates.combine(
                        Updates.set(
                                "paymentStatus",
                                normalizedStatus
                        ),
                        Updates.set("updatedAt", new Date())
                )
        );

        return result.getMatchedCount() > 0;
    }
}
