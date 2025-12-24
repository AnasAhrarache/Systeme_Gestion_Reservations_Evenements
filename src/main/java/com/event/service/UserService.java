package com.event.service;

import com.event.exception.BadRequestException;
import com.event.exception.ConflictException;
import com.event.exception.ResourceNotFoundException;
import com.event.model.entities.User;
import com.event.model.enums.UserRole;
import com.event.repository.EventRepository;
import com.event.repository.ReservationRepository;
import com.event.repository.UserRepository;
import com.event.util.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       EventRepository eventRepository,
                       ReservationRepository reservationRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user
     */
    public User registerUser(User user) {
        // Validate email uniqueness
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Un compte avec cet email existe déjà");
        }

        // Validate password strength
        if (!passwordEncoder.isPasswordStrong(user.getPassword())) {
            throw new BadRequestException(
                    "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule et un chiffre"
            );
        }

        // Hash password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set default role if not specified
        if (user.getRole() == null) {
            user.setRole(UserRole.CLIENT);
        }

        return userRepository.save(user);
    }

    /**
     * Authenticate user
     */
    public Optional<User> authenticate(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();

        // Check if account is active
        if (!user.getActif()) {
            throw new BadRequestException("Ce compte est désactivé");
        }

        // Verify password. Support two cases:
        // - stored password is a bcrypt hash (starts with $2a/$2b/$2y) -> use encoder.matches
        // - stored password is plain text (migration scenario) -> compare directly and migrate to bcrypt
        String stored = user.getPassword();

        if (stored == null) {
            return Optional.empty();
        }

        boolean authenticated = false;

        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            if (passwordEncoder.matches(password, stored)) {
                authenticated = true;
            }
        } else {
            // Plain-text password in DB: allow login and migrate to bcrypt
            if (stored.equals(password)) {
                authenticated = true;
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
            }
        }

        if (!authenticated) {
            return Optional.empty();
        }

        return Optional.of(user);
    }

    /**
     * Update user profile
     */
    public User updateProfile(Long userId, User updatedUser) {
        User user = findById(userId);

        // Update allowed fields
        user.setNom(updatedUser.getNom());
        user.setPrenom(updatedUser.getPrenom());
        user.setTelephone(updatedUser.getTelephone());

        // Check email uniqueness if changed
        if (!user.getEmail().equals(updatedUser.getEmail())) {
            if (userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new ConflictException("Cet email est déjà utilisé");
            }
            user.setEmail(updatedUser.getEmail());
        }

        return userRepository.save(user);
    }

    /**
     * Change password
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findById(userId);

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Mot de passe actuel incorrect");
        }

        // Validate new password strength
        if (!passwordEncoder.isPasswordStrong(newPassword)) {
            throw new BadRequestException(
                    "Le nouveau mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule et un chiffre"
            );
        }

        // Hash and save new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Deactivate user account
     */
    public void deactivateAccount(Long userId) {
        User user = findById(userId);
        user.setActif(false);
        userRepository.save(user);
    }

    /**
     * Activate user account
     */
    public void activateAccount(Long userId) {
        User user = findById(userId);
        user.setActif(true);
        userRepository.save(user);
    }

    /**
     * Change user role (admin only)
     */
    public User changeRole(Long userId, UserRole newRole) {
        User user = findById(userId);
        user.setRole(newRole);
        return userRepository.save(user);
    }

    /**
     * Get user statistics
     */
    public Map<String, Object> getUserStatistics(Long userId) {
        User user = findById(userId);
        Map<String, Object> stats = new HashMap<>();

        long eventsCreated = user.getRole().isOrganizer()
                ? eventRepository.countByOrganisateur(user)
                : 0;

        long totalReservations = reservationRepository.countByUtilisateur(user);

        Double totalSpent = reservationRepository.calculateTotalAmountByUser(user);

        stats.put("eventsCreated", eventsCreated);
        stats.put("totalReservations", totalReservations);
        stats.put("totalSpent", totalSpent != null ? totalSpent : 0.0);

        return stats;
    }

    /**
     * Find user by ID
     */
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
    }

    /**
     * Find user by email
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get users by role
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * Get active users by role
     */
    @Transactional(readOnly = true)
    public List<User> getActiveUsersByRole(UserRole role) {
        return userRepository.findActiveUsersByRole(role);
    }

    /**
     * Search users
     */
    @Transactional(readOnly = true)
    public List<User> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return userRepository.findAll();
        }
        return userRepository.searchUsers(searchTerm.trim());
    }

    /**
     * Get all active organizers
     */
    @Transactional(readOnly = true)
    public List<User> getActiveOrganizers() {
        return userRepository.findActiveOrganizers();
    }

    /**
     * Get global user statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getGlobalStatistics() {
        Map<String, Long> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.countByActifTrue());
        stats.put("admins", userRepository.countByRole(UserRole.ADMIN));
        stats.put("organizers", userRepository.countByRole(UserRole.ORGANIZER));
        stats.put("clients", userRepository.countByRole(UserRole.CLIENT));

        return stats;
    }
}