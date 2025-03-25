package com.axreng.backend.service;

import static com.axreng.backend.util.Constants.CRAWL_MAX_DEPTH;
import static com.axreng.backend.util.Constants.CRAWL_MAX_URLS;
import static com.axreng.backend.util.Constants.EVN_BASE_URL;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.axreng.backend.persistence.dao.CrawlDao;
import com.axreng.backend.util.CrawlProperties;
import com.axreng.backend.vo.ResponseVO;
import com.axreng.backend.vo.Status;

class CrawlServiceTest {

    @Mock
    private CrawlDao crawlDao;
    @Mock
    private CrawlProperties properties;
    @Mock
    private HttpClient httpClient;
    
    private String crawlId = "test-id";

    private String url = "http://example.com";

    private String keyword = "test";

    private CrawlService crawlService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(properties.getProperty(EVN_BASE_URL)).thenReturn("http://example.com");
        when(properties.getProperty(CRAWL_MAX_DEPTH)).thenReturn(10);
        when(properties.getProperty(CRAWL_MAX_URLS)).thenReturn(100);
        crawlService = new CrawlService(properties, crawlDao);
    }

    @Test
    void testGetCrawlById() throws Exception {
        String crawlId = "test-id";
        ResponseVO mockResponse = ResponseVO.Builder().build();
        when(crawlDao.getCrawlByIdWithLimitUrls(crawlId, 100)).thenReturn(mockResponse);

        ResponseVO response = crawlService.getCrawlById(crawlId);

        verify(crawlDao, times(1)).getCrawlByIdWithLimitUrls(crawlId, 100);
        assert response == mockResponse;
    }

    @Test
    void testCrawlSearch() throws Exception {
        Set<String> visitedLinks = new HashSet<>();

        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.body()).thenReturn("<html><body>test</body></html>");
        when(httpResponse.statusCode()).thenReturn(200);

        CompletableFuture<HttpResponse<String>> completableFuture = CompletableFuture.completedFuture(httpResponse);
        try {
            when(httpClient.sendAsync(
                    HttpRequest.newBuilder().uri(URI.create(url)).GET().header("User-Agent", "Java WebCrawler").build(),
                    HttpResponse.BodyHandlers.ofString())).thenReturn(completableFuture);
        } catch (Exception e) {
            e.printStackTrace();
        }

        CompletableFuture<Void> future = crawlService.crawlSearch(crawlId, url, visitedLinks, keyword, 0);

        assert future != null;
    }

    @Test
    void testGetCrawlByUrlAndStatus() throws Exception {
        Status status = Status.ACTIVE;
        ResponseVO mockResponse = ResponseVO.Builder().build();
        when(crawlDao.getCrawlByUrlAndStatus(url, status)).thenReturn(mockResponse);

        ResponseVO response = crawlService.getCrawlByUrlAndStatus(url, status);

        verify(crawlDao, times(1)).getCrawlByUrlAndStatus(url, status);
        assert response == mockResponse;
    }

    @Test
    void testCrawlSearch_MaxDepthReached() throws Exception {

        Set<String> visitedLinks = new HashSet<>();
        int maxDepth = 10;

        CompletableFuture<Void> future = crawlService.crawlSearch(crawlId, url, visitedLinks, keyword, maxDepth + 1);

        assert future.isDone();
    }

    @Test
    void testCrawlSearch_LinkAlreadyVisited() throws Exception {

        Set<String> visitedLinks = new HashSet<>();
        visitedLinks.add(url);

        CompletableFuture<Void> future = crawlService.crawlSearch(crawlId, url, visitedLinks, keyword, 0);

        assert future.isDone();
    }

    @Test
    void testCrawlSearch_HttpRequestFailed() throws Exception {

        Set<String> visitedLinks = new HashSet<>();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("User-Agent", "Java WebCrawler")
                .build();

        when(httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                .thenThrow(new RuntimeException("Simulated HTTP request failure"));

        CompletableFuture<Void> future = crawlService.crawlSearch(crawlId, url, visitedLinks, keyword, 0);

        assert future != null;
    }

    @Test
    void testCrawlSearch_insertCrawlUrl_throwsException() throws Exception {

        Set<String> visitedLinks = new HashSet<>();

        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.body()).thenReturn("<html><body>test</body></html>");
        when(httpResponse.statusCode()).thenReturn(200);

        CompletableFuture<HttpResponse<String>> completableFuture = CompletableFuture.completedFuture(httpResponse);

        when(httpClient.sendAsync(
                HttpRequest.newBuilder().uri(URI.create(url)).GET().header("User-Agent", "Java WebCrawler").build(),
                HttpResponse.BodyHandlers.ofString())).thenReturn(completableFuture);

        doThrow(new Exception("Simulated exception")).when(crawlDao).insertCrawlUrl(anyString(), anyString());

        CompletableFuture<Void> future = crawlService.crawlSearch(crawlId, url, visitedLinks, keyword, 0);

        assert future != null;
    }
}
