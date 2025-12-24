package com.event.service;

import com.event.exception.BadRequestException;
import com.event.exception.BusinessException;
import com.event.exception.ResourceNotFoundException;
import com.event.model.entities.Event;
import com.event.model.entities.Reservation;
import com.event.model.entities.User;
import com.event.model.enums.EventStatus;
import com.event.model.enums.ReservationStatus;
import com.event.repository.ReservationRepository;
import com.event.util.DateValidator;
import com.event.util.ReservationCodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationService {

    private static final int MAX_PLACES_PER_RESERVATION = 10;

    private final ReservationRepository reservationRepository;
    private final EventService eventService;
    private final ReservationCodeGenerator codeGenerator;
    private final DateValidator dateValidator;

    public ReservationService(ReservationRepository reservationRepository,
                              EventService eventService,
                              ReservationCodeGenerator codeGenerator,
                              DateValidator dateValidator) {
        this.reservationRepository = reservationRepository;
        this.eventService = eventService;
        this.codeGenerator = codeGenerator;
        this.dateValidator = dateValidator;
    }

    /**
     * Create a new reservation
     */
    public Reservation createReservation(Reservation reservation, User user, Long eventId) {
        Event event = eventService.findById(eventId);

        // Validate event availability
        validateEventAvailability(event);

        // Validate number of places
        validateNumberOfPlaces(reservation.getNombrePlaces(), event);

        // Generate unique reservation code
        String code = generateUniqueCode();
        reservation.setCodeReservation(code);

        // Set relationships
        reservation.setUtilisateur(user);
        reservation.setEvenement(event);

        // Calculate total amount
        reservation.calculateMontantTotal();

        // Set initial status
        reservation.setStatut(ReservationStatus.EN_ATTENTE);

        return reservationRepository.save(reservation);
    }

    /**
     * Confirm a reservation
     */
    public Reservation confirmReservation(Long reservationId, User currentUser) {
        Reservation reservation = findById(reservationId);

        // Validate user can confirm (organizer or admin)
        if (!canManageReservation(reservation, currentUser)) {
            throw new BusinessException("Vous n'avez pas la permission de confirmer cette réservation");
        }

        if (reservation.getStatut().isConfirmed()) {
            throw new BusinessException("Cette réservation est déjà confirmée");
        }

        if (reservation.getStatut().isCancelled()) {
            throw new BusinessException("Cette réservation est annulée et ne peut pas être confirmée");
        }

        reservation.setStatut(ReservationStatus.CONFIRMEE);
        return reservationRepository.save(reservation);
    }

    /**
     * Cancel a reservation
     */
    public Reservation cancelReservation(Long reservationId, User currentUser) {
        Reservation reservation = findById(reservationId);

        // Validate user can cancel (owner, organizer, or admin)
        if (!canCancelReservation(reservation, currentUser)) {
            throw new BusinessException("Vous n'avez pas la permission d'annuler cette réservation");
        }

        if (reservation.getStatut().isCancelled()) {
            throw new BusinessException("Cette réservation est déjà annulée");
        }

        // Check cancellation deadline (48h before event)
        if (!reservation.canBeCancelled()) {
            throw new BusinessException(
                    "Les réservations ne peuvent être annulées que jusqu'à 48h avant l'événement"
            );
        }

        reservation.setStatut(ReservationStatus.ANNULEE);
        return reservationRepository.save(reservation);
    }

    /**
     * Get reservations by user
     */
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByUser(User user) {
        List<Reservation> list = reservationRepository.findByUtilisateur(user);
        // Initialize lazy associations to avoid LazyInitializationException in the UI
        list.forEach(this::initializeReservation);
        return list;
    }

    /**
     * Get upcoming reservations for user
     */
    @Transactional(readOnly = true)
    public List<Reservation> getUpcomingReservations(User user) {
        return reservationRepository.findUpcomingReservationsByUser(user, LocalDateTime.now());
    }

    /**
     * Get reservations by event
     */
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByEvent(Long eventId) {
        Event event = eventService.findById(eventId);
        return reservationRepository.findByEvenement(event);
    }

    /**
     * Get reservations by event and status
     */
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByEventAndStatus(Long eventId, ReservationStatus status) {
        Event event = eventService.findById(eventId);
        return reservationRepository.findByEvenementAndStatut(event, status);
    }

    /**
     * Find reservation by code
     */
    @Transactional(readOnly = true)
    public Optional<Reservation> findByCode(String code) {
        return reservationRepository.findByCodeReservation(code);
    }

    /**
     * Verify reservation by code
     */
    @Transactional(readOnly = true)
    public Reservation verifyReservation(String code) {
        return reservationRepository.findByCodeReservation(code)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation", "code", code));
    }

    /**
     * Get reservation summary
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getReservationSummary(Long reservationId) {
        Reservation reservation = findById(reservationId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("code", reservation.getCodeReservation());
        summary.put("eventTitle", reservation.getEvenement().getTitre());
        summary.put("eventDate", reservation.getEvenement().getDateDebut());
        summary.put("eventLocation", reservation.getEvenement().getLieu());
        summary.put("eventCity", reservation.getEvenement().getVille());
        summary.put("numberOfPlaces", reservation.getNombrePlaces());
        summary.put("totalAmount", reservation.getMontantTotal());
        summary.put("status", reservation.getStatut());
        summary.put("reservationDate", reservation.getDateReservation());
        summary.put("userName", reservation.getUtilisateur().getFullName());
        summary.put("userEmail", reservation.getUtilisateur().getEmail());
        summary.put("canBeCancelled", reservation.canBeCancelled());

        return summary;
    }

    /**
     * Get user reservation statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserReservationStatistics(User user) {
        Map<String, Object> stats = new HashMap<>();

        long totalReservations = reservationRepository.countByUtilisateur(user);
        long confirmedReservations = reservationRepository.countByUtilisateurAndStatut(
                user, ReservationStatus.CONFIRMEE
        );
        long pendingReservations = reservationRepository.countByUtilisateurAndStatut(
                user, ReservationStatus.EN_ATTENTE
        );
        long cancelledReservations = reservationRepository.countByUtilisateurAndStatut(
                user, ReservationStatus.ANNULEE
        );

        Double totalSpent = reservationRepository.calculateTotalAmountByUser(user);

        List<Reservation> upcomingReservations = reservationRepository.findUpcomingReservationsByUser(
                user, LocalDateTime.now()
        );

        stats.put("totalReservations", totalReservations);
        stats.put("confirmedReservations", confirmedReservations);
        stats.put("pendingReservations", pendingReservations);
        stats.put("cancelledReservations", cancelledReservations);
        stats.put("totalSpent", totalSpent != null ? totalSpent : 0.0);
        stats.put("upcomingReservationsCount", upcomingReservations.size());

        return stats;
    }

    /**
     * Get event reservation statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getEventReservationStatistics(Long eventId) {
        Event event = eventService.findById(eventId);

        Map<String, Object> stats = new HashMap<>();

        long totalReservations = reservationRepository.countByEvenement(event);
        long confirmedReservations = reservationRepository.countByEvenementAndStatut(
                event, ReservationStatus.CONFIRMEE
        );

        int totalPlacesReserved = event.getPlacesReservees();
        int availablePlaces = event.getPlacesDisponibles();
        double fillRate = event.getTauxRemplissage();

        Double totalRevenue = reservationRepository.calculateTotalRevenueByEvent(event);

        stats.put("totalReservations", totalReservations);
        stats.put("confirmedReservations", confirmedReservations);
        stats.put("totalPlacesReserved", totalPlacesReserved);
        stats.put("availablePlaces", availablePlaces);
        stats.put("fillRate", fillRate);
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        stats.put("capacity", event.getCapaciteMax());

        return stats;
    }

    /**
     * Search reservations
     */
    @Transactional(readOnly = true)
    public List<Reservation> searchReservations(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return reservationRepository.findAll();
        }
        return reservationRepository.searchReservations(searchTerm.trim());
    }

    /**
     * Get all reservations
     */
    @Transactional(readOnly = true)
    public List<Reservation> getAllReservations() {
        List<Reservation> list = reservationRepository.findAll();
        list.forEach(this::initializeReservation);
        return list;
    }

    /**
     * Get reservations by status
     */
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByStatus(ReservationStatus status) {
        return reservationRepository.findByStatut(status);
    }

    /**
     * Get recent reservations
     */
    @Transactional(readOnly = true)
    public List<Reservation> getRecentReservations(int days) {
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        return reservationRepository.findRecentReservations(sinceDate);
    }

    /**
     * Get global reservation statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getGlobalStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalReservations = reservationRepository.count();
        long activeReservations = reservationRepository.countActiveReservations();

        long confirmedReservations = reservationRepository.countByStatut(ReservationStatus.CONFIRMEE);
        long pendingReservations = reservationRepository.countByStatut(ReservationStatus.EN_ATTENTE);
        long cancelledReservations = reservationRepository.countByStatut(ReservationStatus.ANNULEE);

        Double totalRevenue = reservationRepository.calculateTotalRevenue();

        stats.put("totalReservations", totalReservations);
        stats.put("activeReservations", activeReservations);
        stats.put("confirmedReservations", confirmedReservations);
        stats.put("pendingReservations", pendingReservations);
        stats.put("cancelledReservations", cancelledReservations);
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);

        return stats;
    }

    /**
     * Find reservation by ID
     */
    @Transactional(readOnly = true)
    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation", "id", id));
    }

    // Private helper methods

    private void validateEventAvailability(Event event) {
        if (event.getStatut() != EventStatus.PUBLIE) {
            throw new BusinessException("Cet événement n'est pas disponible pour les réservations");
        }

        if (!dateValidator.isFuture(event.getDateDebut())) {
            throw new BusinessException("Cet événement est déjà passé");
        }

        if (event.getPlacesDisponibles() <= 0) {
            throw new BusinessException("Il n'y a plus de places disponibles pour cet événement");
        }
    }

    private void validateNumberOfPlaces(Integer numberOfPlaces, Event event) {
        if (numberOfPlaces == null || numberOfPlaces <= 0) {
            throw new BadRequestException("Le nombre de places doit être supérieur à 0");
        }

        if (numberOfPlaces > MAX_PLACES_PER_RESERVATION) {
            throw new BadRequestException(
                    "Vous ne pouvez pas réserver plus de " + MAX_PLACES_PER_RESERVATION + " places à la fois"
            );
        }

        if (numberOfPlaces > event.getPlacesDisponibles()) {
            throw new BusinessException(
                    "Il n'y a que " + event.getPlacesDisponibles() + " place(s) disponible(s)"
            );
        }
    }

    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        int maxAttempts = 100;

        do {
            code = codeGenerator.generateCode();
            attempts++;

            if (attempts >= maxAttempts) {
                throw new BusinessException("Impossible de générer un code de réservation unique");
            }
        } while (reservationRepository.existsByCodeReservation(code));

        return code;
    }

    private boolean canManageReservation(Reservation reservation, User user) {
        // Admin can manage all reservations
        if (user.getRole().isAdmin()) {
            return true;
        }

        // Organizer can manage their event's reservations
        if (user.getRole().isOrganizer() &&
                reservation.getEvenement().getOrganisateur().getId().equals(user.getId())) {
            return true;
        }

        return false;
    }

    private boolean canCancelReservation(Reservation reservation, User user) {
        // Owner can cancel their own reservation
        if (reservation.getUtilisateur().getId().equals(user.getId())) {
            return true;
        }

        // Admin can cancel any reservation
        if (user.getRole().isAdmin()) {
            return true;
        }

        // Organizer can cancel their event's reservations
        if (user.getRole().isOrganizer() &&
                reservation.getEvenement().getOrganisateur().getId().equals(user.getId())) {
            return true;
        }

        return false;
    }

    /**
     * Access commonly used related fields to force initialization while inside transaction
     */
    private void initializeReservation(Reservation reservation) {
        if (reservation == null) return;
        try {
            // Access related properties
            if (reservation.getUtilisateur() != null) {
                reservation.getUtilisateur().getFullName();
                reservation.getUtilisateur().getEmail();
            }
            if (reservation.getEvenement() != null) {
                reservation.getEvenement().getTitre();
                reservation.getEvenement().getDateDebut();
                reservation.getEvenement().getLieu();
            }
        } catch (Exception ignored) {
            // Defensive: ignore initialization errors here; callers will handle failures
        }
    }
}