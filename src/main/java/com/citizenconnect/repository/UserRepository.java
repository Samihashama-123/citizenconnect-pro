package com.citizenconnect.repository;

import com.citizenconnect.config.MongoDBConnection;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

public class UserRepository {

    private static final String COLLECTION_NAME = "users";

    private static final int ITERATIONS = 210000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private static final int MIN_ITERATIONS = 100000;
    private static final int MAX_ITERATIONS = 2000000;

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    public static class DuplicateEmailException extends Exception {

        public DuplicateEmailException() {
            super("An account with this email already exists.");
        }
    }

    private MongoCollection<Document> getUsersCollection() {

        return MongoDBConnection.getDatabase()
                .getCollection(COLLECTION_NAME);
    }

    // =========================================
    // REGISTRATION VALIDATION
    // =========================================

    private void validateRegistration(
            String name,
            String email,
            String phone,
            String address,
            char[] password
    ) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Full name is required."
            );
        }

        if (email == null || email.isBlank()
                || !email.trim().matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )) {

            throw new IllegalArgumentException(
                    "Please provide a valid email address."
            );
        }

        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException(
                    "Phone number is required."
            );
        }

        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException(
                    "Address is required."
            );
        }

        if (password == null || password.length == 0) {
            throw new IllegalArgumentException(
                    "Password is required."
            );
        }
    }

    // =========================================
    // REGISTRATION
    // =========================================

    public void registerCitizen(
            String name,
            String email,
            String phone,
            String address,
            char[] password
    ) throws Exception {

        validateRegistration(
                name, email, phone, address, password
        );

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        byte[] salt = new byte[SALT_LENGTH];
        byte[] hash = null;

        try {

            SECURE_RANDOM.nextBytes(salt);

            hash = hashPassword(password, salt);

            Date currentDate = new Date();

            Document user = new Document()
                    .append("fullName", name.trim())
                    .append("email", normalizedEmail)
                    .append("phone", phone.trim())
                    .append("address", address.trim())
                    .append(
                            "passwordHash",
                            Base64.getEncoder().encodeToString(hash)
                    )
                    .append(
                            "passwordSalt",
                            Base64.getEncoder().encodeToString(salt)
                    )
                    .append("passwordIterations", ITERATIONS)
                    .append("role", "CITIZEN")
                    .append("accountStatus", "ACTIVE")
                    .append("createdAt", currentDate)
                    .append("updatedAt", currentDate);

            try {

                getUsersCollection().insertOne(user);

            } catch (MongoWriteException e) {

                if (e.getCode() == 11000) {
                    throw new DuplicateEmailException();
                }

                throw e;
            }

        } finally {

            if (hash != null) {
                Arrays.fill(hash, (byte) 0);
            }

            Arrays.fill(salt, (byte) 0);
        }
    }

    // =========================================
    // PASSWORD HASHING
    // =========================================

    private byte[] hashPassword(
            char[] password,
            byte[] salt
    ) throws Exception {

        PBEKeySpec spec = new PBEKeySpec(
                password,
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        try {

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

            return factory.generateSecret(spec).getEncoded();

        } finally {

            spec.clearPassword();
        }
    }

    // =========================================
    // AUTHENTICATED USER
    // =========================================

    public record AuthenticatedUser(
            String id,
            String fullName,
            String email,
            String role
    ) {

        public AuthenticatedUser(
                String fullName,
                String email,
                String role
        ) {
            this(null, fullName, email, role);
        }

        public String userId() {
            return id;
        }
    }

    // =========================================
    // LOGIN AUTHENTICATION
    // =========================================

    public AuthenticatedUser authenticate(
            String email,
            char[] password
    ) throws Exception {

        if (email == null || email.isBlank()
                || password == null || password.length == 0) {

            return null;
        }

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        Document user = getUsersCollection().find(
                Filters.eq("email", normalizedEmail)
        ).first();

        if (user == null) {
            return null;
        }

        String storedHash = user.getString("passwordHash");
        String storedSalt = user.getString("passwordSalt");
        Integer iterations = user.getInteger("passwordIterations");

        if (storedHash == null
                || storedSalt == null
                || iterations == null
                || iterations < MIN_ITERATIONS
                || iterations > MAX_ITERATIONS) {

            return null;
        }

        byte[] salt = null;
        byte[] expectedHash = null;
        byte[] calculatedHash = null;

        PBEKeySpec spec = null;

        try {

            salt = Base64.getDecoder().decode(storedSalt);
            expectedHash = Base64.getDecoder().decode(storedHash);

            if (salt.length != SALT_LENGTH
                    || expectedHash.length != KEY_LENGTH / 8) {

                return null;
            }

            spec = new PBEKeySpec(
                    password,
                    salt,
                    iterations,
                    expectedHash.length * 8
            );

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

            calculatedHash =
                    factory.generateSecret(spec).getEncoded();

            boolean passwordMatches =
                    MessageDigest.isEqual(
                            expectedHash,
                            calculatedHash
                    );

            if (!passwordMatches) {
                return null;
            }

        } catch (IllegalArgumentException e) {

            return null;

        } finally {

            if (spec != null) {
                spec.clearPassword();
            }

            if (salt != null) {
                Arrays.fill(salt, (byte) 0);
            }

            if (expectedHash != null) {
                Arrays.fill(expectedHash, (byte) 0);
            }

            if (calculatedHash != null) {
                Arrays.fill(calculatedHash, (byte) 0);
            }
        }

        String accountStatus = user.getString("accountStatus");

        if (accountStatus != null
                && !accountStatus.equalsIgnoreCase("ACTIVE")) {

            return null;
        }

        String role = user.getString("role");

        if (!"CITIZEN".equals(role)
                && !"ADMIN".equals(role)) {

            return null;
        }

        ObjectId userObjectId = user.getObjectId("_id");

        if (userObjectId == null) {
            return null;
        }

        return new AuthenticatedUser(
                userObjectId.toHexString(),
                user.getString("fullName"),
                user.getString("email"),
                role
        );
    }

    // =========================================
    // GET CITIZEN PROFILE
    // =========================================

    public Document getCitizenProfile(String userId) {

        if (userId == null || !ObjectId.isValid(userId)) {
            throw new IllegalArgumentException(
                    "Invalid user ID."
            );
        }

        return getUsersCollection()
                .find(
                        Filters.and(
                                Filters.eq("_id", new ObjectId(userId)),
                                Filters.eq("role", "CITIZEN")
                        )
                )
                .projection(
                        Projections.exclude(
                                "passwordHash",
                                "passwordSalt",
                                "passwordIterations"
                        )
                )
                .first();
    }

    // =========================================
    // UPDATE CITIZEN PROFILE
    // =========================================

    public void updateCitizenProfile(
            String userId,
            Document profileData
    ) {

        if (userId == null || !ObjectId.isValid(userId)) {
            throw new IllegalArgumentException(
                    "Invalid user ID."
            );
        }

        if (profileData == null) {
            throw new IllegalArgumentException(
                    "Profile information is required."
            );
        }

        String[] requiredFields = {
                "fullName",
                "phone",
                "dateOfBirth",
                "gender",
                "division",
                "district",
                "upazila",
                "detailedAddress",
                "preferredLanguage"
        };

        for (String field : requiredFields) {

            String value = profileData.getString(field);

            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(
                        field + " is required."
                );
            }
        }

        String fullName = profileData.getString("fullName").trim();
        String phone = profileData.getString("phone").trim();
        String detailedAddress =
                profileData.getString("detailedAddress").trim();

        String fullAddress = String.join(", ",
                detailedAddress,
                safeValue(profileData.getString("localArea")),
                safeValue(profileData.getString("upazila")),
                safeValue(profileData.getString("district")),
                safeValue(profileData.getString("division"))
        );

        Document updateData = new Document()
                .append("fullName", fullName)
                .append("phone", phone)
                .append("alternativePhone",
                        safeValue(profileData.getString("alternativePhone")))
                .append("dateOfBirth",
                        profileData.getString("dateOfBirth").trim())
                .append("gender",
                        profileData.getString("gender").trim())
                .append("division",
                        profileData.getString("division").trim())
                .append("district",
                        profileData.getString("district").trim())
                .append("upazila",
                        profileData.getString("upazila").trim())
                .append("localArea",
                        safeValue(profileData.getString("localArea")))
                .append("wardNumber",
                        safeValue(profileData.getString("wardNumber")))
                .append("detailedAddress", detailedAddress)
                .append("postalCode",
                        safeValue(profileData.getString("postalCode")))
                .append("nidNumber",
                        safeValue(profileData.getString("nidNumber")))
                .append("emergencyContactName",
                        safeValue(profileData.getString("emergencyContactName")))
                .append("emergencyContactNumber",
                        safeValue(profileData.getString("emergencyContactNumber")))
                .append("preferredLanguage",
                        profileData.getString("preferredLanguage").trim())
                .append("address", fullAddress)
                .append("updatedAt", new Date());

        Bson update = Updates.combine(
                Updates.set("fullName", updateData.getString("fullName")),
                Updates.set("phone", updateData.getString("phone")),
                Updates.set("alternativePhone", updateData.getString("alternativePhone")),
                Updates.set("dateOfBirth", updateData.getString("dateOfBirth")),
                Updates.set("gender", updateData.getString("gender")),
                Updates.set("division", updateData.getString("division")),
                Updates.set("district", updateData.getString("district")),
                Updates.set("upazila", updateData.getString("upazila")),
                Updates.set("localArea", updateData.getString("localArea")),
                Updates.set("wardNumber", updateData.getString("wardNumber")),
                Updates.set("detailedAddress", updateData.getString("detailedAddress")),
                Updates.set("postalCode", updateData.getString("postalCode")),
                Updates.set("nidNumber", updateData.getString("nidNumber")),
                Updates.set("emergencyContactName", updateData.getString("emergencyContactName")),
                Updates.set("emergencyContactNumber", updateData.getString("emergencyContactNumber")),
                Updates.set("preferredLanguage", updateData.getString("preferredLanguage")),
                Updates.set("address", updateData.getString("address")),
                Updates.set("updatedAt", new Date())
        );

        var result = getUsersCollection().updateOne(
                Filters.and(
                        Filters.eq("_id", new ObjectId(userId)),
                        Filters.eq("role", "CITIZEN")
                ),
                update
        );

        if (result.getMatchedCount() != 1) {
            throw new IllegalStateException(
                    "Citizen profile could not be found."
            );
        }
    }

    private String safeValue(String value) {

        return value == null ? "" : value.trim();
    }
}