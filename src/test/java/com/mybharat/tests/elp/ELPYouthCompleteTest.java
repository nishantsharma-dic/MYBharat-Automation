package com.mybharat.tests.elp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.elp.ELPYouthCompletePage;

/**
 * ELPYouthCompleteTest - Youth re-logs in after admin accepts and completes the ELP.
 * 
 * Runs AFTER ELPAdminAcceptTest in the ELP suite.
 * 
 * Flow:
 *   1. Logout from admin/partner account
 *   2. Re-login as youth
 *   3. Handle popup if available
 *   4. Click Experiential Learning
 *   5. Click "MY ELP" tab
 *   6. Click "Approved" tab
 *   7. Select joining date (today)
 *   8. Click Submit
 *   9. Select last day (today)
 *  10. Enter completed hours (72)
 *  11. Upload work done file (sample.pdf)
 *  12. Click Submit
 *  13. Rate 5 stars
 *  14. Enter feedback
 *  15. Click Submit in popup
 */
@Listeners(TestListeners.class)
public class ELPYouthCompleteTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(ELPYouthCompleteTest.class);
    private ELPYouthCompletePage completePage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        completePage = new ELPYouthCompletePage(driver);
    }

    @Test(priority = 1, groups = {"regression", "elp"},
          description = "Logout admin, re-login as youth, navigate to MY ELP, and complete the ELP")
    public void youthCompletesELP() throws Exception {
        log.info("=== Starting: Youth Completes ELP ===");

        // Step 1: Logout from admin account
        completePage.logoutAdmin();
        log.info("Step 1: Admin logged out");

        // Step 2: Re-login as youth
        completePage.loginAsYouth();
        log.info("Step 2: Youth re-logged in");

        // Step 3: Handle popup if available
        completePage.handlePopupIfPresent();
        log.info("Step 3: Popup handled");

        // Step 4: Click Experiential Learning
        completePage.clickExperientialLearning();
        log.info("Step 4: On Experiential Learning page");

        // Step 5: Click MY ELP tab
        completePage.clickMyELPTab();
        log.info("Step 5: MY ELP tab selected");

        // Step 6: Click Approved tab
        completePage.clickApprovedTab();
        log.info("Step 6: Approved tab selected");

        // Step 7: Select joining date (today)
        completePage.selectJoiningDate();
        log.info("Step 7: Joining date selected");

        // Step 8: Click Submit
        completePage.clickSubmit();
        log.info("Step 8: Joining date submitted");

        // Step 9: Select last day (today)
        completePage.selectLastDay();
        log.info("Step 9: Last day selected");

        // Step 10: Enter completed hours
        completePage.enterCompletedHours("72");
        log.info("Step 10: Completed hours entered (72)");

        // Step 11: Upload work done file
        completePage.uploadWorkDoneFile();
        log.info("Step 11: Work done file uploaded");

        // Step 12: Click Submit
        completePage.clickCompletionSubmit();
        log.info("Step 12: Completion submitted");

        // Step 13: Rate 5 stars
        completePage.rateFiveStars();
        log.info("Step 13: Rated 5 stars");

        // Step 14: Enter feedback
        completePage.enterFeedback("Happy to complete this ELP and will be helpful in career.");
        log.info("Step 14: Feedback entered");

        // Step 15: Click Submit in popup
        completePage.clickFeedbackSubmit();
        log.info("✅ ELP completed successfully with 5-star rating and feedback");
    }
}
