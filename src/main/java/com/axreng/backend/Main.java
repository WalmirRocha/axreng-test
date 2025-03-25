package com.axreng.backend;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axreng.backend.controller.CrawlController;
import com.axreng.backend.init.context.BeanContext;
import com.axreng.backend.init.context.LoaderInicializer;
import com.axreng.backend.service.CrawlService;
import com.axreng.backend.util.CrawlProperties;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static CrawlController controller;

    public static void main(String[] args) throws Exception {


            new LoaderInicializer(CrawlProperties.getInstance()).init();   

            controller = new CrawlController(
                 BeanContext.getContext().get(CrawlService.class),
                 CrawlProperties.getInstance()
            );

            defineRoutes();
    }

    private static void defineRoutes() {

        port(4567);

        get("/crawl/:id", (req, res) -> {
            return controller.get(req, res);
        });

        post("/crawl", (req, res) -> {
            return controller.post(req, res);
        });
    }
}
