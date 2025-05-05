package application.studyspace.services;
import java.util.regex.Pattern;

public class ValidationUtils {

    // Email validation regex pattern
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    /**
     * Validates whether a given email string matches the standard email format.
     *
     * @param email The email to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false; // Null or empty strings are invalid
        }
        return Pattern.compile(EMAIL_PATTERN).matcher(email).matches();
    }
}


