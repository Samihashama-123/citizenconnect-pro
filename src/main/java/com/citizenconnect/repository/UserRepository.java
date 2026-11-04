
package com.citizenconnect.repository;

import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

import static com.mongodb.client.model.Indexes.ascending;

public class UserRepository {

    private static final String DATABASE_NAME = "citizenconnect_db";
    private static final String COLLECTION_NAME = "users";

    private static final int ITERATIONS = 210000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    public static class DuplicateEmailException extends Exception {
        public DuplicateEmailException() {
            super("An account with this email already exists.");
        }
    }

    public void registerCitizen(
            String name,
            String email,
            String phone,
            String address,
            char[] password
    ) throws Exception {

        String uri = System.getenv("MONGODB_URI");

        if (uri == null || uri.isBlank()) {
            throw new IllegalStateException(
                    "MONGODB_URI environment variable is missing."
            );
        }

        String normalizedEmail =
                email.trim().toLowerCase(java.util.Locale.ROOT);

        try (MongoClient client = MongoClients.create(uri)) {

            MongoDatabase database =
                    client.getDatabase(DATABASE_NAME);

            MongoCollection<Document> users =
                    database.getCollection(COLLECTION_NAME);

            // Enforce unique emails at database level.
            users.createIndex(
                    ascending("email"),
                    new IndexOptions().unique(true)
            );

            byte[] salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);

            byte[] hash = hashPassword(password, salt);

            try {
                Document user = new Document()
                        .append("fullName", name)
                        .append("email", normalizedEmail)
                        .append("phone", phone)
                        .append("address", address)
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
                        .append("createdAt", Date.from(Instant.now()));

                try {
                    users.insertOne(user);

                } catch (MongoWriteException e) {
                    if (e.getCode() == 11000) {
                        throw new DuplicateEmailException();
                    }
                    throw e;
                }

            } finally {
                Arrays.fill(hash, (byte) 0);
                Arrays.fill(salt, (byte) 0);
            }
        }
    }

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
    // LOGIN AUTHENTICATION
    // =========================================

    public record AuthenticatedUser(
            String fullName,
            String email,
            String role
    ) {}

    public AuthenticatedUser authenticate(
            String email,
            char[] password
    ) throws Exception {

        String uri = System.getenv("MONGODB_URI");

        if (uri == null || uri.isBlank()) {
            throw new IllegalStateException(
                    "MONGODB_URI environment variable is missing."
            );
        }

        String normalizedEmail =
                email.trim().toLowerCase(java.util.Locale.ROOT);

        try (MongoClient client = MongoClients.create(uri)) {

            MongoDatabase database =
                    client.getDatabase(DATABASE_NAME);

            MongoCollection<Document> users =
                    database.getCollection(COLLECTION_NAME);

            Document user = users.find(
                    new Document("email", normalizedEmail)
            ).first();

            if (user == null) {
                return null;
            }

            String storedHash = user.getString("passwordHash");
            String storedSalt = user.getString("passwordSalt");
            Integer iterations = user.getInteger("passwordIterations");

            if (storedHash == null ||
                    storedSalt == null ||
                    iterations == null ||
                    iterations < 100000) {
                return null;
            }

            byte[] salt =
                    Base64.getDecoder().decode(storedSalt);

            byte[] expectedHash =
                    Base64.getDecoder().decode(storedHash);

            byte[] calculatedHash = null;

            PBEKeySpec spec = new PBEKeySpec(
                    password,
                    salt,
                    iterations,
                    expectedHash.length * 8
            );

            boolean passwordMatches;

            try {

                SecretKeyFactory factory =
                        SecretKeyFactory.getInstance(
                                "PBKDF2WithHmacSHA256"
                        );

                calculatedHash =
                        factory.generateSecret(spec).getEncoded();

                passwordMatches =
                        java.security.MessageDigest.isEqual(
                                expectedHash,
                                calculatedHash
                        );

            } finally {

                spec.clearPassword();

                Arrays.fill(salt, (byte) 0);
                Arrays.fill(expectedHash, (byte) 0);

                if (calculatedHash != null) {
                    Arrays.fill(calculatedHash, (byte) 0);
                }
            }

            if (!passwordMatches) {
                return null;
            }

            return new AuthenticatedUser(
                    user.getString("fullName"),
                    user.getString("email"),
                    user.getString("role")
            );
        }
    }

}
