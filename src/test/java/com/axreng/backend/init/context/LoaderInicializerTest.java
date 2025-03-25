
package com.axreng.backend.init.context;

import static com.axreng.backend.util.Constants.EVN_BASE_URL;
import static com.axreng.backend.util.Constants.EVN_PROFILE;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axreng.backend.util.CrawlProperties;

public class LoaderInicializerTest {

    private LoaderInicializer loaderInicializer;
    private CrawlProperties properties;

    @BeforeEach
    public void setUp() {
        System.setProperty(EVN_BASE_URL, "http://example.com");
        System.setProperty(EVN_PROFILE, "TST");
        this.properties = CrawlProperties.getInstance();    
    }

    @Test
    public void testInit() throws Exception {
        this.loaderInicializer = new LoaderInicializer(properties);
        loaderInicializer.init();

    }

    @Test
    void testInit_throws_NullPointerException() {

        LoaderInicializer loaderInicializerMock = new LoaderInicializer(null);
        assertThrows(NullPointerException.class, () -> loaderInicializerMock.init());

    }

    // @Test
    // void testInit_throwsExceptionWhenSqlInitPathNotFound() {

    //     LoaderInicializer loaderInicializerMock = new LoaderInicializer(null);
    //     assertThrows(NullPointerException.class, () -> loaderInicializerMock.init());

    // }
}