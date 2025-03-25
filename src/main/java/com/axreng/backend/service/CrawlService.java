package com.axreng.backend.service;

import static com.axreng.backend.util.Constants.CRAWL_HTTP_TIMEOUT;
import static com.axreng.backend.util.Constants.CRAWL_MAX_DEPTH;
import static com.axreng.backend.util.Constants.CRAWL_MAX_URLS;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axreng.backend.persistence.dao.CrawlDao;
import com.axreng.backend.util.CrawlProperties;
import com.axreng.backend.vo.ResponseVO;
import com.axreng.backend.vo.Status;

public class CrawlService {

    private static final Logger LOG = LoggerFactory.getLogger(CrawlService.class);
    private final int MAX_DEPTH;
    private final int MAX_URLS;
    private final Pattern pattern;
    private final HttpClient httpClient;
    private final CrawlDao crawlDao;

    public CrawlService(CrawlProperties properties, CrawlDao crawlDao) {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(CRAWL_HTTP_TIMEOUT))
                .build();
        this.crawlDao = crawlDao;
        this.MAX_DEPTH = properties.getProperty(CRAWL_MAX_DEPTH);
        this.pattern = Pattern.compile("href=[\"'](http[s]?://[^\"'>]+)[\"']");
        this.MAX_URLS = properties.getProperty(CRAWL_MAX_URLS);
    }

    public ResponseVO getCrawlById(String crawlId) throws Exception {
        return crawlDao.getCrawlByIdWithLimitUrls(crawlId, MAX_URLS);
    }

    public void insertCrawl(ResponseVO responseVO) throws Exception {
        crawlDao.insertCrawl(responseVO.getId(), responseVO.getStatus().getValue(), responseVO.getUrlBase());
    }

    public void insertCrawlUrl(String url, String idCrawl) throws Exception {
        crawlDao.insertCrawlUrl(url, idCrawl);
    }

    public void updateCrawl(String idCrawl, String status) throws Exception {
        crawlDao.updateCrawl(idCrawl, status);
    }

    public CompletableFuture<Void> crawlSearch(String crawlId, String url, Set<String> visitedLinks, String keyword,
            int currentDepth) {

        if (currentDepth > MAX_DEPTH || visitedLinks.contains(url)) {
            return CompletableFuture.completedFuture(null);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("User-Agent", "Java WebCrawler")
                .build();

        visitedLinks.add(url);

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {

                    String content = response.body();
                    LOG.info("Request for URL: {}", url);
                    Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(content);
                    boolean existKeyWord = matcher.find();
                    if (response.statusCode() >= 200 && response.statusCode() < 300 && existKeyWord) {
                        LOG.info("Request for URL: {}", url);
                        try {
                            crawlDao.insertCrawlUrl(url, crawlId);
                        } catch (Exception e) {
                            LOG.error("Error inserting crawl URL: {} - {}", url, e);
                        }
                    }

                    Set<String> links = extractLinks(response.body());

                    Set<CompletableFuture<Void>> futures = new HashSet<>();
                    for (String nextUrl : links) {
                        if (!visitedLinks.contains(nextUrl)) {
                            futures.add(crawlSearch(crawlId, nextUrl, visitedLinks, keyword, currentDepth + 1));
                        }
                    }
                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

                }).exceptionally(e -> {
                    LOG.error("Request fail URL: {} - {}", url, e.getMessage());
                    throw new RuntimeException("Error during HTTP request", e);
                });

    }

    private Set<String> extractLinks(String content) {

        Set<String> links = new HashSet<>();
        Matcher matcher = this.pattern.matcher(content);

        while (matcher.find()) {
            links.add(matcher.group(1));
        }

        return links;
    }

    public ResponseVO getCrawlByUrlAndStatus(String baseUrl, Status active) throws Exception {
        return crawlDao.getCrawlByUrlAndStatus(baseUrl, active);
    }
}
