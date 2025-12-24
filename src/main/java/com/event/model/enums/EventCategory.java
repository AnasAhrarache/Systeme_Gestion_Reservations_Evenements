package com.event.model.enums;

import com.vaadin.flow.component.icon.VaadinIcon;

public enum EventCategory {
    CONCERT("Concert", VaadinIcon.MUSIC, "#E74C3C"),
    THEATRE("Théâtre", VaadinIcon.TICKET, "#9B59B6"),
    CONFERENCE("Conférence", VaadinIcon.PRESENTATION, "#3498DB"),
    SPORT("Sport", VaadinIcon.TROPHY, "#2ECC71"),
    AUTRE("Autre", VaadinIcon.CALENDAR, "#95A5A6");

    private final String label;
    private final VaadinIcon icon;
    private final String color;

    EventCategory(String label, VaadinIcon icon, String color) {
        this.label = label;
        this.icon = icon;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public VaadinIcon getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }
}