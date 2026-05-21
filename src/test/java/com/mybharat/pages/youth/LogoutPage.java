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
 * Two logout contexts:
 *  1. mybharat.gov.in homepage — "Welcome [name]" circle in top-right nav
 *     → click circle → dropdown → click "Log Out"
 *  2. Org portal — rounded-full button → menuitem button
 */
public class LogoutPage extends BasePage {

    private static final Logger log = LogManager.getLogger(LogoutPage.class);
    private WebDriverWait longWait;

    // ── Homepage: "Welcome [name]" circle ────────────────────────────────────
    // From screenshot: the circle is a clickable element containing "Welcome" text
    private static final By WELCOME_CIRCLE = By.xpath(
        "//*[contains(text(),'Welcome')]/ancestor::*[self::a or self::button or self::div][@role or contains(@class,'cursor') or contains(@class,'rounded')]" +
        " | //*[contains(text(),'Welcome')]/parent::*"
    );

    // ── Homepage dropdown: "Log Out" item ────────────────────────────────────
    // From screenshot: dropdown has "MY Bharat Profile" and "Log Out"
    private static final By LOG_OUT_ITEM = By.xpath(
        "//*[normalize-space(text())='Log Out'] | " +
        "//*[normalize-space(text())='Logout'] | " +
        "//*[contains(text(),'Log Out')]/parent::a | " +
        "//*[contains(text(),'Log Out')]/parent::button | " +
        "//a[contains(.,'Log Out')] | " +
        "//button[contains(.,'Log Out')]"
    );

    // ── Org portal fallback ───────────────────────────────────────────────────
    private static final By ORG_MENU_BUTTON = By.xpath(
        "//button[@class='flex items-center rounded-full cursor-pointer']"
    );
    private static final By ORG_LOGOUT_BUTTON = By.xpath(
        "//button[@role='menuitem']"
    );

    public LogoutPage(WebDriver driver) {
        super(driver);
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    /**
     * Perform logout: click Welcome circle → click Log Out from dropdown.
     */
    public void logout() throws InterruptedException {
        log.info("Performing logout...");
        Thread.sleep(2000);

        openUserMenu();
        Thread.sleep(1000);
        clickLogOut();

        waitForPageLoad();
        Thread.sleep(3000);
        log.info("✅ User logged out successfully");
    }

    private void openUserMenu() throws InterruptedException {
        // Strategy 1: Homepage "Welcome [name]" circle
        try {
            WebElement circle = longWait.until(ExpectedConditions.elementToBeClickable(WELCOME_CIRCLE));
            scrollToElement(circle);
            Thread.sleep(500);
            jsClick(circle);
            log.info("✅ Clicked Welcome circle");
            return;
        } catch (Exception e) {
            log.warn("Welcome circle not found — trying org portal button...");
        }

        // Strategy 2: Org portal rounded button
        try {
            WebElement menu = longWait.until(ExpectedConditions.elementToBeClickable(ORG_MENU_BUTTON));
            scrollToElement(menu);
            Thread.sleep(500);
            jsClick(menu);
            log.info("✅ Clicked org portal menu button");
        } catch (Exception e) {
            log.warn("Org portal button also failed, retrying...");
            WebElement menu = longWait.until(ExpectedConditions.elementToBeClickable(ORG_MENU_BUTTON));
            jsClick(menu);
        }
    }

    private void clickLogOut() throws InterruptedException {
        Thread.sleep(500);

        // Strategy 1: "Log Out" text item from homepage dropdown
        try {
            WebElement logOut = longWait.until(ExpectedConditions.elementToBeClickable(LOG_OUT_ITEM));
            jsClick(logOut);
            log.info("✅ Clicked Log Out");
            return;
        } catch (Exception e) {
            log.warn("'Log Out' item not found — trying org portal menuitem...");
        }

        // Strategy 2: Org portal menuitem button
        try {
            WebElement logOut = longWait.until(ExpectedConditions.elementToBeClickable(ORG_LOGOUT_BUTTON));
            jsClick(logOut);
            log.info("✅ Clicked org portal logout button");
        } catch (Exception e) {
            log.warn("Org portal logout also failed, retrying...");
            WebElement logOut = longWait.until(ExpectedConditions.elementToBeClickable(ORG_LOGOUT_BUTTON));
            jsClick(logOut);
        }
    }
}
