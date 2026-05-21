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
 * LogoutTest - Logs out the currently signed-in user.
 * 
 * This test is designed to run after any flow that leaves the user logged in
 * (e.g., after registration, after profile completion).
 * 
 * Run:
 *   mvn test -Denv=prod -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-youth.xml
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
