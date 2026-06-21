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
 * BasicInfoTest - Test for the Basic Info tab in the youth profile.
 *
 * Purpose: Navigates to the "Basic Info" tab on the React profile page and extracts
 *          the registered email from the form field for verification purposes.
 *
 * Prerequisites: User is already logged in (runs after certificate download in the
 *                same browser session within the testng-youth.xml suite).
 *
 * Flow:
 *   1. Navigate to Basic Info tab (closes any open modals first)
 *   2. Extract email from the user_email input field
 *   3. Log the extracted email for verification
 *
 * Key Methods:
 *   - clickBasicInfoAndExtractEmail() — navigates to tab and reads email field
 *
 * Dependencies: BaseTest, YouthProfilePage, TestListeners
 * Developer: Nishant Sharma (QA Team)
 *
 * @see YouthProfilePage
 * @see YouthProfileTest
 */
@Listeners(TestListeners.class)
public class BasicInfoTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(BasicInfoTest.class);

    private YouthProfilePage profilePage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        profilePage = new YouthProfilePage(driver);
    }

    @Test(priority = 1, groups = {"regression", "profile"}, retryAnalyzer = Retry.class,
          description = "Navigate to Basic Info tab, update name/DOB/gender, save, and extract email")
    public void clickBasicInfoAndExtractEmail() throws Exception {
        log.info("Starting: Basic Info — Update Details + Extract Email");

        profilePage.navigateToBasicInfo();
        profilePage.fillBasicInfoAndSave();
        profilePage.extractEmailFromProfile();

        log.info("✅ Basic Info updated and email extracted");
    }
}
