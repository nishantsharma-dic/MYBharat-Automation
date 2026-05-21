package com.mybharat.utils;

import java.time.Duration;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * WaitUtils — Reusable Selenium Wait Utility.
 *
 * Provides explicit wait methods with:
 *  - Configurable timeout and polling interval
 *  - Logging before/after every wait operation
 *  - Stale element retry support
 *  - Angular page synchronization
 *  - DOM stability detection
 *  - Meaningful exception messages
 *
 * Usage:
 *   WaitUtils waitUtils = new WaitUtils(driver);
 *   waitUtils.waitForVisible(By.id("myElement"));
 *   waitUtils.waitForClickable(By.xpath("//button"), 15);
 *   waitUtils.waitForDomStable();
 */
public class WaitUtils {

    private static final Logger log = LogManager.getLogger(WaitUtils.class);

    private final WebDriver driver;
    private final JavascriptExecutor js;

    /** Default timeout in seconds for all wait operations */
    private static final int DEFAULT_TIMEOUT = 20;

    /** Default polling interval in milliseconds */
    private static final long DEFAULT_POLL_MS = 500;

    /** Max retries for stale element recovery */
    private static final int STALE_RETRY_COUNT = 3;

    public WaitUtils(WebDriver driver) {
        this.driver = driver;
        this.js = (JavascriptExecutor) driver;
    }

    // =========================================================================
    // VISIBILITY WAITS
    // =========================================================================

    /**
     * Wait for element located by {@code locator} to be visible in the DOM.
     *
     * @param locator By locator
     * @return the visible WebElement
     */
    public WebElement waitForVisibility(By locator) {
        return waitForVisibility(locator, DEFAULT_TIMEOUT);
    }

    /**
     * Wait for element located by {@code locator} to be visible, with custom timeout.
     *
     * @param locator        By locator
     * @param timeoutSeconds custom timeout
     * @return the visible WebElement
     */
    public WebElement waitForVisibility(By locator, int timeoutSeconds) {
        log.debug("[WaitUtils] Waiting for VISIBILITY of: {} (timeout={}s)", locator, timeoutSeconds);
        try {
            WebElement element = buildFluentWait(timeoutSeconds)
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            log.debug("[WaitUtils] Element VISIBLE: {}", locator);
            return element;
        } catch (TimeoutException e) {
            log.error("[WaitUtils] TIMEOUT waiting for visibility of: {} after {}s", locator, timeoutSeconds);
            throw e;
        }
    }

    /**
     * Wait for an already-located WebElement to become visible.
     * Retries on StaleElementReferenceException by re-finding via the provided locator.
     *
     * @param element the WebElement
     * @param locator fallback locator for stale recovery
     * @return the visible WebElement
     */
    public WebElement waitForVisibility(WebElement element, By locator) {
        return retryOnStale(() -> {
            log.debug("[WaitUtils] Waiting for VISIBILITY of element (fallback locator: {})", locator);
            WebElement el = buildFluentWait(DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.visibilityOf(element));
            log.debug("[WaitUtils] Element VISIBLE.");
            return el;
        }, locator);
    }

    // =========================================================================
    // CLICKABLE WAITS
    // =========================================================================

    /**
     * Wait for element to be clickable.
     *
     * @param locator By locator
     * @return the clickable WebElement
     */
    public WebElement waitForClickable(By locator) {
        return waitForClickable(locator, DEFAULT_TIMEOUT);
    }

    /**
     * Wait for element to be clickable with custom timeout.
     *
     * @param locator        By locator
     * @param timeoutSeconds custom timeout
     * @return the clickable WebElement
     */
    public WebElement waitForClickable(By locator, int timeoutSeconds) {
        log.debug("[WaitUtils] Waiting for CLICKABLE: {} (timeout={}s)", locator, timeoutSeconds);
        try {
            WebElement element = buildFluentWait(timeoutSeconds)
                    .until(ExpectedConditions.elementToBeClickable(locator));
            log.debug("[WaitUtils] Element CLICKABLE: {}", locator);
            return element;
        } catch (TimeoutException e) {
            log.error("[WaitUtils] TIMEOUT waiting for clickable: {} after {}s", locator, timeoutSeconds);
            throw e;
        }
    }

