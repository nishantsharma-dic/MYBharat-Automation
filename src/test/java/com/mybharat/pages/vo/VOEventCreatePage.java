package com.mybharat.pages.vo;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * VOEventCreatePage - Fills the "Add Event" form for VO (Partner side).
 *
 * URL: /orgeventmanagement/add_event/{templateId}
 *
 * Form fields:
 *   1. Event Name (required)
 *   2. Event Description (textarea)
 *   3. State & District selection
 *   4. Event Start/End Date & Time
 *   5. Max Participants
 *   6. Upload Event Image
 *   7. Preview → Publish
 */
public class VOEventCreatePage extends BasePage {

    private static final Logger log = LogManager.getLogger(VOEventCreatePage.class);
    private final ConfigReader config = new ConfigReader();

    private String eventName;

    public VOEventCreatePage(WebDriver driver) {
        super(driver);
    }

    public String getEventName() {
        return eventName;
    }

    /**
     * Fill the complete Add Event form.
     */
    public void fillEventForm() throws InterruptedException {
        log.info("Filling Add Event form...");

        fillEventName();
        fillEventDescription();
        selectStateAndDistrict();
        fillEventDates();
        fillMaxParticipants();
        uploadEventImage();

        log.info("✅ Event form filled completely. Event: {}", eventName);
    }

