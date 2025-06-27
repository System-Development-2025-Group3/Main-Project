package application.studyspace.services.DataBase;


import java.nio.ByteBuffer;
import java.sql.*;
import java.util.UUID;

import static application.studyspace.services.DataBase.UUIDHelper.uuidToBytes;


public class DatabaseHelper {
    /**
     * Retrieves a UUID from the database using an email address.
     * Assumes that the UUID is stored as BINARY(16) in the "id" column.
     * @param email the user's email address
     * @return the user's UUID if found, null otherwise
     * @throws SQLException if a database error occurs
     */
    public UUID getUserUUIDByEmail(String email) throws SQLException {
        String query = "SELECT user_id FROM users WHERE email = ?";
        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {


            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();


            if (rs.next()) {
                byte[] uuidBytes = rs.getBytes("user_id");
                ByteBuffer byteBuffer = ByteBuffer.wrap(uuidBytes);
                long high = byteBuffer.getLong();
                long low = byteBuffer.getLong();
                return new UUID(high, low);
            } else {
                return null;
            }
        }
    }

    /**
     * Retrieves the email associated with a given UUID from the users table.
     *
     * @param userId The UUID of the user whose email needs to be fetched.
     * @return The email if found, null otherwise.
     * @throws SQLException If there is an issue with the database connectivity or query execution.
     */
    public static String getEmailByUUID(UUID userId) throws SQLException {
        String email = null;
        String query = "SELECT email FROM users WHERE user_id = ?";

        try (Connection connection = new DatabaseConnection().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Convert UUID to a byte array if it's stored as binary(16) in the database.
            statement.setBytes(1, uuidToBytes(userId));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    email = resultSet.getString("email");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error while retrieving email for UUID: " + userId);
            e.printStackTrace();
            throw e; // Re-throw the exception for further handling if needed.
        }

        return email;
    }

}



