package com.mybharat.tests.elp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.Retry;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.elp.ELPLoginPage;

/**
 * ELPLoginTest - Logs in with a random ELP admin user from Partner_beta.xlsx or Partner_prod.xlsx.
 * This is the first step in the ELP test cycle.
 */
@Listeners(TestListeners.class)
public class ELPLoginTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(ELPLoginTest.class);

    private ELPLoginPage elpLoginPage;

    /** Shared email for subsequent ELP tests */
    public static String loggedInEmail;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        elpLoginPage = new ELPLoginPage(driver);
    }

    @Test(priority = 1, groups = {"elp", "login"}, retryAnalyzer = Retry.class)
    public void loginAsELPAdmin() throws Exception {
        log.info("=== Starting: ELP Admin Login ===");

        elpLoginPage.loginWithRandomELPUser();

        loggedInEmail = elpLoginPage.getLoginEmail();
        Assert.assertNotNull(loggedInEmail, "Login email should not be null");

        log.info("=== ✅ ELP Login PASSED — logged in as: {} ===", loggedInEmail);
    }
}
