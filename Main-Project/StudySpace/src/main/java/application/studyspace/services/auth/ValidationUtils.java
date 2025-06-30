package application.studyspace.services.auth;

// Imports
import application.studyspace.services.DataBase.DataSourceManager;
import application.studyspace.services.DataBase.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Utility methods for validating user input (emails, passwords, exams).
 */
public class ValidationUtils {

    /**
     * Results returned by general validation routines.
     */
    public enum ValidationResult {
        EMPTY_EMAIL,
        INVALID_EMAIL,
        UNKNOWN_EMAIL,
        DUPLICATE_EMAIL,
        EMPTY_PASSWORD,
        PASSWORD_MISMATCH,
        PASSWORD_INVALID,
        INVALID_CREDENTIALS,
        OK
    }

    /**
     * Results returned by exam validation routines.
     */
    public enum ExamValidationResult {
        OK,
        EMPTY_NAME,
        INVALID_DATES,
        END_BEFORE_START,
        INVALID_MINUTES
    }

    // Precompiled regex pattern for email validation
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Levenshtein distance object for fuzzy matching emails
    private static final LevenshteinDistance LEVENSHTEIN = new LevenshteinDistance();

    /**
     * Checks if the email string has a valid format.
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Checks whether the email is already registered in the database.
     */
    public static boolean isKnownEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        String sql = "SELECT email FROM users WHERE email = ?";
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns a list of all known emails in the database.
     */
    public static List<String> listOfKnownEmails() {
        List<String> emails = new ArrayList<>();
        String sql = "SELECT email FROM users";
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                emails.add(rs.getString("email"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return emails;
    }

    /**
     * Checks if the input email is similar to any email in the provided list,
     * using Levenshtein distance within the specified threshold.
     */
    public static boolean isSimilarEmail(String inputEmail, List<String> emailList, int threshold) {
        if (inputEmail == null || emailList == null || emailList.isEmpty()) return false;
        String norm = inputEmail.trim().toLowerCase();
        for (String email : emailList) {
            String e2 = email.trim().toLowerCase();
            Integer dist = LEVENSHTEIN.apply(norm, e2);
            if (dist != null && dist <= threshold) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates login input for email and password correctness.
     */
    public static ValidationResult validateLogin(String email, String password) {
        if (email == null || email.isBlank()) return ValidationResult.EMPTY_EMAIL;
        if (!isValidEmail(email)) return ValidationResult.INVALID_EMAIL;
        if (!isKnownEmail(email)) return ValidationResult.UNKNOWN_EMAIL;
        if (password == null || password.isEmpty()) return ValidationResult.EMPTY_PASSWORD;
        if (!LoginChecker.checkLogin(email, password))
            return ValidationResult.INVALID_CREDENTIALS;
        return ValidationResult.OK;
    }

    /**
     * Validates a password update input.
     */
    public static ValidationResult validatePasswordUpdate(String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            return ValidationResult.EMPTY_PASSWORD;
        }
        if (!isStrongPassword(newPassword)) {
            return ValidationResult.PASSWORD_INVALID;
        }
        if (!newPassword.equals(confirmPassword)) {
            return ValidationResult.PASSWORD_MISMATCH;
        }
        return ValidationResult.OK;
    }

    /**
     * Validates registration input for email and passwords.
     */
    public static ValidationResult validateRegistration(String email,
                                                        String password,
                                                        String confirm) {
        if (email == null || email.isBlank()) return ValidationResult.EMPTY_EMAIL;
        if (!isValidEmail(email)) return ValidationResult.INVALID_EMAIL;
        if (isKnownEmail(email)) return ValidationResult.DUPLICATE_EMAIL;
        if (password == null || confirm == null || password.isEmpty() || confirm.isEmpty())
            return ValidationResult.EMPTY_PASSWORD;
        if (!password.equals(confirm)) return ValidationResult.PASSWORD_MISMATCH;
        if (!isStrongPassword(password)) return ValidationResult.PASSWORD_INVALID;
        return ValidationResult.OK;
    }

    /**
     * Checks if a password meets strength requirements:
     * - at least 12 characters
     * - at least one uppercase letter
     * - at least one digit
     * - at least one special character
     */
    public static boolean isStrongPassword(String pw) {
        return pw.length() >= 12
                && pw.chars().anyMatch(Character::isUpperCase)
                && pw.chars().anyMatch(Character::isDigit)
                && pw.matches(".*[%@&!?#_\\-$].*");
    }

    /**
     * Generates a new UUID token (e.g., for password reset).
     */
    public static UUID generateToken() {
        return UUID.randomUUID();
    }

    /**
     * Placeholder for token validation logic.
     */
    public static boolean validateToken(String username, String token) {
        // Implement real token validation logic if needed
        return false;
    }

    /**
     * Validates input fields for creating or updating an exam event.
     */
    public static ExamValidationResult validateExamFields(
            String name,
            LocalDate startDate,
            LocalTime startTime,
            LocalTime endTime,
            String minutesText
    ) {
        if (name == null || name.isBlank()) {
            return ExamValidationResult.EMPTY_NAME;
        }
        if (startDate == null || startTime == null || endTime == null) {
            return ExamValidationResult.INVALID_DATES;
        }

        if (!endTime.isAfter(startTime)) {
            return ExamValidationResult.END_BEFORE_START;
        }

        try {
            int minutes = Integer.parseInt(minutesText);
            if (minutes <= 0) {
                return ExamValidationResult.INVALID_MINUTES;
            }
        } catch (NumberFormatException ex) {
            return ExamValidationResult.INVALID_MINUTES;
        }
        return ExamValidationResult.OK;
    }
}
