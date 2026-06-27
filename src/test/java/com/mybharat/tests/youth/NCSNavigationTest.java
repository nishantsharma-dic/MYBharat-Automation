package com.mybharat.tests.youth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.Retry;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.youth.LoginPage;
import com.mybharat.pages.youth.NCSPage;

/**
 * NCSNavigationTest - E2E test for NCS (National Career Service) navigation from Youth Profile.
 *
 * Purpose: Verifies that a logged-in user can navigate to the NCS page by clicking
 *          "National Career Service" link in the profile sidebar under "Links for Me".
 *
 * Flow:
 *   Step 1: Login as a valid user (OTP-based login via Yopmail)
 *   Step 2: Navigate to Youth Profile page
 *   Step 3: Click "National Career Service" link in sidebar
 *   Step 4: Verify NCS page opens at https://mybharat.gov.in/ncs/
 *
 * Prerequisites:
 *   - Youth_prod.xlsx or Youth_beta.xlsx must contain valid registered user email
 *   - User must have profile access with "Links for Me" sidebar section visible
 *
 * Run:
 *   mvn test -Denv=prod -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-ncs.xml
 *   mvn test -Denv=beta -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-ncs.xml
 *
 * Dependencies: BaseTest, LoginPage, NCSPage, TestListeners
 * Developer: Manoj Kumar (QA Team)
 *
 * @see NCSPage
 * @see LoginPage
 */
@Listeners(TestListeners.class)
public class NCSNavigationTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(NCSNavigationTest.class);

    private LoginPage loginPage;
    private NCSPage ncsPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        loginPage = new LoginPage(driver);
        ncsPage = new NCSPage(driver);
    }

    @Test(priority = 1, groups = {"smoke", "ncs"},
          description = "Login with valid user credentials via email and password")
    public void loginWithValidUser() throws Exception {
        log.info("=== Step 1: Login with valid user (ki_nodal_p1@sharklasers.com) ===");

        // Perform login with email + password (not OTP)
        loginPage.performLoginWithPassword("ki_nodal_p1@sharklasers.com");

        // Verify login was successful
        boolean isLoggedIn = loginPage.isLoginSuccessful();
        Assert.assertTrue(isLoggedIn, "Login should be successful before navigating to NCS");
        log.info("✅ Step 1 PASSED — User logged in successfully with ki_nodal_p1@sharklasers.com");
    }

    @Test(priority = 2, groups = {"smoke", "ncs"}, dependsOnMethods = "loginWithValidUser",
          retryAnalyzer = Retry.class,
          description = "Navigate to profile page and click National Career Service link in sidebar")
    public void navigateToNCS() {
        log.info("=== Step 2: Navigate to Youth Profile ===");

        // Navigate to the youth profile page where sidebar is visible
        ncsPage.navigateToProfilePage();
        log.info("✅ Step 2 PASSED — On Youth Profile page");

        log.info("=== Step 3: Click 'National Career Service' link ===");

        // Click the NCS link in the sidebar
        ncsPage.clickNCSLink();
        log.info("✅ Step 3 PASSED — Clicked NCS link");
    }

    @Test(priority = 3, groups = {"smoke", "ncs"}, dependsOnMethods = "navigateToNCS",
          retryAnalyzer = Retry.class,
          description = "Verify NCS page opened at /ncs/ URL with correct content")
    public void verifyNCSPageOpened() {
        log.info("=== Step 4: Verify NCS page opened ===");

        // Verify the NCS page is loaded
        boolean ncsOpened = ncsPage.verifyNCSPageOpened();
        Assert.assertTrue(ncsOpened, "NCS page should open after clicking 'National Career Service' link");

        // Additional URL assertion
        String currentUrl = ncsPage.getCurrentPageUrl();
        Assert.assertTrue(currentUrl.contains("/ncs"),
                "URL should contain '/ncs'. Actual URL: " + currentUrl);

        log.info("✅ Step 4 PASSED — NCS page verified at: {}", currentUrl);
        log.info("=== ✅ NCS Navigation Test PASSED ===");
    }
}
