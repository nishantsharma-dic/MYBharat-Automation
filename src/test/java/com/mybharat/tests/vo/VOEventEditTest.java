package com.mybharat.tests.vo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.vo.VOEventEditPage;

/**
 * VOEventEditTest - Org logs back in after youth applies,
 * navigates to Events, clicks Edit on latest event, clicks "Save as draft", logs out.
 */
@Listeners(TestListeners.class)
public class VOEventEditTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(VOEventEditTest.class);

    private VOEventEditPage editPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        editPage = new VOEventEditPage(driver);
    }

    @Test(priority = 1, groups = {"vo", "edit"})
    public void editEventAndSaveAsDraft() throws Exception {
        log.info("=== Starting: Edit Event and Save as Draft ===");

        // Scroll up to top of page (after image approval we're scrolled down)
        editPage.scrollToTop();

        // Click Edit button on the current event page
        editPage.clickEditButton();

        // Scroll down and click Save as Draft
        editPage.clickSaveAsDraft();

        // Logout
        editPage.clickLogoAndLogout();

        log.info("=== ✅ Edit Event → Save as Draft → Logout PASSED ===");
    }
}