    /**
     * Click Preview button.
     */
    public void clickPreview() throws InterruptedException {
        log.info("Clicking Preview...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            WebElement previewBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(),'Preview') or @id='preview_button']"
                            + " | //a[contains(text(),'Preview')]")));
            scrollToElement(previewBtn);
            Thread.sleep(300);
            safeClick(previewBtn);
            Thread.sleep(3000);
            waitForPageLoad();
            dismissOverlay();
            log.info("✅ Preview clicked");
        } catch (Exception e) {
            log.error("Preview button not found: {}", e.getMessage());
        }
    }

    /**
     * Click Publish button (on preview page).
     */
    public void clickPublish() throws InterruptedException {
        log.info("Clicking Publish...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            WebElement publishBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(),'Publish') or @id='publish_button']"
                            + " | //a[contains(text(),'Publish')]"
                            + " | //button[contains(text(),'Submit')]")));
            scrollToElement(publishBtn);
            Thread.sleep(300);
            safeClick(publishBtn);
            Thread.sleep(3000);
            waitForPageLoad();
            dismissOverlay();
            log.info("✅ Event published");
        } catch (Exception e) {
            log.error("Publish button not found: {}", e.getMessage());
        }
    }

    /**
     * Save event name to Excel for youth-side tests to use.
     */
    public void saveEventNameToExcel() {
        String path = System.getProperty("user.dir") + File.separator
                + "resources" + File.separator + "Event_Name.xlsx";
        File file = new File(path);
        file.getParentFile().mkdirs();

        try {
            Workbook workbook;
            if (file.exists() && file.length() > 0) {
                java.io.FileInputStream fis = new java.io.FileInputStream(file);
                workbook = new XSSFWorkbook(fis);
                fis.close();
            } else {
                workbook = new XSSFWorkbook();
            }

            Sheet sheet = workbook.getSheet("Event_Data");
            if (sheet == null) {
                sheet = workbook.createSheet("Event_Data");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Event_Name");
            }

            int nextRow = sheet.getLastRowNum() + 1;
            Row row = sheet.createRow(nextRow);
            row.createCell(0).setCellValue(eventName);

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            log.info("✅ Event name saved to Excel: {} (row {})", eventName, nextRow);
        } catch (Exception e) {
            log.error("Failed to save event name to Excel: {}", e.getMessage());
        }
    }

    // =========================================================================
    // Form Section Methods
    // =========================================================================

    private void fillEventName() throws InterruptedException {
        log.info("Filling Event Name...");
        eventName = "VO Automation Event " + System.currentTimeMillis() % 100000;

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@name='event_name' or @id='event_name' or @placeholder='Enter Event Name']")));
        scrollToElement(nameInput);
        nameInput.clear();
        nameInput.sendKeys(eventName);
        Thread.sleep(300);
        log.info("Event Name: {}", eventName);
    }

    private void fillEventDescription() throws InterruptedException {
        log.info("Filling Event Description...");
        try {
            WebElement descTextarea = driver.findElement(
                    By.xpath("//textarea[@name='event_description' or @id='event_description' or @name='description']"));
            scrollToElement(descTextarea);
            descTextarea.clear();
            descTextarea.sendKeys("Automated VO event for community service and youth engagement. Created by automation framework.");
            log.info("✅ Event Description filled");
        } catch (Exception e) {
            log.warn("Event Description textarea not found: {}", e.getMessage());
        }
        Thread.sleep(300);
    }

    private void selectStateAndDistrict() throws InterruptedException {
        log.info("Selecting State and District...");

        try {
            WebElement stateDropdown = driver.findElement(
                    By.xpath("//select[@name='state' or @id='state' or @id='event_state']"));
            scrollToElement(stateDropdown);
            Select stateSelect = new Select(stateDropdown);
            // Select first available state (index 1, skipping "Select")
            if (stateSelect.getOptions().size() > 1) {
                stateSelect.selectByIndex(1);
                Thread.sleep(1000); // Wait for district to load
                log.info("✅ State selected");
            }
        } catch (Exception e) {
            log.warn("State dropdown not found: {}", e.getMessage());
        }

        try {
            WebElement districtDropdown = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//select[@name='district' or @id='district' or @id='event_district']")));
            Select districtSelect = new Select(districtDropdown);
            if (districtSelect.getOptions().size() > 1) {
                districtSelect.selectByIndex(1);
                log.info("✅ District selected");
            }
        } catch (Exception e) {
            log.warn("District dropdown not found: {}", e.getMessage());
        }
        Thread.sleep(300);
    }

    private void fillEventDates() throws InterruptedException {
        log.info("Filling Event Dates...");

        // Start Date — 1 month ahead
        setDateField("event_start_date", 1);
        Thread.sleep(300);

        // End Date — 2 months ahead
        setDateField("event_end_date", 2);
        Thread.sleep(300);

        // Start Time
        setTimeField("event_start_time", "09:00");
        Thread.sleep(300);

        // End Time
        setTimeField("event_end_time", "17:00");
        Thread.sleep(300);
    }

    private void setDateField(String fieldName, int monthsAhead) {
        LocalDate futureDate = LocalDate.now().plusMonths(monthsAhead).withDayOfMonth(15);
        String dateStr = futureDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        try {
            WebElement dateInput = driver.findElement(
                    By.xpath("//input[@name='" + fieldName + "' or @id='" + fieldName + "']"));
            scrollToElement(dateInput);
            ((JavascriptExecutor) driver).executeScript(
                    "var el = arguments[0]; el.readOnly = false; el.value = arguments[1];" +
                    "el.dispatchEvent(new Event('change', {bubbles: true}));" +
                    "try { $(el).trigger('change'); } catch(e) {}",
                    dateInput, dateStr);
            log.info("✅ Date set for {}: {}", fieldName, dateStr);
        } catch (Exception e) {
            log.warn("Date field {} not found: {}", fieldName, e.getMessage());
        }
    }

    private void setTimeField(String fieldName, String timeValue) {
        try {
            WebElement timeInput = driver.findElement(
                    By.xpath("//input[@name='" + fieldName + "' or @id='" + fieldName + "']"));
            scrollToElement(timeInput);
            ((JavascriptExecutor) driver).executeScript(
                    "var el = arguments[0];" +
                    "var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                    "nativeSetter.call(el, arguments[1]);" +
                    "el.dispatchEvent(new Event('input', {bubbles: true}));" +
                    "el.dispatchEvent(new Event('change', {bubbles: true}));",
                    timeInput, timeValue);
            log.info("✅ Time set for {}: {}", fieldName, timeValue);
        } catch (Exception e) {
            log.warn("Time field {} not found: {}", fieldName, e.getMessage());
        }
    }

    private void fillMaxParticipants() throws InterruptedException {
        log.info("Filling Max Participants...");
        try {
            WebElement maxInput = driver.findElement(
                    By.xpath("//input[@name='max_participants' or @id='max_participants' or @placeholder='Max Participants']"));
            scrollToElement(maxInput);
            maxInput.clear();
            maxInput.sendKeys("50");
            log.info("✅ Max Participants: 50");
        } catch (Exception e) {
            log.warn("Max Participants input not found: {}", e.getMessage());
        }
        Thread.sleep(300);
    }

    private void uploadEventImage() throws InterruptedException {
        log.info("Uploading Event Image...");
        try {
            String imagePath = getImagePath();
            WebElement fileInput = driver.findElement(
                    By.xpath("//input[@type='file' and (@name='event_image' or @id='event_image' or @accept='image/*')]"));
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].style.display='block'; arguments[0].style.opacity='1';", fileInput);
            Thread.sleep(300);
            fileInput.sendKeys(imagePath);
            Thread.sleep(2000);
            log.info("✅ Event image uploaded: {}", imagePath);
        } catch (Exception e) {
            log.warn("Event image upload failed: {}", e.getMessage());
        }
    }

    /**
     * Get an image path from the UploadImages folder.
     */
    public static String getImagePath() {
        File imagesDir = Paths.get(System.getProperty("user.dir"), "UploadImages").toFile();
        if (!imagesDir.exists()) {
            throw new RuntimeException("UploadImages folder not found");
        }
        File[] files = imagesDir.listFiles((dir, name) ->
                name.toLowerCase().matches(".*\\.(jpg|png|jpeg)"));
        if (files == null || files.length == 0) {
            throw new RuntimeException("No images found in UploadImages");
        }
        // Return first image that's >= 50KB
        for (File f : files) {
            if (f.length() >= 50 * 1024) return f.getAbsolutePath();
        }
        return files[0].getAbsolutePath();
    }

    private void dismissOverlay() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.invisibilityOfElementLocated(By.id("overlay")));
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript(
                    "var o = document.getElementById('overlay'); if(o) o.style.display='none';" +
                    "var l = document.getElementById('loader2'); if(l) l.style.display='none';" +
                    "try { $('#overlay').hide(); $('#loader2').hide(); } catch(e) {}");
        }
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }
}
