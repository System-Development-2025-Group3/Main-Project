package application.studyspace.services.DataBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Try env var first; if missing, use your local dev DB settings
    private static final String URL = System.getenv().getOrDefault(
            "DB_URL",
            "jdbc:mariadb://localhost:3306/studyspace_dev?useUnicode=true&characterEncoding=UTF-8"
    );
    private static final String USER = System.getenv().getOrDefault("DB_USER", "dev");
    private static final String PW   = System.getenv().getOrDefault("DB_PASS", "devpass");

    public Connection getConnection() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PW);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MariaDB JDBC driver not found", e);
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to connect to database at " + URL +
                            " (user=" + USER + ")", e
            );
        }
    }
}
