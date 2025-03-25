package com.axreng.backend.init.persistence.datasource;

import static com.axreng.backend.util.Constants.CRAWL_H2_JDBC_URL;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axreng.backend.util.CrawlProperties;

public class DataSourceImpl implements DataSource {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceImpl.class);

    private final Driver driver;
    private final Properties properties;

    public DataSourceImpl(Driver driver, Properties properties) {
        this.driver = driver;
        this.properties = properties;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException("Unimplemented method 'getConnection'");
    }

    @Override
    public Connection getConnection() throws SQLException {

        Connection connection = null;
        try {
            connection = driver.connect(CrawlProperties.getInstance().getProperty(CRAWL_H2_JDBC_URL),
                    properties);
        } catch (SQLException e) {
            LOG.error("Error initializing database connection: {}", e.getMessage(), e);
            throw e;
        }

        if (connection == null) {

            LOG.error("Error initializing database connection for JDBC-URL: {}",
                    CrawlProperties.getInstance().getProperty(CRAWL_H2_JDBC_URL).toString());
            throw new SQLException("Error initializing database connection for JDBC-URL:" +
                    CrawlProperties.getInstance().getProperty(CRAWL_H2_JDBC_URL).toString());
        }

        return connection;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLogWriter(java.io.PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public java.util.logging.Logger getParentLogger() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }

}
