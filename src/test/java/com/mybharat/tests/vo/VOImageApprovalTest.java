package com.mybharat.tests.vo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.vo.VOImageApprovalPage;

/**
 * VOImageApprovalTest - Org side: Youth Photo Moderation + Edit Event + Save as Draft
 *
 * Flow:
 *   1. Navigate to Org Dashboard (View More → Click org)
 *   2. Click "Youth Photo Moderation" in sidebar
 *   3. Click "VO" tab
 *   4. Approve 1st image, Reject 2nd image
 *   5. Click "Events" in sidebar
 *   6. Click "Edit Event" on latest card
 *   7. Scroll down → Save as draft
 *   8. Logout
 */
@Listeners(TestListeners.class)
public class VOImageApprovalTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(VOImageApprovalTest.class);

    private VOImageApprovalPage approvalPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        approvalPage = new VOImageApprovalPage(driver);
    }

    @Test(priority = 1, groups = {"vo", "image-approval"})
    public void reviewAndEditEvent() throws Exception {
        log.info("=== Starting: VO Image Approval + Edit Event ===");

        // Step 1: Navigate to Org Dashboard
        approvalPage.navigateToOrgDashboard();

        // Step 2: Click Youth Photo Moderation
        approvalPage.clickYouthPhotoModeration();

        // Step 3: Click VO tab
        approvalPage.clickVOTab();

        // Step 3b: Search event by city name (second-last from Excel)
        String eventName = getSecondLastEventName();
        if (eventName != null && !eventName.isEmpty()) {
            String city = eventName.split("\\s+")[0]; // first word = city
            log.info("Searching moderation by city: {} (from event: {})", city, eventName);
            approvalPage.searchEventInModeration(city);
        }

        // Step 4: Approve 1st, Reject 2nd
        approvalPage.approveFirstRejectSecond();

        // Step 5: Click Events in sidebar
        approvalPage.clickEventsInSidebar();

        // Step 6: Click "Add/Edit Gallery" on latest card → Upload 5 → Publish → Delete 1 → Back
        approvalPage.clickAddEditGalleryOnLatestCard();
        approvalPage.uploadGalleryImagesAndPublish();

        log.info("=== ✅ Image Approval + Gallery Upload PASSED ===");
    }

    private String getSecondLastEventName() {
        String path = System.getProperty("user.dir") + java.io.File.separator
                + "resources" + java.io.File.separator + "Event_Name.xlsx";
        try (java.io.FileInputStream fis = new java.io.FileInputStream(path);
             org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(fis)) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet("Event_Data");
            if (sheet == null) sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();
            int targetRow = (lastRow >= 2) ? lastRow - 1 : lastRow;
            org.apache.poi.ss.usermodel.Row row = sheet.getRow(targetRow);
            if (row != null && row.getCell(0) != null) {
                return row.getCell(0).getStringCellValue().trim();
            }
        } catch (Exception e) {
            log.warn("Could not read Event_Name.xlsx: {}", e.getMessage());
        }
        return "";
    }
}
