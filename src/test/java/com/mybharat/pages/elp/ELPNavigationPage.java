package com.mybharat.pages.elp;

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

/**
 * ELPNavigationPage - Handles navigation from profile to ELP section.
 * 
 * After login, navigates to profile page, scrolls to bottom,
 * and clicks "View More" button in the Organizations section.
 */
public class ELPNavigationPage extends BasePage {

    private static final Logger log = LogManager.getLogger(ELPNavigationPage.class);
    private final ConfigReader config = new ConfigReader();

    public ELPNavigationPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Navigate to profile page, scroll down, and click "View More" button.
     * Then click on the first organisation name in the table.
     */
    public void navigateToProfileAndClickViewMore() throws InterruptedException {
        // Navigate to profile page
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
            // Fallback: try by href
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
     * The org name is a link in the "Organisation Name" column (2nd column).
     */
    public void clickFirstOrganisation() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            // Find the first link inside the table body (organisation name is a clickable link)
            WebElement orgLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//table//tbody//tr[1]//td[2]//a")));
            String orgName = orgLink.getText();
            scrollToElement(orgLink);
            Thread.sleep(300);
            orgLink.click();
            log.info("✅ Clicked organisation: {}", orgName);
        } catch (Exception e) {
            // Fallback: find any link in the table that looks like an org name
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
        log.info("✅ Navigated to organisation page. URL: {}", driver.getCurrentUrl());

        // Click on "Experiential Learning" link
        clickExperientialLearning();
    }

    /**
     * Click on "Experiential Learning" link/tab on the organisation page.
     * Waits for any overlay/loader to disappear first.
     */
    public void clickExperientialLearning() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Wait for overlay to disappear
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.invisibilityOfElementLocated(By.id("overlay")));
            log.info("Overlay disappeared");
        } catch (Exception e) {
            // Try removing overlay via JS
            ((JavascriptExecutor) driver).executeScript(
                    "var overlay = document.getElementById('overlay');" +
                    "if(overlay) overlay.style.display='none';");
            Thread.sleep(500);
        }

        try {
            WebElement elpLink = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//span[normalize-space()='Experiential Learning']")));
            scrollToElement(elpLink);
            Thread.sleep(500);
            try {
                elpLink.click();
            } catch (Exception clickEx) {
                // Element intercepted — use JS click
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", elpLink);
            }
            log.info("✅ Clicked 'Experiential Learning'");
        } catch (Exception e) {
            log.error("Experiential Learning link not found: {}", e.getMessage());
            throw new RuntimeException("Experiential Learning link not found");
        }

        waitForPageLoad();
        Thread.sleep(2000);
        log.info("✅ On Experiential Learning page. URL: {}", driver.getCurrentUrl());
    }
}