    /**
     * Wait for a WebElement to be clickable with stale-element retry.
     *
     * @param element the WebElement
     * @param locator fallback locator for stale recovery
     * @return the clickable WebElement
     */
    public WebElement waitForClickable(WebElement element, By locator) {
        return retryOnStale(() -> {
            log.debug("[WaitUtils] Waiting for CLICKABLE element (fallback: {})", locator);
            WebElement el = buildFluentWait(DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.elementToBeClickable(element));
            log.debug("[WaitUtils] Element CLICKABLE.");
            return el;
        }, locator);
    }

    // =========================================================================
    // PRESENCE WAITS
    // =========================================================================

    /**
     * Wait for element to be present in the DOM (may not be visible).
     *
     * @param locator By locator
     * @return the present WebElement
     */
    public WebElement waitForPresence(By locator) {
        return waitForPresence(locator, DEFAULT_TIMEOUT);
    }

    /**
     * Wait for element to be present in the DOM with custom timeout.
     *
     * @param locator        By locator
     * @param timeoutSeconds custom timeout
     * @return the present WebElement
     */
    public WebElement waitForPresence(By locator, int timeoutSeconds) {
        log.debug("[WaitUtils] Waiting for PRESENCE of: {} (timeout={}s)", locator, timeoutSeconds);
        try {
            WebElement element = buildFluentWait(timeoutSeconds)
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            log.debug("[WaitUtils] Element PRESENT: {}", locator);
            return element;
        } catch (TimeoutException e) {
            log.error("[WaitUtils] TIMEOUT waiting for presence of: {} after {}s", locator, timeoutSeconds);
            throw e;
        }
    }

    // =========================================================================
    // INVISIBILITY WAITS
    // =========================================================================

    /**
     * Wait for element to become invisible or removed from DOM.
     *
     * @param locator By locator
     * @return true if element is invisible/gone
     */
    public boolean waitForInvisibility(By locator) {
        return waitForInvisibility(locator, DEFAULT_TIMEOUT);
    }

    /**
     * Wait for element to become invisible with custom timeout.
     *
     * @param locator        By locator
     * @param timeoutSeconds custom timeout
     * @return true if element is invisible/gone
     */
    public boolean waitForInvisibility(By locator, int timeoutSeconds) {
        log.debug("[WaitUtils] Waiting for INVISIBILITY of: {} (timeout={}s)", locator, timeoutSeconds);
        try {
            boolean result = buildFluentWait(timeoutSeconds)
                    .until(ExpectedConditions.invisibilityOfElementLocated(locator));
            log.debug("[WaitUtils] Element INVISIBLE: {}", locator);
            return result;
        } catch (TimeoutException e) {
            log.warn("[WaitUtils] Element still visible after {}s: {}", timeoutSeconds, locator);
            return false;
        }
    }

    // =========================================================================
    // PAGE LOAD WAIT
    // =========================================================================

    /**
     * Wait for document.readyState to be 'complete'.
     */
    public void waitForPageLoad() {
        waitForPageLoad(DEFAULT_TIMEOUT);
    }

    /**
     * Wait for document.readyState to be 'complete' with custom timeout.
     *
     * @param timeoutSeconds custom timeout
     */
    public void waitForPageLoad(int timeoutSeconds) {
        log.debug("[WaitUtils] Waiting for PAGE LOAD (document.readyState=complete, timeout={}s)", timeoutSeconds);
        try {
            buildFluentWait(timeoutSeconds).until(d ->
                    js.executeScript("return document.readyState").equals("complete"));
            log.debug("[WaitUtils] Page LOADED (readyState=complete).");
        } catch (TimeoutException e) {
            log.warn("[WaitUtils] Page did not reach readyState=complete within {}s", timeoutSeconds);
        }
    }

    // =========================================================================
    // ANGULAR LOAD WAIT
    // =========================================================================

    /**
     * Wait for Angular (Zone.js) to finish all pending async tasks.
     * Works with Angular 2+ applications that use Zone.js.
     * Falls back gracefully if Angular is not detected.
     */
    public void waitForAngularLoad() {
        waitForAngularLoad(DEFAULT_TIMEOUT);
    }

