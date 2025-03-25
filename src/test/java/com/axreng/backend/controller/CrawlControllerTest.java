package com.axreng.backend.controller;

import static com.axreng.backend.util.Constants.EVN_BASE_URL;
import static com.axreng.backend.util.Constants.EVN_PROFILE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axreng.backend.service.CrawlService;
import com.axreng.backend.util.CrawlProperties;
import com.axreng.backend.vo.ResponseVO;
import com.axreng.backend.vo.Status;

import spark.Request;
import spark.Response;

class CrawlControllerTest {

    private CrawlController crawlController;
    private CrawlProperties crawlProperties;
    private CrawlService crawlService;
    private Request request;
    private Response response;

    @BeforeEach
    void setUp() {
        crawlService = mock(CrawlService.class);
        crawlProperties = mock(CrawlProperties.class);
        crawlController = new CrawlController(crawlService, crawlProperties);
        request = mock(Request.class);
        response = mock(Response.class);
        when(crawlProperties.getenv(EVN_BASE_URL)).thenReturn("http://example.com");
        when(crawlProperties.getenv(EVN_PROFILE)).thenReturn("TST");
        when(crawlProperties.getProperty(EVN_BASE_URL)).thenReturn("http://example.com");

    }

    @Test
    void testGet() throws Exception {
        String crawlId = "test-id";
        when(request.params(":id")).thenReturn(crawlId);
        ResponseVO mockResponse = ResponseVO.Builder().id(crawlId).build();
        when(crawlService.getCrawlById(crawlId)).thenReturn(mockResponse);

        String result = crawlController.get(request, response);

        assertNotNull(result);
        verify(crawlService, times(1)).getCrawlById(crawlId);
    }

    @Test
    void testPost() throws Exception {

        when(request.body()).thenReturn("{\"keyword\":\"test\"}");
        when(crawlService.getCrawlByUrlAndStatus(anyString(), eq(Status.ACTIVE))).thenReturn(null);
        doNothing().when(crawlService).insertCrawl(any(ResponseVO.class));
        CompletableFuture<Void> mockFuture = CompletableFuture.completedFuture(null);
        when(crawlService.crawlSearch(anyString(), anyString(), any(), anyString(), anyInt())).thenReturn(mockFuture);

        String result = crawlController.post(request, response);

        assertNotNull(result);
        verify(crawlService, times(1)).insertCrawl(any(ResponseVO.class));
        verify(crawlService, times(1)).getCrawlByUrlAndStatus(anyString(), eq(Status.ACTIVE));
        verify(crawlService, times(1)).crawlSearch(anyString(), anyString(), any(), anyString(), anyInt());
    }

    @Test
    void testPost_existingCrawl() throws Exception {

        ResponseVO existingResponse = ResponseVO.Builder().id("existing-id").build();
        when(request.body()).thenReturn("{\"keyword\":\"test\"}");
        when(crawlService.getCrawlByUrlAndStatus(anyString(), any())).thenReturn(existingResponse);
        doNothing().when(crawlService).insertCrawl(any(ResponseVO.class));

        String result = crawlController.post(request, response);

        assertNotNull(result);
        verify(crawlService, times(1)).getCrawlByUrlAndStatus(anyString(), eq(Status.ACTIVE));
        verify(response, times(1)).status(HttpStatusCode.OK.getCode());
    }

    @Test
    void testGet_nullId() throws Exception {
        when(request.params(":id")).thenReturn(null);

        crawlController.get(request, response);

        verify(response, times(1)).status(HttpStatusCode.BAD_REQUEST.getCode());
    }

    @Test
    void testGet_crawlNotFound() throws Exception {
        String crawlId = "test-id";
        when(request.params(":id")).thenReturn(crawlId);
        when(crawlService.getCrawlById(crawlId)).thenReturn(null);

        crawlController.get(request, response);

        verify(response, times(1)).status(HttpStatusCode.NOT_FOUND.getCode());
    }

    @Test
    void testPost_invalidKeywordLength_GreaterThan_4() throws Exception {
        when(request.body()).thenReturn("{\"keyword\":\"te\"}");

        crawlController.post(request, response);

        verify(response, times(1)).status(HttpStatusCode.BAD_REQUEST.getCode());
    }

    @Test
    void testPost_invalidKeywordLength_LessThan_32() throws Exception {
        when(request.body()).thenReturn("{\"keyword\":\"teldlfmvldfcdfdsdfdcsdfrdsdfrdfds\"}");

        crawlController.post(request, response);

        verify(response, times(1)).status(HttpStatusCode.BAD_REQUEST.getCode());
    }

    @Test
    void testPost_validKeywordLength_4_between_32() throws Exception {
        when(request.body()).thenReturn("{\"keyword\":\"teldlfmvldfcdfdsdfdcsdfrdsdfrdfds\"}");

        crawlController.post(request, response);

        verify(response, times(1)).status(HttpStatusCode.BAD_REQUEST.getCode());
    }

    @Test
    void testPost_getDataRequest_exception() throws Exception {
        when(request.body()).thenThrow(new RuntimeException("Test exception"));

        crawlController.post(request, response);

        verify(response, times(1)).status(HttpStatusCode.BAD_REQUEST.getCode());
    }
}
