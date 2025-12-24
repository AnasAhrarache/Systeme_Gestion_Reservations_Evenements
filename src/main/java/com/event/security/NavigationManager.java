package com.event.security;

import com.event.model.entities.User;
import com.event.model.enums.UserRole;
import com.vaadin.flow.component.UI;
import org.springframework.stereotype.Component;

@Component
public class NavigationManager {

    private final SessionManager sessionManager;

    public NavigationManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Navigate to home page based on user role
     */
    public void navigateToHome() {
        User user = sessionManager.getCurrentUser().orElse(null);

        if (user == null) {
            navigateTo("login");
            return;
        }

        switch (user.getRole()) {
            case ADMIN:
                navigateTo("admin/dashboard");
                break;
            case ORGANIZER:
                navigateTo("organizer/dashboard");
                break;
            case CLIENT:
                navigateTo("dashboard");
                break;
            default:
                navigateTo("");
        }
    }

    /**
     * Navigate to a specific route
     */
    public void navigateTo(String route) {
        UI.getCurrent().navigate(route);
    }

    /**
     * Navigate to event details
     */
    public void navigateToEventDetails(Long eventId) {
        navigateTo("event/" + eventId);
    }

    /**
     * Navigate to event reservation
     */
    public void navigateToReservation(Long eventId) {
        if (!sessionManager.isUserLoggedIn()) {
            navigateTo("login");
            return;
        }
        navigateTo("event/" + eventId + "/reserve");
    }

    /**
     * Navigate to login
     */
    public void navigateToLogin() {
        navigateTo("login");
    }

    /**
     * Navigate to register
     */
    public void navigateToRegister() {
        navigateTo("register");
    }

    /**
     * Navigate to events list
     */
    public void navigateToEvents() {
        navigateTo("events");
    }

    /**
     * Navigate to my reservations
     */
    public void navigateToMyReservations() {
        navigateTo("my-reservations");
    }

    /**
     * Navigate to profile
     */
    public void navigateToProfile() {
        navigateTo("profile");
    }

    /**
     * Navigate to organizer events
     */
    public void navigateToMyEvents() {
        navigateTo("organizer/events");
    }

    /**
     * Navigate to create event
     */
    public void navigateToCreateEvent() {
        navigateTo("organizer/event/new");
    }

    /**
     * Navigate to edit event
     */
    public void navigateToEditEvent(Long eventId) {
        navigateTo("organizer/event/edit/" + eventId);
    }

    /**
     * Navigate to organizer reservations
     */
    public void navigateToOrganizerReservations() {
        navigateTo("organizer/reservations");
    }

    /**
     * Navigate back
     */
    public void navigateBack() {
        UI.getCurrent().getPage().getHistory().back();
    }

    /**
     * Check if user has access to route
     */
    public boolean hasAccess(String route, User user) {
        if (user == null) {
            return isPublicRoute(route);
        }

        UserRole role = user.getRole();

        if (route.startsWith("admin/")) {
            return role == UserRole.ADMIN;
        }

        if (route.startsWith("organizer/")) {
            return role == UserRole.ORGANIZER || role == UserRole.ADMIN;
        }

        return true;
    }

    /**
     * Check if route is public (no authentication required)
     */
    private boolean isPublicRoute(String route) {
        return route.equals("") ||
                route.equals("login") ||
                route.equals("register") ||
                route.equals("events") ||
                route.startsWith("event/") && !route.contains("/reserve");
    }
}