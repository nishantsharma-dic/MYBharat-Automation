package com.mybharat.pages.vo;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;
import com.mybharat.utils.SessionHelper;

/**
 * VOTemplateNodalDyoPage - Same flow as ELPNavigationPage but clicks
 * "Volunteer for Bharat" tab instead of "Experiential Learning".
 *
 * Flow:
 *   1. Navigate to /youth-profile
 *   2. Scroll to bottom → Click "View More" in Organizations section
 *   3. Click first Organisation in the table
 *   4. Click "Volunteer for Bharat" tab
 *   5. Click "Templates -Nodal/DYO" in sidebar
 *   6. Click "Create Template" button
 */
public class VOTemplateNodalDyoPage extends BasePage {

    private static final Logger log = LogManager.getLogger(VOTemplateNodalDyoPage.class);
    private final ConfigReader config = new ConfigReader();

    public VOTemplateNodalDyoPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Full navigation: Profile → View More → Org → Templates -Nodal/DYO → Create Template
     */
    public void navigateToCreateTemplate() throws InterruptedException {
        // Check session before navigating
        SessionHelper sessionHelper = new SessionHelper(driver);
        if (!sessionHelper.isSessionActive()) {
            log.warn("Session expired before template navigation — re-logging in...");
            sessionHelper.ensureLoggedIn(null);
        }

        navigateToProfileAndClickViewMore();
        clickTemplatesNodalDyo();
        clickCreateTemplateButton();
    }

    /**
     * Navigate to profile page, scroll down, and click "View More" button.
     * Then click on the first organisation name in the table.
     */
    public void navigateToProfileAndClickViewMore() throws InterruptedException {
        String profileUrl = config.getProperty("profileUrl");
        if (profileUrl == null || profileUrl.isEmpty()) {
            profileUrl = config.getUrl() + "/youth-profile";
        }

        log.info("Navigating to profile: {}", profileUrl);
        driver.get(profileUrl);
        waitForPageLoad();
        Thread.sleep(3000);

        // Scroll to bottom of the page
        log.info("Scrolling to bottom of profile page...");
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        Thread.sleep(2000);

        // Click "View More" button in Organizations section
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            WebElement viewMoreBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[normalize-space()='View More']")));
            scrollToElement(viewMoreBtn);
            Thread.sleep(300);
            viewMoreBtn.click();
            log.info("✅ Clicked 'View More' button");
        } catch (Exception e) {
            try {
                WebElement viewMoreBtn = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//a[contains(@href,'mybharat_organizations')]")));
                scrollToElement(viewMoreBtn);
                viewMoreBtn.click();
                log.info("✅ Clicked 'View More' button (href fallback)");
            } catch (Exception e2) {
                log.error("View More button not found: {}", e2.getMessage());
                throw new RuntimeException("View More button not found on profile page");
            }
        }

        waitForPageLoad();
        Thread.sleep(2000);
        log.info("On organizations page. URL: {}", driver.getCurrentUrl());

