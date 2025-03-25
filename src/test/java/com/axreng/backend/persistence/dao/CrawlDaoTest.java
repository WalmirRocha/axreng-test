package com.axreng.backend.persistence.dao;

import static com.axreng.backend.util.Constants.EVN_BASE_URL;
import static com.axreng.backend.util.Constants.EVN_PROFILE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.axreng.backend.vo.ResponseVO;
import com.axreng.backend.vo.Status;

public class CrawlDaoTest {

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement stmt;
    @Mock
    private ResultSet resultSet;
    private CrawlDao crawlDao;
    @Mock
    private DataSource dataSource;
    @Mock
    private NamedQueryLoader namedQueryLoader;
    @Mock
    private Query query;
    @Mock
    private Consumer<PreparedStatement> parameterSetter;
    @Mock
    private Function<ResultSet, ResponseVO> mapper;

    @BeforeEach
    void setUp() throws Exception {
        System.setProperty(EVN_BASE_URL, "http://example.com");
        System.setProperty(EVN_PROFILE, "TST");
        MockitoAnnotations.openMocks(this);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(resultSet);

    }

    @Test
    void getCrawlByIdWithLimitUrls_ValidCrawlId_ReturnsResponseVO() throws Exception {
        String sqlName = "queryName";
        String expectedSql = "SELECT ... FROM ... WHERE ...";
        String expectedResult = "Resultado OK";

        when(namedQueryLoader.getSqlByName(sqlName)).thenReturn(expectedSql);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("column")).thenReturn(expectedResult);

        Consumer<PreparedStatement> paramSetter = ps -> {
        };

        Function<ResultSet, ResponseVO> mapper = rs -> {
            try {
                if (rs.next()) {
                    return ResponseVO.Builder().urlBase(rs.getString("column")).build();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        };

        crawlDao = new CrawlDao(namedQueryLoader, dataSource);

        ResponseVO result = crawlDao.getSingle(sqlName, paramSetter, mapper);

        assertEquals(expectedResult, result.getUrlBase());

        verify(dataSource).getConnection();
        verify(stmt).executeQuery();
    }

    @Test
    void insertCrawl_ValidData_ExecutesInsertStatement() throws Exception {

        String id = "ABC12345";
        String status = "active";
        String urlBase = "http://example.com";
        String queryName = "INSERT-CRAWL";
        String sql = "INSERT INTO ...";

        when(namedQueryLoader.getSqlByName(queryName)).thenReturn(sql);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);

        crawlDao = new CrawlDao(namedQueryLoader, dataSource);
        crawlDao.insertCrawl(id, status, urlBase);

        // Verifica se métodos foram chamados
        verify(stmt).setString(1, id);
        verify(stmt).setString(2, status);
        verify(stmt).setString(3, urlBase);
        verify(stmt).executeUpdate();
    }

    @Test
    void updateCrawl_ValidData_ExecutesUpdateStatement() throws Exception {
        String idCrawl = "ABC12345";
        String status = "completed";
        String queryName = "UPDATE-CRAWL-STATUS";
        String sql = "UPDATE ... SET ... WHERE ...";

        when(namedQueryLoader.getSqlByName(queryName)).thenReturn(sql);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);

        crawlDao = new CrawlDao(namedQueryLoader, dataSource);
        crawlDao.updateCrawl(idCrawl, status);

        verify(stmt).setString(1, status);
        verify(stmt).setString(2, idCrawl);
        verify(stmt).executeUpdate();
    }

    @Test
    void getCrawlByUrlAndStatus_ValidUrlAndStatus_ReturnsResponseVO() throws Exception {
        // Arrange
        String url = "http://example.com/page";
        Status active = Status.ACTIVE;
        String queryName = "SELECT-CRAWL-STATUS";
        String sql = "SELECT ID FROM Crawls WHERE URL = ? AND STATUS = ?";
        String expectedId = "CRAWL123";

        when(namedQueryLoader.getSqlByName(queryName)).thenReturn(sql);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false); // Simulate one result
        when(resultSet.getString("ID")).thenReturn(expectedId);

        crawlDao = new CrawlDao(namedQueryLoader, dataSource);

        // Act
        ResponseVO result = crawlDao.getCrawlByUrlAndStatus(url, active);

        // Assert
        assertNotNull(result);
        assertEquals(expectedId, result.getId());

        verify(stmt).setString(1, url);
        verify(stmt).setString(2, active.getValue());
        verify(stmt).executeQuery();
    }

    @Test
    void getCrawlByUrlAndStatus_NoResultFound_ReturnsNull() throws Exception {
        // Arrange
        String url = "http://example.com/nonexistent";
        Status active = Status.ACTIVE;
        String queryName = "SELECT-CRAWL-STATUS";
        String sql = "SELECT ID FROM Crawls WHERE URL = ? AND STATUS = ?";

        when(namedQueryLoader.getSqlByName(queryName)).thenReturn(sql);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // Simulate no result

        crawlDao = new CrawlDao(namedQueryLoader, dataSource);

        // Act
        ResponseVO result = crawlDao.getCrawlByUrlAndStatus(url, active);

        // Assert
        assertNull(result);

        verify(stmt).setString(1, url);
        verify(stmt).setString(2, active.getValue());
        verify(stmt).executeQuery();
    }

    @Test
    void getCrawlByUrlAndStatus_SQLException_ThrowsRuntimeException() throws Exception {

        String url = "http://example.com/error";
        Status active = Status.ACTIVE;
        String queryName = "SELECT-CRAWL-STATUS";
        String sql = "SELECT ID FROM Crawls WHERE URL = ? AND STATUS = ?";

        when(namedQueryLoader.getSqlByName(queryName)).thenReturn(sql);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(stmt);
        when(stmt.executeQuery()).thenThrow(new SQLException("Database error"));

        crawlDao = new CrawlDao(namedQueryLoader, dataSource);

        // Act & Assert
        assertThrows(SQLException.class, () -> crawlDao.getCrawlByUrlAndStatus(url, active));

        verify(stmt).setString(1, url);
        verify(stmt).setString(2, active.getValue());
        verify(stmt).executeQuery();
    }
}
