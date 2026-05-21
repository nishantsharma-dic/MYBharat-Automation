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
 *   1. Login with existing user
 *   2. Navigate to homepage → Hover "Events & Program" → Click "Mega Events"
 *   3. Read Mega Event name from Excel (src/test/resources/testdata/MegaEvents.xlsx)
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

    private static final String EXCEL_PATH = "src/test/resources/testdata/MegaEvents.xlsx";
    private static final String SHEET_NAME = "MegaEvents";
    private static final int IMAGE_COUNT = 2;

    // Login email for prod
    private static final String LOGIN_EMAIL = "mega_uplaod_images_01@yopmail.com";

    // Will be read from Excel
    private String megaEventName;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        megaEventPage = new MegaEventPage(driver);
        uploadPage = new MegaEventPhotoUploadPage(driver);

        // Read Mega Event name from Excel
        log.info("[SETUP] Reading Mega Event name from Excel: {}", EXCEL_PATH);
        List<String> eventNames = ExcelUtils.readColumn(EXCEL_PATH, SHEET_NAME, 0);
        Assert.assertFalse(eventNames.isEmpty(), "Excel should contain at least one Mega Event name");
        megaEventName = eventNames.get(0); // Use first event
        log.info("[SETUP] Selected Mega Event: '{}'", megaEventName);
    }

    @Test(priority = 1, groups = {"smoke", "megaevent-upload"})
    public void loginToApplication() {
        log.info("=== Step 1: Login with {} ===", LOGIN_EMAIL);
        megaEventPage.loginWithOTP(LOGIN_EMAIL);
        log.info("✅ Step 1 PASSED — Logged in");
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
