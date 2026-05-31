package com.mybharat.tests.megaevent;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.megaevent.MegaEventPage;
import com.mybharat.pages.megaevent.MegaEventPhotoUploadPage;
import com.mybharat.utils.ExcelUtils;

/**
 * MegaEventPhotoUploadTest - Youth uploads photos/video to an existing Mega Event.
 *
 * Single test method: uploadPhoto
 * Flow: Login (if needed) → Homepage → Mega Events → Search → Open Card
 *       → Upload Media → Upload Images → Upload Video → Submit → Logout
 *
 * Run standalone:
 *   mvn test "-Denv=prod" "-Dbrowser=chrome" "-Dsurefire.suiteXmlFiles=testSuites/testng-megaevent-upload.xml"
 */
@Listeners(TestListeners.class)
public class MegaEventPhotoUploadTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(MegaEventPhotoUploadTest.class);

    private MegaEventPage megaEventPage;
    private MegaEventPhotoUploadPage uploadPage;

    // Excel paths — environment-aware (beta/prod)
    private static final String ENV         = System.getProperty("env", "beta");
    private static final String USER_EXCEL  = "resources/Youth_" + ENV + ".xlsx";
    private static final String USER_SHEET  = "UserData";
    private static final String EVENT_EXCEL = "resources/Create_MegaEvent_" + capitalize(ENV) + ".xlsx";
    private static final String EVENT_SHEET = "MegaEvents";

    private static String capitalize(String s) {
        return (s == null || s.isEmpty()) ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private static final int IMAGE_COUNT = 2;

    private String loginEmail;
    private String megaEventName;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        megaEventPage = new MegaEventPage(driver);
        uploadPage = new MegaEventPhotoUploadPage(driver);

        List<String> emails = ExcelUtils.readColumn(USER_EXCEL, USER_SHEET, 0);
        Assert.assertFalse(emails.isEmpty(), USER_EXCEL + " must have at least one email");
        loginEmail = emails.get(emails.size() - 1);
        log.info("[SETUP] Login email: '{}'", loginEmail);

        List<String> eventNames = ExcelUtils.readColumn(EVENT_EXCEL, EVENT_SHEET, 0);
        Assert.assertFalse(eventNames.isEmpty(), EVENT_EXCEL + " must have at least one event name");
        megaEventName = eventNames.get(eventNames.size() - 1);
        log.info("[SETUP] Mega Event: '{}'", megaEventName);
    }

    /**
     * Single test: Upload photos/video to an existing Mega Event.
     * Appears as one test case in ExtentReport.
     */
    @Test(groups = {"smoke", "megaevent-upload"},
          description = "Upload photos/video to Mega Event (Youth user)")
    public void uploadPhoto() throws Exception {
        // Step 1: Login if not already logged in
        String currentUrl = driver.getCurrentUrl();
        boolean alreadyLoggedIn = currentUrl != null
                && !currentUrl.isEmpty()
                && !currentUrl.contains("login")
                && !currentUrl.equals("about:blank")
                && !currentUrl.equals("data:,");

        if (!alreadyLoggedIn) {
            com.mybharat.pages.youth.LoginPage lp = new com.mybharat.pages.youth.LoginPage(driver);
            alreadyLoggedIn = lp.isLoginSuccessful();
        }

        if (alreadyLoggedIn) {
            log.info("Step 1: Already logged in — skipping login");
        } else {
            log.info("Step 1: Logging in as {}", loginEmail);
            megaEventPage.loginWithOTP(loginEmail);
        }

        // Step 2: Navigate to homepage
        log.info("Step 2: Navigate to homepage");
        uploadPage.clickMyBharatLogo();

        // Step 3: Navigate to Mega Events public page
        log.info("Step 3: Navigate to Mega Events");
        uploadPage.hoverEventsAndClickMegaEvents();

        // Step 4: Search for event
        log.info("Step 4: Search for '{}'", megaEventName);
        uploadPage.searchEvent(megaEventName);

        // Step 5: Open event card
        log.info("Step 5: Open event card");
        uploadPage.clickEventCard(megaEventName);

        // Step 6: Click Upload Media
        log.info("Step 6: Click Upload Media");
        uploadPage.clickUploadMedia();

        // Step 7: Upload images
        log.info("Step 7: Upload {} images", IMAGE_COUNT);
        uploadPage.uploadImages(IMAGE_COUNT);

        // Step 8: Upload video
        log.info("Step 8: Upload video");
        uploadPage.uploadVideo();

        // Step 9: Submit
        log.info("Step 9: Submit upload");
        uploadPage.clickSubmit();
        boolean success = uploadPage.isUploadSuccessful();
        Assert.assertTrue(success, "Mega Event media upload should be successful");

        log.info("✅ uploadPhoto PASSED — Event: {}, Images: {}, Video: 1", megaEventName, IMAGE_COUNT);

        // Step 10: Logout so MegaEventTest can login as Partner
        log.info("Step 10: Logout");
        try {
            com.mybharat.pages.youth.LogoutPage logoutPage = new com.mybharat.pages.youth.LogoutPage(driver);
            logoutPage.logout();
            log.info("Logged out successfully");
        } catch (Exception e) {
            log.warn("Logout failed (non-critical): {}", e.getMessage());
            try { driver.get(new com.mybharat.utils.ConfigReader().getUrl()); } catch (Exception ignored) {}
        }
    }
}
