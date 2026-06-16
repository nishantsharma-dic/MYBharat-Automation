package com.mybharat.tests.youth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.Retry;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.youth.LogoutPage;

/**
 * LogoutTest - End-to-end test for user logout functionality.
 *
 * Purpose: Verifies that the currently logged-in user can be successfully logged out
 *          by clicking the user menu and selecting logout.
 *
 * Prerequisites: User must be logged in (runs after registration in testng-youth.xml suite,
 *                or after any flow that leaves the user logged in).
 *
 * Flow:
 *   1. Open the user avatar/profile menu
 *   2. Click the logout menu item
 *   3. Wait for redirect to home page
 *
 * Key Methods:
 *   - logoutUser() — performs the complete logout via LogoutPage
 *
 * Run:
 *   mvn test -Denv=prod -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-youth.xml
 *
 * Dependencies: BaseTest, LogoutPage, TestListeners
 * Developer: Nishant Sharma (QA Team)
 *
 * @see LogoutPage
 * @see LoginTest
 */
@Listeners(TestListeners.class)
public class LogoutTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(LogoutTest.class);

    private LogoutPage logoutPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        logoutPage = new LogoutPage(driver);
    }

    @Test(priority = 1, groups = {"smoke", "logout"}, retryAnalyzer = Retry.class,
          description = "Logout the currently signed-in user: Open user menu → Click logout → Verify redirect to home page")
    public void logoutUser() throws Exception {
        log.info("Starting: Logout current user");
        logoutPage.logout();
        log.info("✅ Logout completed successfully");
    }
}
