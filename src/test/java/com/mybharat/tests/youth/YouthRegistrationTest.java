package com.mybharat.tests.youth;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.Retry;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.LandingPage;
import com.mybharat.pages.youth.PlayQuizPage;
import com.mybharat.pages.youth.PublicProfilePage;
import com.mybharat.pages.youth.RegistrationPage;
import com.mybharat.utils.RedashClient;

/**
 * YouthRegistrationTest - End-to-end test for Youth module.
 * 
 * Execution order (sequential):
 *   1. Register Indian user
 *   2. Verify user in DB via Redash
 *   3. Fill public profile & download certificate
 *   4. Play quiz
 * 
 * Run:
 *   mvn test -Denv=beta -Dbrowser=chrome -DsuiteXmlFile=testSuites/testng-youth.xml
 */
@Listeners(TestListeners.class)
public class YouthRegistrationTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(YouthRegistrationTest.class);

    private LandingPage landingPage;
    private RegistrationPage registrationPage;
    private String registeredEmail;

    @BeforeClass(alwaysRun = true)
    @Override
    public void setUp() {
        super.setUp();
        landingPage = new LandingPage(driver);
        registrationPage = new RegistrationPage(driver);
    }

    // -------------------------------------------------------------------------
    // Test 1: Register Indian Youth
    // -------------------------------------------------------------------------

    @Test(priority = 1, groups = {"smoke", "registration"}, retryAnalyzer = Retry.class)
    public void registerIndianYouth() throws Exception {
        log.info("Starting: Register Indian Youth");

        openApp();
        landingPage.closePopupIfPresent();
        landingPage.clickRegisterForIndian();

        registrationPage.enterEmailAndRequestOTP();
        registrationPage.fetchAndVerifyOTP();
        registrationPage.fillRegistrationForm();
        registrationPage.submitForm();

        // Save email for DB verification
        registeredEmail = registrationPage.getEmail();
        log.info("Registration completed for: {}", registeredEmail);
    }

    // -------------------------------------------------------------------------
    // Test 2: Verify user exists in DB via Redash
    // -------------------------------------------------------------------------

    @Test(priority = 2, groups = {"smoke", "registration"},
          dependsOnMethods = "registerIndianYouth")
    public void verifyUserInDatabase() throws Exception {
        log.info("Verifying user in DB: {}", registeredEmail);

        String baseUrl = System.getProperty("redashBaseUrl");
        String queryId = System.getProperty("redashQueryId");
        String apiKey = System.getProperty("redashApiKey");

        if (baseUrl == null || queryId == null || apiKey == null) {
            log.warn("Redash credentials not provided. Skipping DB verification.");
            log.warn("To enable: -DredashBaseUrl=... -DredashQueryId=... -DredashApiKey=...");
            return;
        }

        List<Map<String, String>> results = RedashClient.getQueryResult(baseUrl, queryId, apiKey);
        boolean found = RedashClient.isUserInDatabase(registeredEmail, results, "email");

        Assert.assertTrue(found, "User " + registeredEmail + " should exist in database after registration");
        log.info("✅ User verified in database: {}", registeredEmail);
    }

    // -------------------------------------------------------------------------
    // Test 3: Fill Public Profile & Download Certificate
    // -------------------------------------------------------------------------

    @Test(priority = 3, groups = {"regression"},
          dependsOnMethods = "registerIndianYouth", enabled = false)
    public void fillPublicProfileAndDownloadCertificate() throws Exception {
        log.info("Starting: Fill Public Profile");

        PublicProfilePage profilePage = new PublicProfilePage(driver);
        boolean downloaded = profilePage.downloadCertificate();

        Assert.assertTrue(downloaded, "Certificate should be downloaded successfully");
        log.info("✅ Certificate downloaded");
    }

    // -------------------------------------------------------------------------
    // Test 4: Play Quiz
    // -------------------------------------------------------------------------

    @Test(priority = 4, groups = {"regression"},
          dependsOnMethods = "fillPublicProfileAndDownloadCertificate", enabled = false)
    public void playQuiz() throws Exception {
        log.info("Starting: Play Quiz");

        PlayQuizPage quizPage = new PlayQuizPage(driver, "English");
        quizPage.startQuiz();
        quizPage.attemptAllQuestionsAndSubmit();

        log.info("✅ Quiz completed");
    }
}
