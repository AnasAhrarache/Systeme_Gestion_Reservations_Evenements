package com.event.service;

import com.event.dto.EventDTO;
import com.event.exception.BadRequestException;
import com.event.exception.BusinessException;
import com.event.exception.ForbiddenException;
import com.event.exception.ResourceNotFoundException;
import com.event.model.entities.Event;
import com.event.model.entities.User;
import com.event.model.enums.EventCategory;
import com.event.model.enums.EventStatus;
import com.event.model.enums.ReservationStatus;
import com.event.model.enums.UserRole;
import com.event.repository.EventRepository;
import com.event.repository.ReservationRepository;
import com.event.util.DateValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final DateValidator dateValidator;

    public EventService(EventRepository eventRepository,
                        ReservationRepository reservationRepository,
                        DateValidator dateValidator) {
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.dateValidator = dateValidator;
    }

    /**
     * Create a new event
     */
    public Event createEvent(Event event, User organizer) {
        // Validate organizer role
        if (!organizer.getRole().isOrganizer()) {
            throw new ForbiddenException("Seuls les organisateurs et administrateurs peuvent créer des événements");
        }

        // Validate dates
        validateEventDates(event);

        // Validate required fields
        validateEventFields(event);

        // Set organizer
        event.setOrganisateur(organizer);

        // Set default status
        if (event.getStatut() == null) {
            event.setStatut(EventStatus.BROUILLON);
        }

        return eventRepository.save(event);
    }

    /**
     * Update event
     */
    public Event updateEvent(Long eventId, Event updatedEvent, User currentUser) {
        Event event = findById(eventId);

        // Check permissions
        validateUpdatePermissions(event, currentUser);

        // Check if event can be modified
        if (!event.canBeModified()) {
            throw new BusinessException("Cet événement ne peut plus être modifié");
        }

        // Validate dates if changed
        if (!event.getDateDebut().equals(updatedEvent.getDateDebut()) ||
                !event.getDateFin().equals(updatedEvent.getDateFin())) {
            validateEventDates(updatedEvent);
        }

        // Update fields
        event.setTitre(updatedEvent.getTitre());
        event.setDescription(updatedEvent.getDescription());
        event.setCategorie(updatedEvent.getCategorie());
        event.setDateDebut(updatedEvent.getDateDebut());
        event.setDateFin(updatedEvent.getDateFin());
        event.setLieu(updatedEvent.getLieu());
        event.setVille(updatedEvent.getVille());
        event.setCapaciteMax(updatedEvent.getCapaciteMax());
        event.setPrixUnitaire(updatedEvent.getPrixUnitaire());
        event.setImageUrl(updatedEvent.getImageUrl());

        return eventRepository.save(event);
    }

    /**
     * Publish event
     */
    public Event publishEvent(Long eventId, User currentUser) {
        Event event = findById(eventId);

        // Check permissions
        validateUpdatePermissions(event, currentUser);

        // Validate event is complete
        if (!isEventComplete(event)) {
            throw new BusinessException("L'événement doit avoir toutes les informations requises avant d'être publié");
        }

        // Validate dates
        if (!dateValidator.isFuture(event.getDateDebut())) {
            throw new BusinessException("La date de début doit être dans le futur");
        }

        event.setStatut(EventStatus.PUBLIE);
        return eventRepository.save(event);
    }

    /**
     * Cancel event
     */
    public Event cancelEvent(Long eventId, User currentUser) {
        Event event = findById(eventId);

        // Check permissions
        validateUpdatePermissions(event, currentUser);

        if (event.getStatut() == EventStatus.TERMINE) {
            throw new BusinessException("Un événement terminé ne peut pas être annulé");
        }

        event.setStatut(EventStatus.ANNULE);
        return eventRepository.save(event);
    }

    /**
     * Delete event
     */
    public void deleteEvent(Long eventId, User currentUser) {
        Event event = findById(eventId);

        // Check permissions
        validateUpdatePermissions(event, currentUser);

        // Check reserved places
        int reservedPlaces = reservationRepository.calculateTotalReservedPlaces(event);
        if (reservedPlaces > 0) {
            throw new BusinessException("Cet événement ne peut pas être supprimé car il a des réservations actives");
        }

        eventRepository.delete(event);
    }

    /**
     * Calculate available places - returns DTO with reserved places calculated
     */
    @Transactional(readOnly = true)
    public int getAvailablePlaces(Long eventId) {
        Event event = findById(eventId);
        int reservedPlaces = reservationRepository.calculateTotalReservedPlaces(event);
        return event.getCapaciteMax() - reservedPlaces;
    }

    /**
     * Convert Event to EventDTO with reserved places
     */
    private EventDTO toDTO(Event event) {
        int reservedPlaces = reservationRepository.calculateTotalReservedPlaces(event);
        return new EventDTO(event, reservedPlaces);
    }

    /**
     * Search events with filters - returns DTOs
     */
    @Transactional(readOnly = true)
    public List<EventDTO> searchEventsDTO(EventCategory category, String ville,
                                          Double minPrice, Double maxPrice,
                                          LocalDateTime startDate, LocalDateTime endDate,
                                          EventStatus status, String keyword) {
        List<Event> events = eventRepository.searchEvents(
                category, ville, minPrice, maxPrice,
                startDate, endDate, status, keyword
        );

        return events.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get available events - returns DTOs
     */
    @Transactional(readOnly = true)
    public List<EventDTO> getAvailableEventsDTO() {
        List<Event> events = eventRepository.findAvailableEvents(LocalDateTime.now());
        return events.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get most popular events - returns DTOs
     */
    @Transactional(readOnly = true)
    public List<EventDTO> getMostPopularEventsDTO(int limit) {
        List<Event> events = eventRepository.findMostPopularEvents();
        return events.stream()
                .limit(limit)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get event by ID with DTO
     */
    @Transactional(readOnly = true)
    public EventDTO getEventDTO(Long eventId) {
        Event event = findById(eventId);
        return toDTO(event);
    }

    /**
     * Get events by organizer
     */
    @Transactional(readOnly = true)
    public List<Event> getEventsByOrganizer(User organizer) {
        List<Event> events = eventRepository.findByOrganisateurWithReservations(organizer);
        return events != null ? events : new ArrayList<>();
    }

    /**
     * Get organizer statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getOrganizerStatistics(User organizer) {
        Map<String, Object> stats = new HashMap<>();

        List<Event> events = eventRepository.findByOrganisateur(organizer);

        long totalEvents = events.size();
        long draftEvents = events.stream().filter(e -> e.getStatut().isDraft()).count();
        long publishedEvents = events.stream().filter(e -> e.getStatut().isPublished()).count();
        long cancelledEvents = events.stream().filter(e -> e.getStatut().isCancelled()).count();
        long finishedEvents = events.stream().filter(e -> e.getStatut().isFinished()).count();

        long totalReservations = events.stream()
                .mapToLong(e -> reservationRepository.countByEvenement(e))
                .sum();

        Double totalRevenue = reservationRepository.calculateTotalRevenueByOrganizer(organizer);

        stats.put("totalEvents", totalEvents);
        stats.put("draftEvents", draftEvents);
        stats.put("publishedEvents", publishedEvents);
        stats.put("cancelledEvents", cancelledEvents);
        stats.put("finishedEvents", finishedEvents);
        stats.put("totalReservations", totalReservations);
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);

        return stats;
    }

    /**
     * Mark finished events automatically
     */
    public void markFinishedEvents() {
        List<Event> eventsToFinish = eventRepository.findEventsToMarkAsFinished(LocalDateTime.now());

        eventsToFinish.forEach(event -> {
            event.setStatut(EventStatus.TERMINE);
            eventRepository.save(event);
        });
    }

    /**
     * Get events by category - returns DTOs
     */
    @Transactional(readOnly = true)
    public List<EventDTO> getEventsByCategoryDTO(EventCategory category) {
        List<Event> events = eventRepository.findPublishedEventsByCategory(category);
        return events.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get events in city - returns DTOs
     */
    @Transactional(readOnly = true)
    public List<EventDTO> getEventsInCityDTO(String ville) {
        List<Event> events = eventRepository.findPublishedEventsInCity(ville, LocalDateTime.now());
        return events.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find event by ID
     */
    @Transactional(readOnly = true)
    public Event findById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Événement", "id", id));
    }

    /**
     * Get all events
     */
    @Transactional(readOnly = true)
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    /**
     * Get global event statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getGlobalStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalEvents", eventRepository.count());
        stats.put("publishedEvents", eventRepository.countByStatut(EventStatus.PUBLIE));
        stats.put("draftEvents", eventRepository.countByStatut(EventStatus.BROUILLON));
        stats.put("cancelledEvents", eventRepository.countByStatut(EventStatus.ANNULE));
        stats.put("finishedEvents", eventRepository.countByStatut(EventStatus.TERMINE));

        Map<String, Long> byCategory = new HashMap<>();
        for (EventCategory category : EventCategory.values()) {
            byCategory.put(category.name(), eventRepository.countByCategorie(category));
        }
        stats.put("byCategory", byCategory);

        return stats;
    }

    // Private helper methods

    private void validateEventDates(Event event) {
        if (!dateValidator.isFuture(event.getDateDebut())) {
            throw new BadRequestException("La date de début doit être dans le futur");
        }

        if (!dateValidator.isEndAfterStart(event.getDateDebut(), event.getDateFin())) {
            throw new BadRequestException("La date de fin doit être après la date de début");
        }
    }

    private void validateEventFields(Event event) {
        if (event.getTitre() == null || event.getTitre().trim().isEmpty()) {
            throw new BadRequestException("Le titre est obligatoire");
        }

        if (event.getCategorie() == null) {
            throw new BadRequestException("La catégorie est obligatoire");
        }

        if (event.getLieu() == null || event.getLieu().trim().isEmpty()) {
            throw new BadRequestException("Le lieu est obligatoire");
        }

        if (event.getVille() == null || event.getVille().trim().isEmpty()) {
            throw new BadRequestException("La ville est obligatoire");
        }

        if (event.getCapaciteMax() == null || event.getCapaciteMax() <= 0) {
            throw new BadRequestException("La capacité maximale doit être supérieure à 0");
        }

        if (event.getPrixUnitaire() == null || event.getPrixUnitaire() < 0) {
            throw new BadRequestException("Le prix unitaire doit être positif ou nul");
        }
    }

    private void validateUpdatePermissions(Event event, User currentUser) {
        if (currentUser.getRole() != UserRole.ADMIN &&
                !event.getOrganisateur().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Vous n'avez pas la permission de modifier cet événement");
        }
    }

    private boolean isEventComplete(Event event) {
        return event.getTitre() != null && !event.getTitre().trim().isEmpty() &&
                event.getCategorie() != null &&
                event.getDateDebut() != null &&
                event.getDateFin() != null &&
                event.getLieu() != null && !event.getLieu().trim().isEmpty() &&
                event.getVille() != null && !event.getVille().trim().isEmpty() &&
                event.getCapaciteMax() != null && event.getCapaciteMax() > 0 &&
                event.getPrixUnitaire() != null && event.getPrixUnitaire() >= 0;
    }
}