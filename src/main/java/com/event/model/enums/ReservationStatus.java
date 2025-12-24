package com.event.model.enums;

public enum ReservationStatus {
    EN_ATTENTE("En attente", "#F39C12", "⏳"),
    CONFIRMEE("Confirmée", "#27AE60", "✓"),
    ANNULEE("Annulée", "#E74C3C", "✗");

    private final String label;
    private final String color;
    private final String symbol;

    ReservationStatus(String label, String color, String symbol) {
        this.label = label;
        this.color = color;
        this.symbol = symbol;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isPending() {
        return this == EN_ATTENTE;
    }

    public boolean isConfirmed() {
        return this == CONFIRMEE;
    }

    public boolean isCancelled() {
        return this == ANNULEE;
    }
}