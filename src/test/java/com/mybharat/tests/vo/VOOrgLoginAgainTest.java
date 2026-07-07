package com.mybharat.tests.vo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.vo.VOLoginPage;

/**
 * VOOrgLoginAgainTest - Logs in as Org user again (after youth logout).
 * Used for the "Edit Event → Save as Draft" step.
 */
@Listeners(TestListeners.class)
public class VOOrgLoginAgainTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(VOOrgLoginAgainTest.class);

    private VOLoginPage voLoginPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        // Reset prefix to Partner (org user)
        System.setProperty("voExcelPrefix", "Partner");

        // Clear cookies to ensure clean state
        try {
            driver.manage().deleteAllCookies();
            org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
            js.executeScript("try{window.localStorage.clear();window.sessionStorage.clear();}catch(e){}");
            safeSleep(500);
            driver.navigate().refresh();
            safeSleep(2000);
        } catch (Exception e) {
            // ignore
        }

        voLoginPage = new VOLoginPage(driver);
    }

    @Test(priority = 1, groups = {"vo", "org-login-again"})
    public void loginAsOrgUserAgain() throws Exception {
        log.info("=== Starting: Org User Login Again ===");

        voLoginPage.loginWithRandomVOUser();

        // Navigate to profile page after login (popup already dismissed by login)
        String baseUrl = new com.mybharat.utils.ConfigReader().getUrl();
        driver.get(baseUrl + "/youth-profile");
        safeSleep(1500);

        String currentUrl = driver.getCurrentUrl();
        Assert.assertFalse(currentUrl.contains("login"),
                "Should not be on login page after successful OTP verification");

        log.info("=== ✅ Org Login Again PASSED — logged in as: {} ===", voLoginPage.getLoginEmail());
    }
    private void safeSleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

}
