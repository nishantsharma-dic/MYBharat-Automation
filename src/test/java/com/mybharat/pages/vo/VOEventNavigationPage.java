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
 * VOEventNavigationPage - Navigates to the Add Event page.
 *
 * After template is created, navigates to Events section and clicks "Add Event".
 * The user should already be on the org/VFB section from the previous test.
 */
public class VOEventNavigationPage extends BasePage {

    private static final Logger log = LogManager.getLogger(VOEventNavigationPage.class);
    private final ConfigReader config = new ConfigReader();

    public VOEventNavigationPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Navigate to Add Event page.
     * Assumes user is already logged in and on the org page from previous tests.
     */
    public void navigateToAddEvent() throws InterruptedException {
        dismissOverlay();
        clickEventsTab();
        dismissOverlay();
        clickAddEventButton();
    }

    /**
     * Click "Events" tab/link on the organisation page.
     */
    public void clickEventsTab() throws InterruptedException {
        log.info("Clicking Events tab...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        dismissOverlay();

        WebElement eventsLink = null;
        String[] locators = {
                "//span[normalize-space()='Events']",
                "//a[normalize-space()='Events']",
                "//a[contains(@href,'event_list')]",
                "//li//a[contains(text(),'Event')]",
                "//span[contains(text(),'Events')]"
        };

        for (String xpath : locators) {
            try {
                eventsLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
                if (eventsLink != null) break;
            } catch (Exception e) {
                // try next
            }
        }

        if (eventsLink != null) {
            scrollToElement(eventsLink);
            Thread.sleep(300);
            try {
                eventsLink.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", eventsLink);
            }
            Thread.sleep(2000);
            waitForPageLoad();
            dismissOverlay();
            log.info("✅ Clicked Events tab");
        } else {
            log.warn("Events tab not found — trying direct URL");
            driver.get(config.getUrl() + "/orgeventmanagement/event_list");
            waitForPageLoad();
            Thread.sleep(2000);
            dismissOverlay();
        }
    }

    /**
     * Click "Add Event" button on the events list page.
     */
    public void clickAddEventButton() throws InterruptedException {
        log.info("Clicking Add Event button...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        dismissOverlay();

        WebElement addBtn = null;
        String[] locators = {
                "//a[contains(text(),'Add Event')]",
                "//button[contains(text(),'Add Event')]",
                "//a[contains(@href,'add_event')]",
                "//a[contains(text(),'Create Event')]",
                "//button[contains(text(),'Create Event')]"
        };

        for (String xpath : locators) {
            try {
                addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
                if (addBtn != null) break;
            } catch (Exception e) {
                // try next
            }
        }

        if (addBtn != null) {
            scrollToElement(addBtn);
            Thread.sleep(300);
            safeClick(addBtn);
            Thread.sleep(2000);
            waitForPageLoad();
            dismissOverlay();
            log.info("✅ Clicked Add Event. URL: {}", driver.getCurrentUrl());
        } else {
            log.warn("Add Event button not found");
        }
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
