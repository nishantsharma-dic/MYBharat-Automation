package com.mybharat.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retry - Retries failed tests up to 2 times before marking as failed.
 * Useful for handling flaky UI elements.
 */
public class Retry implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(Retry.class);
    private static final int MAX_RETRY = 2;
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
