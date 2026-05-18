package com.mybharat.tests.megaevent;

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
import com.mybharat.tests.youth.YouthRegistrationTest;

/**
 * MegaEventPhotoUploadTest - Youth uploads photos to an existing Mega Event.
 *
 * Flow:
 *   1. Register new youth user
 *   2. Navigate to profile → Click MY Bharat logo → Homepage
 *   3. Hover "Events & Program" → Click "Mega Events"
 *   4. Search for "test event" in Ongoing tab
 *   5. Click event card → Event detail page
 *   6. Click "Upload Media" → Upload 7 images → Submit
 *
 * Run:
 *   mvn test -Denv=beta -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-megaevent-upload.xml
 */
@Listeners(TestListeners.class)
public class MegaEventPhotoUploadTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(MegaEventPhotoUploadTest.class);

    private MegaEventPage megaEventPage;
    private MegaEventPhotoUploadPage uploadPage;

    private static final String EVENT_NAME = "Test Event";
    private static final int IMAGE_COUNT = 2;

    private static final String LOGIN_EMAIL = "nissh_create_01@yopmail.com";

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        megaEventPage = new MegaEventPage(driver);
        uploadPage = new MegaEventPhotoUploadPage(driver);
    }

    @Test(priority = 1, groups = {"smoke", "megaevent-upload"})
    public void loginToApplication() {
        log.info("=== Step 1: Login ===");
        megaEventPage.loginWithOTP(LOGIN_EMAIL);
        log.info("✅ Logged in with: {}", LOGIN_EMAIL);
    }

    @Test(priority = 2, groups = {"smoke", "megaevent-upload"}, dependsOnMethods = "loginToApplication")
    public void clickMyBharatLogo() {
        log.info("=== Step 2: Click MY Bharat Logo ===");
        uploadPage.clickMyBharatLogo();
        log.info("✅ Navigated to homepage");
    }

    @Test(priority = 3, groups = {"smoke", "megaevent-upload"}, dependsOnMethods = "clickMyBharatLogo")
    public void navigateToMegaEvents() {
        log.info("=== Step 3: Hover Events & Program → Click Mega Events ===");
        uploadPage.hoverEventsAndClickMegaEvents();
        log.info("✅ On Mega Events page");
    }

    @Test(priority = 4, groups = {"smoke", "megaevent-upload"}, dependsOnMethods = "navigateToMegaEvents")
    public void searchAndOpenEvent() {
        log.info("=== Step 4: Search for event '{}' ===", EVENT_NAME);
        uploadPage.searchEvent(EVENT_NAME);
        uploadPage.clickEventCard(EVENT_NAME);
        log.info("✅ Event detail page opened");
    }

    @Test(priority = 5, groups = {"smoke", "megaevent-upload"}, dependsOnMethods = "searchAndOpenEvent")
    public void uploadMediaToEvent() {
        log.info("=== Step 5: Upload {} images ===", IMAGE_COUNT);
        uploadPage.clickUploadMedia();
        uploadPage.uploadImages(IMAGE_COUNT);
        log.info("✅ {} images uploaded", IMAGE_COUNT);
    }

    @Test(priority = 6, groups = {"smoke", "megaevent-upload"}, dependsOnMethods = "uploadMediaToEvent")
    public void submitUpload() {
        log.info("=== Step 6: Submit ===");
        uploadPage.clickSubmit();
        boolean success = uploadPage.isUploadSuccessful();
        Assert.assertTrue(success, "Photo upload should be successful");
        log.info("✅ Photo upload submitted successfully");
    }

    // =========================================================================
    // HELPER
    // =========================================================================

    private String readLastEmailFromExcel() {
        String env = System.getProperty("env", "beta");
        String filePath = System.getProperty("user.dir") + java.io.File.separator
                + "resources" + java.io.File.separator + "UserDetails_" + env + ".xlsx";
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            filePath = System.getProperty("user.dir") + java.io.File.separator
                    + "resources" + java.io.File.separator + "UserDetails.xlsx";
            file = new java.io.File(filePath);
        }
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
             org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(fis)) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet("UserData");
            if (sheet == null) sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();
            for (int i = lastRow; i >= 1; i--) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row != null && row.getCell(0) != null) {
                    String email = row.getCell(0).getStringCellValue().trim();
                    if (!email.isEmpty()) return email;
                }
            }
        } catch (Exception e) {
            log.error("Failed to read email from Excel: {}", e.getMessage());
        }
        throw new RuntimeException("No email found in Excel");
    }
}
