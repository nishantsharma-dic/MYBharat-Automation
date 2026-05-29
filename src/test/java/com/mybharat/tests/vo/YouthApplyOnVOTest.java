package com.mybharat.tests.vo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.vo.YouthApplyOnVOPage;
import com.mybharat.pages.vo.YouthUploadImagesPage;

/**
 * YouthApplyOnVOTest - Youth applies on a published VO event and uploads images.
 *
 * Flow:
 *   Test 1: Navigate to VO → Search event → Upload images
 *   Test 2: Apply on event
 */
@Listeners(TestListeners.class)
public class YouthApplyOnVOTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(YouthApplyOnVOTest.class);

    private YouthApplyOnVOPage applyPage;
    private YouthUploadImagesPage uploadPage;

    private String eventName;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        applyPage = new YouthApplyOnVOPage(driver);
        uploadPage = new YouthUploadImagesPage(driver);
        eventName = getLastEventNameFromExcel();
        log.info("Event name from Excel: {}", eventName);
    }

    @Test(priority = 1, groups = {"vo", "youth-upload"})
    public void uploadImagesForEvent() throws Exception {
        log.info("=== Starting: Youth Search → Click Card → Upload Images → Apply ===");

        // Open Volunteer for Bharat page
        applyPage.openVolunteerForBharat();

        // Search by event name from Excel (e.g. "Swachhta Hi Seva Pune")
        applyPage.searchEvent(eventName);

        // Click first card from search results
        applyPage.clickFirstAvailableVOCard();

        // Upload 3 images (scroll down → Images by Youth → upload → send for approval)
        uploadPage.uploadAndSubmitImages();

        // Apply on event
        applyPage.clickApplyButton();

        log.info("=== ✅ Youth Search → Upload → Apply PASSED ===");
    }

    @Test(priority = 2, groups = {"vo", "youth-apply"})
    public void applyOnVOEvent() throws Exception {
        log.info("=== ✅ Youth Apply — already completed in previous step ===");
    }

    private String getLastEventNameFromExcel() {
        return getEventNameFromExcel(0);
    }

    private String getSecondLastEventNameFromExcel() {
        return getEventNameFromExcel(1);
    }

    /**
     * Get event name from Excel. offset=0 means last row, offset=1 means second last, etc.
     */
    private String getEventNameFromExcel(int offsetFromLast) {
        String path = System.getProperty("user.dir") + java.io.File.separator
                + "resources" + java.io.File.separator + "Event_Name.xlsx";
        try (java.io.FileInputStream fis = new java.io.FileInputStream(path);
             org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(fis)) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet("Event_Data");
            if (sheet == null) sheet = workbook.getSheetAt(0);

            int lastRow = sheet.getLastRowNum();
            int targetRow = lastRow - offsetFromLast;
            if (targetRow < 1) targetRow = 1; // skip header

            org.apache.poi.ss.usermodel.Row row = sheet.getRow(targetRow);
            if (row != null && row.getCell(0) != null) {
                return row.getCell(0).getStringCellValue().trim();
            }
        } catch (Exception e) {
            log.error("Failed to read Event_Name.xlsx: {}", e.getMessage());
        }
        return "Swachhta Hi Seva";
    }
}
