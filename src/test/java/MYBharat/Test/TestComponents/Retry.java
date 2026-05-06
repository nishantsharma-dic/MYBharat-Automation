package MYBharat.Test.TestComponents;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retry analyzer for flaky tests.
 * Retries a failed test up to MAX_RETRY_COUNT times before marking it as failed.
 */
public class Retry implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(Retry.class);

    /** Maximum number of retry attempts per test. */
    private static final int MAX_RETRY_COUNT = 2;

    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            log.warn("Retrying test '{}' - attempt {}/{}", result.getName(), retryCount, MAX_RETRY_COUNT);
            return true;
        }
        return false;
    }
}
