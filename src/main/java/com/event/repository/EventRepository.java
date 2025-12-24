package com.event.repository;

import com.event.dto.EventDTO;
import com.event.model.entities.Event;
import com.event.model.entities.User;
import com.event.model.enums.EventCategory;
import com.event.model.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Find events by category
     */
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.organisateur LEFT JOIN FETCH e.reservations WHERE e.categorie = :category ORDER BY e.dateDebut ASC")
    List<Event> findByCategorie(@Param("category") EventCategory category);

    /**
     * Find published events between two dates
     */
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.organisateur LEFT JOIN FETCH e.reservations WHERE e.statut = 'PUBLIE' AND " +
            "e.dateDebut >= :startDate AND e.dateDebut <= :endDate " +
            "ORDER BY e.dateDebut ASC")
    List<Event> findPublishedEventsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find events by organizer and status
     */
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.organisateur LEFT JOIN FETCH e.reservations WHERE e.organisateur = :organizer AND e.statut = :status ORDER BY e.dateDebut DESC")
    List<Event> findByOrganisateurAndStatut(@Param("organizer") User organizer, @Param("status") EventStatus status);

    /**
     * Find all available events (published and not finished)
     */
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.organisateur LEFT JOIN FETCH e.reservations WHERE e.statut = 'PUBLIE' AND " +
            "e.dateDebut > :now ORDER BY e.dateDebut ASC")
    List<Event> findAvailableEvents(@Param("now") LocalDateTime now);

    /**
     * Count events by category
     */
    long countByCategorie(EventCategory category);

    /**
     * Find events by location or city
     */
    @Query("SELECT e FROM Event e WHERE " +
            "LOWER(e.lieu) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(e.ville) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Event> findByLieuOrVille(@Param("searchTerm") String searchTerm);

    /**
     * Search events by title (case insensitive)
     */
    @Query("SELECT e FROM Event e WHERE " +
            "LOWER(e.titre) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Event> searchByTitre(@Param("keyword") String keyword);

    /**
     * Find events by price range
     */
    @Query("SELECT e FROM Event e WHERE " +
            "e.prixUnitaire >= :minPrice AND e.prixUnitaire <= :maxPrice")
    List<Event> findByPriceRange(
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice
    );

    /**
     * Find events by organizer
     */
    List<Event> findByOrganisateur(User organizer);

    /**
     * Find events by status
     */
    List<Event> findByStatut(EventStatus status);

    /**
     * Find published events by category
     */
    @Query("SELECT e FROM Event e WHERE e.categorie = :category AND e.statut = 'PUBLIE' " +
            "ORDER BY e.dateDebut ASC")
    List<Event> findPublishedEventsByCategory(@Param("category") EventCategory category);

    /**
     * Find events by city and status
     */
    List<Event> findByVilleAndStatut(String ville, EventStatus status);

    /**
     * Complex search with multiple filters
     */
    @Query("SELECT e FROM Event e WHERE " +
            "(:category IS NULL OR e.categorie = :category) AND " +
            "(:ville IS NULL OR LOWER(e.ville) = LOWER(:ville)) AND " +
            "(:minPrice IS NULL OR e.prixUnitaire >= :minPrice) AND " +
            "(:maxPrice IS NULL OR e.prixUnitaire <= :maxPrice) AND " +
            "(:startDate IS NULL OR e.dateDebut >= :startDate) AND " +
            "(:endDate IS NULL OR e.dateDebut <= :endDate) AND " +
            "(:status IS NULL OR e.statut = :status) AND " +
            "(:keyword IS NULL OR LOWER(e.titre) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY e.dateDebut ASC")
    List<Event> searchEvents(
            @Param("category") EventCategory category,
            @Param("ville") String ville,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") EventStatus status,
            @Param("keyword") String keyword
    );

    /**
     * Find most popular events (most reservations)
     */
    @Query("""
    SELECT e
    FROM Event e
    LEFT JOIN e.reservations r
    WHERE e.statut = com.event.model.enums.EventStatus.PUBLIE
    GROUP BY e
    ORDER BY COUNT(r) DESC
    """)
    List<Event> findMostPopularEvents();



    /**
     * Count events by status
     */
    long countByStatut(EventStatus status);

    /**
     * Find events that should be marked as finished
     */
    @Query("SELECT e FROM Event e WHERE e.statut = 'PUBLIE' AND e.dateFin < :now")
    List<Event> findEventsToMarkAsFinished(@Param("now") LocalDateTime now);

    /**
     * Find upcoming events for organizer
     */
    @Query("SELECT e FROM Event e WHERE e.organisateur = :organizer AND " +
            "e.dateDebut > :now AND e.statut != 'ANNULE' " +
            "ORDER BY e.dateDebut ASC")
    List<Event> findUpcomingEventsByOrganizer(
            @Param("organizer") User organizer,
            @Param("now") LocalDateTime now
    );

    /**
     * Count events by organizer and status
     */
    long countByOrganisateurAndStatut(User organizer, EventStatus status);

    /**
     * Find published events in a city
     */
    @Query("SELECT e FROM Event e WHERE LOWER(e.ville) = LOWER(:ville) " +
            "AND e.statut = 'PUBLIE' AND e.dateDebut > :now " +
            "ORDER BY e.dateDebut ASC")
    List<Event> findPublishedEventsInCity(
            @Param("ville") String ville,
            @Param("now") LocalDateTime now
    );

    long countByOrganisateur(User organisateur);

    /**
     * Find events by organizer with eagerly loaded reservations
     */
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.reservations " +
            "WHERE e.organisateur = :organizer ORDER BY e.dateCreation DESC")
    List<Event> findByOrganisateurWithReservations(@Param("organizer") User organizer);

    /**
     * Get all events with eagerly loaded organizer and reservations
     */
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.organisateur LEFT JOIN FETCH e.reservations ORDER BY e.dateCreation DESC")
    List<Event> findAll();
}