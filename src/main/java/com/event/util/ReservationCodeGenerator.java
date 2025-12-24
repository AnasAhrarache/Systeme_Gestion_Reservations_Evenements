package com.event.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ReservationCodeGenerator {

    private static final String PREFIX = "EVT-";
    private static final int CODE_LENGTH = 5;
    private final SecureRandom random;

    public ReservationCodeGenerator() {
        this.random = new SecureRandom();
    }

    /**
     * Generate a unique reservation code in format EVT-XXXXX
     */
    public String generateCode() {
        StringBuilder code = new StringBuilder(PREFIX);

        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }

        return code.toString();
    }

    /**
     * Validate code format
     */
    public boolean isValidFormat(String code) {
        if (code == null) {
            return false;
        }

        String pattern = "^EVT-\\d{5}$";
        return code.matches(pattern);
    }
}