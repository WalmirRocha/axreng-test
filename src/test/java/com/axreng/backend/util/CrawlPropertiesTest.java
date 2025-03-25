
package com.axreng.backend.util;

import static com.axreng.backend.util.Constants.CRAWL_MAX_DEPTH;
import static com.axreng.backend.util.Constants.EVN_BASE_URL;
import static com.axreng.backend.util.Constants.EVN_PROFILE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CrawlPropertiesTest {

    @BeforeEach
    public void setUp() {
        System.setProperty(EVN_BASE_URL, "http://example.com");
        System.setProperty(EVN_PROFILE, "TST");
    }

    @Test
    public void testGetInstance() {
        CrawlProperties instance1 = CrawlProperties.getInstance();
        CrawlProperties instance2 = CrawlProperties.getInstance();
        assertEquals(instance1, instance2);
    }

    @Test
    public void testGetProperty_String() {
        String propertyValue = CrawlProperties.getInstance().getProperty(EVN_BASE_URL);
        assertNotNull(propertyValue);
    }

    @Test
    public void testGetProperty_Integer() {
        Integer propertyValue = CrawlProperties.getInstance().getProperty(CRAWL_MAX_DEPTH);
        assertNotNull(propertyValue);
    }

    @Test
    public void testGetProperty_Boolean() {
        Boolean propertyValue = CrawlProperties.getInstance().getProperty("Boolean");
        assertNull(propertyValue);
    }

    @Test
    public void testGetProperty_Double() {

        Double propertyValue = CrawlProperties.getInstance().getProperty("Double");
        assertNull(propertyValue);
    }

    @Test
    public void testGetProperty_NotFound() {
        assertNull(CrawlProperties.getInstance().getProperty("REST"));
    }

    @Test
    public void testGetenv() {
        assertEquals(null, CrawlProperties.getInstance().getenv("ERROR"));
    }
}