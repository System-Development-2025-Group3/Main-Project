package application.studyspace;

import java.sql.*;

public class DatabaseHelper {

    public static void select(String what, String from, String whereColumn, String whereValue) {
        Connection connectDB = new DatabaseConnection().getConnection();

        String selectQuery = "SELECT " + what + " FROM " + from + " WHERE " + whereColumn + " = ?";

        try {
            PreparedStatement preparedStatement = connectDB.prepareStatement(selectQuery);
            preparedStatement.setString(1, whereValue);

            ResultSet resultSet = preparedStatement.executeQuery();


            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();


            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {

                    System.out.print(resultSet.getString(i) + "\t");
                }
                System.out.println();

            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }
    }

}

