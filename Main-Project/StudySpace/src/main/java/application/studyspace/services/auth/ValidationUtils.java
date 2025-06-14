package application.studyspace.services.auth;

import application.studyspace.services.DataBase.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class ValidationUtils {

    /**
     * A regular expression pattern used for validating the format of email addresses.
     * This pattern ensures that the email follows the standard structure, which includes:
     * - A combination of alphanumeric characters, dots, underscores, percent signs, plus, and hyphens
     *   as the local part of the email (before the "@" symbol).
     * - A valid domain name composed of alphanumeric characters, dots, and hyphens.
     * - A top-level domain (TLD) with at least two alphabetic characters.

     * Examples of valid email formats:
     * - example@example.com
     * - user.name+tag@sub.domain.org

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
            return true;
        }
        return !Pattern.compile(EMAIL_PATTERN).matcher(email).matches();
    }

    /**
     * Checks whether the provided email exists in the database.
     *
     * @param email the email address to check for existence in the database
     * @return true if the email exists, false otherwise
     */
    public static boolean isKnownEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        String sql = """
            SELECT email FROM users u WHERE email = ?;""";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email); // Bind the email parameter to the query

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // If a row is returned, the email exists in the database
                    return true;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    
/**
 * Checks whether the given input email is similar to any email in the provided list
 * based on the specified Levenshtein distance threshold. This method normalizes
 * the input email and list of emails by converting them to lowercase and trimming
 * any leading or trailing whitespace. It then compares the Levenshtein distance
 * between the input email and each email in the list, returning true if a similar
 * email is found within the threshold.
 *
 * @param inputEmail The email address to be checked for similarity.
 * @param emailList A list of email addresses to compare against.
 * @param threshold The maximum allowable Levenshtein distance for two emails to be considered similar.
 * @return true if a similar email is found within the given threshold, false otherwise.
 */
public static boolean isSimilarEmail(String inputEmail, List<String> emailList, int threshold) {
    if (inputEmail == null || emailList == null || emailList.isEmpty()) return false;

    LevenshteinDistance distanceCalculator = new LevenshteinDistance(threshold);

    inputEmail = inputEmail.toLowerCase().trim(); // Normalize input email

    for (String email : emailList) {
        String normalizedEmail = email.toLowerCase().trim(); // Normalize emails in list
        int distance = distanceCalculator.apply(inputEmail, normalizedEmail);

        if (distance != -1 && distance <= threshold) { // Ensure distance is valid
            System.out.println("Similar email found: " + email + " (distance: " + distance + ")");
            return true;
        }
    }
    System.out.println("No similar email found within the threshold: " + threshold);
    return false;
}

    /**
     * Retrieves a list of known email addresses from the database.
     * This method queries the "users" table in the database to fetch all
     * email addresses and returns them as a list of strings.
     *
     * @return a List of strings containing email addresses retrieved from the database
     */
    public static List<String> listOfKnownEmails() {
    List<String> emailList = new ArrayList<>();

    String sql = """
        SELECT email FROM users u;
    """;

    try (Connection conn = new DatabaseConnection().getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            emailList.add(rs.getString("email"));
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return emailList;
    }
    
}