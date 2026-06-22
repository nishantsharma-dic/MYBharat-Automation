package com.mybharat.tests.elp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.Retry;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.elp.ELPCreatePage;

/**
 * ELPCreateTest - Creates a new ELP after navigating to the ELP section.
 * Runs after ELPNavigationTest (user is on the Experiential Learning page).
 */
@Listeners(TestListeners.class)
public class ELPCreateTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(ELPCreateTest.class);

    private ELPCreatePage createPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        createPage = new ELPCreatePage(driver);
    }

    @Test(priority = 1, groups = {"elp", "create"}, retryAnalyzer = Retry.class)
    public void createELP() throws Exception {
        log.info("=== Starting: Create ELP ===");

        // Click Create ELP button
        createPage.clickCreateELP();

        // Fill the ELP form
        createPage.fillELPForm();

        // Click Preview
        createPage.clickPreview();

        // Click Publish on preview page
        createPage.clickPublish();

        log.info("=== ✅ ELP Created and Published ===");
    }
}