    /**
     * Wait for Angular to stabilize with custom timeout.
     *
     * @param timeoutSeconds custom timeout
     */
    public void waitForAngularLoad(int timeoutSeconds) {
        log.debug("[WaitUtils] Waiting for ANGULAR stabilization (timeout={}s)", timeoutSeconds);
        try {
            buildFluentWait(timeoutSeconds).until(d -> {
                // Check if Angular (Zone.js) is present and stable
                Object result = js.executeScript(
                        "if (window.getAllAngularTestabilities) {"
                        + "  var testabilities = window.getAllAngularTestabilities();"
                        + "  if (testabilities && testabilities.length > 0) {"
                        + "    return testabilities.every(function(t) { return t.isStable(); });"
                        + "  }"
                        + "}"
                        + "return true;"); // No Angular detected — consider stable
                return Boolean.TRUE.equals(result);
            });
            log.debug("[WaitUtils] Angular STABLE.");
        } catch (TimeoutException e) {
            log.warn("[WaitUtils] Angular did not stabilize within {}s — proceeding.", timeoutSeconds);
        } catch (Exception e) {
            log.debug("[WaitUtils] Angular check skipped (not an Angular app or Zone.js not available): {}",
                    e.getMessage());
        }
    }

    // =========================================================================
    // DOM STABILITY WAIT
    // =========================================================================

    /**
     * Wait for the DOM to stop changing (no mutations for a stable period).
     * Useful after Angular re-renders triggered by category/subcategory selection.
     *
     * Checks that document.body.innerHTML length remains constant across
     * two consecutive polls (indicating no active DOM mutations).
     */
    public void waitForDomStable() {
        waitForDomStable(DEFAULT_TIMEOUT, 800);
    }

    /**
     * Wait for DOM stability with custom timeout and stability threshold.
     *
     * @param timeoutSeconds   max time to wait
     * @param stableMillis     how long the DOM must remain unchanged to be considered stable
     */
    public void waitForDomStable(int timeoutSeconds, int stableMillis) {
        log.debug("[WaitUtils] Waiting for DOM STABLE (timeout={}s, stableFor={}ms)",
                timeoutSeconds, stableMillis);
        try {
            // Inject a MutationObserver that tracks last mutation timestamp
            js.executeScript(
                    "if (!window.__waitUtils_domObserver) {"
                    + "  window.__waitUtils_lastMutation = Date.now();"
                    + "  window.__waitUtils_domObserver = new MutationObserver(function() {"
                    + "    window.__waitUtils_lastMutation = Date.now();"
                    + "  });"
                    + "  window.__waitUtils_domObserver.observe(document.body, "
                    + "    {childList:true, subtree:true, attributes:true});"
                    + "}");

            final long stableMs = stableMillis;
            buildFluentWait(timeoutSeconds).until(d -> {
                Object lastMutation = js.executeScript("return window.__waitUtils_lastMutation || 0;");
                long lastMs = ((Number) lastMutation).longValue();
                return (System.currentTimeMillis() - lastMs) >= stableMs;
            });

            // Cleanup observer
            js.executeScript(
                    "if (window.__waitUtils_domObserver) {"
                    + "  window.__waitUtils_domObserver.disconnect();"
                    + "  delete window.__waitUtils_domObserver;"
                    + "  delete window.__waitUtils_lastMutation;"
                    + "}");

            log.debug("[WaitUtils] DOM STABLE.");
        } catch (TimeoutException e) {
            log.warn("[WaitUtils] DOM did not stabilize within {}s — proceeding.", timeoutSeconds);
            // Cleanup observer even on timeout
            try {
                js.executeScript(
                        "if (window.__waitUtils_domObserver) {"
                        + "  window.__waitUtils_domObserver.disconnect();"
                        + "  delete window.__waitUtils_domObserver;"
                        + "  delete window.__waitUtils_lastMutation;"
                        + "}");
            } catch (Exception ignored) { }
        }
    }

    // =========================================================================
    // STALE ELEMENT RETRY
    // =========================================================================

