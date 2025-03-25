package com.axreng.backend.init.context;

import static com.axreng.backend.util.Constants.CRAWL_H2_DRIVER;
import static com.axreng.backend.util.Constants.CRAWL_H2_JAR;
import static com.axreng.backend.util.Constants.CRAWL_H2_JDBC_URL;
import static com.axreng.backend.util.Constants.CRAWL_H2_PASSWORD;
import static com.axreng.backend.util.Constants.CRAWL_H2_SQL_INIT;
import static com.axreng.backend.util.Constants.CRAWL_H2_USERNAME;
import static com.axreng.backend.util.Constants.CRAWL_PASSWORD;
import static com.axreng.backend.util.Constants.CRAWL_USER;
import static com.axreng.backend.util.Constants.EVN_BASE_URL;
import static com.axreng.backend.util.Constants.EVN_PROFILE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axreng.backend.init.persistence.datasource.DataSourceImpl;
import com.axreng.backend.persistence.dao.CrawlDao;
import com.axreng.backend.persistence.dao.NamedQueryLoader;
import com.axreng.backend.service.CrawlService;
import com.axreng.backend.util.CrawlProperties;
import com.axreng.backend.util.ValidationUtil;

public class LoaderInicializer {

    private static final Logger LOG = LoggerFactory.getLogger(LoaderInicializer.class);

    private final CrawlProperties properties;
    private final ValidationUtil validationUtil = ValidationUtil.getInstance();
    private DataSource dataSource;

    public void init() throws Exception {
        validateFieldInitialized();
        createDataSource();
        createScriptSchemaSqlInit();
        setBeanContext();
    }

    public LoaderInicializer(CrawlProperties properties){
        this.properties = properties;
    }

    @SuppressWarnings("unused")
    private void validateFieldInitialized() throws Exception {

        URL urlSqlInit = PropertiesLoader.class.getClassLoader().getResource(properties.getProperty(CRAWL_H2_SQL_INIT));
        if (urlSqlInit == null) {
            throw new ExceptionInInitializerError("Path not found: " + properties.getProperty(CRAWL_H2_SQL_INIT));
        }

        URL urlJar = PropertiesLoader.class.getClassLoader().getResource(properties.getProperty(CRAWL_H2_JAR));
        if (urlSqlInit == null) {
            throw new ExceptionInInitializerError("Path not found: " + properties.getProperty(CRAWL_H2_JAR));
        }

        String baseUrl = properties.getenv(EVN_BASE_URL);
        validationUtil.validateProperty(baseUrl, EVN_BASE_URL);

        String profile = properties.getenv(EVN_PROFILE);
        validationUtil.validateProperty(profile, EVN_PROFILE);

        validationUtil.validateProperty(urlSqlInit.toString(), CRAWL_H2_JAR);
        validationUtil.validateProperty(urlJar.toString(), CRAWL_H2_SQL_INIT);
        validationUtil.validateProperty(properties.getProperty(CRAWL_H2_JDBC_URL), CRAWL_H2_JDBC_URL);
        validationUtil.validateProperty(properties.getProperty(CRAWL_H2_DRIVER), CRAWL_H2_DRIVER);
        validationUtil.validateProperty(properties.getProperty(CRAWL_H2_USERNAME), CRAWL_H2_USERNAME);
        validationUtil.validateProperty(properties.getProperty(CRAWL_H2_PASSWORD), CRAWL_H2_PASSWORD);
    }

    private void createDataSource() throws MalformedURLException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {

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
        Properties props = new Properties();
        props.setProperty(CRAWL_USER, properties.getProperty(CRAWL_H2_USERNAME));
        props.setProperty(CRAWL_PASSWORD, properties.getProperty(CRAWL_H2_PASSWORD));

        dataSource = new DataSourceImpl(driver, props);
    }

    private void createScriptSchemaSqlInit() throws Exception {

        LOG.info("Starting H2 database...");

        URL urlJar = PropertiesLoader.class.getClassLoader().getResource(properties.getProperty(CRAWL_H2_SQL_INIT));

        String sqlFleContent = "";

        try (InputStream inputStream = urlJar.openStream()) {
            if (inputStream == null) {
                throw new IOException("InputStream is null for URL: " + urlJar);
            }

            sqlFleContent = new String(convertToBytes(inputStream));
            LOG.info("SQL file content successfully loaded.");
        } catch (IOException e) {
            LOG.error("Error while loading SQL file content: {}", e.getMessage(), e);
            throw e; // Re-throw the exception if necessary
        }

        try (Connection conn = dataSource.getConnection()) {

            LOG.info("Executing SQL commands...");
            String[] commandsSql = sqlFleContent.split(";");

            for (String sql : commandsSql) {
                if (!sql.trim().isEmpty()) {
                    LOG.info("Executing SQL command: {}", sql);

                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(sql.trim());
                    } catch (Exception e) {
                        LOG.error("Error while executing SQL command: {}", sql, e);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error while establishing database connection or executing SQL commands: {}", e.getMessage(), e);
            throw e;
        }
    }

    private byte[] convertToBytes(InputStream inputStream) throws IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int bytesRead;

        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }

        return buffer.toByteArray();
    }

    private void setBeanContext() {
        NamedQueryLoader namedQueryLoader = NamedQueryLoader.getInstance();
        BeanContext.getContext().set(NamedQueryLoader.class, namedQueryLoader);
        BeanContext.getContext().set(DataSource.class, dataSource);
        BeanContext.getContext().set(CrawlService.class,
                new CrawlService(this.properties, new CrawlDao(namedQueryLoader, dataSource)));
    }
}
