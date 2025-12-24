package com.event.security;

import com.event.model.entities.User;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SessionManager {

    private static final String USER_SESSION_KEY = "CURRENT_USER";

    /**
     * Store user in session
     */
    public void setCurrentUser(User user) {
        VaadinSession.getCurrent().setAttribute(USER_SESSION_KEY, user);
    }

    /**
     * Get current user from session
     */
    public Optional<User> getCurrentUser() {
        if (VaadinSession.getCurrent() == null) {
            return Optional.empty();
        }

        Object userObj = VaadinSession.getCurrent().getAttribute(USER_SESSION_KEY);
        if (userObj instanceof User) {
            return Optional.of((User) userObj);
        }
        return Optional.empty();
    }

    /**
     * Check if user is logged in
     */
    public boolean isUserLoggedIn() {
        return getCurrentUser().isPresent();
    }

    /**
     * Logout current user
     */
    public void logout() {
        if (VaadinSession.getCurrent() != null) {
            VaadinSession.getCurrent().setAttribute(USER_SESSION_KEY, null);
            VaadinSession.getCurrent().close();
        }
    }

    /**
     * Require authentication
     */
    public User requireAuthentication() {
        return getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Authentification requise"));
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return getCurrentUser()
                .map(user -> user.getRole().isAdmin())
                .orElse(false);
    }

    /**
     * Check if current user is organizer
     */
    public boolean isOrganizer() {
        return getCurrentUser()
                .map(user -> user.getRole().isOrganizer())
                .orElse(false);
    }

    /**
     * Check if current user is client
     */
    public boolean isClient() {
        return getCurrentUser()
                .map(user -> user.getRole().isClient())
                .orElse(false);
    }
}