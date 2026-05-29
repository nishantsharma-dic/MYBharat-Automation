package com.mybharat.tests.vo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.vo.VOImageApprovalPage;

/**
 * VOImageApprovalTest - Org side: Youth Photo Moderation + Edit Event + Save as Draft
 *
 * Flow:
 *   1. Navigate to Org Dashboard (View More → Click org)
 *   2. Click "Youth Photo Moderation" in sidebar
 *   3. Click "VO" tab
 *   4. Approve 1st image, Reject 2nd image
 *   5. Click "Events" in sidebar
 *   6. Click "Edit Event" on latest card
 *   7. Scroll down → Save as draft
 *   8. Logout
 */
@Listeners(TestListeners.class)
public class VOImageApprovalTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(VOImageApprovalTest.class);

    private VOImageApprovalPage approvalPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        approvalPage = new VOImageApprovalPage(driver);
    }

    @Test(priority = 1, groups = {"vo", "image-approval"})
    public void reviewAndEditEvent() throws Exception {
        log.info("=== Starting: VO Image Approval + Edit Event ===");

        // Step 1: Navigate to Org Dashboard
        approvalPage.navigateToOrgDashboard();

        // Step 2: Click Youth Photo Moderation
        approvalPage.clickYouthPhotoModeration();

        // Step 3: Click VO tab
        approvalPage.clickVOTab();

        // Step 4: Approve 1st, Reject 2nd
        approvalPage.approveFirstRejectSecond();

        // Step 5: Click Events in sidebar
        approvalPage.clickEventsInSidebar();

        // Step 6: Click Edit Event on latest card
        approvalPage.clickEditEventOnLatestCard();

        // Step 7: Save as draft
        approvalPage.clickSaveAsDraft();

        // Step 8: Logout
        approvalPage.logout();

        log.info("=== ✅ Image Approval + Edit Event + Logout PASSED ===");
    }
}