        // Click on the first organisation name link in the table
        clickFirstOrganisation();
    }

    /**
     * Click on the first organisation name link in the My Bharat Organization table.
     */
    public void clickFirstOrganisation() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            WebElement orgLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//table//tbody//tr[1]//td[2]//a")));
            String orgName = orgLink.getText();
            scrollToElement(orgLink);
            Thread.sleep(300);
            orgLink.click();
            log.info("✅ Clicked organisation: {}", orgName);
        } catch (Exception e) {
            try {
                WebElement orgLink = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//table//tbody//a[1]")));
                String orgName = orgLink.getText();
                scrollToElement(orgLink);
                orgLink.click();
                log.info("✅ Clicked organisation (fallback): {}", orgName);
            } catch (Exception e2) {
                log.error("Organisation link not found in table: {}", e2.getMessage());
                throw new RuntimeException("Organisation link not found");
            }
        }

        waitForPageLoad();
        Thread.sleep(2000);
        dismissOverlay();
        log.info("✅ Navigated to organisation page. URL: {}", driver.getCurrentUrl());
    }

    /**
     * Click on "Volunteer for Bharat" tab on the organisation page.
     * Same as ELP's clickExperientialLearning() but for VFB tab.
     */
    public void clickVolunteerForBharatTab() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Step 1: Wait for overlay to disappear, force-hide if stuck
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.invisibilityOfElementLocated(By.id("overlay")));
            log.info("Overlay disappeared");
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript(
                    "var overlay = document.getElementById('overlay');" +
                    "if(overlay) overlay.style.display='none';");
            Thread.sleep(500);
            log.info("Overlay force-hidden via JS");
        }

        // Step 2: Click "Volunteer for Bharat" tab

        try {
            WebElement voTab = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//span[normalize-space()='Volunteer for Bharat']")));
            scrollToElement(voTab);
            Thread.sleep(500);
            try {
                voTab.click();
            } catch (Exception clickEx) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", voTab);
            }
            log.info("✅ Clicked 'Volunteer for Bharat' tab");
        } catch (Exception e) {
            log.error("Volunteer for Bharat tab not found: {}", e.getMessage());
            throw new RuntimeException("Volunteer for Bharat tab not found");
        }

        waitForPageLoad();
        Thread.sleep(2000);
        dismissOverlay();
        log.info("✅ On Volunteer for Bharat page. URL: {}", driver.getCurrentUrl());
    }

    /**
     * Click "Templates -Nodal/DYO" in the left sidebar.
     */
    public void clickTemplatesNodalDyo() throws InterruptedException {
        log.info("Clicking 'Templates -Nodal/DYO' in sidebar...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        dismissOverlay();

        try {
            WebElement templatesLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(text(),'Templates') and contains(text(),'Nodal')]"
                            + " | //a[contains(text(),'Templates -Nodal/DYO')]"
                            + " | //a[contains(@href,'event_template')]")));
            scrollToElement(templatesLink);
            Thread.sleep(300);
            safeClick(templatesLink);
            log.info("✅ Clicked 'Templates -Nodal/DYO'");
        } catch (Exception e) {
            log.error("Templates -Nodal/DYO link not found: {}", e.getMessage());
            throw new RuntimeException("Templates -Nodal/DYO link not found in sidebar");
        }

        waitForPageLoad();
        Thread.sleep(2000);
        dismissOverlay();
        log.info("✅ On Templates page. URL: {}", driver.getCurrentUrl());
    }

    /**
     * Click "Create Template" button on the template list page.
     */
    public void clickCreateTemplateButton() throws InterruptedException {
        log.info("Clicking 'Create Template' button...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        dismissOverlay();

        try {
            WebElement createBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[normalize-space()='Create Template']"
                            + " | //button[normalize-space()='Create Template']"
                            + " | //a[contains(@href,'add_event_template')]")));
            scrollToElement(createBtn);
            Thread.sleep(300);
            safeClick(createBtn);
            log.info("✅ Clicked 'Create Template'");
        } catch (Exception e) {
            log.error("Create Template button not found: {}", e.getMessage());
            throw new RuntimeException("Create Template button not found");
        }

        waitForPageLoad();
        Thread.sleep(2000);
        dismissOverlay();
        log.info("✅ On Add Template page. URL: {}", driver.getCurrentUrl());
    }

    /**
     * Dismiss loader/overlay if it's blocking the page.
     */
    private void dismissOverlay() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.invisibilityOfElementLocated(By.id("overlay")));
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript(
                    "var o = document.getElementById('overlay'); if(o) o.style.display='none';" +
                    "var l = document.getElementById('loader2'); if(l) l.style.display='none';" +
                    "try { $('#overlay').hide(); $('#loader2').hide(); $('.loader').hide(); } catch(e) {}");
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
    }
}
