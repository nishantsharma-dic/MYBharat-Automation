package com.mybharat.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retry - TestNG retry analyzer for handling flaky test failures.
 *
 * Purpose: Automatically retries a failed test method up to MAX_RETRY times before
 *          marking it as permanently failed. This is essential for UI tests where
 *          transient issues (network delays, element loading, animations) can cause
 *          intermittent failures.
 *
 * Flow:
 *   1. A test method fails
 *   2. TestNG calls retry() to check if it should re-run
 *   3. If retryCount &lt; MAX_RETRY → returns true (re-run the test)
 *   4. If retryCount >= MAX_RETRY → returns false (mark as failed)
 *
 * Configuration: MAX_RETRY = 1 (each test gets 1 retry, so 2 total attempts)
 *
 * Usage in test classes:
 *   @Test(retryAnalyzer = Retry.class)
 *   public void myTestMethod() { ... }
 *
 * Dependencies: TestNG IRetryAnalyzer, Log4j2
 * Developer: Nishant Sharma (QA Team)
 *
 * @see TestListeners
 */
public class Retry implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(Retry.class);
    private static final int MAX_RETRY = 1;
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY) {
            retryCount++;
            log.warn("Retrying '{}' - attempt {}/{}", result.getName(), retryCount, MAX_RETRY);
            return true;
        }
        return false;
    }
}
