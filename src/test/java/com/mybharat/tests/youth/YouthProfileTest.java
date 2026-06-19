package com.mybharat.tests.youth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.Retry;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.youth.YouthProfilePage;

/**
 * YouthProfileTest - End-to-end test for completing the youth profile on MYBharat.
 *
 * Purpose: After login, navigates to the React profile page and fills all profile
 *          sections (photo, about, interests, education, languages, professional
 *          summary, work experience, tools).
 *
 * Prerequisites: User is already logged in (runs AFTER LoginTest in the same browser
 *                session within the testng-youth.xml suite). User is on dashboard/home.
 *
 * Flow:
 *   1. Navigate to /youth-profile
 *   2. Upload profile photo
 *   3. Fill About section
 *   4. Add Area of Interest (react-select)
 *   5. Add Education Qualification (native dropdowns)
 *   6. Add Language (react-select)
 *   7. Fill Professional Summary + skills
 *   8. Add Work Experience
 *   9. Fill Tools section + social links
 *
 * Key Methods:
 *   - completeYouthProfile() — orchestrates all section fills via YouthProfilePage
 *
 * Dependencies: BaseTest, YouthProfilePage, TestListeners
 * Developer: Nishant Sharma (QA Team)
 *
 * @see YouthProfilePage
 * @see LoginTest
 */
@Listeners(TestListeners.class)
public class YouthProfileTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(YouthProfileTest.class);

    private YouthProfilePage profilePage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        profilePage = new YouthProfilePage(driver);
    }

    @Test(priority = 2, groups = {"regression", "profile"}, retryAnalyzer = Retry.class,
          description = "Complete youth profile: Navigate to profile → Verify public profile → Upload photo → About → Area of Interest → Education → Language → Professional Summary → Work Experience → Tools")
    public void completeYouthProfile() throws Exception {
        log.info("Starting: Youth Profile Completion");

        // User should be on dashboard after registration (already logged in)
        // Navigate to profile page
        profilePage.navigateToProfilePage();
        log.info("Navigated to profile page");

        // Now complete the profile
        profilePage.completeYouthProfile();

        log.info("✅ Youth profile completed successfully");
    }
}
