package com.axreng.backend.init.persistence.datasource;

import static com.axreng.backend.util.Constants.CRAWL_H2_DRIVER;
import static com.axreng.backend.util.Constants.CRAWL_H2_JAR;
import static com.axreng.backend.util.Constants.CRAWL_H2_PASSWORD;
import static com.axreng.backend.util.Constants.CRAWL_H2_USERNAME;
import static com.axreng.backend.util.Constants.CRAWL_PASSWORD;
import static com.axreng.backend.util.Constants.CRAWL_USER;
import static com.axreng.backend.util.Constants.EVN_BASE_URL;
import static com.axreng.backend.util.Constants.EVN_PROFILE;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axreng.backend.init.context.PropertiesLoader;
import com.axreng.backend.util.CrawlProperties;

public class DataSourceImplTest {

    private CrawlProperties properties;

    @BeforeEach
    public void setUp() {
        System.setProperty(EVN_BASE_URL, "http://example.com");
        System.setProperty(EVN_PROFILE, "TST");
        properties = CrawlProperties.getInstance();
    }

    private DataSourceImpl createDataSource() throws Exception {
        Properties props = new Properties();
        props.setProperty(CRAWL_USER, properties.getProperty(CRAWL_H2_USERNAME));
        props.setProperty(CRAWL_PASSWORD, properties.getProperty(CRAWL_H2_PASSWORD));
        URL jarPath = PropertiesLoader.class.getClassLoader().getResource(properties.getProperty(CRAWL_H2_JAR));
        if (jarPath == null) {
            throw new ExceptionInInitializerError("Path not found: " + properties.getProperty(CRAWL_H2_JAR));
        }
        String urlJar = "jar:" + jarPath.toString() + "!/";

        URL[] urls = new URL[] { new URL(urlJar) };
        URLClassLoader classLoader = new URLClassLoader(urls);
        Class<?> driverClass = Class.forName(properties.getProperty(CRAWL_H2_DRIVER), true, classLoader);
        Driver driver = (Driver) driverClass.getDeclaredConstructor().newInstance();

        if (driver == null) {
            throw new ExceptionInInitializerError("Class not found: " + properties.getProperty(CRAWL_H2_DRIVER));
        }

        return new DataSourceImpl(driver, props);
    }

    @Test
    public void testGetConnectionSuccess() throws SQLException, Exception {

        Connection connection = createDataSource().getConnection();
        assertNotNull(connection);
    }

}