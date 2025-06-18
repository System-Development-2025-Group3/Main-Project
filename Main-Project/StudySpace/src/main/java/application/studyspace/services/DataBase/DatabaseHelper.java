package application.studyspace.services.DataBase;

import java.nio.ByteBuffer;
import java.sql.*;
import java.util.UUID;

public class DatabaseHelper {

    /**
     * Executes a SELECT query with optional WHERE clause and returns formatted results.
     *
     * @param what        columns to select (e.g. "*", or "email")
     * @param from        table name
     * @param whereColumn optional column name for filtering
     * @param whereValue  value to match for filtering (if whereColumn is provided)
     * @return string of formatted results (tab-separated rows) or empty string if none/failure
     */
    public static String SELECT(String what, String from, String whereColumn, String whereValue) {
        Connection connectDB = new DatabaseConnection().getConnection();

        // sanitize identifiers by escaping backticks
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

        try (PreparedStatement preparedStatement = connectDB.prepareStatement(selectQuery)) {
            if (whereColumn != null && !whereColumn.isEmpty()) {
                preparedStatement.setString(1, whereValue);
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (resultSet.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        result.append(resultSet.getString(i)).append("\t");
                    }
                    result.append("\n");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }

        return result.toString();
    }
    // … your existing SELECT(), uuidToBytes(), bytesToUUID(), etc. …

    /**
     * Look up a user's UUID (stored as BINARY(16)) by their email address.
     * @param email the user's email
     * @return the UUID if found, or null otherwise
     * @throws SQLException on database error
     */
    public static UUID getUserUUIDByEmail(String email) throws SQLException {
        String sql = "SELECT uuid FROM users WHERE email = ?";
        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    byte[] raw = rs.getBytes("uuid");
                    // delegate to UUIDHelper
                    return UUIDHelper.BytesToUUID(raw);
                }
            }
        }
        return null;
    }

    /**
     * Optionally: look up all user fields (or a subset) by their UUID.
     * @param uuid the user's UUID
     * @return a ResultSet (or map it into a user‐model object), or null if not found
     * @throws SQLException on database error
     */
    public static ResultSet getUserByUUID(UUID uuid) throws SQLException {
        String sql = "SELECT * FROM users WHERE uuid = ?";
        Connection conn = new DatabaseConnection().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        // delegate to UUIDHelper
        ps.setBytes(1, UUIDHelper.uuidToBytes(uuid));
        return ps.executeQuery();
    }

}
