package com.axreng.backend.persistence.dao;

import static com.axreng.backend.util.Constants.CRAWL_H2_SQL_QUERY;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axreng.backend.init.context.PropertiesLoader;
import com.axreng.backend.util.CrawlProperties;

public class NamedQueryLoader {

    private static NamedQueryLoader instance = new NamedQueryLoader();

    private static final Logger LOG = LoggerFactory.getLogger(NamedQueryLoader.class);

    private final String SQL = "sql=";
    private final String NAMED_QUERY = "@NAMED-QUERY";
    private final Map<String, String> sqlMap = new HashMap<>();
    private final Pattern NAME_PATTERN = Pattern.compile("name\\s*=\\s*\"([^\"]+)\"");
    private final Pattern SQL_PATTERN = Pattern.compile("sql\\s*=\\s*\"(.*)");
    private final CrawlProperties properties = CrawlProperties.getInstance();

    private NamedQueryLoader() {
        loadSqlFile();
    }

    public static NamedQueryLoader getInstance() {
        return instance;
    }

    public void loadSqlFile() {

        URL urlJar = PropertiesLoader.class.getClassLoader().getResource(properties.getProperty(CRAWL_H2_SQL_QUERY));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlJar.openStream()))) {
            String line;
            String currentName = null;
            StringBuilder sqlBuilder = new StringBuilder();
            boolean readingSql = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Detect start of @NAMED-QUERY
                if (line.startsWith(NAMED_QUERY)) {
                    Matcher nameMatcher = NAME_PATTERN.matcher(line);
                    if (nameMatcher.find()) {
                        currentName = nameMatcher.group(1);
                    }
                }

                // Detect sql=" start
                if (line.contains(SQL)) {
                    Matcher sqlMatcher = SQL_PATTERN.matcher(line);
                    if (sqlMatcher.find()) {
                        String firstLine = sqlMatcher.group(1);
                        // Remove trailing quote if it's a one-liner
                        if (firstLine.endsWith("\"")) {
                            sqlBuilder.append(firstLine, 0, firstLine.length() - 1).append(System.lineSeparator());
                            readingSql = false;
                            saveQuery(currentName, sqlBuilder.toString());
                            sqlBuilder.setLength(0);
                            currentName = null;
                        } else {
                            sqlBuilder.append(firstLine).append(System.lineSeparator());
                            readingSql = true;
                        }
                    }
                } else if (readingSql) {
                    // Keep reading SQL lines
                    if (line.endsWith("\"")) {
                        sqlBuilder.append(line, 0, line.length() - 1).append(System.lineSeparator());
                        readingSql = false;
                        saveQuery(currentName, sqlBuilder.toString());
                        sqlBuilder.setLength(0);
                        currentName = null;
                    } else {
                        sqlBuilder.append(line).append(System.lineSeparator());
                    }
                }
            }
        } catch (FileNotFoundException e) {
            LOG.error("SQL file not found at path: {}", urlJar.toString(), e);
        } catch (IOException e) {
            LOG.error("Error reading SQL file at path: {}", urlJar.toString(), e);
        }
    }

    private void saveQuery(String name, String sql) {
        if (name != null && sql != null) {
            sqlMap.put(name, sql.trim());
        }
    }

    public String getSqlByName(String name) {

        if (!sqlMap.containsKey(name)) {
            LOG.error("SQL query with name '{}' not found.", name);
        }
        return sqlMap.get(name);
    }
}
