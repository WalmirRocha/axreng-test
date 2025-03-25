package com.axreng.backend.persistence.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Query {

    private static final Logger LOG = LoggerFactory.getLogger(Query.class);
    private final NamedQueryLoader loaderQuery;
    private final DataSource dataSource;

    public Query(NamedQueryLoader loaderQuery, DataSource dataSource) {
        this.loaderQuery = loaderQuery;
        this.dataSource = dataSource;
    }

    protected <T> List<T> getList(String nameQuery, Function<ResultSet, T> mapper) throws SQLException {
        List<T> results = new ArrayList<>();
        String sql = loaderQuery.getSqlByName(nameQuery);
        try (PreparedStatement stmt = dataSource.getConnection().prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(mapper.apply(rs));
            }

        } catch (SQLException e) {
            LOG.error("Error executing getList query: {} - {}", sql, e.getMessage(), e);
            throw e;
        }
        return results;
    }

    protected <T> T getSingle(String nameQuery, Consumer<PreparedStatement> parameterSetter,
            Function<ResultSet, T> mapper) throws SQLException {
        String sql = loaderQuery.getSqlByName(nameQuery);
        try (PreparedStatement stmt = dataSource.getConnection().prepareStatement(sql)) {
            parameterSetter.accept(stmt);
            ResultSet rs = stmt.executeQuery();
            return mapper.apply(rs);
        } catch (SQLException e) {
            LOG.error("Error executing getSingle query: {} - {}", sql, e.getMessage(), e);
            throw e;
        }
    }

    protected <T> void execute(String nameQuery, Consumer<PreparedStatement> parameterSetter) throws SQLException {
        String sql = loaderQuery.getSqlByName(nameQuery);
        try (PreparedStatement stmt = dataSource.getConnection().prepareStatement(sql)) {
            parameterSetter.accept(stmt);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOG.error("Error executing insert: {} - {}", sql, e.getMessage(), e);
            throw e;
        }
    }
}
