package application.studyspace.services.DataBase;

import java.sql.*;

public class DatabaseConnection {
    public Connection databaseLink;

    public Connection getConnection() {
        String databaseName = "systemdevelopment2025_maindatabase";
        String databaseUser = "408880";
        String databasePassword = "cefhyp-7jerba-qikGyr";
        String url = "jdbc:mariadb://mysql-systemdevelopment2025.alwaysdata.net:3306/" + databaseName;


        try {
            Class.forName("org.mariadb.jdbc.Driver");
            databaseLink = DriverManager.getConnection(url, databaseUser, databasePassword);
            System.out.println("Database connection successful!");
            return databaseLink;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver class not found. Check if MariaDB driver is added!", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database. Check URL, username, password!", e);
        }
    }
}
