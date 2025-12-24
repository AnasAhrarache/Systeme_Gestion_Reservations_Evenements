package com.event.repository;

import com.event.model.entities.User;
import com.event.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find all active users by role
     */
    List<User> findByActifTrueAndRole(UserRole role);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find users by name or surname (case insensitive)
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.prenom) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findByNomOrPrenomContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    /**
     * Count users by role
     */
    long countByRole(UserRole role);

    /**
     * Find all active users
     */
    List<User> findByActifTrue();

    /**
     * Find all inactive users
     */
    List<User> findByActifFalse();

    /**
     * Find users by role
     */
    List<User> findByRole(UserRole role);

    /**
     * Find active users by role
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.actif = true")
    List<User> findActiveUsersByRole(@Param("role") UserRole role);

    /**
     * Search users by email or name
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.prenom) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    /**
     * Count all active users
     */
    long countByActifTrue();

    /**
     * Find organizers with active status
     */
    @Query("SELECT u FROM User u WHERE u.role = 'ORGANIZER' AND u.actif = true")
    List<User> findActiveOrganizers();
}