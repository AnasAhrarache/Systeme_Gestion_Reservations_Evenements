package com.event.model.enums;

public enum UserRole {
    ADMIN("Administrateur", "#FF6B6B"),
    ORGANIZER("Organisateur", "#4ECDC4"),
    CLIENT("Client", "#95E1D3");

    private final String label;
    private final String color;

    UserRole(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isOrganizer() {
        return this == ORGANIZER || this == ADMIN;
    }

    public boolean isClient() {
        return this == CLIENT;
    }
}