package application.studyspace.services.DataBase;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceManager {
    private static final HikariDataSource ds;
    static {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:mariadb://mysql-systemdevelopment2025.alwaysdata.net:3306/systemdevelopment2025_maindatabase");
        cfg.setUsername("408880");
        cfg.setPassword("cefhyp-7jerba-qikGyr");
        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(2);
        cfg.setPoolName("StudyspacePool");
        ds = new HikariDataSource(cfg);
    }

    private DataSourceManager(){}

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
