
package com.citizenconnect.config;

import com.citizenconnect.repository.UserRepository;

public class UserSession {

    private static UserRepository.AuthenticatedUser currentUser;

    private UserSession() {
    }

    public static void setCurrentUser(
            UserRepository.AuthenticatedUser user
    ) {

        if (user == null || user.id() == null) {
            throw new IllegalArgumentException(
                    "Valid authenticated user is required."
            );
        }

        currentUser = user;
    }

    public static UserRepository.AuthenticatedUser getCurrentUser() {
        return currentUser;
    }

    public static String getUserId() {
        return currentUser == null ? null : currentUser.id();
    }

    public static String getUserName() {
        return currentUser == null ? null : currentUser.fullName();
    }

    public static String getUserEmail() {
        return currentUser == null ? null : currentUser.email();
    }

    public static String getUserRole() {
        return currentUser == null ? null : currentUser.role();
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null
                && "ADMIN".equals(currentUser.role());
    }

    public static boolean isCitizen() {
        return currentUser != null
                && "CITIZEN".equals(currentUser.role());
    }

    public static void clearSession() {
        currentUser = null;
    }
}
