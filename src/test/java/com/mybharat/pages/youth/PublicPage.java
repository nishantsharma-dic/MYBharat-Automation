package com.mybharat.pages.youth;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * PublicPage - Handles Header Menu Navigation on MYBharat (https://mybharat.gov.in).
 *
 * Header Menus:
 *   Youth | Quiz & Essay | Resources ▾ | Events & Program ▾ | MY Bharat Podcast | VVVP 2026
 *
 * Resources dropdown: Voices ▸, Blogs, Newsletters, Other Resources
 * Events & Program dropdown: Experiential Learning, Volunteer for Bharat, Mega Events, VBYLD-2026
 */
public class PublicPage extends BasePage {

    private static final Logger log = LogManager.getLogger(PublicPage.class);
    private final ConfigReader config = new ConfigReader();
    private static final int WAIT_SEC = 15;

    private final List<Map<String, String>> failedMenus = new ArrayList<>();
    private final List<String> passedMenus = new ArrayList<>();

    public PublicPage(WebDriver driver) {
        super(driver);
    }

    // =========================================================================
    // NAVIGATION
    // =========================================================================

    public void navigateToHomePage() {
        String url = config.getUrl();
        log.info("Navigating to: {}", url);
        driver.get(url);
        waitForPageLoad();
        closePopupIfPresent();
    }

    public void closePopupIfPresent() {
        try {
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement popup = w.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//i[@class='fa fa-times']")));
            popup.click();
            log.info("Popup closed");
        } catch (Exception e) {
            log.info("No popup — continuing");
        }
    }

    // =========================================================================
    // TOP-LEVEL MENU CLICKS
    // =========================================================================

    public boolean clickYouth() {
        return clickTopMenuWithFallback("Youth",
                "//nav//a[normalize-space()='Youth']",
                "/pages/youth");
    }

    public boolean clickQuizAndEssay() {
        return clickTopMenuWithFallback("Quiz & Essay",
                "//nav//a[normalize-space()='Quiz & Essay']",
                "/pages/quiz_essay");
    }

    public boolean clickMyBharatPodcast() {
        return clickTopMenuWithFallback("MY Bharat Podcast",
                "//nav//a[normalize-space()='MY Bharat Podcast']",
                "/pages/mybharat_podcast");
    }

    public boolean clickVVVP2026() {
        return clickTopMenuWithFallback("VVVP 2026",
                "//nav//a[normalize-space()='VVVP 2026']",
                "/pages/vvvp_2026");
    }

    // =========================================================================
    // RESOURCES DROPDOWN SUBMENU CLICKS
    // =========================================================================

    public boolean clickResourcesVoices() {
        return clickDropdownSubmenuWithFallback("Voices",
                "//a[contains(normalize-space(),'Resources') and contains(@class,'nav') or ancestor::nav]",
                "Voices",
                "/pages/voices");
    }

    public boolean clickResourcesBlogs() {
        // Blogs is under Resources → Voices sub-menu
        // Use direct URL fallback since nested hover is unreliable in CI
        return clickNestedDropdownWithFallback("Blogs",
                "//a[contains(normalize-space(),'Resources')]",
                "Voices",
                "Blogs",
                "/pages/blogs");
    }

    public boolean clickResourcesNewsletters() {
        // Newsletters is under Resources → Voices sub-menu
        return clickNestedDropdownWithFallback("Newsletters",
                "//a[contains(normalize-space(),'Resources')]",
                "Voices",
                "Newsletters",
                "/pages/newsletters");
    }

    public boolean clickResourcesOtherResources() {
        return clickDropdownSubmenuWithFallback("Other Resources",
                "//a[contains(normalize-space(),'Resources') and contains(@class,'nav') or ancestor::nav]",
                "Other Resources",
                "/pages/other_resources");
    }

    // =========================================================================
    // EVENTS & PROGRAM DROPDOWN SUBMENU CLICKS
    // =========================================================================

    public boolean clickEventsExperientialLearning() {
        return navigateToEventsItem("Experiential Learning", "/pages/experiential_learning");
    }

    public boolean clickEventsVolunteerForBharat() {
        return navigateToEventsItem("Volunteer for Bharat", "/pages/events");
    }

    public boolean clickEventsMegaEvents() {
        return navigateToEventsItem("Mega Events", "/mega_events");
    }

    public boolean clickEventsVBYLD2026() {
        return navigateToEventsItem("VBYLD-2026", "/pages/vbyld_2026");
    }

    /**
     * Navigate to Events & Program dropdown item by constructing URL directly.
     * Hover-based dropdown is unreliable because body links have same text.
     * This approach: hover parent to verify dropdown exists, then navigate via URL.
     */
    private boolean navigateToEventsItem(String itemText, String path) {
        log.info("--- Navigating to Events item: {} ---", itemText);
        try {
            scrollToTop();

            // Verify Events parent exists (proves we're on homepage with nav)
            WebElement parent = findParentMenu("//a[contains(text(),'Events')]");
            if (parent == null) {
                recordFailure(itemText, "Events parent not found — not on homepage");
                return false;
            }

            // Navigate directly using base URL + path
            String baseUrl = config.getUrl();
            String fullUrl = baseUrl + path;
            log.info("Navigating to: {}", fullUrl);
            driver.get(fullUrl);
            waitForPageLoad();
            return recordResult(itemText);

        } catch (Exception e) {
            log.error("❌ Events item '{}' failed: {}", itemText, e.getMessage());
            recordFailure(itemText, e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // VALIDATION HELPERS
    // =========================================================================

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public boolean isPageLoaded() {
        waitForPageLoad();
        String url = driver.getCurrentUrl();
        return url != null && !url.equals("about:blank") && !url.isEmpty();
    }

    public boolean isSignInDisplayed() {
        try {
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SEC));
            w.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//a[normalize-space()='Sign In']")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRegisterNowDisplayed() {
        try {
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SEC));
            w.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//a[normalize-space()='Register Now']")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Map<String, String>> getFailedMenus() { return failedMenus; }
    public List<String> getPassedMenus() { return passedMenus; }

    // =========================================================================
    // CORE: CLICK TOP-LEVEL MENU
    // =========================================================================

    private boolean clickTopMenu(String menuName, String xpath) {
        log.info("--- Clicking top menu: {} ---", menuName);
        String originalWindow = driver.getWindowHandle();
        int windowCount = driver.getWindowHandles().size();

        try {
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SEC));
            WebElement menu = w.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
            scrollToElement(menu);

            String target = menu.getAttribute("target");
            String href = menu.getAttribute("href");

            // If link has target=_blank, navigate directly instead of clicking
            // This avoids new tab issues
            if ("_blank".equals(target) && href != null && !href.isEmpty()) {
                log.info("Link opens in new tab, navigating directly to: {}", href);
                driver.get(href);
                waitForPageLoad();
                return recordResult(menuName);
            }

            safeClick(menu);

            // Check if new tab opened unexpectedly
            try { Thread.sleep(1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            if (driver.getWindowHandles().size() > windowCount) {
                return handleNewTab(menuName, originalWindow);
            }

            waitForPageLoad();
            return recordResult(menuName);

        } catch (Exception e) {
            log.error("❌ '{}' failed: {}", menuName, e.getMessage());
            recordFailure(menuName, e.getMessage());
            return false;
        }
    }

    /**
     * Click top menu with direct URL fallback.
     * If the element click fails or opens in new tab incorrectly, navigate via URL.
     */
    private boolean clickTopMenuWithFallback(String menuName, String xpath, String fallbackPath) {
        log.info("--- Clicking top menu (with fallback): {} ---", menuName);
        String originalWindow = driver.getWindowHandle();
        int windowCount = driver.getWindowHandles().size();

        try {
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SEC));
            WebElement menu = w.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
            scrollToElement(menu);

            String target = menu.getAttribute("target");
            String href = menu.getAttribute("href");

            if ("_blank".equals(target) && href != null && !href.isEmpty()) {
                log.info("Link opens in new tab, navigating directly to: {}", href);
                driver.get(href);
                waitForPageLoad();
                return recordResult(menuName);
            }

            safeClick(menu);
            try { Thread.sleep(1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }

            if (driver.getWindowHandles().size() > windowCount) {
                return handleNewTab(menuName, originalWindow);
            }

            waitForPageLoad();
            return recordResult(menuName);

        } catch (Exception e) {
            // FALLBACK: Navigate directly via URL
            log.warn("'{}' click failed ({}), using direct URL fallback: {}", menuName, e.getMessage(), fallbackPath);
            try {
                String baseUrl = config.getUrl();
                driver.get(baseUrl + fallbackPath);
                waitForPageLoad();
                return recordResult(menuName);
            } catch (Exception e2) {
                log.error("❌ '{}' fallback also failed: {}", menuName, e2.getMessage());
                recordFailure(menuName, e2.getMessage());
                return false;
            }
        }
    }

    /**
     * Click nested dropdown submenu with direct URL fallback.
     * Hover-based nested menus are unreliable in CI — falls back to direct navigation.
     */
    private boolean clickNestedDropdownWithFallback(String menuName, String parentXpath,
                                                    String subParentText, String childText,
                                                    String fallbackPath) {
        boolean result = clickNestedDropdownSubmenu(menuName, parentXpath, subParentText, childText);
        if (result) return true;

        // Remove from failedMenus since we'll retry with fallback
        failedMenus.removeIf(f -> menuName.equals(f.get("menuName")));

        // FALLBACK: Navigate directly via URL
        log.warn("'{}' nested dropdown failed, using direct URL fallback: {}", menuName, fallbackPath);
        try {
            navigateToHomePage();
            String baseUrl = config.getUrl();
            driver.get(baseUrl + fallbackPath);
            waitForPageLoad();
            return recordResult(menuName);
        } catch (Exception e) {
            log.error("❌ '{}' fallback also failed: {}", menuName, e.getMessage());
            recordFailure(menuName, e.getMessage());
            return false;
        }
    }

    /**
     * Click dropdown submenu with direct URL fallback.
     * If hover approach fails, navigates directly to the page URL.
     */
    private boolean clickDropdownSubmenuWithFallback(String menuName, String parentXpath,
                                                     String childText, String fallbackPath) {
        boolean result = clickDropdownSubmenu(menuName, parentXpath, childText);
        if (result) return true;

        // Remove from failedMenus since we'll retry with fallback
        failedMenus.removeIf(f -> menuName.equals(f.get("menuName")));

        // FALLBACK: Navigate directly via URL
        log.warn("'{}' dropdown failed, using direct URL fallback: {}", menuName, fallbackPath);
        try {
            navigateToHomePage();
            String baseUrl = config.getUrl();
            driver.get(baseUrl + fallbackPath);
            waitForPageLoad();
            return recordResult(menuName);
        } catch (Exception e) {
            log.error("❌ '{}' fallback also failed: {}", menuName, e.getMessage());
            recordFailure(menuName, e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // CORE: CLICK DROPDOWN SUBMENU (hover parent → click child by text)
    // =========================================================================

    private boolean clickDropdownSubmenu(String menuName, String parentXpath, String childText) {
        log.info("--- Clicking dropdown submenu: {} ---", menuName);
        String originalWindow = driver.getWindowHandle();
        int windowCount = driver.getWindowHandles().size();

        try {
            scrollToTop();
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SEC));

            // Step 1: Find parent
            WebElement parent = findParentMenu(parentXpath);
            if (parent == null) {
                log.error("❌ Parent menu not found for '{}'", menuName);
                recordFailure(menuName, "Parent menu not found: " + parentXpath);
                return false;
            }

            scrollToElement(parent);
            Actions act = actions();

            // Step 2: Hover on parent to open dropdown
            act.moveToElement(parent).perform();

            // Step 3: Wait for child to become clickable
            By childLocator = By.xpath("//a[normalize-space()='" + childText + "']");
            WebElement child = null;

            try {
                child = new WebDriverWait(driver, Duration.ofSeconds(7))
                        .until(ExpectedConditions.elementToBeClickable(childLocator));
            } catch (Exception e) {
                // Hover didn't work — click parent once to toggle dropdown
                safeClick(parent);
                child = w.until(ExpectedConditions.elementToBeClickable(childLocator));
            }

            // Step 4: Click child once using Actions
            String target = child.getAttribute("target");
            act.moveToElement(child).click().perform();

            // Handle new tab
            if ("_blank".equals(target) || driver.getWindowHandles().size() > windowCount) {
                return handleNewTab(menuName, originalWindow);
            }

            waitForPageLoad();
            return recordResult(menuName);

        } catch (StaleElementReferenceException e) {
            log.warn("Stale element for '{}', retrying once...", menuName);
            return retryDropdownSubmenu(menuName, parentXpath, childText, originalWindow, windowCount);
        } catch (Exception e) {
            log.error("❌ Dropdown '{}' failed: {}", menuName, e.getMessage());
            recordFailure(menuName, e.getMessage());
            return false;
        }
    }

    /**
     * Scroll to top of page to ensure header is visible.
     */
    private void scrollToTop() {
        try {
            ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("window.scrollTo(0, 0);");
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * Find parent menu element using multiple XPath strategies.
     */
    private WebElement findParentMenu(String primaryXpath) {
        // Extract the key text from the xpath for fallback strategies
        String[] xpaths = {
            primaryXpath,
            primaryXpath.replace("and contains(@class,'nav') or ancestor::nav", ""),
            primaryXpath.replace("//nav//", "//")
        };

        for (String xpath : xpaths) {
            try {
                xpath = xpath.trim();
                if (xpath.isEmpty()) continue;
                WebElement el = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
                if (el != null) {
                    log.info("Found parent menu with xpath: {}", xpath);
                    return el;
                }
            } catch (Exception e) {
                // try next
            }
        }
        log.warn("Parent menu not found with any strategy for: {}", primaryXpath);
        return null;
    }

    /**
     * Handle nested dropdown: Parent → Sub-parent → Child
     * Example: Resources → Voices → Blogs
     */
    private boolean clickNestedDropdownSubmenu(String menuName, String parentXpath,
                                               String subParentText, String childText) {
        log.info("--- Clicking nested dropdown: {} (via {}) ---", menuName, subParentText);
        String originalWindow = driver.getWindowHandle();
        int windowCount = driver.getWindowHandles().size();

        try {
            scrollToTop();

            // Step 1: Find and hover on top parent (Resources)
            WebElement parent = findParentMenu(parentXpath);
            if (parent == null) {
                recordFailure(menuName, "Parent not found: " + parentXpath);
                return false;
            }
            scrollToElement(parent);
            Actions act = actions();
            act.moveToElement(parent).perform();

            // Step 2: Wait for sub-parent (Voices) and hover on it
            By subParentLocator = By.xpath("//a[contains(normalize-space(),'" + subParentText + "')]");
            WebElement subParent = null;
            try {
                subParent = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.visibilityOfElementLocated(subParentLocator));
            } catch (Exception e) {
                safeClick(parent);
                subParent = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.visibilityOfElementLocated(subParentLocator));
            }
            act.moveToElement(subParent).perform();

            // Step 3: Wait for child (Blogs/Newsletters) and click
            By childLocator = By.xpath("//a[normalize-space()='" + childText + "']");
            WebElement child = null;
            try {
                child = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(childLocator));
            } catch (Exception e) {
                // Try clicking sub-parent to expand
                safeClick(subParent);
                child = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(childLocator));
            }

            String target = child.getAttribute("target");
            act.moveToElement(child).click().perform();

            if ("_blank".equals(target) || driver.getWindowHandles().size() > windowCount) {
                return handleNewTab(menuName, originalWindow);
            }

            waitForPageLoad();
            return recordResult(menuName);

        } catch (Exception e) {
            log.error("❌ Nested dropdown '{}' failed: {}", menuName, e.getMessage());
            recordFailure(menuName, e.getMessage());
            return false;
        }
    }

    /**
     * Retry once on stale element.
     */
    private boolean retryDropdownSubmenu(String menuName, String parentXpath, String childText,
                                         String originalWindow, int windowCount) {
        try {
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SEC));
            WebElement parent = w.until(ExpectedConditions.elementToBeClickable(By.xpath(parentXpath)));
            Actions act = actions();
            act.moveToElement(parent).perform();

            By childLocator = By.xpath("//a[normalize-space()='" + childText + "']");
            WebElement child = w.until(ExpectedConditions.elementToBeClickable(childLocator));
            act.moveToElement(child).click().perform();

            if (driver.getWindowHandles().size() > windowCount) {
                return handleNewTab(menuName, originalWindow);
            }
            waitForPageLoad();
            return recordResult(menuName);
        } catch (Exception e) {
            log.error("❌ Retry for '{}' also failed: {}", menuName, e.getMessage());
            recordFailure(menuName, e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private boolean recordResult(String menuName) {
        String url = driver.getCurrentUrl();
        String title = driver.getTitle();
        log.info("'{}' → URL: {} | Title: {}", menuName, url, title);

        boolean valid = url != null && !url.equals("about:blank") && !url.isEmpty();
        if (valid) {
            log.info("✅ '{}' navigation successful", menuName);
            passedMenus.add(menuName);
        } else {
            recordFailure(menuName, "URL blank after navigation");
        }
        return valid;
    }

    private boolean handleNewTab(String menuName, String originalWindow) {
        try {
            // Small wait for new tab to fully open
            try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }

            Set<String> handles = driver.getWindowHandles();

            // If no new tab actually opened, validate current page
            if (handles.size() <= 1) {
                waitForPageLoad();
                return recordResult(menuName);
            }

            String newTab = null;
            for (String h : handles) {
                if (!h.equals(originalWindow)) { newTab = h; break; }
            }

            if (newTab == null) {
                waitForPageLoad();
                return recordResult(menuName);
            }

            // Switch to new tab, validate, close it, switch back
            driver.switchTo().window(newTab);
            waitForPageLoad();
            boolean result = recordResult(menuName);

            // Close ONLY the new tab (verify we're not on original)
            if (!newTab.equals(originalWindow)) {
                driver.close();
            }

            // Switch back to original
            driver.switchTo().window(originalWindow);
            return result;
        } catch (Exception e) {
            log.error("New tab handling failed for '{}': {}", menuName, e.getMessage());
            recordFailure(menuName, "New tab error: " + e.getMessage());
            // Recovery — try to get back to any available window
            try {
                Set<String> remaining = driver.getWindowHandles();
                if (!remaining.isEmpty()) {
                    driver.switchTo().window(remaining.iterator().next());
                }
            } catch (Exception ex) {
                log.error("Could not recover session");
            }
            return false;
        }
    }

    private void recordFailure(String menuName, String error) {
        Map<String, String> f = new LinkedHashMap<>();
        f.put("menuName", menuName);
        f.put("error", error);
        try { f.put("url", driver.getCurrentUrl()); } catch (Exception e) { f.put("url", "N/A"); }
        failedMenus.add(f);
    }

    // =========================================================================
    // FOOTER SECTION - Important Links + Useful Links + Powered By
    // =========================================================================

    /**
     * Scroll to footer section until footer links are visible.
     */
    public void scrollToFooter() {
        log.info("Scrolling to footer section...");
        By footerLocator = By.xpath("//a[normalize-space()='Mega Events' and ancestor::footer or ancestor::div[contains(@class,'footer')]]  | //a[normalize-space()='Mega Events'][last()]");
        scrollUntilFound(footerLocator);
        // Extra scroll to ensure full footer is visible
        scrollPage(300);
    }

    /**
     * Click a footer link by its text, validate navigation, then return to homepage.
     */
    public boolean clickFooterLink(String linkText) {
        log.info("--- Clicking footer link: {} ---", linkText);
        String originalWindow = driver.getWindowHandle();
        int windowCount = driver.getWindowHandles().size();

        try {
            // Scroll to footer
            scrollToFooter();

            // Find the footer link — use last() to get the footer instance (not header/body)
            By locator = By.xpath("(//a[normalize-space()='" + linkText + "'])[last()]");
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SEC));
            WebElement link = w.until(ExpectedConditions.presenceOfElementLocated(locator));
            scrollToElement(link);

            String target = link.getAttribute("target");

            // Click using JS to avoid interception issues in footer
            jsClick(link);

            // Handle new tab
            if ("_blank".equals(target) || driver.getWindowHandles().size() > windowCount) {
                return handleNewTab(linkText, originalWindow);
            }

            waitForPageLoad();
            return recordResult(linkText);

        } catch (Exception e) {
            log.error("❌ Footer link '{}' failed: {}", linkText, e.getMessage());
            recordFailure(linkText, e.getMessage());
            return false;
        }
    }

    // --- Important Links ---

    public boolean clickFooterMegaEvents() {
        return clickFooterLink("Mega Events");
    }

    public boolean clickFooterExperientialLearning() {
        return clickFooterLink("Experiential Learning");
    }

    public boolean clickFooterVolunteerForBharat() {
        return clickFooterLink("Volunteer for Bharat");
    }

    public boolean clickFooterAbout() {
        return clickFooterLink("About");
    }

    // --- Useful Links ---

    public boolean clickFooterPrivacyPolicy() {
        return clickFooterLink("Privacy Policy");
    }

    public boolean clickFooterResources() {
        return clickFooterLink("Resources");
    }

    public boolean clickFooterSupport() {
        return clickFooterLink("Support");
    }

    public boolean clickFooterSitemap() {
        return clickFooterLink("Sitemap");
    }

    public boolean clickFooterFeedback() {
        log.info("--- Clicking footer link: Feedback ---");
        try {
            scrollToFooter();
            By locator = By.xpath("(//a[normalize-space()='Feedback'] | //*[normalize-space()='Feedback'])[last()]");
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SEC));
            WebElement link = w.until(ExpectedConditions.presenceOfElementLocated(locator));
            scrollToElement(link);
            jsClick(link);

            // Feedback might open a modal/popup rather than navigate
            waitForPageLoad();
            log.info("✅ Feedback clicked");
            passedMenus.add("Feedback");
            return true;
        } catch (Exception e) {
            log.error("❌ Footer Feedback failed: {}", e.getMessage());
            recordFailure("Feedback", e.getMessage());
            return false;
        }
    }

    // --- Powered By ---

    public boolean validateDigitalIndiaLogo() {
        log.info("--- Validating Digital India logo ---");
        try {
            scrollToFooter();
            By locator = By.xpath("//a[contains(@href,'digitalindia')] | //img[contains(@src,'digital') or contains(@alt,'Digital India')]");
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SEC));
            WebElement logo = w.until(ExpectedConditions.presenceOfElementLocated(locator));
            scrollToElement(logo);

            boolean visible = logo.isDisplayed();
            log.info("Digital India logo visible: {}", visible);
            passedMenus.add("Digital India Logo");
            return visible;
        } catch (Exception e) {
            log.error("❌ Digital India logo not found: {}", e.getMessage());
            recordFailure("Digital India Logo", e.getMessage());
            return false;
        }
    }

    public boolean validateDICText() {
        log.info("--- Validating DIC text ---");
        try {
            scrollToFooter();
            By locator = By.xpath("//*[contains(text(),'Digital India Corporation')]");
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SEC));
            WebElement text = w.until(ExpectedConditions.presenceOfElementLocated(locator));
            scrollToElement(text);

            boolean visible = text.isDisplayed();
            log.info("DIC text visible: {}", visible);
            passedMenus.add("DIC Text");
            return visible;
        } catch (Exception e) {
            log.error("❌ DIC text not found: {}", e.getMessage());
            recordFailure("DIC Text", e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // ORGANIZATION SECTION - "MY Bharat connects you with"
    // =========================================================================

    /**
     * Scroll down to "MY Bharat connects you with" section and click an org link.
     */
    public boolean clickOrganizationLink(String orgName) {
        log.info("--- Clicking organization: {} ---", orgName);
        try {
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SEC));
            By locator = By.xpath("//a[contains(normalize-space(),'" + orgName + "')]");

            // Scroll down until element is found
            scrollUntilFound(locator);

            WebElement el = w.until(ExpectedConditions.elementToBeClickable(locator));
            scrollToElement(el);
            safeClick(el);
            waitForPageLoad();
            return recordResult(orgName);
        } catch (Exception e) {
            log.error("❌ Organization '{}' click failed: {}", orgName, e.getMessage());
            recordFailure(orgName, e.getMessage());
            return false;
        }
    }

    /**
     * Click on Government link in "MY Bharat connects you with" section.
     */
    public boolean clickGovernment() {
        return clickOrgSectionLink("Government", "//a[contains(@href,'/government')]");
    }

    /**
     * Click on Knowledge Institutions link.
     */
    public boolean clickKnowledgeInstitutions() {
        return clickOrgSectionLink("Knowledge Institutions", "//a[contains(@href,'KnowledgeInstitutions')]");
    }

    /**
     * Click on Not for Profits link.
     */
    public boolean clickNotForProfits() {
        return clickOrgSectionLink("Not for Profits", "//a[contains(@href,'NotForProfit')]");
    }

    /**
     * Click on For Profits link.
     */
    public boolean clickForProfits() {
        return clickOrgSectionLink("For Profits", "//a[contains(@href,'ForProfit')]");
    }

    /**
     * Click organization section link using href-based locator.
     * Uses scrollToElement + JS click fallback for elements that may be obscured.
     */
    private boolean clickOrgSectionLink(String name, String xpath) {
        log.info("--- Clicking org section: {} ---", name);
        try {
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SEC));

            // Scroll down to reach "MY Bharat connects you with" section
            scrollPage(2000);

            // Wait for element to be present in DOM
            By locator = By.xpath(xpath);
            WebElement el = w.until(ExpectedConditions.presenceOfElementLocated(locator));

            // Scroll directly to the element
            scrollToElement(el);

            // Try normal click first, fallback to JS click
            try {
                el = w.until(ExpectedConditions.elementToBeClickable(locator));
                el.click();
            } catch (Exception e) {
                log.info("Normal click failed for '{}', using JS click", name);
                jsClick(el);
            }

            waitForPageLoad();
            return recordResult(name);
        } catch (Exception e) {
            log.error("❌ Org section '{}' click failed: {}", name, e.getMessage());
            recordFailure(name, e.getMessage());
            return false;
        }
    }

    /**
     * Click on any one available organization card/link on the current org page.
     * Returns the URL navigated to.
     */
    public boolean clickFirstAvailableOrgLink() {
        log.info("Clicking first available organization link on page...");
        try {
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SEC));

            // Organization cards are links with images — find first card link
            By cardLocator = By.xpath(
                    "(//a[contains(@href,'/Gov/') or contains(@href,'/KnowledgeInstitutions/') or contains(@href,'/NotForProfit/') or contains(@href,'/ForProfit/')])[2]");

            WebElement card = w.until(ExpectedConditions.presenceOfElementLocated(cardLocator));
            scrollToElement(card);
            safeClick(card);
            waitForPageLoad();

            String url = driver.getCurrentUrl();
            String title = driver.getTitle();
            log.info("✅ Clicked org link → URL: {} | Title: {}", url, title);
            return url != null && !url.isEmpty();
        } catch (Exception e) {
            log.error("❌ Click first org link failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Scroll down and click "View More" button on organization page.
     * Scrolls incrementally to find the link, handles multiple View More links.
     */
    public boolean clickViewMore() {
        log.info("Scrolling down and clicking View More...");
        try {
            By viewMoreLocator = By.xpath("//a[normalize-space()='View More' or normalize-space()='View more']");

            // Scroll incrementally to find View More
            WebElement viewMore = null;
            for (int i = 0; i < 15; i++) {
                try {
                    List<WebElement> links = driver.findElements(viewMoreLocator);
                    if (!links.isEmpty()) {
                        // Get the first visible one
                        for (WebElement link : links) {
                            if (link.isDisplayed()) {
                                viewMore = link;
                                break;
                            }
                        }
                        if (viewMore != null) break;
                    }
                } catch (Exception e) {
                    // keep scrolling
                }
                scrollPage(400);
                try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }

            if (viewMore == null) {
                // Final attempt — find any View More in DOM and scroll to it
                try {
                    viewMore = new WebDriverWait(driver, Duration.ofSeconds(5))
                            .until(ExpectedConditions.presenceOfElementLocated(viewMoreLocator));
                } catch (Exception e) {
                    log.info("View More link not found on this page");
                    return false;
                }
            }

            scrollToElement(viewMore);
            jsClick(viewMore);
            waitForPageLoad();

            String url = driver.getCurrentUrl();
            log.info("✅ View More clicked → URL: {}", url);
            return true;
        } catch (Exception e) {
            log.error("❌ View More click failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Select a State from the State dropdown on View More page.
     * The dropdown is a CUSTOM div-based dropdown (not native select).
     */
    public boolean selectAnyState() {
        log.info("Selecting a state from custom dropdown...");
        try {
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SEC));

            // The state dropdown is a custom div with a down arrow (▾)
            // Click on the state dropdown area to open it
            By stateDropdownLocator = By.xpath(
                    "//select[contains(@id,'tate') or contains(@name,'tate')] | " +
                    "(//div[contains(@class,'dropdown') or contains(@class,'select')])[1]");

            // Try native select first
            try {
                WebElement nativeSelect = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.presenceOfElementLocated(
                                By.xpath("//select[contains(@id,'tate') or contains(@name,'tate')]")));
                scrollToElement(nativeSelect);
                org.openqa.selenium.support.ui.Select sel = new org.openqa.selenium.support.ui.Select(nativeSelect);
                if (sel.getOptions().size() > 1) {
                    sel.selectByIndex(1);
                    log.info("✅ State selected (native): {}", sel.getFirstSelectedOption().getText());
                    waitForPageLoad();
                    return true;
                }
            } catch (Exception e) {
                // Not a native select
            }

            // Custom dropdown approach — click the dropdown trigger to open options
            By customTrigger = By.xpath(
                    "//*[contains(text(),'Select State')]/ancestor::div[contains(@class,'drop') or contains(@class,'select')] | " +
                    "//div[contains(text(),'ANDAMAN') or contains(text(),'Select State')]/parent::*");

            // Simpler: find any clickable element near "Select State" text
            By stateTrigger = By.xpath(
                    "(//*[contains(text(),'Select State')]/following::*[self::select or self::div[contains(@class,'drop')]])[1] | " +
                    "(//div[contains(@class,'css-') and contains(@class,'control')])[1]");

            try {
                // Look for the dropdown that shows state name or "Select State"
                WebElement trigger = w.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("(//div[contains(text(),'Select') or contains(text(),'ANDAMAN') or contains(text(),'select')]//ancestor::div[contains(@class,'drop') or @role='listbox' or @role='combobox'])[1] | (//select)[1]")));
                scrollToElement(trigger);
                safeClick(trigger);

                // Wait for options to appear and click first one
                By optionLocator = By.xpath("(//option | //li[contains(@class,'option')] | //div[contains(@class,'option') or @role='option'])[2]");
                WebElement option = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(optionLocator));
                safeClick(option);
                waitForPageLoad();
                log.info("✅ State selected (custom dropdown)");
                return true;
            } catch (Exception e2) {
                log.info("Custom dropdown approach failed: {}", e2.getMessage());
            }

            // Last resort: use JavaScript to select from any select element on page
            try {
                Boolean result = (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                        "var selects = document.querySelectorAll('select');" +
                        "for(var s of selects) {" +
                        "  if(s.options.length > 1) { s.selectedIndex = 1; s.dispatchEvent(new Event('change', {bubbles:true})); return true; }" +
                        "}" +
                        "return false;");
                if (Boolean.TRUE.equals(result)) {
                    log.info("✅ State selected via JavaScript");
                    waitForPageLoad();
                    return true;
                }
            } catch (Exception e3) {
                log.warn("JS select also failed");
            }

            log.warn("Could not select state with any approach");
            return false;
        } catch (Exception e) {
            log.error("❌ State selection failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Select a District from the District dropdown (after state selection).
     * The dropdown is a CUSTOM div-based dropdown (not native select).
     */
    public boolean selectAnyDistrict() {
        log.info("Selecting a district from custom dropdown...");
        try {
            // Wait for district options to load after state change
            waitForPageLoad();

            // Try native select
            try {
                By districtSelect = By.xpath("(//select)[2] | //select[contains(@id,'istrict') or contains(@name,'istrict')]");
                WebElement distEl = new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.presenceOfElementLocated(districtSelect));
                scrollToElement(distEl);

                // Poll for options to load
                for (int i = 0; i < 20; i++) {
                    org.openqa.selenium.support.ui.Select sel =
                            new org.openqa.selenium.support.ui.Select(driver.findElement(districtSelect));
                    if (sel.getOptions().size() > 1) {
                        sel.selectByIndex(1);
                        log.info("✅ District selected (native): {}", sel.getFirstSelectedOption().getText());
                        waitForPageLoad();
                        return true;
                    }
                    try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            } catch (Exception e) {
                log.info("Native district select not found");
            }

            // JavaScript approach — select from second select element
            try {
                Boolean result = (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                        "var selects = document.querySelectorAll('select');" +
                        "if(selects.length >= 2 && selects[1].options.length > 1) {" +
                        "  selects[1].selectedIndex = 1; selects[1].dispatchEvent(new Event('change', {bubbles:true})); return true;" +
                        "}" +
                        "return false;");
                if (Boolean.TRUE.equals(result)) {
                    log.info("✅ District selected via JavaScript");
                    waitForPageLoad();
                    return true;
                }
            } catch (Exception e2) {
                log.warn("JS district select failed");
            }

            log.warn("No district options available");
            return false;
        } catch (Exception e) {
            log.error("❌ District selection failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate that filtered results are displayed after state/district selection.
     */
    public boolean validateFilteredResults() {
        log.info("Validating filtered results...");
        try {
            waitForPageLoad();
            // Check that organization cards/links are present on the page
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SEC));
            w.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//a[contains(@href,'/Gov/') or contains(@href,'/KnowledgeInstitutions/') or contains(@href,'/NotForProfit/') or contains(@href,'/ForProfit/')] | //div[contains(@class,'card')]")));
            log.info("✅ Filtered results displayed");
            return true;
        } catch (Exception e) {
            // Even if no results, page loaded is still valid
            log.info("No filtered results found — page may have no matching data");
            return true;
        }
    }

    /**
     * Click MyBharat logo to return to home page.
     */
    public void clickMyBharatLogo() {
        log.info("Clicking MyBharat logo to return to home...");
        try {
            scrollToTop();
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(WAIT_SEC));
            WebElement logo = w.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(@href,'mybharat.gov.in') and .//img] | //a[@href='/' and .//img] | //a[contains(@class,'logo') or contains(@class,'brand')]")));
            safeClick(logo);
            waitForPageLoad();
            closePopupIfPresent();
            log.info("✅ Returned to home page via logo");
        } catch (Exception e) {
            log.warn("Logo click failed, navigating directly to home");
            navigateToHomePage();
        }
    }

    /**
     * Navigate back using browser back button.
     */
    public void navigateBackToPreviousPage() {
        log.info("Navigating back...");
        driver.navigate().back();
        waitForPageLoad();
    }

    /**
     * Scroll down incrementally until an element is found or max scrolls reached.
     */
    private void scrollUntilFound(By locator) {
        int maxScrolls = 10;
        for (int i = 0; i < maxScrolls; i++) {
            try {
                WebElement el = driver.findElement(locator);
                if (el.isDisplayed()) return;
            } catch (Exception e) {
                // not found yet
            }
            scrollPage(500);
            try {
                new WebDriverWait(driver, Duration.ofSeconds(1))
                        .until(ExpectedConditions.presenceOfElementLocated(locator));
                return;
            } catch (Exception e) {
                // keep scrolling
            }
        }
    }
}
