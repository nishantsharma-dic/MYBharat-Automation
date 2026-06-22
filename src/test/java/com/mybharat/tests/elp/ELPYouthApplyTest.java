package com.mybharat.tests.elp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.elp.ELPYouthApplyPage;

/**
 * ELPYouthApplyTest - Youth navigates to Experiential Learning after login.
 * 
 * Runs AFTER LogoutTest + LoginTest in the ELP suite.
 * The youth user is already logged in at this point.
 * 
 * Flow:
 *   1. Handle post-login popup (submit/close)
 *   2. Click "Experiential Learning" from side menu
 *   3. (TODO) Search and apply to ELP
 */
@Listeners(TestListeners.class)
public class ELPYouthApplyTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(ELPYouthApplyTest.class);
    private ELPYouthApplyPage youthApplyPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        youthApplyPage = new ELPYouthApplyPage(driver);
    }

    @Test(priority = 1, groups = {"regression", "elp"},
          description = "Handle post-login popup, fill Basic Info, and navigate to Experiential Learning")
    public void navigateToExperientialLearning() throws Exception {
        log.info("Starting: Navigate to Experiential Learning");

        // Step 1: Handle popup after login
        youthApplyPage.handlePostLoginPopup();
        log.info("Step 1: Popup handled");

        // Step 2: Click Basic Info tab and fill details
        youthApplyPage.clickBasicInfoTab();
        youthApplyPage.fillBasicInfoAndSave("Nishant", "Sharma", "Male", "25-03-1990");
        log.info("Step 2: Basic Info filled and saved");

        // Step 3: Click Experiential Learning from side menu
        youthApplyPage.clickExperientialLearning();
        log.info("✅ Navigated to Experiential Learning page");
    }

    @Test(priority = 2, groups = {"regression", "elp"}, dependsOnMethods = "navigateToExperientialLearning",
          description = "Click 'Opportunity near me' and apply location filter to find nearby ELPs")
    public void applyOpportunityNearMe() throws Exception {
        log.info("Starting: Apply Opportunity Near Me");

        // Step 3: Click "Opportunity near me"
        youthApplyPage.clickOpportunityNearMe();
        log.info("Step 3: Location modal opened");

        // Step 4: Click Apply in the location modal
        youthApplyPage.clickApplyInLocationModal();
        log.info("✅ Location filter applied — nearby ELPs displayed");
    }
}
