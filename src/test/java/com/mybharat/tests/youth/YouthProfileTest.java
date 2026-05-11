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
 * YouthProfileTest - Completes the youth profile after registration.
 * 
 * Runs on the SAME browser session after QuizAttemptTest.
 * The user is already logged in and on the profile page.
 */
@Listeners(TestListeners.class)
public class YouthProfileTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(YouthProfileTest.class);

    private YouthProfilePage profilePage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        profilePage = new YouthProfilePage(driver);
    }

    @Test(priority = 1, groups = {"regression", "profile"}, retryAnalyzer = Retry.class)
    public void completeYouthProfile() throws Exception {
        log.info("Starting: Youth Profile Completion");

        profilePage.completeYouthProfile();

        log.info("✅ Youth profile completed successfully");
    }
}
