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
 * VOLoginTest - Logs in as a VO Partner/Organization user.
 *
 * Picks a random email from VO_<env>.xlsx and logs in via OTP.
 * After this test, the user is logged in and ready for template/event creation.
 *
 * Run:
 *   mvn test -Denv=beta -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-vo.xml
 */
@Listeners(TestListeners.class)
public class VOLoginTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(VOLoginTest.class);

    private VOLoginPage voLoginPage;

    /** Shared across tests — accessible by subsequent test classes */
    public static String voLoginEmail;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        voLoginPage = new VOLoginPage(driver);
    }

    @Test(priority = 1, groups = {"vo", "login"})
    public void loginAsVOUser() throws Exception {
        log.info("=== Starting: VO User Login ===");

        voLoginPage.loginWithRandomVOUser();
        voLoginEmail = voLoginPage.getLoginEmail();

        // Verify login by checking URL or page state
        String currentUrl = driver.getCurrentUrl();
        Assert.assertFalse(currentUrl.contains("login"),
                "Should not be on login page after successful OTP verification");

        log.info("=== ✅ VO Login PASSED — logged in as: {} ===", voLoginEmail);
    }
}
