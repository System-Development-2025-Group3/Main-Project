package application.studyspace.services.DataBase;


import java.nio.ByteBuffer;
import java.sql.*;
import java.util.UUID;


public class DatabaseHelper {


    /**
     * Converts a UUID to a 16-byte array for storing in a BINARY(16) database field.
     * @param uuid the UUID to convert
     * @return byte array representing the UUID
     */
    public static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }


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


    /**
     * Executes a SELECT query with optional WHERE clause and returns formatted results.
     * @param what columns to select (e.g. "*", or "email")
     * @param from table name
     * @param whereColumn optional column name for filtering
     * @param whereValue value to match for filtering (if whereColumn is provided)
     * @return string of formatted results or empty string if failed
     */
    public static String SELECT(String what, String from, String whereColumn, String whereValue) {
        Connection connectDB = new DatabaseConnection().getConnection();


        String escapedWhat = "`" + what.replace("`", "``") + "`";
        String escapedFrom = "`" + from.replace("`", "``") + "`";


        String selectQuery;
        if (whereColumn != null && !whereColumn.isEmpty()) {
            String escapedWhereColumn = "`" + whereColumn.replace("`", "``") + "`";
            selectQuery = "SELECT " + escapedWhat + " FROM " + escapedFrom + " WHERE " + escapedWhereColumn + " = ?";
        } else {
            selectQuery = "SELECT " + escapedWhat + " FROM " + escapedFrom;
        }


        System.out.println("SQL Query: " + selectQuery);


        StringBuilder result = new StringBuilder();


        try {
            PreparedStatement preparedStatement;
            if (whereColumn != null && !whereColumn.isEmpty()) {
                preparedStatement = connectDB.prepareStatement(selectQuery);
                preparedStatement.setString(1, whereValue);
            } else {
                preparedStatement = connectDB.prepareStatement(selectQuery);
            }


            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();


            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    result.append(resultSet.getString(i)).append("\t");
                }
                result.append("\n");
            }


        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }


        return result.toString();
    }
}