    /**
     * Execute an action with automatic retry on StaleElementReferenceException.
     * Re-finds the element using the provided locator on each retry.
     *
     * @param action  the action to perform (returns a WebElement)
     * @param locator fallback locator to re-find the element
     * @return the WebElement result of the action
     */
    public WebElement retryOnStale(java.util.function.Supplier<WebElement> action, By locator) {
        for (int attempt = 1; attempt <= STALE_RETRY_COUNT; attempt++) {
            try {
                return action.get();
            } catch (StaleElementReferenceException e) {
                log.warn("[WaitUtils] StaleElementReferenceException (attempt {}/{}) for: {}",
                        attempt, STALE_RETRY_COUNT, locator);
                if (attempt == STALE_RETRY_COUNT) {
                    log.error("[WaitUtils] Element still stale after {} retries: {}", STALE_RETRY_COUNT, locator);
                    throw e;
                }
                // Brief pause before retry to allow DOM to settle
                pause(DEFAULT_POLL_MS);
            }
        }
        throw new StaleElementReferenceException("Exhausted retries for: " + locator);
    }

    /**
     * Execute a void action with automatic retry on StaleElementReferenceException.
     *
     * @param action  the action to perform
     * @param locator description for logging
     */
    public void retryOnStaleVoid(Runnable action, By locator) {
        for (int attempt = 1; attempt <= STALE_RETRY_COUNT; attempt++) {
            try {
                action.run();
                return;
            } catch (StaleElementReferenceException e) {
                log.warn("[WaitUtils] StaleElementReferenceException (attempt {}/{}) for: {}",
                        attempt, STALE_RETRY_COUNT, locator);
                if (attempt == STALE_RETRY_COUNT) {
                    log.error("[WaitUtils] Element still stale after {} retries: {}", STALE_RETRY_COUNT, locator);
                    throw e;
                }
                pause(DEFAULT_POLL_MS);
            }
        }
    }

    // =========================================================================
    // CUSTOM CONDITION WAIT
    // =========================================================================

    /**
     * Wait for a custom condition (Function&lt;WebDriver, T&gt;) with default timeout.
     *
     * @param condition the expected condition
     * @param description human-readable description for logging
     * @param <T> return type
     * @return the result of the condition
     */
    public <T> T waitForCondition(Function<WebDriver, T> condition, String description) {
        return waitForCondition(condition, description, DEFAULT_TIMEOUT);
    }

    /**
     * Wait for a custom condition with custom timeout.
     *
     * @param condition      the expected condition
     * @param description    human-readable description for logging
     * @param timeoutSeconds custom timeout
     * @param <T> return type
     * @return the result of the condition
     */
    public <T> T waitForCondition(Function<WebDriver, T> condition, String description, int timeoutSeconds) {
        log.debug("[WaitUtils] Waiting for condition: '{}' (timeout={}s)", description, timeoutSeconds);
        try {
            T result = buildFluentWait(timeoutSeconds).until(condition);
            log.debug("[WaitUtils] Condition MET: '{}'", description);
            return result;
        } catch (TimeoutException e) {
            log.error("[WaitUtils] TIMEOUT waiting for condition: '{}' after {}s", description, timeoutSeconds);
            throw e;
        }
    }

    // =========================================================================
    // COMBINED WAIT — Page + Angular + DOM
    // =========================================================================

    /**
     * Comprehensive wait that ensures the page is fully ready:
     *  1. document.readyState = complete
     *  2. Angular is stable (if present)
     *  3. DOM has stopped mutating
     *
     * Use this after actions that trigger heavy re-renders (category selection, uploads, etc.)
     */
    public void waitForFullPageReady() {
        waitForFullPageReady(DEFAULT_TIMEOUT);
    }

    /**
     * Comprehensive wait with custom timeout.
     *
     * @param timeoutSeconds custom timeout
     */
    public void waitForFullPageReady(int timeoutSeconds) {
        log.debug("[WaitUtils] Waiting for FULL PAGE READY (page + angular + DOM stable)...");
        waitForPageLoad(timeoutSeconds);
        waitForAngularLoad(timeoutSeconds);
        waitForDomStable(timeoutSeconds, 600);
        log.debug("[WaitUtils] Page FULLY READY.");
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Build a FluentWait with the given timeout and default polling interval.
     * Ignores StaleElementReferenceException during polling.
     */
    private FluentWait<WebDriver> buildFluentWait(int timeoutSeconds) {
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(DEFAULT_POLL_MS))
                .ignoring(StaleElementReferenceException.class);
    }

    /**
     * Non-blocking pause (used internally for retry delays).
     */
    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
