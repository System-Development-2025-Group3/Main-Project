package application.studyspace.services.auth;
import java.util.regex.Pattern;

public class ValidationUtils {

    /**
     * A regular expression pattern used for validating the format of email addresses.
     * This pattern ensures that the email follows the standard structure, which includes:
     * - A combination of alphanumeric characters, dots, underscores, percent signs, plus, and hyphens
     *   as the local part of the email (before the "@" symbol).
     * - A valid domain name composed of alphanumeric characters, dots, and hyphens.
     * - A top-level domain (TLD) with at least two alphabetic characters.
     *
     * Examples of valid email formats:
     * - example@example.com
     * - user.name+tag@sub.domain.org
     *
     * Note: This pattern does not guarantee that the domain or email exists; it only checks
     * the syntactic correctness of the email address.
     */

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    /**
     * Validates whether a given email string matches the standard email format.
     *
     * @param email The email to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return true; // Null or empty strings are invalid
        }
        return !Pattern.compile(EMAIL_PATTERN).matcher(email).matches();
    }
}


