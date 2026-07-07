package com.mybharat.utils;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.vo.VOLoginPage;

/**
 * SessionHelper - Checks if the user session is still active.
 * If session expired, automatically re-logs in with the same email.
 */
public class SessionHelper {

    private static final Logger log = LogManager.getLogger(SessionHelper.class);

    private final WebDriver driver;
    private final ConfigReader config;

    public SessionHelper(WebDriver driver) {
        this.driver = driver;
        this.config = new ConfigReader();
    }

    /**
     * Check if session is still active. If expired, re-login with the given email.
     * Call this before any major navigation step.
     *
     * @param email The email to re-login with if session expired
     * @return true if session was active or re-login succeeded
     */
    public boolean ensureLoggedIn(String email) {
        if (isSessionActive()) {
            return true;
        }

        log.warn("Session expired! Re-logging in with: {}", email);
        try {
            VOLoginPage loginPage = new VOLoginPage(driver);
            // Clear state
            driver.manage().deleteAllCookies();
            ((JavascriptExecutor) driver).executeScript(
                    "try{window.localStorage.clear();window.sessionStorage.clear();}catch(e){}");
            Thread.sleep(1000);

            // Re-login
            loginPage.loginWithRandomVOUser();
            log.info("✅ Re-login successful");
            return true;
        } catch (Exception e) {
            log.error("Re-login failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if the current page indicates an active session.
     * Returns false if on login page, session expired page, or shows login prompt.
     */
    public boolean isSessionActive() {
        try {
            String currentUrl = driver.getCurrentUrl();
            String pageSource = driver.getPageSource().toLowerCase();

            // Check URL for login page
            if (currentUrl.contains("/login") || currentUrl.contains("signin")) {
                log.info("Session check: On login page — session expired");
                return false;
            }

            // Check for session expired messages
            if (pageSource.contains("session expired") || pageSource.contains("session timed out")
                    || pageSource.contains("please login again") || pageSource.contains("please sign in")) {
                log.info("Session check: Found session expired message");
                return false;
            }

            // Check if Sign In button is visible (means not logged in)
            try {
                WebElement signIn = driver.findElement(
                        By.xpath("//span[normalize-space()='Sign In']"));
                if (signIn.isDisplayed()) {
                    log.info("Session check: Sign In button visible — not logged in");
                    return false;
                }
            } catch (Exception e) {
                // Sign In not found — good, means we're logged in
            }

            // Check if profile icon/dropdown exists (means logged in)
            try {
                driver.findElement(By.xpath(
                        "//a[@data-bs-toggle='dropdown' or @data-toggle='dropdown']"
                        + " | //a[contains(@class,'dropdown-toggle')]"
                        + " | //span[contains(@class,'user-name')]"));
                return true;
            } catch (Exception e) {
                // Profile dropdown not found — might still be logged in on a different page type
            }

            // Default: assume session is active if none of the above triggered
            return true;

        } catch (Exception e) {
            log.warn("Session check failed: {}", e.getMessage());
            return true; // Assume active to avoid unnecessary re-login
        }
    }
}
