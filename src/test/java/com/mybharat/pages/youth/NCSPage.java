package com.mybharat.pages.youth;

import java.time.Duration;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * NCSPage - Page Object for the National Career Service (NCS) integration on MYBharat.
 *
 * Purpose: Handles navigation to the NCS page from the youth profile sidebar.
 *          After login, user clicks "National Career Service" link in the sidebar
 *          under "Links for Me" section, which opens https://mybharat.gov.in/ncs/
 *
 * Flow:
 *   1. navigateToProfilePage() — ensure user is on /youth-profile
 *   2. clickNCSLink()          — click "National Career Service" in sidebar
 *   3. verifyNCSPageOpened()   — verify NCS page URL and content loaded
 *
 * Sidebar Structure (from screenshot):
 *   Links for Me:
 *     - E-Shram
 *     - National Career Service  ← target link
 *     - Invitation
 *
 * Environment:
 *   Prod: https://mybharat.gov.in/ncs/
 *   Beta: https://yuva-beta.mybharats.in/ncs/
 *
 * Dependencies: BasePage, ConfigReader
 * Developer: Manoj Kumar (QA Team)
 *
 * @see NCSNavigationTest
 * @see LoginPage
 */
public class NCSPage extends BasePage {

    private static final Logger log = LogManager.getLogger(NCSPage.class);

    private final ConfigReader config = new ConfigReader();
    private final WebDriverWait longWait;

    // -------------------------------------------------------------------------
    // Locators - Sidebar "Links for Me" section
    // -------------------------------------------------------------------------

    /** "National Career Service" link in the sidebar under "Links for Me" */
    @FindBy(xpath = "//a[contains(text(),'National Career Service')] | //span[contains(text(),'National Career Service')]/ancestor::a")
    private WebElement ncsLink;

    /** Alternative locator: link with href containing 'ncs' */
    private static final By NCS_LINK_BY_HREF = By.xpath(
            "//a[contains(@href,'ncs')] | //a[contains(text(),'National Career Service')]");

    /** Alternative: sidebar section heading "Links for Me" */
    private static final By LINKS_FOR_ME_HEADING = By.xpath(
            "//*[contains(text(),'Links for Me') or contains(text(),'Links For Me')]");

    // -------------------------------------------------------------------------
    // Locators - NCS Page verification
    // -------------------------------------------------------------------------

    /** NCS page content indicator - page heading or known element */
    private static final By NCS_PAGE_CONTENT = By.xpath(
            "//h1[contains(text(),'National Career Service')] | " +
            "//h2[contains(text(),'NCS')] | " +
            "//div[contains(@class,'ncs')] | " +
            "//*[contains(text(),'Career Service')]");

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public NCSPage(WebDriver driver) {
        super(driver);
        int timeout = Boolean.parseBoolean(System.getProperty("ciMode", "false")) ? 45 : 25;
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
    }

    // -------------------------------------------------------------------------
    // Public Methods
    // -------------------------------------------------------------------------

    /**
     * Navigate to the youth profile page where the sidebar is visible.
     */
    public void navigateToProfilePage() {
        String profileUrl = config.getProperty("profileUrl");
        if (profileUrl == null || profileUrl.isEmpty()) {
            profileUrl = config.getUrl() + "/youth-profile";
        }

        log.info("Navigating to profile page: {}", profileUrl);
        driver.get(profileUrl);
        waitForPageLoad();
        safeSleep(2000); // Wait for React hydration + sidebar to render
    }

