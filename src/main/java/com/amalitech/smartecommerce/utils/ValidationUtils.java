package com.amalitech.smartecommerce.utils;

import java.util.regex.Pattern;

/**
 * Utility class for input validation.
 * Prevents SQL injection and ensures data integrity.
 */
public class ValidationUtils {

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    // Password requirements: at least 8 chars, 1 uppercase, 1 lowercase, 1 digit
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"
    );

    // SKU pattern: alphanumeric with hyphens
    private static final Pattern SKU_PATTERN = Pattern.compile(
        "^[A-Z0-9]+-[A-Z0-9]+-[0-9]+$"
    );

    // Dangerous SQL characters/patterns
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        ".*([';\"\\-\\-]|(\\/\\*)|(\\*\\/)|" +
        "(\\b(ALTER|CREATE|DELETE|DROP|EXEC|INSERT|MERGE|SELECT|UPDATE|UNION|TRUNCATE)\\b)).*",
        Pattern.CASE_INSENSITIVE
    );

    // ==========================================
    // STRING VALIDATION
    // ==========================================

    /**
     * Validates that a string is not null or empty.
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validates string length is within bounds.
     */
    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if (value == null) return minLength == 0;
        int length = value.length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Sanitizes input by removing potentially dangerous characters.
     * Use parameterized queries instead for SQL - this is a secondary defense.
     */
    public static String sanitize(String input) {
        if (input == null) return null;
        // Remove potential SQL injection characters
        return input.replaceAll("[;'\"\\-\\-]", "").trim();
    }

    /**
     * Checks if input contains potential SQL injection patterns.
     */
    public static boolean containsSqlInjection(String input) {
        if (input == null) return false;
        return SQL_INJECTION_PATTERN.matcher(input).matches();
    }

    // ==========================================
    // EMAIL VALIDATION
    // ==========================================

    /**
     * Validates email format.
     */
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    // ==========================================
    // PASSWORD VALIDATION
    // ==========================================

    /**
     * Validates password strength.
     */
    public static boolean isValidPassword(String password) {
        if (password == null) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Returns password strength feedback.
     */
    public static String getPasswordStrengthFeedback(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter";
        }
        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one digit";
        }
        return "Password is valid";
    }

    // ==========================================
    // NUMERIC VALIDATION
    // ==========================================

    /**
     * Validates that a value is a positive number.
     */
    public static boolean isPositive(double value) {
        return value > 0;
    }

    /**
     * Validates that a value is non-negative.
     */
    public static boolean isNonNegative(double value) {
        return value >= 0;
    }

    /**
     * Validates that a value is within a range.
     */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    /**
     * Validates that an integer is positive.
     */
    public static boolean isPositive(int value) {
        return value > 0;
    }

    /**
     * Validates that an integer is non-negative.
     */
    public static boolean isNonNegative(int value) {
        return value >= 0;
    }

    /**
     * Validates that an integer is within a range.
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    // ==========================================
    // PRODUCT VALIDATION
    // ==========================================

    /**
     * Validates product name.
     */
    public static ValidationResult validateProductName(String name) {
        if (!isNotEmpty(name)) {
            return ValidationResult.error("Product name is required");
        }
        if (!isValidLength(name, 2, 255)) {
            return ValidationResult.error("Product name must be between 2 and 255 characters");
        }
        if (containsSqlInjection(name)) {
            return ValidationResult.error("Product name contains invalid characters");
        }
        return ValidationResult.success();
    }

    /**
     * Validates product price.
     */
    public static ValidationResult validatePrice(double price) {
        if (!isNonNegative(price)) {
            return ValidationResult.error("Price cannot be negative");
        }
        if (price > 999999.99) {
            return ValidationResult.error("Price exceeds maximum allowed value");
        }
        return ValidationResult.success();
    }

    /**
     * Validates stock quantity.
     */
    public static ValidationResult validateStockQuantity(int quantity) {
        if (!isNonNegative(quantity)) {
            return ValidationResult.error("Stock quantity cannot be negative");
        }
        if (quantity > 999999) {
            return ValidationResult.error("Stock quantity exceeds maximum allowed value");
        }
        return ValidationResult.success();
    }

    /**
     * Validates SKU format.
     */
    public static boolean isValidSku(String sku) {
        if (sku == null) return true; // SKU is optional
        return SKU_PATTERN.matcher(sku).matches();
    }

    // ==========================================
    // RATING VALIDATION
    // ==========================================

    /**
     * Validates rating (1-5 stars).
     */
    public static ValidationResult validateRating(int rating) {
        if (!isInRange(rating, 1, 5)) {
            return ValidationResult.error("Rating must be between 1 and 5");
        }
        return ValidationResult.success();
    }

    // ==========================================
    // USER VALIDATION
    // ==========================================

    /**
     * Validates user role.
     */
    public static ValidationResult validateRole(String role) {
        if (!isNotEmpty(role)) {
            return ValidationResult.error("Role is required");
        }
        String[] validRoles = {"admin", "manager", "customer"};
        for (String validRole : validRoles) {
            if (validRole.equalsIgnoreCase(role)) {
                return ValidationResult.success();
            }
        }
        return ValidationResult.error("Invalid role. Must be admin, manager, or customer");
    }

    // ==========================================
    // VALIDATION RESULT CLASS
    // ==========================================

    /**
     * Result class for validation operations.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return valid ? "Valid" : "Invalid: " + message;
        }
    }
}
