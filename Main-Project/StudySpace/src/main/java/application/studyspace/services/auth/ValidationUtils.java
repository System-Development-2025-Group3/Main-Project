package application.studyspace.services.auth;

import application.studyspace.services.DataBase.DatabaseConnection;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class ValidationUtils {
    // ----------------------------------------------------------------
    // Email regex used everywhere
    // ----------------------------------------------------------------
    private static final String EMAIL_PATTERN =
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    // ----------------------------------------------------------------
    // 1) The old login/DB helpers
    // ----------------------------------------------------------------

    /** Returns true if the email is syntactically valid. */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return Pattern.compile(EMAIL_PATTERN).matcher(email).matches();
    }

    /** Returns true if that exact email exists in your `users` table. */
    public static boolean isKnownEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        String sql = "SELECT 1 FROM users WHERE email = ?;";
        try ( Connection conn = new DatabaseConnection().getConnection();
              PreparedStatement ps = conn.prepareStatement(sql) ) {
            ps.setString(1, email);
            try ( ResultSet rs = ps.executeQuery() ) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Fetches all emails from the DB for “did you mean …” suggestions. */
    public static List<String> listOfKnownEmails() {
        List<String> out = new ArrayList<>();
        String sql = "SELECT email FROM users;";
        try ( Connection conn = new DatabaseConnection().getConnection();
              PreparedStatement ps = conn.prepareStatement(sql);
              ResultSet rs = ps.executeQuery() ) {
            while (rs.next()) out.add(rs.getString("email"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    /**
     * Uses a Levenshtein threshold to see if the user maybe just
     * typo’d their address by a small edit distance.
     */
    public static boolean isSimilarEmail(String inputEmail, List<String> emailList, int threshold) {
        if (inputEmail == null || emailList == null || emailList.isEmpty()) return false;
        LevenshteinDistance dist = new LevenshteinDistance(threshold);
        String normIn = inputEmail.toLowerCase().trim();
        for (String e : emailList) {
            String norm = e.toLowerCase().trim();
            int d = dist.apply(normIn, norm);
            if (d != -1 && d <= threshold) {
                System.out.println("Similar email: " + e + " (dist=" + d + ")");
                return true;
            }
        }
        return false;
    }

    /**
     * Returns "VALID" if the password meets your policy,
     * otherwise one of "NOT_LONG_ENOUGH","NO_UPPERCASE","NO_NUMBER","NO_SPECIAL_CHAR"
     */
    public static String isValidPassword(String password) {
        if (password == null) return "NO_SPECIAL_CHAR";
        if (password.length() < 12)        return "NOT_LONG_ENOUGH";
        if (!password.matches(".*[A-Z].*")) return "NO_UPPERCASE";
        if (!password.matches(".*\\d.*"))   return "NO_NUMBER";
        if (!password.matches(".*[%&!?#_\\-\\$].*"))
            return "NO_SPECIAL_CHAR";
        return "VALID";
    }

    // ----------------------------------------------------------------
    // 2) The new registration‐validator API
    // ----------------------------------------------------------------

    public enum RegistrationError {
        NONE,
        EMAIL_EMPTY,
        EMAIL_INVALID,
        PASSWORD_EMPTY,
        PASSWORD_MISMATCH,
        PASSWORD_WEAK
    }

    /**
     * A single‐call registration validator for your RegisterController.
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
        // reuse isValidPassword
        if (!isValidPassword(pw1).equals("VALID")) {
            return RegistrationError.PASSWORD_WEAK;
        }
        return RegistrationError.NONE;
    }
}
