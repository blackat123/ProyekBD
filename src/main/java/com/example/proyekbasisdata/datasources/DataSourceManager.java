package com.example.proyekbasisdata.datasources;

import java.sql.Connection;
import java.sql.SQLException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSourceManager {
    private static HikariDataSource databaseData;

    static {
        databaseData = createDataSource("postgres", "admin");
    }

    private static HikariDataSource createDataSource(String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/ProyekBD");
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return new HikariDataSource(config);
    }

    public static Connection getDatabaseConnection() throws SQLException {
        return databaseData.getConnection();
    }

    private DataSourceManager() {
    }
}
