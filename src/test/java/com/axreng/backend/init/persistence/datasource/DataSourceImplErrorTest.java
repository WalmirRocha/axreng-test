package com.axreng.backend.init.persistence.datasource;

import static com.axreng.backend.util.Constants.CRAWL_H2_PASSWORD;
import static com.axreng.backend.util.Constants.CRAWL_H2_USERNAME;
import static com.axreng.backend.util.Constants.CRAWL_PASSWORD;
import static com.axreng.backend.util.Constants.CRAWL_USER;
import static com.axreng.backend.util.Constants.EVN_BASE_URL;
import static com.axreng.backend.util.Constants.EVN_PROFILE;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.Properties;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axreng.backend.init.context.PropertiesLoader;
import com.axreng.backend.util.CrawlProperties;

public class DataSourceImplErrorTest {

    private CrawlProperties properties;

    @BeforeEach
    public void setUp() {
        System.setProperty(EVN_BASE_URL, "http://example.com");
        System.setProperty(EVN_PROFILE, "ERROR");
        properties = CrawlProperties.getInstance();
    }

    private DataSource createDataSource(String pathJar, String classDrive) throws Exception {
        Properties props = new Properties();
        props.setProperty(CRAWL_USER, properties.getProperty(CRAWL_H2_USERNAME));
        props.setProperty(CRAWL_PASSWORD, properties.getProperty(CRAWL_H2_PASSWORD));

        URL jarPath = PropertiesLoader.class.getClassLoader().getResource(pathJar);
        if (jarPath == null) {
            throw new ExceptionInInitializerError("Path not found: " + pathJar);
        }
        String urlJar = "jar:" + jarPath.toString() + "!/";

        URL[] urls = new URL[] { new URL(urlJar) };
        URLClassLoader classLoader = new URLClassLoader(urls);
        Class<?> driverClass = Class.forName(classDrive, true, classLoader);
        Driver driver = (Driver) driverClass.getDeclaredConstructor().newInstance();
        if (driver == null) {
            throw new ExceptionInInitializerError("Class not found: " + classDrive);
        }
        return new DataSourceImpl(driver, props);
    }

    @Test
    public void testGetConnectionWithUsernameAndPasswordThrowsException() {
        assertThrows(ExceptionInInitializerError.class, () -> createDataSource("pathJar", classDrive).getConnection());
    }

    @Test
    public void testGetConnectionSQLException() throws Exception {
        assertThrows(ClassNotFoundException.class,
                () -> createDataSource("lib/h2-2.2.224.jar", "org.h2.Driver1").getConnection());
    }

}