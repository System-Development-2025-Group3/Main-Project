package application.studyspace.services.auth;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.auth.LoginChecker;
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
import java.time.ZonedDateTime;

/**
 * Utility methods for validating user input (email, password) and
 * for checking against stored data (known emails, login credentials).
 */
public class ValidationUtils {

    /**
     * Results of validation routines.
     */
    public enum ValidationResult {
        EMPTY_EMAIL,
        INVALID_EMAIL,
        UNKNOWN_EMAIL,       // login only
        DUPLICATE_EMAIL,     // register only
        EMPTY_PASSWORD,
        PASSWORD_MISMATCH,   // register only
        PASSWORD_INVALID,
        INVALID_CREDENTIALS, // login only
        OK
    }

    /**
     * Results of exam validation routines.
     */
    public enum ExamValidationResult {
        OK,
        EMPTY_NAME,
        INVALID_DATES,
        END_BEFORE_START,
        END_TIME_BEFORE_START_TIME_SAME_DAY,
        INVALID_MINUTES
    }

    // Precompile email regex once
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Levenshtein for autocorrect suggestions
    private static final LevenshteinDistance LEVENSHTEIN = new LevenshteinDistance();

    /**
     * Checks if the email string has a valid format.
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Checks if the given email is already registered.
     */
    public static boolean isKnownEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        String sql = "SELECT email FROM users WHERE email = ?";
        try (Connection conn = new DatabaseConnection().getConnection();
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
     * Retrieves all known emails from the database.
     */
    public static List<String> listOfKnownEmails() {
        List<String> emails = new ArrayList<>();
        String sql = "SELECT email FROM users";
        try (Connection conn = new DatabaseConnection().getConnection();
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
     * Returns true if inputEmail is within the Levenshtein threshold of any known email.
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
     * Validates login input: email and password.
     */
    public static ValidationResult validateLogin(String email, String password) {
        if (email == null || email.isBlank()) return ValidationResult.EMPTY_EMAIL;
        if (!isValidEmail(email))           return ValidationResult.INVALID_EMAIL;
        if (!isKnownEmail(email))           return ValidationResult.UNKNOWN_EMAIL;
        if (password == null || password.isEmpty()) return ValidationResult.EMPTY_PASSWORD;
        if (!LoginChecker.checkLogin(email, password))
            return ValidationResult.INVALID_CREDENTIALS;
        return ValidationResult.OK;
    }

    /**
     * Validates a new password and its confirmation.
     *
     * @param newPassword     The new password to validate.
     * @param confirmPassword The confirmation of the new password.
     * @return A {@link ValidationResult} indicating the outcome.
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
     * Validates registration input: email, password, and confirmation.
     */
    public static ValidationResult validateRegistration(String email,
                                                        String password,
                                                        String confirm) {
        if (email == null || email.isBlank())    return ValidationResult.EMPTY_EMAIL;
        if (!isValidEmail(email))                return ValidationResult.INVALID_EMAIL;
        if (isKnownEmail(email))                 return ValidationResult.DUPLICATE_EMAIL;
        if (password == null || confirm == null || password.isEmpty() || confirm.isEmpty())
            return ValidationResult.EMPTY_PASSWORD;
        if (!password.equals(confirm))           return ValidationResult.PASSWORD_MISMATCH;
        if (!isStrongPassword(password))         return ValidationResult.PASSWORD_INVALID;
        return ValidationResult.OK;
    }

    /**
     * Checks password strength: ≥12 chars, ≥1 uppercase, ≥1 digit, ≥1 special.
     */
    public static boolean isStrongPassword(String pw) {
        return pw.length() >= 12
               && pw.chars().anyMatch(Character::isUpperCase)
               && pw.chars().anyMatch(Character::isDigit)
               && pw.matches(".*[%&!?#_\\-$].*");
    }

    /**
     * Generates a new random UUID token (e.g. for password reset).
     */
    public static UUID generateToken() {
        return UUID.randomUUID();
    }

    /**
     * Placeholder for future token validation logic.
     */
    public static boolean validateToken(String username, String token) {
        // implement as needed
        return false;
    }

    /**
     * Validates exam fields: name, dates, times, and minutes.
     */
    public static ExamValidationResult validateExamFields(String name, LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime, String minutesText) {
        if (name == null || name.isBlank()) {
            return ExamValidationResult.EMPTY_NAME;
        }
        if (startDate == null || endDate == null || startTime == null || endTime == null) {
            return ExamValidationResult.INVALID_DATES;
        }
        ZonedDateTime start = ZonedDateTime.of(startDate, startTime, java.time.ZoneId.systemDefault());
        ZonedDateTime end = ZonedDateTime.of(endDate, endTime, java.time.ZoneId.systemDefault());
        if (!end.isAfter(start)) {
            return ExamValidationResult.END_BEFORE_START;
        }
        if (startDate.equals(endDate) && endTime.isBefore(startTime)) {
            return ExamValidationResult.END_TIME_BEFORE_START_TIME_SAME_DAY;
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
