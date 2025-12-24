package com.event.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateValidator {

    /**
     * Check if date is in the future
     */
    public boolean isFuture(LocalDateTime date) {
        return date != null && date.isAfter(LocalDateTime.now());
    }

    /**
     * Check if end date is after start date
     */
    public boolean isEndAfterStart(LocalDateTime start, LocalDateTime end) {
        return start != null && end != null && end.isAfter(start);
    }

    /**
     * Check if cancellation is allowed (48h before event)
     */
    public boolean canCancelReservation(LocalDateTime eventStartDate) {
        if (eventStartDate == null) {
            return false;
        }

        LocalDateTime cancellationDeadline = eventStartDate.minusHours(48);
        return LocalDateTime.now().isBefore(cancellationDeadline);
    }

    /**
     * Get hours until event
     */
    public long getHoursUntilEvent(LocalDateTime eventDate) {
        if (eventDate == null) {
            return -1;
        }

        return java.time.Duration.between(LocalDateTime.now(), eventDate).toHours();
    }

    /**
     * Check if event has passed
     */
    public boolean hasEventPassed(LocalDateTime eventEndDate) {
        return eventEndDate != null && eventEndDate.isBefore(LocalDateTime.now());
    }
}