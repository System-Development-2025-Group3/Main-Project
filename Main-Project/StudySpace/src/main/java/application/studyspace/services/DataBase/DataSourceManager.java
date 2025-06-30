package application.studyspace.services.DataBase;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Central singleton providing pooled JDBC connections using HikariCP.
 *
 * This class initializes the connection pool once at application startup,
 * and exposes a static method to retrieve connections.
 */
public class DataSourceManager {

    // The single shared connection pool instance
    private static final HikariDataSource ds;

    // Static initializer block: runs ONCE when this class is loaded
    static {
        // Create the configuration object for HikariCP
        HikariConfig cfg = new HikariConfig();

        // Set JDBC connection URL to your MariaDB server
        cfg.setJdbcUrl("jdbc:mariadb://mysql-systemdevelopment2025.alwaysdata.net:3306/systemdevelopment2025_maindatabase");

        // Database credentials
        cfg.setUsername("408880");
        cfg.setPassword("cefhyp-7jerba-qikGyr");

        // The maximum number of connections that can be in the pool at once
        cfg.setMaximumPoolSize(10);

        // The minimum number of idle connections to keep around
        cfg.setMinimumIdle(2);

        // A friendly pool name (helps debugging connection leaks)
        cfg.setPoolName("StudyspacePool");

        // Initialize the datasource (this actually opens the pool)
        ds = new HikariDataSource(cfg);
    }

    // Private constructor to prevent instantiation (static-only utility)
    private DataSourceManager() {}

    /**
     * Retrieves a connection from the pool.
     * IMPORTANT: You must always call conn.close() when done,
     * or the connection won't be returned to the pool.
     *
     * @return a ready-to-use Connection
     * @throws SQLException if the pool cannot provide a connection
     */
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
