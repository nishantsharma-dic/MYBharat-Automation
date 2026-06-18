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

/**
 * VOEventNavigationPage - After template creation, clicks "Events" in sidebar
 * then clicks "Add Event" button.
 *
 * Flow (from screenshot):
 *   After template is created → back on /orgeventmanagement/event_template
 *   1. Click "Events" in left sidebar
 *   2. Click "Add Event" button
 */
public class VOEventNavigationPage extends BasePage {

    private static final Logger log = LogManager.getLogger(VOEventNavigationPage.class);
    private final ConfigReader config = new ConfigReader();

    public VOEventNavigationPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Full navigation: Click Events in sidebar → Click Add Event
     */
    public void navigateToAddEvent() throws InterruptedException {
        dismissOverlay();
        clickEventsTab();
        clickAddEventButton();
    }

    /**
     * Click "Events" link in the left sidebar.
     * We're already on the org dashboard after template creation.
     */
    public void clickEventsTab() throws InterruptedException {
        log.info("Clicking Events tab in sidebar...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        dismissOverlay();

        try {
            WebElement eventsLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[normalize-space()='Events']"
                            + " | //span[normalize-space()='Events']/ancestor::a")));
            scrollToElement(eventsLink);
            Thread.sleep(300);
            try {
                eventsLink.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", eventsLink);
            }
            log.info("✅ Clicked Events tab");
        } catch (Exception e) {
            log.error("Events tab not found: {}", e.getMessage());
            throw new RuntimeException("Events tab not found in sidebar");
        }

        waitForPageLoad();
        Thread.sleep(2000);
        dismissOverlay();
        log.info("✅ On Events page. URL: {}", driver.getCurrentUrl());
    }

    /**
     * Click "Add Event" button on the events list page.
     */
    public void clickAddEventButton() throws InterruptedException {
        log.info("Clicking Add Event button...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        dismissOverlay();

        try {
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(text(),'Add Event')]"
                            + " | //button[contains(text(),'Add Event')]"
                            + " | //a[contains(@class,'btn') and contains(text(),'Add Event')]"
                            + " | //*[contains(text(),'+ Add Event')]"
                            + " | //a[contains(@href,'add_event')]")));
            scrollToElement(addBtn);
            Thread.sleep(300);
            safeClick(addBtn);
            log.info("✅ Clicked Add Event");
        } catch (Exception e) {
            log.error("Add Event button not found: {}", e.getMessage());
            throw new RuntimeException("Add Event button not found");
        }

        waitForPageLoad();
        Thread.sleep(2000);
        dismissOverlay();
        log.info("✅ On Add Event page. URL: {}", driver.getCurrentUrl());
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
                    "var overlay = document.getElementById('overlay');" +
                    "if(overlay) overlay.style.display='none';" +
                    "var loader = document.getElementById('loader2');" +
                    "if(loader) loader.style.display='none';" +
                    "try { $('#overlay').hide(); $('#loader2').hide(); } catch(e) {}");
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
    }
}
