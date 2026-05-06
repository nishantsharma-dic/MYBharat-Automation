package com.mybharat.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * RedashClient - Fetches query results from Redash for DB verification.
 * 
 * Usage in tests:
 *   List<Map<String, String>> results = RedashClient.getQueryResult(baseUrl, queryId, apiKey);
 *   // Verify user exists in DB after registration
 * 
 * Pass credentials via system properties (never hardcode):
 *   -DredashBaseUrl=https://dash-beta.mybharats.in
 *   -DredashQueryId=63
 *   -DredashApiKey=your-key
 */
public class RedashClient {

    private static final Logger log = LogManager.getLogger(RedashClient.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private RedashClient() {
        // utility class
    }

    /**
     * Fetch query results from Redash.
     *
     * @param baseUrl Redash server URL (e.g., https://dash-beta.mybharats.in)
     * @param queryId The query ID to execute
     * @param apiKey  Redash API key
     * @return List of rows, each row is a Map of column-name → value
     */
    public static List<Map<String, String>> getQueryResult(String baseUrl, String queryId, String apiKey)
            throws ParseException {
        List<Map<String, String>> results = new ArrayList<>();
        String url = baseUrl + "/api/queries/" + queryId + "/results.json?api_key=" + apiKey;

        log.info("Fetching Redash query: {}", queryId);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            String json = EntityUtils.toString(client.execute(get).getEntity());
            JsonNode root = mapper.readTree(json);
            JsonNode rows = root.path("query_result").path("data").path("rows");

            for (JsonNode row : rows) {
                Map<String, String> map = new HashMap<>();
                row.fieldNames().forEachRemaining(field -> map.put(field, row.get(field).asText()));
                results.add(map);
            }
            log.info("Redash returned {} rows", results.size());

        } catch (IOException e) {
            log.error("Failed to fetch Redash data: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch Redash data: " + e.getMessage(), e);
        }
        return results;
    }

    /**
     * Verify if a user email exists in Redash query results.
     *
     * @param email   The email to search for
     * @param results The Redash query results
     * @param emailColumn The column name that contains email (e.g., "email")
     * @return true if email found in results
     */
    public static boolean isUserInDatabase(String email, List<Map<String, String>> results, String emailColumn) {
        return results.stream()
                .anyMatch(row -> email.equalsIgnoreCase(row.get(emailColumn)));
    }
}
