package com.event.model.enums;

public enum EventStatus {
    BROUILLON("Brouillon", "#95A5A6", false),
    PUBLIE("Publié", "#2ECC71", true),
    ANNULE("Annulé", "#E74C3C", false),
    TERMINE("Terminé", "#34495E", false);

    private final String label;
    private final String color;
    private final boolean bookable;

    EventStatus(String label, String color, boolean bookable) {
        this.label = label;
        this.color = color;
        this.bookable = bookable;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }

    public boolean isBookable() {
        return bookable;
    }

    public boolean isDraft() {
        return this == BROUILLON;
    }

    public boolean isPublished() {
        return this == PUBLIE;
    }

    public boolean isCancelled() {
        return this == ANNULE;
    }

    public boolean isFinished() {
        return this == TERMINE;
    }
}