package com.axreng.backend.persistence.dao;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.axreng.backend.vo.ResponseVO;
import com.axreng.backend.vo.Status;

public class CrawlDao extends Query {

    private static final Logger LOGGER = Logger.getLogger(CrawlDao.class.getName());

    public CrawlDao(NamedQueryLoader loaderQuery, DataSource dataSource){
        super(loaderQuery, dataSource);
    }

    public ResponseVO getCrawlByIdWithLimitUrls(String crawlId, int limit) throws Exception {
        return getSingle(
                "SELECT-CRAWL-LIMIT-URLS",
                stmt -> { // Parameter setter
                    try {
                        stmt.setString(1, crawlId);
                        stmt.setMaxRows(limit);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                rs -> { // Mapper function
                    ResponseVO response = null;
                    try {
                        while (rs.next()) {
                            if (response == null) {
                                response = ResponseVO.Builder()
                                        .id(rs.getString("ID"))
                                        .status(Status.getStatusByValue(rs.getString("STATUS")))
                                        .build();
                                response.addUrl(rs.getString("URL"));
                            }
                            response.addUrl(rs.getString("URL"));
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException("Error mapping ResultSet to ResponseVO", e);
                    }
                    return response;
                });
    }

    public void insertCrawl(String id, String status, String urlBase) throws Exception {
        execute("INSERT-CRAWL", stmt -> {
            try {
                stmt.setString(1, id);
                stmt.setString(2, status);
                stmt.setString(3, urlBase);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error setting parameters for insertCrawl", e);
            }
        });
    }

    public void insertCrawlUrl(String url, String crawlId) throws Exception {
        execute("INSERT-CRAWL-URL", stmt -> {
            try {
                stmt.setString(1, url);
                stmt.setString(2, crawlId);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error setting parameters for insertCrawlUrl", e);
            }
        });
    }

    public void updateCrawl(String idCrawl, String status) throws Exception {
        execute("UPDATE-CRAWL-STATUS", stmt -> {
            try {
                stmt.setString(1, status);
                stmt.setString(2, idCrawl);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error setting parameters for updateCrawl", e);
            }
        });
    }

    public ResponseVO getCrawlByUrlAndStatus(String url, Status active) throws Exception {

        return getSingle(
                "SELECT-CRAWL-STATUS",
                stmt -> { // Parameter setter
                    try {
                        stmt.setString(1, url);
                        stmt.setString(2, active.getValue());
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE, "Error setting parameters for getCrawlByUrlAndStatus", e);
                    }
                },
                rs -> { // Mapper function
                    ResponseVO response = null;
                    try {
                        while (rs.next()) {
                            response = ResponseVO.Builder()
                                    .id(rs.getString("ID"))
                                    .build();
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException("Error mapping ResultSet to ResponseVO", e);
                    }
                    return response;
                });
    }
}
