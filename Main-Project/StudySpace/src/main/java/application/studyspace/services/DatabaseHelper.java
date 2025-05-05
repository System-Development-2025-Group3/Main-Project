package application.studyspace.services;

import java.sql.*;

public class DatabaseHelper {

    public static String select(String what, String from, String whereColumn, String whereValue) {
        Connection connectDB = new DatabaseConnection().getConnection();

        // Escape identifiers for safety
        String escapedWhat = "`" + what.replace("`", "``") + "`";
        String escapedFrom = "`" + from.replace("`", "``") + "`";
        String escapedWhereColumn = "`" + whereColumn.replace("`", "``") + "`";

        // Construct query
        String selectQuery = "SELECT " + escapedWhat + " FROM " + escapedFrom + " WHERE " + escapedWhereColumn + " = ?";
        System.out.println("SQL Query: " + selectQuery);

        try {
            PreparedStatement preparedStatement = connectDB.prepareStatement(selectQuery);
            preparedStatement.setString(1, whereValue);

            ResultSet resultSet = preparedStatement.executeQuery();

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Process result set
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
        return selectQuery;
    }


    public static boolean userCheck(String username, String password) {
        Connection connectDB = new DatabaseConnection().getConnection();
        String selectQuery = "SELECT * FROM users WHERE email = ? AND password_hash = ?";
        try{
            PreparedStatement preparedStatement =  connectDB.prepareStatement(selectQuery);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2,password);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();

            } catch (SQLException e){
                e.printStackTrace();
                System.out.println("Error: "+ e.getMessage());
            }
        return false;
    }
}