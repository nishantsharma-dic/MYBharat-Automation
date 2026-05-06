package MYBharat.Test.tests.youth;

import java.util.List;
import java.util.Map;

import org.apache.hc.core5.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import MYBharat.Test.TestComponents.RedashClient;

/**
 * Data-driven test that fetches rows from a Redash query and verifies them.
 *
 * <p>Credentials are read from system properties so they are never committed to
 * source control:
 * <pre>
 *   mvn test -DredashBaseUrl=https://dash-beta.mybharats.in \
 *            -DredashQueryId=63 \
 *            -DredashApiKey=&lt;your-key&gt;
 * </pre>
 *
 * <p>If the properties are absent the test is skipped gracefully.
 */
public class RedashDataDrivenTest {

    private static final Logger log = LogManager.getLogger(RedashDataDrivenTest.class);

    @DataProvider(name = "MyBharatData")
    public Object[][] getData() throws ParseException {
        String baseUrl  = System.getProperty("redashBaseUrl");
        String queryId  = System.getProperty("redashQueryId");
        String apiKey   = System.getProperty("redashApiKey");

        if (baseUrl == null || queryId == null || apiKey == null) {
            log.warn("Redash credentials not provided via system properties – returning empty data set.");
            return new Object[0][0];
        }

        List<Map<String, String>> data = RedashClient.getQueryResult(baseUrl, queryId, apiKey);
        Object[][] arr = new Object[data.size()][1];
        for (int i = 0; i < data.size(); i++) {
            arr[i][0] = data.get(i);
        }
        return arr;
    }

    /**
     * Verifies that each row returned by Redash is non-null and non-empty.
     *
     * @param record one row from the Redash query result
     */
    @Test(dataProvider = "MyBharatData", groups = {"data-driven"})
    public void verifyRedashRecord(Map<String, String> record) {
        log.info("Verifying Redash row: {}", record);
        Assert.assertNotNull(record, "Record should not be null");
        Assert.assertFalse(record.isEmpty(), "Record should not be empty");
    }
}
