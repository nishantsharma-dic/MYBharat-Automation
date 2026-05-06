/**
 * RedashClient: Fetches query results from Redash using HTTP GET.
 * 
 * Steps to use (never forget):
 * 1. Prepare Redash API details:
 *    - baseUrl: The Redash server URL (e.g., https://redash.example.com)
 *    - queryId: The ID of the query you want results for
 *    - apiKey: Your Redash API key
 * 2. Call getQueryResult(baseUrl, queryId, apiKey):
 *    - Returns a List of Maps, each map is a row with column names as keys.
 * 3. Handle possible exceptions:
 *    - ParseException: Thrown if response parsing fails
 *    - RuntimeException: Thrown if HTTP request fails
 * 4. Example usage:
 *    List<Map<String, String>> results = RedashClient.getQueryResult(url, id, key);
 *    for (Map<String, String> row : results) {
 *        // process each row
 *    }
 * 5. Notes:
 *    - Uses Apache HttpClient and Jackson for HTTP and JSON parsing
 *    - Automatically closes HTTP client (try-with-resources)
 *    - Throws RuntimeException on IO errors for easier test failure detection
 */
package MYBharat.Test.TestComponents;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

/**
 * Utility class for fetching query results from Redash.
 */
public class RedashClient {

    // Jackson ObjectMapper for JSON parsing
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Fetches query results from Redash and returns them as a list of maps.
     * Each map represents a row, with column names as keys.
     *
     * @param baseUrl The Redash server base URL
     * @param queryId The query ID to fetch results for
     * @param apiKey  The Redash API key
     * @return List of rows, each row is a map of column name to value
     * @throws ParseException if response parsing fails
     * @throws RuntimeException if HTTP request fails
     */
    public static List<Map<String, String>> getQueryResult(String baseUrl, String queryId, String apiKey) throws ParseException {
        List<Map<String, String>> results = new ArrayList<>();
        String url = baseUrl + "/api/queries/" + queryId + "/results.json?api_key=" + apiKey;

        // Use try-with-resources to ensure HTTP client is closed
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            String json = EntityUtils.toString(client.execute(get).getEntity());
            JsonNode root = mapper.readTree(json);
            JsonNode rows = root.path("query_result").path("data").path("rows");

            // Iterate over each row and convert to Map<String, String>
            for (JsonNode row : rows) {
                Map<String, String> map = new HashMap<>();
                row.fieldNames().forEachRemaining(field -> map.put(field, row.get(field).asText()));
                results.add(map);
            }

        } catch (IOException e) {
            // Wrap IO errors in RuntimeException for easier test failure detection
            throw new RuntimeException("❌ Failed to fetch Redash data: " + e.getMessage());
        }
        return results;
    }
}
