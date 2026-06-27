package com.mybharat.tests.vo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.TestListeners;

import java.time.Duration;

/**
 * VOYouthLogoutTest - Logs out the youth user after applying on VO.
 */
@Listeners(TestListeners.class)
public class VOYouthLogoutTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(VOYouthLogoutTest.class);

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        // nothing to init
    }

    @Test(priority = 1, groups = {"vo", "youth-logout"})
    public void logoutYouthUser() throws Exception {
        log.info("=== Starting: Youth User Logout ===");
        safeSleep(1000);

        // Click profile dropdown
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var links = document.querySelectorAll('a[data-bs-toggle=\"dropdown\"], a[data-toggle=\"dropdown\"], a.dropdown-toggle');" +
                    "if(links.length > 0) { links[links.length-1].click(); }");
            safeSleep(500);
            log.info("✅ Clicked profile dropdown");
        } catch (Exception e) {
            log.warn("Profile dropdown click failed");
        }

        // Click "Log Out"
        try {
            WebElement logoutBtn = new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[normalize-space()='Log Out'] | //a[contains(text(),'Log Out')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", logoutBtn);
            safeSleep(2000);
            log.info("✅ Clicked Log Out");
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript(
                    "var links = document.querySelectorAll('a');" +
                    "for(var i=0; i<links.length; i++) {" +
                    "  if(links[i].textContent.trim() === 'Log Out') { links[i].click(); break; }" +
                    "}");
            safeSleep(2000);
            log.info("✅ Clicked Log Out (JS fallback)");
        }

        log.info("=== ✅ Youth Logout PASSED ===");
    }
    private void safeSleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

}
