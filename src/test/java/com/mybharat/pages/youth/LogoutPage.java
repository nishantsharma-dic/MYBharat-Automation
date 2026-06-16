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
 * LogoutPage - Page Object for the user logout functionality.
 *
 * Purpose: Handles user session termination by interacting with the user menu
 *          and clicking the logout button. Includes retry logic for flaky UI interactions.
 *
 * Flow:
 *   1. Wait for page stability (2s delay)
 *   2. openUserMenu()     — clicks the circular profile/avatar button
 *   3. clickLogoutButton() — clicks the logout menu item
 *   4. waitForPageLoad()  — waits for redirect to home page
 *
 * Key Methods:
 *   - logout() — performs the complete logout sequence
 *
 * Usage: Called after registration, profile completion, or any flow
 *        that needs the user to be logged out before the next step.
 *
 * Dependencies: BasePage (parent), Selenium WebDriverWait
 * Developer: Nishant Sharma (QA Team)
 *
 * @see LogoutTest
 * @see LoginPage
 */
public class LogoutPage extends BasePage {

    private static final Logger log = LogManager.getLogger(LogoutPage.class);

    private WebDriverWait longWait;

    // Locators
    private static final By USER_MENU_BUTTON = By.xpath(
            "//button[@class='flex items-center rounded-full cursor-pointer']");
    private static final By LOGOUT_BUTTON = By.xpath("//button[@role='menuitem']");

    public LogoutPage(WebDriver driver) {
        super(driver);
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    /**
     * Perform logout: open user menu → click logout → wait for page load.
     */
    public void logout() throws InterruptedException {
        log.info("Performing logout...");
        Thread.sleep(2000);

        openUserMenu();
        Thread.sleep(1000);
        clickLogoutButton();

        waitForPageLoad();
        Thread.sleep(3000);
        log.info("✅ User logged out successfully");
    }

    /**
     * Open the user profile/avatar menu.
     */
    private void openUserMenu() {
        try {
            WebElement menu = longWait.until(ExpectedConditions.elementToBeClickable(USER_MENU_BUTTON));
            scrollToElement(menu);
            Thread.sleep(500);
            jsClick(menu);
            log.info("Opened user menu");
        } catch (Exception e) {
            log.warn("First attempt to open menu failed, retrying...");
            WebElement menu = longWait.until(ExpectedConditions.elementToBeClickable(USER_MENU_BUTTON));
            jsClick(menu);
            log.info("Opened user menu (retry)");
        }
    }

    /**
     * Click the logout menu item.
     */
    private void clickLogoutButton() {
        try {
            WebElement logoutBtn = longWait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON));
            jsClick(logoutBtn);
            log.info("Clicked logout");
        } catch (Exception e) {
            log.warn("First attempt to click logout failed, retrying...");
            WebElement logoutBtn = longWait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON));
            jsClick(logoutBtn);
            log.info("Clicked logout (retry)");
        }
    }
}
