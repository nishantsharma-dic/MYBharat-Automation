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
 * MegaEventPhotoUploadTest - Youth uploads photos/video to an existing Mega Event on PROD.
 *
 * Flow:
 *   1. Login with user from resources/Uplad_photo_user_MegaEvent_prod.xlsx (sheet=UserData, col 0)
 *   2. Navigate to homepage → Hover "Events & Program" → Click "Mega Events"
 *   3. Pick a RANDOM Mega Event name from resources/Photo_Upload_Mega_Event_Name_prod.xlsx (sheet=Sheet1, col 0)
 *   4. Apply State filter = "All"
 *   5. Search for Mega Event by name
 *   6. Click matching event card
 *   7. Click "Upload Media"
 *   8. Upload 2 images from testdata
 *   9. Upload 1 video from testdata
 *  10. Click Submit → Verify success
 *
 * Run:
 *   mvn test -Denv=prod -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-megaevent-upload.xml
 */
@Listeners(TestListeners.class)
public class MegaEventPhotoUploadTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(MegaEventPhotoUploadTest.class);

    private MegaEventPage megaEventPage;
    private MegaEventPhotoUploadPage uploadPage;

    // Excel: login user (registered youth from Youth_prod.xlsx)
    private static final String USER_EXCEL       = "resources/Youth_prod.xlsx";
    private static final String USER_SHEET        = "UserData";

    // Excel: mega event names (pick LAST created event from Create_MegaEvent_Prod.xlsx)
    private static final String EVENT_EXCEL      = "resources/Create_MegaEvent_Prod.xlsx";
    private static final String EVENT_SHEET      = "ELP_Users";

    private static final int IMAGE_COUNT = 2;

    // Resolved at runtime from Excel
    private String loginEmail;
    private String megaEventName;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        megaEventPage = new MegaEventPage(driver);
        uploadPage = new MegaEventPhotoUploadPage(driver);

        // Read login email from Excel (last row, col 0 — most recently registered user)
        log.info("[SETUP] Reading login email from: {}", USER_EXCEL);
        List<String> emails = ExcelUtils.readColumn(USER_EXCEL, USER_SHEET, 0);
        Assert.assertFalse(emails.isEmpty(), USER_EXCEL + " must have at least one email");
        loginEmail = emails.get(emails.size() - 1); // Last registered user
        log.info("[SETUP] Login email: '{}'", loginEmail);

        // Read last created mega event name (most recent entry)
        log.info("[SETUP] Reading Mega Event name from: {}", EVENT_EXCEL);
        List<String> eventNames = ExcelUtils.readColumn(EVENT_EXCEL, EVENT_SHEET, 0);
        Assert.assertFalse(eventNames.isEmpty(), EVENT_EXCEL + " must have at least one event name");
        megaEventName = eventNames.get(eventNames.size() - 1); // Last created event
        log.info("[SETUP] Using last created Mega Event: '{}'", megaEventName);
    }

    @Test(priority = 1, groups = {"smoke", "megaevent-upload"})
    public void loginToApplication() {
        log.info("=== Step 1: Login check — using already logged-in user or login with {} ===", loginEmail);
        // When running in E2E suite, user is already logged in after quiz flow — skip OTP login
        // loginWithOTP handles this via isAlreadyLoggedIn() check internally
        megaEventPage.loginWithOTP(loginEmail);
        log.info("✅ Step 1 PASSED — Logged in / already logged in");
    }

    @Test(priority = 2, dependsOnMethods = "loginToApplication")
    public void navigateToHomepage() {
        log.info("=== Step 2: Navigate to Homepage ===");
        uploadPage.clickMyBharatLogo();
        log.info("✅ Step 2 PASSED — On homepage");
    }

    @Test(priority = 3, dependsOnMethods = "navigateToHomepage")
    public void navigateToMegaEvents() {
        log.info("=== Step 3: Hover Events & Program → Click Mega Events ===");
        uploadPage.hoverEventsAndClickMegaEvents();
        log.info("✅ Step 3 PASSED — On Mega Events page");
    }

    @Test(priority = 4, dependsOnMethods = "navigateToMegaEvents")
    public void searchMegaEvent() {
        log.info("=== Step 4: Search for Mega Event '{}' ===", megaEventName);
        uploadPage.searchEvent(megaEventName);
        log.info("✅ Step 4 PASSED — Search executed");
    }

    @Test(priority = 5, dependsOnMethods = "searchMegaEvent")
    public void openMegaEventCard() {
        log.info("=== Step 5: Click Mega Event card '{}' ===", megaEventName);
        uploadPage.clickEventCard(megaEventName);
        log.info("✅ Step 5 PASSED — Event detail page opened");
    }

    @Test(priority = 6, dependsOnMethods = "openMegaEventCard")
    public void clickUploadMedia() {
        log.info("=== Step 6: Click Upload Media ===");
        uploadPage.clickUploadMedia();
        log.info("✅ Step 6 PASSED — Upload modal opened");
    }

    @Test(priority = 7, dependsOnMethods = "clickUploadMedia")
    public void uploadImages() {
        log.info("=== Step 7: Upload {} images ===", IMAGE_COUNT);
        uploadPage.uploadImages(IMAGE_COUNT);
        log.info("✅ Step 7 PASSED — Images uploaded");
    }

    @Test(priority = 8, dependsOnMethods = "uploadImages")
    public void uploadVideo() {
        log.info("=== Step 8: Upload video ===");
        uploadPage.uploadVideo();
        log.info("✅ Step 8 PASSED — Video uploaded");
    }

    @Test(priority = 9, dependsOnMethods = "uploadVideo")
    public void submitUpload() {
        log.info("=== Step 9: Submit upload ===");
        uploadPage.clickSubmit();
        boolean success = uploadPage.isUploadSuccessful();
        Assert.assertTrue(success, "Mega Event media upload should be successful");
        log.info("✅ Step 9 PASSED — Upload submitted successfully");
        log.info("=== MEGA EVENT UPLOAD COMPLETE ===");
        log.info("  Event: {}", megaEventName);
        log.info("  Images: {}", IMAGE_COUNT);
        log.info("  Video: 1");
    }
}