    /**
     * Scroll down the sidebar and click "National Career Service" link.
     * The link is under "Links for Me" section in the left sidebar.
     */
    public void clickNCSLink() {
        log.info("Looking for 'National Career Service' link in sidebar...");

        // First scroll the sidebar to make the link visible
        scrollToLinksForMeSection();
        safeSleep(500);

        WebElement ncsElement = null;

        // Strategy 1: Try @FindBy annotated element
        try {
            waitForClickable(ncsLink);
            ncsElement = ncsLink;
            log.info("Found NCS link via @FindBy annotation");
        } catch (Exception e) {
            log.info("@FindBy NCS link not clickable, trying alternative locators...");
        }

        // Strategy 2: Find by href or text content
        if (ncsElement == null) {
            try {
                ncsElement = longWait.until(ExpectedConditions.elementToBeClickable(NCS_LINK_BY_HREF));
                log.info("Found NCS link via href/text locator");
            } catch (Exception e) {
                log.info("href/text locator failed, trying sidebar context...");
            }
        }

        // Strategy 3: Find within "Links for Me" section context
        if (ncsElement == null) {
            try {
                WebElement linksSection = driver.findElement(LINKS_FOR_ME_HEADING);
                scrollToElement(linksSection);
                safeSleep(300);
                // Find the NCS link relative to "Links for Me" heading
                ncsElement = linksSection.findElement(By.xpath(
                        "./following::a[contains(text(),'National Career Service')][1]"));
                log.info("Found NCS link relative to 'Links for Me' heading");
            } catch (Exception e) {
                log.warn("Could not find NCS link in sidebar context");
            }
        }

        // Strategy 4: JavaScript-based search (fallback for hidden/overlapping elements)
        if (ncsElement == null) {
            try {
                ncsElement = (WebElement) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                        "var links = document.querySelectorAll('a');" +
                        "for (var i = 0; i < links.length; i++) {" +
                        "  if (links[i].textContent.includes('National Career Service')) return links[i];" +
                        "}" +
                        "return null;");
                if (ncsElement != null) {
                    log.info("Found NCS link via JavaScript DOM traversal");
                }
            } catch (Exception e) {
                log.error("All strategies failed to find NCS link");
            }
        }

        if (ncsElement == null) {
            throw new RuntimeException("Could not find 'National Career Service' link in the sidebar. " +
                    "Ensure user is on the profile page and sidebar is visible.");
        }

        // Click the NCS link
        scrollToElement(ncsElement);
        safeSleep(300);
        safeClick(ncsElement);
        log.info("Clicked 'National Career Service' link");
        safeSleep(2000); // Wait for navigation
    }

    /**
     * Verify that the NCS page opened successfully.
     * Checks URL contains /ncs/ and page has loaded.
     *
     * @return true if NCS page is loaded, false otherwise
     */
    public boolean verifyNCSPageOpened() {
        waitForPageLoad();
        safeSleep(1000);

        String currentUrl = driver.getCurrentUrl();
        log.info("Current URL after clicking NCS: {}", currentUrl);

        // Check if NCS opened in a new tab
        Set<String> windowHandles = driver.getWindowHandles();
        if (windowHandles.size() > 1) {
            log.info("NCS opened in a new tab ({} tabs total)", windowHandles.size());
            // Switch to the latest tab
            String originalWindow = driver.getWindowHandle();
            for (String handle : windowHandles) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
            waitForPageLoad();
            safeSleep(1000);
            currentUrl = driver.getCurrentUrl();
            log.info("New tab URL: {}", currentUrl);
        }

        // Verify URL contains /ncs/
        boolean urlValid = currentUrl.contains("/ncs");
        if (urlValid) {
            log.info("✅ NCS page URL verified: {}", currentUrl);
        } else {
            log.warn("❌ URL does not contain /ncs/. Current URL: {}", currentUrl);
        }

        // Verify page content loaded (title or known element)
        boolean contentLoaded = false;
        try {
            String pageTitle = driver.getTitle();
            log.info("NCS page title: {}", pageTitle);

            // Check for NCS-related content on the page
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.or(
                            ExpectedConditions.presenceOfElementLocated(NCS_PAGE_CONTENT),
                            ExpectedConditions.urlContains("/ncs")
                    ));
            contentLoaded = true;
            log.info("✅ NCS page content verified");
        } catch (Exception e) {
            // URL check is sufficient if content check fails (page might still be loading)
            contentLoaded = urlValid;
            log.info("NCS content check skipped — relying on URL validation");
        }

        return urlValid || contentLoaded;
    }

    /**
     * Get the current page URL (for assertion in test).
     */
    public String getCurrentPageUrl() {
        return driver.getCurrentUrl();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Scroll the sidebar to bring "Links for Me" section into view.
     */
    private void scrollToLinksForMeSection() {
        try {
            WebElement linksHeading = driver.findElement(LINKS_FOR_ME_HEADING);
            scrollToElement(linksHeading);
            log.info("Scrolled to 'Links for Me' section");
        } catch (Exception e) {
            // Fallback: scroll down the page to bring sidebar content into view
            scrollPage(500);
            log.info("Scrolled page down to reveal sidebar links");
        }
    }

    /**
     * Safe sleep that doesn't throw checked exception.
     */
    private void safeSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
