package com.mybharat.tests.elp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.Retry;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.elp.ELPNavigationPage;

/**
 * ELPNavigationTest - After login, navigates to profile and clicks View More.
 * Runs after ELPLoginTest on the same browser session.
 */
@Listeners(TestListeners.class)
public class ELPNavigationTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(ELPNavigationTest.class);

    private ELPNavigationPage navPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        navPage = new ELPNavigationPage(driver);
    }

    @Test(priority = 1, groups = {"elp", "navigation"}, retryAnalyzer = Retry.class)
    public void navigateToViewMore() throws Exception {
        log.info("Starting: Navigate to profile and click View More");

        navPage.navigateToProfileAndClickViewMore();

        log.info("✅ View More navigation completed");
    }
}
