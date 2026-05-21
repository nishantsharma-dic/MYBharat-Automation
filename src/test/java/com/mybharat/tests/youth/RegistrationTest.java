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
import com.mybharat.pages.youth.RegistrationPage;
import com.mybharat.utils.RedashClient;

/**
 * YouthRegistrationTest - Registers a new Indian youth user.
 * 
 * Flow: Open app → Register → Verify OTP → Fill form → Submit → Verify in DB
 * 
 * Run:
 *   mvn test -Denv=prod -Dbrowser=firefox
 */
@Listeners(TestListeners.class)
public class RegistrationTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(RegistrationTest.class);

    private LandingPage landingPage;
    private RegistrationPage registrationPage;

    /** Shared across tests in this class AND accessible by next class via static */
    public static String registeredEmail;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        landingPage = new LandingPage(driver);
        registrationPage = new RegistrationPage(driver);
    }

    @Test(priority = 1, groups = {"smoke", "registration"}, retryAnalyzer = Retry.class,
          description = "Register a new Indian youth user: Open app → Enter email → Verify OTP → Fill form → Submit → Save email to Excel")
    public void registerIndianYouth() throws Exception {
        log.info("Starting: Register Indian Youth");

        // Step 1: Open app and navigate to registration
        openApp();
        landingPage.closePopupIfPresent();
        landingPage.clickRegisterForIndian();

        // Step 2: Enter email and verify OTP
        registrationPage.enterEmailAndRequestOTP();
        registrationPage.fetchAndVerifyOTP();

        // Step 3: Fill registration form and submit
        registrationPage.fillRegistrationForm();
        registrationPage.submitForm();

        // Step 4: Click the submit popup button
        registrationPage.clickSubmitPopup();

        // Step 5: Save the registration email to Excel
        registeredEmail = registrationPage.getEmail();
        registrationPage.saveEmailToExcel();
        log.info("✅ Registration completed. Email: {}", registeredEmail);
    }

    @Test(priority = 2, groups = {"smoke", "registration"},
          dependsOnMethods = "registerIndianYouth",
          description = "Verify the registered user exists in the database via Redash API query")
    public void verifyUserInDatabase() throws Exception {
        log.info("Verifying user in DB: {}", registeredEmail);

        String baseUrl = System.getProperty("redashBaseUrl");
        String queryId = System.getProperty("redashQueryId");
        String apiKey = System.getProperty("redashApiKey");

        if (baseUrl == null || queryId == null || apiKey == null) {
            log.warn("Redash credentials not provided. Skipping DB verification.");
            return;
        }

        List<Map<String, String>> results = RedashClient.getQueryResult(baseUrl, queryId, apiKey);
        boolean found = RedashClient.isUserInDatabase(registeredEmail, results, "email");

        Assert.assertTrue(found, "User " + registeredEmail + " should exist in database after registration");
        log.info("✅ User verified in database: {}", registeredEmail);
    }
}
