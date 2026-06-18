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
 * VOYouthLoginTest - Logs in as a Youth user (maynak@yopmail.com).
 *
 * Reads email from VO_Youth_<env>.xlsx.
 * Used after org-side event creation + logout, to continue with youth-side flow.
 */
@Listeners(TestListeners.class)
public class VOYouthLoginTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(VOYouthLoginTest.class);

    private VOLoginPage voLoginPage;

    public static String youthLoginEmail;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        // Set prefix to Youth so VOLoginPage reads from Youth_<env>.xlsx
        System.setProperty("voExcelPrefix", "Youth");

        // Clear cookies and refresh to ensure fully logged out state
        try {
            driver.manage().deleteAllCookies();
            // Also clear local/session storage
            org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
            js.executeScript("try{window.localStorage.clear();window.sessionStorage.clear();}catch(e){}");
            Thread.sleep(1000);
            // Navigate fresh to homepage
            driver.navigate().refresh();
            Thread.sleep(5000);
        } catch (Exception e) {
            // ignore
        }

        voLoginPage = new VOLoginPage(driver);
    }

    @Test(priority = 1, groups = {"vo", "youth-login"})
    public void loginAsYouthUser() throws Exception {
        log.info("=== Starting: Youth User Login ===");

        voLoginPage.loginWithRandomVOUser();
        youthLoginEmail = voLoginPage.getLoginEmail();

        String currentUrl = driver.getCurrentUrl();
        Assert.assertFalse(currentUrl.contains("login"),
                "Should not be on login page after successful OTP verification");

        log.info("=== ✅ Youth Login PASSED — logged in as: {} ===", youthLoginEmail);
    }
}
