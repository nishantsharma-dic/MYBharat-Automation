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
 * YouthBasicInfoTest - Clicks Basic Info tab and extracts email.
 * 
 * Runs after certificate download on the SAME browser session.
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
          description = "Navigate to Basic Info tab and extract the registered email from the profile form for verification")
    public void clickBasicInfoAndExtractEmail() throws Exception {
        log.info("Starting: Basic Info + Extract Email");

        profilePage.navigateToBasicInfo();
        profilePage.extractEmailFromProfile();

        log.info("✅ Basic Info completed, email extracted");
    }
}
