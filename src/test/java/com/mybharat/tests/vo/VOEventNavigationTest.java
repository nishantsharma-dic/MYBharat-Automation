package com.mybharat.tests.vo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;

import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.vo.VOEventNavigationPage;

/**
 * VOEventNavigationTest - Navigates to the Add Event page.
 *
 * Flow: Org Dashboard → Events → Add Event
 *
 * Run:
 *   mvn test -Denv=beta -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-vo.xml
 */
@Listeners(TestListeners.class)
public class VOEventNavigationTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(VOEventNavigationTest.class);

    private VOEventNavigationPage eventNavPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        eventNavPage = new VOEventNavigationPage(driver);
    }

    @Test(priority = 1, groups = {"vo", "navigation"})
    public void navigateToAddEvent() throws Exception {
        log.info("=== Starting: Navigate to Add Event ===");

        eventNavPage.navigateToAddEvent();

        String currentUrl = driver.getCurrentUrl();
        log.info("Current URL after navigation: {}", currentUrl);

        log.info("=== ✅ Navigation to Add Event PASSED ===");
    }
}
