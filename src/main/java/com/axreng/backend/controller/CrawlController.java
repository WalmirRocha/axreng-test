package com.axreng.backend.controller;

import static com.axreng.backend.util.Constants.CRAWL_LIMITE_LENGTH_ID;
import static com.axreng.backend.util.Constants.EVN_BASE_URL;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axreng.backend.service.CrawlService;
import com.axreng.backend.util.CrawlProperties;
import com.axreng.backend.util.IDGenerator;
import com.axreng.backend.util.ValidationUtil;
import com.axreng.backend.vo.ResponseVO;
import com.axreng.backend.vo.Status;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import spark.Request;
import spark.Response;

public class CrawlController {

    private static final Logger LOG = LoggerFactory.getLogger(CrawlController.class);

    private final CrawlService crawlService;
    private final IDGenerator idGenerator = IDGenerator.getInstance();
    private final ValidationUtil validationUtil = ValidationUtil.getInstance();
    private final Gson gson = new GsonBuilder().create();
    private final CrawlProperties crawlProperties;

    public CrawlController(CrawlService crawlService, CrawlProperties crawlProperties) {
        this.crawlService = crawlService;
        this.crawlProperties = crawlProperties;
    }

    public String get(Request req, Response res) throws Exception {

        String id = req.params(":id");
        if (id == null) {
            res.status(HttpStatusCode.BAD_REQUEST.getCode());
            return HttpStatusCode.BAD_REQUEST.getDescription();
        }

        ResponseVO responseVO = crawlService.getCrawlById(id);
        if (responseVO == null) {
            res.status(HttpStatusCode.NOT_FOUND.getCode());
            return HttpStatusCode.NOT_FOUND.getDescription();
        }

        res.type("application/json");
        return gson.toJson(responseVO);
    }

    public String post(Request req, Response res) throws Exception {

        String keyword = getDataRequest(req, res);
        if (keyword == null) {
            return res.body();
        }

        res.type("application/json");
        String baseUrl = crawlProperties.getProperty(EVN_BASE_URL);

        ResponseVO responseVOStatus = this.crawlService.getCrawlByUrlAndStatus(baseUrl, Status.ACTIVE);
        if (responseVOStatus != null) {
            res.status(HttpStatusCode.OK.getCode());
            return gson.toJson(responseVOStatus);
        }

        String id = idGenerator.getId(CRAWL_LIMITE_LENGTH_ID);

        ResponseVO responseVO = ResponseVO.Builder()
                .id(id)
                .urlBase(baseUrl)
                .status(Status.ACTIVE)
                .build();

        this.crawlService.insertCrawl(responseVO);

        responseVO = executeWebCrawler(res, responseVO, baseUrl, keyword, req, res);

        return gson.toJson(ResponseVO.Builder().id(id).build());
    }

    private String getDataRequest(Request req, Response res) {

        try {

            String body = req.body();
            validationUtil.validateProperty(body, "body");
            JsonObject data = new Gson().fromJson(body, JsonObject.class);

            String keyword = data.get("keyword").getAsString();
            validationUtil.validateProperty(keyword, "keyword");

            if (keyword.length() < 4 || keyword.length() > 32) {
                res.status(HttpStatusCode.BAD_REQUEST.getCode());
                res.body(
                        " The keyword length must be greater than or equal to 4 and less than or equal to 32 characters");
                return null;
            }

            return keyword;

        } catch (Exception e) {
            res.status(HttpStatusCode.BAD_REQUEST.getCode());
            res.body(HttpStatusCode.BAD_REQUEST.getDescription());
            return null;
        }

    }

    private ResponseVO executeWebCrawler(Response response, ResponseVO responseVO, String baseUrl, String keyword,
            Request req,
            Response res) throws Exception {

        if (keyword == null) {
            CompletableFuture.completedFuture(null);
        }

        Set<String> visitedLinks = new HashSet<>();

        CompletableFuture<Void> future = crawlService.crawlSearch(responseVO.getId(), baseUrl, visitedLinks, keyword, 0)
                .thenRun(() -> {
                    responseVO.setStatus(Status.DONE);
                    LOG.info("Crawling completed for ID: {}", responseVO.getId());
                }).exceptionally(ex -> {
                    LOG.error("Error during crawling process: ", ex);
                    responseVO.setStatus(Status.DONE);
                    response.status(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode());
                    throw new RuntimeException(HttpStatusCode.INTERNAL_SERVER_ERROR.getDescription(), ex);
                });

        future.whenComplete((result, throwable) -> {
            try {
                this.crawlService.updateCrawl(responseVO.getId(), responseVO.getStatus().getValue());
            } catch (Exception e) {
                response.status(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode());
                throw new RuntimeException(HttpStatusCode.INTERNAL_SERVER_ERROR.getDescription(), e);
            }
        });

        return responseVO;

    }
}
