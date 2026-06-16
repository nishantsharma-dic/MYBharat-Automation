package com.mybharat.tests.youth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.Retry;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.youth.LoginPage;

/**
 * LoginTest - End-to-end test for OTP-based user login on MYBharat.
 *
 * Purpose: Verifies that a previously registered user can log in using their email
 *          and OTP verification. Reads the email from the Excel file saved during
 *          registration, sends OTP, retrieves it from Yopmail, and completes login.
 *
 * Prerequisites: RegistrationTest must have run first (saves email to Youth_{env}.xlsx).
 *                This test runs AFTER LogoutTest in the testng-youth.xml suite.
 *
 * Flow:
 *   1. Navigate to home page
 *   2. Close popup if present
 *   3. Click "Sign In"
 *   4. Enter email (read from Excel — most recent registration)
 *   5. Check consent checkbox
 *   6. Click Login (sends OTP)
 *   7. Open Yopmail → fetch OTP → enter OTP
 *   8. Click Verify OTP
 *   9. Assert login was successful (user menu visible or URL indicates logged-in state)
 *
 * After this test, the user is logged in and ready for Profile and Quiz tests.
 *
 * Run:
 *   mvn test -Denv=prod -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-login.xml
 *
 * Dependencies: BaseTest, LoginPage, TestListeners
 * Developer: Nishant Sharma (QA Team)
 *
 * @see LoginPage
 * @see RegistrationTest
 */
@Listeners(TestListeners.class)
public class LoginTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(LoginTest.class);

    private LoginPage loginPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        loginPage = new LoginPage(driver);
    }

    @Test(priority = 1, groups = {"smoke", "login"}, retryAnalyzer = Retry.class,
          description = "Login with OTP: Navigate → Sign In → Enter email from Excel → Send OTP → Fetch OTP from Yopmail → Verify → Login success")
    public void loginWithOTP() throws Exception {
        log.info("=== Starting: Login with OTP ===");

        // Step 1: Navigate to home page
        loginPage.navigateToHomePage();
        log.info("Step 1: Navigated to home page");

        // Step 2: Close popup if present
        loginPage.closePopupIfPresent();
        log.info("Step 2: Popup handled");

        // Step 3: Click Sign In
        loginPage.clickSignIn();
        log.info("Step 3: Clicked Sign In");

        // Step 4: Enter email from Excel in OTP login field
        loginPage.enterEmailForOTPLogin();
        String email = loginPage.getLastRegisteredEmail();
        log.info("Step 4: Email entered from Excel: {}", email);

        // Step 5: Click consent checkbox
        loginPage.clickConsentCheckbox();
        log.info("Step 5: Consent checkbox checked");

        // Step 6: Click Login button to send OTP
        loginPage.clickLoginToSendOTP();
        log.info("Step 6: OTP sent");

        // Step 7: Fetch OTP from Yopmail
        loginPage.fetchOTPFromYopmail();
        log.info("Step 7: OTP fetched from Yopmail and entered");

        // Step 8: Click Verify OTP
        loginPage.clickVerifyOTP();
        log.info("Step 8: OTP verified");

        // Step 9: Verify login success
        boolean isLoggedIn = loginPage.isLoginSuccessful();
        Assert.assertTrue(isLoggedIn, "Login should be successful after OTP verification");
        log.info("=== ✅ Login test PASSED — user is now logged in ===");
    }
}
