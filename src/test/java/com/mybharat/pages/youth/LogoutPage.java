package com.mybharat.pages.youth;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;

/**
 * LogoutPage - Handles user logout functionality.
 * 
 * Opens the user menu and clicks the logout button.
 * Can be reused after any flow that requires logging out (registration, profile, etc.)
 */
public class LogoutPage extends BasePage {

    private static final Logger log = LogManager.getLogger(LogoutPage.class);

    private WebDriverWait longWait;

    // Locators
    private static final By USER_MENU_BUTTON = By.xpath(
            "//a[@id='user-options']//div[contains(@class,'user-info-wrapper')]");
    private static final By LOGOUT_BUTTON = By.xpath(
            "//a[contains(@class,'firebase-profile-logout-btn')]");

    public LogoutPage(WebDriver driver) {
        super(driver);
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Perform logout: click user menu circle → click Log Out.
     */
    public void logout() throws InterruptedException {
        log.info("Performing logout...");
        openUserMenu();
        Thread.sleep(500);
        clickLogoutButton();
        waitForPageLoad();
        log.info("✅ User logged out successfully");
    }

    /**
     * Open the user profile/avatar menu.
     */
    private void openUserMenu() {
        try {
            WebElement menu = longWait.until(ExpectedConditions.elementToBeClickable(USER_MENU_BUTTON));
            jsClick(menu);
            log.info("Opened user menu");
        } catch (Exception e) {
            // Fallback: try clicking the logout button directly (visible on some pages)
            log.warn("User menu button not found, trying logout button directly");
        }
    }

    /**
     * Click the logout button.
     */
    private void clickLogoutButton() {
        WebElement logoutBtn = longWait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON));
        jsClick(logoutBtn);
        log.info("Clicked logout");
    }
}
