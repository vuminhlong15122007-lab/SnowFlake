package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DatabaseConnectionException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public final class JDBCUtil {
    private static final Logger log = LoggerFactory.getLogger(JDBCUtil.class);

    private static final String DEFAULT_HOST = "gateway01.ap-southeast-1.prod.aws.tidbcloud.com";
    private static final String DEFAULT_PORT = "4000";
    private static final String DEFAULT_DATABASE = "test";
    private static final String DEFAULT_USERNAME = "3sSzrSFdZfqFKd5.root";
    private static final String DEFAULT_PASSWORD = "8J1D7oKnbj8npKF1";

    private static final HikariDataSource dataSource = createDataSource();

    private JDBCUtil() {
    }

    public static Connection getConnection() throws DatabaseConnectionException {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error("Loi lay ket noi database tu pool: {}", e.getMessage(), e);
            throw new DatabaseConnectionException(e);
        }
    }

    public static void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            log.warn("Loi khi dong ket noi: {}", e.getMessage());
        }
    }

    private static HikariDataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(configValue("snowflake.db.url", "SNOWFLAKE_DB_URL", defaultUrl()));
        config.setUsername(configValue("snowflake.db.user", "SNOWFLAKE_DB_USER", DEFAULT_USERNAME));
        config.setPassword(configValue("snowflake.db.password", "SNOWFLAKE_DB_PASSWORD", DEFAULT_PASSWORD));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(intConfig("snowflake.db.pool.max", "SNOWFLAKE_DB_POOL_MAX", 10));
        config.setMinimumIdle(intConfig("snowflake.db.pool.minIdle", "SNOWFLAKE_DB_POOL_MIN_IDLE", 2));
        config.setIdleTimeout(longConfig(
                "snowflake.db.pool.idleTimeoutMs",
                "SNOWFLAKE_DB_POOL_IDLE_TIMEOUT_MS",
                300_000L
        ));
        config.setMaxLifetime(longConfig(
                "snowflake.db.pool.maxLifetimeMs",
                "SNOWFLAKE_DB_POOL_MAX_LIFETIME_MS",
                1_800_000L
        ));
        config.setConnectionTimeout(longConfig(
                "snowflake.db.pool.connectionTimeoutMs",
                "SNOWFLAKE_DB_POOL_CONNECTION_TIMEOUT_MS",
                10_000L
        ));
        config.setPoolName("SnowFlakePool");
        return new HikariDataSource(config);
    }

    private static String defaultUrl() {
        String host = configValue("snowflake.db.host", "SNOWFLAKE_DB_HOST", DEFAULT_HOST);
        String port = configValue("snowflake.db.port", "SNOWFLAKE_DB_PORT", DEFAULT_PORT);
        String database = configValue("snowflake.db.name", "SNOWFLAKE_DB_NAME", DEFAULT_DATABASE);
        return "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?enabledTLSProtocols=TLSv1.2,TLSv1.3&sslMode=REQUIRED";
    }

    private static String configValue(String propertyName, String envName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }

        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        return defaultValue;
    }

    private static int intConfig(String propertyName, String envName, int defaultValue) {
        return Integer.parseInt(configValue(propertyName, envName, String.valueOf(defaultValue)));
    }

    private static long longConfig(String propertyName, String envName, long defaultValue) {
        return Long.parseLong(configValue(propertyName, envName, String.valueOf(defaultValue)));
    }
}
