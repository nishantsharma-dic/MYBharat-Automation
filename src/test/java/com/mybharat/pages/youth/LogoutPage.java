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
