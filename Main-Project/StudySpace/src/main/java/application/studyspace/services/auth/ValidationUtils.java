package application.studyspace.services.auth;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final String EMAIL_PATTERN =
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    /**
     * The possible outcomes of validating a registration form.
     */
    public enum RegistrationError {
        NONE,
        EMAIL_EMPTY,
        EMAIL_INVALID,
        PASSWORD_EMPTY,
        PASSWORD_MISMATCH,
        PASSWORD_WEAK
    }

    /**
     * Validates email + two password fields in the exact sequence your controller needs.
     *
     * @param email the raw email input
     * @param pw1   the first password input
     * @param pw2   the second password input
     * @return RegistrationError.NONE if everything passes, otherwise the specific error
     */
    public static RegistrationError validateRegistration(
            String email, String pw1, String pw2) {

        if (email == null || email.isBlank()) {
            return RegistrationError.EMAIL_EMPTY;
        }
        if (!Pattern.compile(EMAIL_PATTERN).matcher(email).matches()) {
            return RegistrationError.EMAIL_INVALID;
        }
        if (pw1 == null || pw1.isEmpty() || pw2 == null || pw2.isEmpty()) {
            return RegistrationError.PASSWORD_EMPTY;
        }
        if (!pw1.equals(pw2)) {
            return RegistrationError.PASSWORD_MISMATCH;
        }
        // at least 12 chars, one uppercase, one digit, one special char
        boolean strong =
                pw1.length() >= 12
                        && pw1.matches(".*[A-Z].*")
                        && pw1.matches(".*\\d.*")
                        && pw1.matches(".*[!%&?#_\\-\\$].*");
        if (!strong) {
            return RegistrationError.PASSWORD_WEAK;
        }
        return RegistrationError.NONE;
    }
}
