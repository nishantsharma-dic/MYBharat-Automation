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
 * Supports two UI types with fallback strategy:
 *   1. PHP portal (mega event / org pages) — circle icon + firebase logout link
 *   2. React profile page (youth flow) — rounded-full button + menuitem role
 *
 * Can be reused after any flow that requires logging out.
 */
public class LogoutPage extends BasePage {

    private static final Logger log = LogManager.getLogger(LogoutPage.class);

    private WebDriverWait longWait;

    // Locators — PHP portal (mega event / org admin pages)
    private static final By USER_MENU_BUTTON_PHP = By.xpath(
            "//a[@id='user-options']//div[contains(@class,'user-info-wrapper')]");
    private static final By LOGOUT_BUTTON_PHP = By.xpath(
            "//a[contains(@class,'firebase-profile-logout-btn')]");

    // Locators — React profile page (youth registration / login flow)
    private static final By USER_MENU_BUTTON_REACT = By.xpath(
            "//button[contains(@class,'rounded-full') and contains(@class,'cursor-pointer')]");
    private static final By LOGOUT_BUTTON_REACT = By.xpath(
            "//button[@role='menuitem'] | //*[normalize-space()='Logout'] | //*[normalize-space()='Log Out']");

    public LogoutPage(WebDriver driver) {
        super(driver);
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Perform logout: click user menu circle → click Log Out.
     * Tries PHP portal locators first, falls back to React profile locators.
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
     * Tries PHP portal locator first, then React profile locator.
     */
    private void openUserMenu() {
        // Try PHP portal circle icon first
        try {
            WebElement menu = longWait.until(ExpectedConditions.elementToBeClickable(USER_MENU_BUTTON_PHP));
            jsClick(menu);
            log.info("Opened user menu (PHP portal)");
            return;
        } catch (Exception e) {
            log.warn("PHP portal user menu not found, trying React profile locator...");
        }
        // Fallback: React profile rounded-full button
        try {
            WebElement menu = longWait.until(ExpectedConditions.elementToBeClickable(USER_MENU_BUTTON_REACT));
            jsClick(menu);
            log.info("Opened user menu (React profile)");
        } catch (Exception e) {
            log.warn("React user menu also not found — will try logout button directly");
        }
    }

    /**
     * Click the logout button.
     * Tries PHP portal locator first, then React profile locator.
     */
    private void clickLogoutButton() {
        // Try PHP portal logout link first
        try {
            WebElement logoutBtn = longWait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON_PHP));
            jsClick(logoutBtn);
            log.info("Clicked logout (PHP portal)");
            return;
        } catch (Exception e) {
            log.warn("PHP portal logout button not found, trying React profile locator...");
        }
        // Fallback: React profile logout button/menuitem
        WebElement logoutBtn = longWait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON_REACT));
        jsClick(logoutBtn);
        log.info("Clicked logout (React profile)");
    }
}
