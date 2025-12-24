package com.event.repository;

import com.event.model.entities.Event;
import com.event.model.entities.Reservation;
import com.event.model.entities.User;
import com.event.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Find reservations by user
     */
    List<Reservation> findByUtilisateur(User user);

    /**
     * Find reservations by event and status
     */
    List<Reservation> findByEvenementAndStatut(Event event, ReservationStatus status);

    /**
     * Calculate total reserved places for an event (excluding cancelled)
     */
    @Query("SELECT COALESCE(SUM(r.nombrePlaces), 0) FROM Reservation r WHERE " +
            "r.evenement = :event AND r.statut != 'ANNULEE'")
    int calculateTotalReservedPlaces(@Param("event") Event event);

    /**
     * Find reservation by code
     */
    Optional<Reservation> findByCodeReservation(String code);

    /**
     * Find reservations between two dates
     */
    @Query("SELECT r FROM Reservation r WHERE " +
            "r.dateReservation >= :startDate AND r.dateReservation <= :endDate " +
            "ORDER BY r.dateReservation DESC")
    List<Reservation> findReservationsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find confirmed reservations by user
     */
    List<Reservation> findByUtilisateurAndStatut(User user, ReservationStatus status);

    /**
     * Calculate total amount spent by user
     */
    @Query("SELECT COALESCE(SUM(r.montantTotal), 0.0) FROM Reservation r WHERE " +
            "r.utilisateur = :user AND r.statut = 'CONFIRMEE'")
    Double calculateTotalAmountByUser(@Param("user") User user);

    /**
     * Find reservations by event
     */
    List<Reservation> findByEvenement(Event event);

    /**
     * Find upcoming reservations for user
     */
    @Query("SELECT r FROM Reservation r WHERE r.utilisateur = :user AND " +
            "r.evenement.dateDebut > :now AND r.statut != 'ANNULEE' " +
            "ORDER BY r.evenement.dateDebut ASC")
    List<Reservation> findUpcomingReservationsByUser(
            @Param("user") User user,
            @Param("now") LocalDateTime now
    );

    /**
     * Count reservations by user
     */
    long countByUtilisateur(User user);

    /**
     * Count confirmed reservations by user
     */
    long countByUtilisateurAndStatut(User user, ReservationStatus status);

    /**
     * Find all confirmed reservations
     */
    List<Reservation> findByStatut(ReservationStatus status);

    /**
     * Calculate total revenue for an event
     */
    @Query("SELECT COALESCE(SUM(r.montantTotal), 0.0) FROM Reservation r WHERE " +
            "r.evenement = :event AND r.statut = 'CONFIRMEE'")
    Double calculateTotalRevenueByEvent(@Param("event") Event event);

    /**
     * Calculate total revenue for an organizer
     */
    @Query("SELECT COALESCE(SUM(r.montantTotal), 0.0) FROM Reservation r WHERE " +
            "r.evenement.organisateur = :organizer AND r.statut = 'CONFIRMEE'")
    Double calculateTotalRevenueByOrganizer(@Param("organizer") User organizer);

    /**
     * Check if code already exists
     */
    boolean existsByCodeReservation(String code);

    /**
     * Search reservations by user name or code
     */
    @Query("SELECT r FROM Reservation r WHERE " +
            "LOWER(r.codeReservation) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.utilisateur.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.utilisateur.prenom) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Reservation> searchReservations(@Param("searchTerm") String searchTerm);

    /**
     * Count reservations by event
     */
    long countByEvenement(Event event);

    /**
     * Count confirmed reservations by event
     */
    long countByEvenementAndStatut(Event event, ReservationStatus status);

    /**
     * Find reservations for events by organizer
     */
    @Query("SELECT r FROM Reservation r WHERE r.evenement.organisateur = :organizer " +
            "ORDER BY r.dateReservation DESC")
    List<Reservation> findByEventOrganizer(@Param("organizer") User organizer);

    /**
     * Calculate global statistics - total reservations
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.statut != 'ANNULEE'")
    long countActiveReservations();

    /**
     * Calculate global statistics - total revenue
     */
    @Query("SELECT COALESCE(SUM(r.montantTotal), 0.0) FROM Reservation r WHERE " +
            "r.statut = 'CONFIRMEE'")
    Double calculateTotalRevenue();

    /**
     * Find recent reservations (last N days)
     */
    @Query("SELECT r FROM Reservation r WHERE " +
            "r.dateReservation >= :sinceDate " +
            "ORDER BY r.dateReservation DESC")
    List<Reservation> findRecentReservations(@Param("sinceDate") LocalDateTime sinceDate);

    long countByStatut(ReservationStatus statut);
}