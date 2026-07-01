package com.mybharat.tests.elp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.elp.ELPAdminAcceptPage;
import com.mybharat.utils.ELPTestContext;

/**
 * ELPAdminAcceptTest - Admin re-logs in and accepts the youth's ELP application.
 * 
 * Runs AFTER ELPYouthApplyTest in the ELP suite.
 * 
 * Flow:
 *   1. Logout youth user (from /elp/listing?tab=my-elp page)
 *   2. Re-login as the nodal admin
 *   3. Navigate to organisation → Experiential Learning
 *   4. Find and click the created ELP
 *   5. Click "Accept" on the youth's application
 */
@Listeners(TestListeners.class)
public class ELPAdminAcceptTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(ELPAdminAcceptTest.class);
    private ELPAdminAcceptPage adminAcceptPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        adminAcceptPage = new ELPAdminAcceptPage(driver);
    }

    @Test(priority = 1, groups = {"regression", "elp"},
          description = "Logout youth, re-login as admin, navigate to ELP, and accept the application")
    public void adminAcceptsApplication() throws Exception {
        log.info("=== Starting: Admin Accepts Youth Application ===");

        // Step 1: Logout youth user
        adminAcceptPage.logoutYouth();
        log.info("Step 1: Youth logged out");

        // Step 2: Re-login as the nodal admin
        String adminEmail = ELPTestContext.getAdminEmail();
        log.info("Re-logging in as admin: {}", adminEmail);
        adminAcceptPage.loginAsAdmin(adminEmail);
        log.info("Step 2: Admin re-logged in as: {}", adminEmail);

        // Step 3: Navigate to organisation → Experiential Learning
        adminAcceptPage.navigateToELPSection();
        log.info("Step 3: Navigated to ELP admin section");

        // Step 4: Find and click the created ELP
        String elpTitle = ELPTestContext.getCreatedELPTitle();
        log.info("Looking for ELP: {}", elpTitle);
        adminAcceptPage.clickCreatedELP(elpTitle);
        log.info("Step 4: Clicked ELP: {}", elpTitle);

        // Step 5: Click Accept
        adminAcceptPage.clickAccept();
        log.info("✅ Admin accepted youth's application for ELP: {}", elpTitle);
    }
}
