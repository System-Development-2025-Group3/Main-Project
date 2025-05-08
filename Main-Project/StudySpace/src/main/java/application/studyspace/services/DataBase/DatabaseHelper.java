package application.studyspace.services.DataBase;

import java.nio.ByteBuffer;
import java.sql.*;
import java.util.UUID;

public class DatabaseHelper {

    /**
     * Executes a SELECT query on the database using the specified parameters.
     * Constructs a SQL SELECT statement dynamically by escaping any user inputs,
     * and optionally includes a WHERE clause. Returns the results as a formatted string.
     *
     * @param what the columns to retrieve in the query, e.g., "*" or specific column names
     * @param from the table name to retrieve data from
     * @param whereColumn the column name to use in the WHERE clause (optional, can be null or empty)
     * @param whereValue the value to match in the WHERE clause (only used if whereColumn is provided)
     * @return a formatted string of the query results, or an empty string in case of an error
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

    public UUID getUserUUIDByEmail(String email) throws SQLException {
        String query = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                byte[] uuidBytes = rs.getBytes("id");
                ByteBuffer byteBuffer = ByteBuffer.wrap(uuidBytes);
                long high = byteBuffer.getLong();
                long low = byteBuffer.getLong();
                return new UUID(high, low);
            } else {
                return null;
            }
        }
    }

    public byte[] uuidToBytes(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }





}