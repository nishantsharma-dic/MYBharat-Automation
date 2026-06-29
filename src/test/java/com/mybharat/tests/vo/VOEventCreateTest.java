package com.mybharat.tests.vo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.vo.VOEventCreatePage;

/**
 * VOEventCreateTest - Fills the Add Event form, clicks Preview, then Publish.
 *
 * Prerequisite: Must be on the Add Event page (after VOEventNavigationTest).
 */
@Listeners(TestListeners.class)
public class VOEventCreateTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(VOEventCreateTest.class);

    private VOEventCreatePage eventCreatePage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        eventCreatePage = new VOEventCreatePage(driver);
    }

    @Test(priority = 1, groups = {"vo", "create"})
    public void createVOEvent() throws Exception {
        log.info("=== Starting: Create VO Event ===");

        // Fill form and click Preview
        eventCreatePage.fillEventFormAndPreview();

        // Click Publish on preview page
        eventCreatePage.clickPublish();

        // Save event name to Excel for youth-side tests
        eventCreatePage.saveEventNameToExcel();

        // Logout
        eventCreatePage.logout();

        log.info("=== ✅ VO Event Created and Logged Out: {} ===", eventCreatePage.getEventName());
    }
}
