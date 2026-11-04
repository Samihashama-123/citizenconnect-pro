
package com.citizenconnect.repository;

import com.citizenconnect.config.MongoDBConnection;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
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

        // Backward-compatible constructor
        public AuthenticatedUser(
                String fullName,
                String email,
                String role
        ) {
            this(null, fullName, email, role);
        }

        // Alternative readable user ID accessor
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

            // Invalid Base64 credentials
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

        // =====================================
        // ACCOUNT STATUS CHECK
        // =====================================

        String accountStatus = user.getString("accountStatus");

        // Older accounts without accountStatus remain ACTIVE.
        if (accountStatus != null
                && !accountStatus.equalsIgnoreCase("ACTIVE")) {

            return null;
        }

        // =====================================
        // ROLE CHECK
        // =====================================

        String role = user.getString("role");

        if (!"CITIZEN".equals(role)
                && !"ADMIN".equals(role)) {

            return null;
        }

        // =====================================
        // RETURN AUTHENTICATED USER
        // =====================================

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
}
