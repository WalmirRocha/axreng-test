package com.axreng.backend.util;

import static com.axreng.backend.util.Constants.EVN_BASE_URL;
import static com.axreng.backend.util.Constants.EVN_PROFILE;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axreng.backend.init.context.PropertiesLoader;

public class CrawlProperties {

    private static final Logger LOG = LoggerFactory.getLogger(CrawlProperties.class);
    private static CrawlProperties instance;
    private Properties properties;

    private CrawlProperties() {
        load();
    }

    public static CrawlProperties getInstance() {
        if (instance == null) {
            instance = new CrawlProperties();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {

        String value = properties.getProperty(key);
        if (value == null) {
            return null;
        }

        try {
            if (value.matches("-?\\d+")) { // Integer
                return (T) Integer.valueOf(value);
            } else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) { // Boolean
                return (T) Boolean.valueOf(value);
            } else if (value.matches("-?\\d+(\\.\\d+)?")) { // Double
                return (T) Double.valueOf(value);
            } else { // String
                return (T) value;
            }
        } catch (Exception e) {
            LOG.error("Error converting property: {} with value: {}", key, value, e);
            return null;
        }
    }

    public enum ProfileType {
        DEV,
        STG,
        TST,
        PRD,
        ERROR;
    }

    private void load() {

        String profile = getenv(Constants.EVN_PROFILE);
        String baseUrl = getenv(EVN_BASE_URL);

        if (ProfileType.valueOf(profile) == null) {
            LOG.error("Profile not found: {}", profile);
            return;
        }

        String profileFile = String.format("application-%s.properties", profile);
        this.properties = new Properties();

        try {
            URL urlProperties = PropertiesLoader.class.getClassLoader().getResource(profileFile);
            properties.load(urlProperties.openStream());
            properties.setProperty(EVN_PROFILE, profile);
            properties.setProperty(EVN_BASE_URL, baseUrl);
            LOG.info("Load profile file: {}", profileFile);
        } catch (IOException e) {
            LOG.error("Error load profile file: {}", profileFile, e);
        }
    }

    public String getenv(String evn) {
        String evnValue = System.getenv(evn);
        if (evnValue != null && !evnValue.isEmpty() && !"null".equalsIgnoreCase(evnValue)) {
            return evnValue;
        }
        evnValue = System.getProperty(evn);
        if (evnValue != null && !evnValue.isEmpty() && !"null".equalsIgnoreCase(evnValue)) {
            return evnValue;
        }

        return null;
    }
}
