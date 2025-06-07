package application.studyspace.services.DataBase;

import java.sql.*;

/**
 * The DatabaseConnection class provides functionality to establish and manage
 * a connection to a MariaDB database using JDBC. The connection is configured
 * with specific database credentials and URL details.
 */
public class DatabaseConnection {
    public Connection databaseLink;

    /**
     * Establishes and returns a connection to a MariaDB database using JDBC.
     * The connection is configured with predefined database credentials and URL.
     *
     * @return a Connection object representing the database connection
     * @throws RuntimeException if the JDBC driver is not found or if the connection fails
     */
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
