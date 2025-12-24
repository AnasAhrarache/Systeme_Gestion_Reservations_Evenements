package com.event.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder {

    private final BCryptPasswordEncoder encoder;

    public PasswordEncoder() {
        this.encoder = new BCryptPasswordEncoder();
    }

    /**
     * Hash a plain text password
     */
    public String encode(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    /**
     * Verify if plain password matches hashed password
     */
    public boolean matches(String plainPassword, String hashedPassword) {
        return encoder.matches(plainPassword, hashedPassword);
    }

    /**
     * Validate password strength
     */
    public boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpperCase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowerCase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        return hasUpperCase && hasLowerCase && hasDigit;
    }

    /**
     * Get password strength message
     */
    public String getPasswordStrengthMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "Le mot de passe est vide";
        }

        if (password.length() < 8) {
            return "Le mot de passe doit contenir au moins 8 caractères";
        }

        boolean hasUpperCase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowerCase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (!hasUpperCase) {
            return "Le mot de passe doit contenir au moins une majuscule";
        }

        if (!hasLowerCase) {
            return "Le mot de passe doit contenir au moins une minuscule";
        }

        if (!hasDigit) {
            return "Le mot de passe doit contenir au moins un chiffre";
        }

        return "Mot de passe fort";
    }
}