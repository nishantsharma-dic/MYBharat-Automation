package com.mybharat.pages.elp;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;

/**
 * ELPCreatePage - Fills the Create ELP form on the Experiential Learning page.
 * 
 * URL: /elp/admin-listing → Click "+ Create ELP" → /elp/create-elp
 * 
 * Form sections:
 *   1. Program Title
 *   2. About the Program (Activities, Objective, Learnings)
 *   3. ELP Type and Duration (Type, Duration, Start/End/Last Date)
 *   4. Activity Criteria (Opportunities, Nature of Work)
 *   5. Reporting Location (Address, Lat/Lon, State, District)
 *   6. ELP Domain
 *   7. Who Can Apply (Academic Qualification)
 *   8. Contact Person Details (Name, Designation, Phone, Email)
 */
public class ELPCreatePage extends BasePage {

    private static final Logger log = LogManager.getLogger(ELPCreatePage.class);

    public ELPCreatePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Click the "+ Create ELP" button on the ELP listing page.
     */
    public void clickCreateELP() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Wait for overlay to disappear
        waitForOverlayToDisappear();

        WebElement createBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Create ELP']")));
        scrollToElement(createBtn);
        Thread.sleep(300);
        try {
            createBtn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", createBtn);
        }
        log.info("✅ Clicked 'Create ELP' button");

        waitForPageLoad();
        Thread.sleep(3000); // Wait for form to load
    }

    /**
     * Fill the complete ELP creation form with test data.
     */
    public void fillELPForm() throws InterruptedException {
        log.info("Filling ELP creation form...");

        fillProgramTitle();
        fillAboutProgram();
        fillELPTypeAndDuration();
        fillActivityCriteria();
        fillReportingLocation();
        fillELPDomain();
        fillWhoCanApply();
        fillContactPersonDetails();

        log.info("✅ ELP form filled completely");
    }

    /**
     * Click Preview button to submit the form.
     */
    public void clickPreview() throws InterruptedException {
        WebElement previewBtn = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[normalize-space()='Preview']")));
        scrollToElement(previewBtn);
        Thread.sleep(300);
        previewBtn.click();
        log.info("✅ Clicked Preview button");
        waitForPageLoad();
        Thread.sleep(3000);
    }

    /**
     * Click Publish button on the preview page.
     */
    public void clickPublish() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Wait for overlay to disappear
        waitForOverlayToDisappear();

        WebElement publishBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Publish']")));
        scrollToElement(publishBtn);
        Thread.sleep(300);
        try {
            publishBtn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", publishBtn);
        }
        log.info("✅ Clicked Publish button");
        waitForPageLoad();
        Thread.sleep(2000);
    }

    // =========================================================================
    // Form Section Methods
    // =========================================================================

    private void fillProgramTitle() throws InterruptedException {
        log.info("Filling Program Title...");

        // Upload Banner image — hidden file input inside the banner section
        try {
            WebElement bannerFileInput = driver.findElement(By.xpath(
                    "//div[contains(@class,'bg-[#062E63]')]//input[@type='file'] | " +
                    "//div[contains(@class,'relative') and contains(@class,'h-[160px]')]//input[@type='file']"));
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].removeAttribute('hidden');" +
                    "arguments[0].classList.remove('hidden');" +
                    "arguments[0].style.display='block';",
                    bannerFileInput);
            Thread.sleep(200);
            String bannerPath = getLogoImagePath();
            bannerFileInput.sendKeys(bannerPath);
            Thread.sleep(2000);
            log.info("✅ Banner uploaded: {}", bannerPath);
        } catch (Exception e) {
            log.warn("Banner upload failed: {}", e.getMessage());
        }

        // Upload Logo — the hidden file input is INSIDE the logo div container
        try {
            WebElement logoContainer = driver.findElement(By.xpath(
                    "//div[contains(@class,'rounded') and contains(@class,'border') and contains(@class,'bg-gray-50')]"));
            WebElement logoFileInput = logoContainer.findElement(By.xpath(".//input[@type='file']"));
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].removeAttribute('hidden');" +
                    "arguments[0].style.display='block';" +
                    "arguments[0].style.opacity='1';" +
                    "arguments[0].style.position='absolute';",
                    logoFileInput);
            Thread.sleep(200);
            String imagePath = getLogoImagePath();
            logoFileInput.sendKeys(imagePath);
            Thread.sleep(3000); // Wait for S3 upload
            log.info("✅ Logo uploaded: {}", imagePath);
        } catch (Exception e) {
            log.warn("Logo upload failed: {}", e.getMessage());
        }

        // Fill Program Title — unique name each time
        String uniqueTitle = "ELP Automation - Digital Literacy " + System.currentTimeMillis() % 100000;
        WebElement titleInput = driver.findElement(
                By.xpath("//input[@placeholder='Add Program Title']"));
        scrollToElement(titleInput);
        titleInput.clear();
        titleInput.sendKeys(uniqueTitle);
        Thread.sleep(300);
        log.info("Program Title: {}", uniqueTitle);
    }

    /**
     * Get image path for logo upload (needs to be < 1MB).
     */
    private String getLogoImagePath() {
        java.io.File imagesDir = java.nio.file.Paths.get(System.getProperty("user.dir"), "UploadImages").toFile();
        java.io.File[] files = imagesDir.listFiles((dir, name) ->
                name.toLowerCase().matches(".*\\.(jpg|png|jpeg)"));
        if (files != null && files.length > 0) {
            for (java.io.File f : files) {
                if (f.length() < 1024 * 1024) {
                    return f.getAbsolutePath();
                }
            }
            return files[0].getAbsolutePath();
        }
        throw new RuntimeException("No images found in UploadImages folder");
    }

    private void fillAboutProgram() throws InterruptedException {
        log.info("Filling About the Program...");

        // These are TipTap rich text editors (contenteditable divs), NOT textareas
        // They have a toolbar (Bold, Italic, Link) and a contenteditable div below
        // Find all contenteditable divs (ProseMirror editors)
        java.util.List<WebElement> editors = driver.findElements(
                By.cssSelector("div.ProseMirror, div[contenteditable='true'], .tiptap"));

        if (editors.size() >= 3) {
            // Activities (first editor)
            scrollToElement(editors.get(0));
            editors.get(0).click();
            Thread.sleep(200);
            editors.get(0).sendKeys("Youth will be trained on digital literacy skills including basic computer operations, internet safety, and online communication tools. They will conduct awareness sessions.");

            // Objective (second editor)
            scrollToElement(editors.get(1));
            editors.get(1).click();
            Thread.sleep(200);
            editors.get(1).sendKeys("The objective of this ELP is to enhance digital literacy among rural youth and empower them with essential technology skills for employment.");

            // Learnings (third editor)
            scrollToElement(editors.get(2));
            editors.get(2).click();
            Thread.sleep(200);
            editors.get(2).sendKeys("Through this ELP, youth will learn practical digital skills, communication techniques, and community engagement methods for professional careers.");
        } else {
            log.warn("Rich text editors not found (found {}), trying textarea fallback", editors.size());
            // Fallback: try textareas
            java.util.List<WebElement> textareas = driver.findElements(By.tagName("textarea"));
            for (int i = 0; i < Math.min(3, textareas.size()); i++) {
                scrollToElement(textareas.get(i));
                textareas.get(i).sendKeys("Automation test content for ELP section. Youth will participate in various activities and gain practical experience in community development and digital literacy programs.");
            }
        }
        Thread.sleep(300);
    }

    private void fillELPTypeAndDuration() throws InterruptedException {
        log.info("Filling ELP Type and Duration...");

        // Select ELP Type
        selectDropdownByLabel("Type of ELP", "Regular ELP");
        Thread.sleep(500);

        // Select Duration (In Hours) — label is "Duration (In Hours)"
        try {
            WebElement durationSelect = driver.findElement(By.xpath(
                    "//select[.//option[contains(text(),'Select duration') or contains(text(),'duration')]]"));
            scrollToElement(durationSelect);
            org.openqa.selenium.support.ui.Select sel = new org.openqa.selenium.support.ui.Select(durationSelect);
            // Select "60 Hours" or first available
            boolean selected = false;
            for (WebElement opt : sel.getOptions()) {
                if (opt.getText().contains("60")) {
                    sel.selectByVisibleText(opt.getText());
                    selected = true;
                    break;
                }
            }
            if (!selected && sel.getOptions().size() > 1) {
                sel.selectByIndex(1);
            }
            Thread.sleep(300);
            log.info("Duration selected");
        } catch (Exception e) {
            log.warn("Duration dropdown not found: {}", e.getMessage());
        }

        // Start Date
        LocalDate startDate = LocalDate.now().plusDays(7);
        setDateField("Start Date", startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        // Reporting Time — it's an <input type="time">, not a select
        // Need to click it, then set value "09:00" (24-hour format for 9:00 AM)
        try {
            WebElement timeInput = driver.findElement(By.xpath(
                    "//input[@type='time']"));
            scrollToElement(timeInput);
            timeInput.click();
            Thread.sleep(300);
            // Set value via JS (React controlled input)
            ((JavascriptExecutor) driver).executeScript(
                    "var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                    "nativeSetter.call(arguments[0], '09:00');" +
                    "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));" +
                    "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
                    timeInput);
            Thread.sleep(300);
            log.info("Reporting time set to 09:00 AM");
        } catch (Exception e) {
            log.warn("Reporting time input not found: {}", e.getMessage());
        }

        // End Date
        LocalDate endDate = startDate.plusDays(60);
        setDateField("End Date", endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        // Last Date to show interest
        LocalDate lastDate = LocalDate.now().plusDays(5);
        setDateField("Last Date", lastDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    private void fillActivityCriteria() throws InterruptedException {
        log.info("Filling Activity Criteria...");

        // No. of Opportunities
        try {
            WebElement oppInput = driver.findElement(
                    By.xpath("//input[contains(@placeholder,'Opportunities') or contains(@placeholder,'opportunities')]"));
            scrollToElement(oppInput);
            oppInput.clear();
            oppInput.sendKeys("10");
        } catch (Exception e) {
            log.info("Opportunities input not found");
        }

        // Mode of Activity — select first real option
        try {
            WebElement modeSelect = driver.findElement(By.xpath(
                    "//select[.//option[contains(text(),'Select Mode')]]"));
            scrollToElement(modeSelect);
            org.openqa.selenium.support.ui.Select sel = new org.openqa.selenium.support.ui.Select(modeSelect);
            if (sel.getOptions().size() > 1) {
                sel.selectByIndex(1);
            }
            Thread.sleep(300);
        } catch (Exception e) {
            selectDropdownByLabel("Mode of Activity", "Online");
        }

        // Nature of Work — select "Office-based"
        selectDropdownByLabel("Nature of work", "Office-based");
        Thread.sleep(300);
    }

    private void fillReportingLocation() throws InterruptedException {
        log.info("Filling Reporting Location...");
        scrollPage(500);
        Thread.sleep(500);

        // Fill Latitude
        try {
            WebElement latInput = driver.findElement(By.xpath(
                    "//input[preceding-sibling::*[contains(text(),'Latitude')] or @placeholder='Latitude' or ancestor::div[contains(.,'Latitude')]/input]"));
            scrollToElement(latInput);
            latInput.clear();
            latInput.sendKeys("28.833935");
            log.info("Latitude filled");
        } catch (Exception e) {
            // Fallback: find by position (Latitude is before Longitude)
            try {
                java.util.List<WebElement> inputs = driver.findElements(By.xpath(
                        "//input[@type='text' or @type='number']"));
                for (WebElement inp : inputs) {
                    String placeholder = inp.getAttribute("placeholder");
                    if (placeholder != null && placeholder.toLowerCase().contains("lat")) {
                        inp.clear();
                        inp.sendKeys("28.833935");
                        break;
                    }
                }
            } catch (Exception e2) {
                log.warn("Latitude input not found");
            }
        }

        // Fill Longitude
        try {
            WebElement lonInput = driver.findElement(By.xpath(
                    "//input[preceding-sibling::*[contains(text(),'Longitude')] or @placeholder='Longitude' or ancestor::div[contains(.,'Longitude')]/input]"));
            lonInput.clear();
            lonInput.sendKeys("77.583189");
            log.info("Longitude filled");
        } catch (Exception e) {
            try {
                java.util.List<WebElement> inputs = driver.findElements(By.xpath(
                        "//input[@type='text' or @type='number']"));
                for (WebElement inp : inputs) {
                    String placeholder = inp.getAttribute("placeholder");
                    if (placeholder != null && placeholder.toLowerCase().contains("lon")) {
                        inp.clear();
                        inp.sendKeys("77.583189");
                        break;
                    }
                }
            } catch (Exception e2) {
                log.warn("Longitude input not found");
            }
        }

        Thread.sleep(500);

        // Click "Get Address" button — this auto-fills State, District, Address etc.
        try {
            WebElement getAddressBtn = driver.findElement(By.xpath(
                    "//button[normalize-space()='Get Address' or contains(text(),'Get Address')]"));
            scrollToElement(getAddressBtn);
            Thread.sleep(300);
            getAddressBtn.click();
            log.info("Clicked 'Get Address' button");
            Thread.sleep(3000); // Wait for reverse geocoding to fill fields
        } catch (Exception e) {
            log.warn("Get Address button not found: {}", e.getMessage());
        }

        // Fill Pincode if still empty
        try {
            WebElement pincodeInput = driver.findElement(
                    By.xpath("//input[contains(@placeholder,'pincode') or contains(@placeholder,'Pincode') or contains(@placeholder,'Enter pincode')]"));
            scrollToElement(pincodeInput);
            if (pincodeInput.getAttribute("value") == null || pincodeInput.getAttribute("value").isEmpty()) {
                pincodeInput.clear();
                pincodeInput.sendKeys("201301");
            }
        } catch (Exception e) {
            log.info("Pincode input not found");
        }

        // Select Urban radio
        try {
            WebElement urbanRadio = driver.findElement(
                    By.xpath("//input[@type='radio' and following-sibling::*[contains(text(),'Urban')] or @value='Urban']"));
            if (!urbanRadio.isSelected()) {
                jsClick(urbanRadio);
            }
        } catch (Exception e) {
            log.info("Urban radio not found");
        }
    }

    private void fillELPDomain() throws InterruptedException {
        log.info("Filling ELP Domain...");
        scrollPage(300);
        Thread.sleep(300);

        // ELP Domain is a custom multi-select with checkboxes
        // Click the dropdown trigger to open it
        try {
            WebElement domainTrigger = driver.findElement(By.xpath(
                    "//div[contains(text(),'Select up to 5 ELP domains')] | " +
                    "//*[contains(text(),'ELP Domain')]/following::div[contains(text(),'Select')][1]"));
            scrollToElement(domainTrigger);
            domainTrigger.click();
            Thread.sleep(800);

            // Now find and click the first checkbox (Communication)
            WebElement checkbox = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//input[@type='checkbox' and following-sibling::*[contains(text(),'Communication')] or ancestor::label[contains(text(),'Communication')]]")));
            checkbox.click();
            Thread.sleep(300);
            log.info("Selected 'Communication' domain");

            // Click outside to close the dropdown
            ((JavascriptExecutor) driver).executeScript("document.body.click();");
            Thread.sleep(300);
        } catch (Exception e) {
            // Fallback: try clicking any visible checkbox
            try {
                WebElement domainTrigger = driver.findElement(By.xpath(
                        "//*[contains(text(),'Select up to 5')]"));
                scrollToElement(domainTrigger);
                domainTrigger.click();
                Thread.sleep(800);

                // Click first checkbox found
                java.util.List<WebElement> checkboxes = driver.findElements(
                        By.xpath("//input[@type='checkbox']"));
                for (WebElement cb : checkboxes) {
                    if (cb.isDisplayed()) {
                        cb.click();
                        log.info("Selected first visible domain checkbox");
                        break;
                    }
                }
                ((JavascriptExecutor) driver).executeScript("document.body.click();");
                Thread.sleep(300);
            } catch (Exception e2) {
                log.warn("ELP Domain selection failed: {}", e2.getMessage());
            }
        }
    }

    private void fillWhoCanApply() throws InterruptedException {
        log.info("Filling Who Can Apply...");
        scrollPage(300);
        Thread.sleep(300);

        // Academic Qualification (Minimum) — use the exact CSS selector from DOM
        try {
            WebElement qualSelect = driver.findElement(By.cssSelector(
                    "div[class='grid grid-cols-1 md:grid-cols-3 gap-6 items-start'] select:nth-child(1)"));
            scrollToElement(qualSelect);
            // Use JS to set value "Open to All" and dispatch change event
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = 'Open to All';" +
                    "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
                    qualSelect);
            Thread.sleep(300);
            log.info("Academic Qualification selected: Open to All");
        } catch (Exception e) {
            log.warn("Academic Qualification dropdown not found: {}", e.getMessage());
        }
    }

    private void fillContactPersonDetails() throws InterruptedException {
        log.info("Filling Contact Person Details...");
        scrollPage(500);
        Thread.sleep(500);

        // Name
        try {
            WebElement nameInput = driver.findElement(
                    By.xpath("//input[contains(@placeholder,'Enter Name') or contains(@placeholder,'name')]"));
            scrollToElement(nameInput);
            nameInput.clear();
            nameInput.sendKeys("Test Automation User");
        } catch (Exception e) {
            log.info("Contact name input not found");
        }

        // Designation
        try {
            WebElement desigInput = driver.findElement(
                    By.xpath("//input[contains(@placeholder,'designation') or contains(@placeholder,'Designation') or contains(@placeholder,'contact person role')]"));
            desigInput.clear();
            desigInput.sendKeys("Program Coordinator");
        } catch (Exception e) {
            log.info("Designation input not found");
        }

        // Phone
        try {
            WebElement phoneInput = driver.findElement(
                    By.xpath("//input[contains(@placeholder,'phone') or contains(@placeholder,'Phone') or contains(@placeholder,'contact person phone')]"));
            phoneInput.clear();
            phoneInput.sendKeys("9876543210");
        } catch (Exception e) {
            log.info("Phone input not found");
        }

        // Email
        try {
            WebElement emailInput = driver.findElement(
                    By.xpath("//input[contains(@placeholder,'email') or contains(@placeholder,'Email') or contains(@placeholder,'contact person email')]"));
            emailInput.clear();
            emailInput.sendKeys("testautomation@yopmail.com");
        } catch (Exception e) {
            log.info("Email input not found");
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Fill a textarea field by finding it near a label containing the given text.
     */
    private void fillTextAreaByLabel(String labelText, String value) {
        try {
            // Find textarea that follows a label/div containing the text
            WebElement textarea = driver.findElement(By.xpath(
                    "//*[contains(text(),'" + labelText + "')]/ancestor::div[1]//textarea | " +
                    "//*[contains(text(),'" + labelText + "')]/following::textarea[1]"));
            scrollToElement(textarea);
            textarea.clear();
            textarea.sendKeys(value);
        } catch (Exception e) {
            log.warn("Textarea for '{}' not found: {}", labelText, e.getMessage());
        }
    }

    /**
     * Select an option from a native <select> dropdown by finding it near a label.
     */
    private void selectDropdownByLabel(String labelText, String optionText) {
        try {
            WebElement select = driver.findElement(By.xpath(
                    "//*[contains(text(),'" + labelText + "')]/ancestor::div[1]//select | " +
                    "//*[contains(text(),'" + labelText + "')]/following::select[1]"));
            scrollToElement(select);
            new org.openqa.selenium.support.ui.Select(select).selectByVisibleText(optionText);
        } catch (Exception e) {
            // Try partial match
            try {
                WebElement select = driver.findElement(By.xpath(
                        "//*[contains(text(),'" + labelText + "')]/following::select[1]"));
                org.openqa.selenium.support.ui.Select sel = new org.openqa.selenium.support.ui.Select(select);
                for (WebElement opt : sel.getOptions()) {
                    if (opt.getText().contains(optionText)) {
                        sel.selectByVisibleText(opt.getText());
                        return;
                    }
                }
            } catch (Exception e2) {
                log.warn("Dropdown for '{}' not found", labelText);
            }
        }
    }

    /**
     * Set a date field value using JavaScript (React date inputs).
     */
    private void setDateField(String labelText, String dateValue) {
        try {
            WebElement dateInput = driver.findElement(By.xpath(
                    "//*[contains(text(),'" + labelText + "')]/ancestor::div[1]//input[@type='date'] | " +
                    "//*[contains(text(),'" + labelText + "')]/following::input[@type='date'][1]"));
            scrollToElement(dateInput);
            ((JavascriptExecutor) driver).executeScript(
                    "var nativeInputValueSetter = Object.getOwnPropertyDescriptor(" +
                    "window.HTMLInputElement.prototype, 'value').set;" +
                    "nativeInputValueSetter.call(arguments[0], arguments[1]);" +
                    "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                    "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                    dateInput, dateValue);
        } catch (Exception e) {
            log.warn("Date field for '{}' not found", labelText);
        }
    }

    /**
     * Wait for overlay element to disappear.
     */
    private void waitForOverlayToDisappear() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.invisibilityOfElementLocated(By.id("overlay")));
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript(
                    "var o = document.getElementById('overlay'); if(o) o.style.display='none';");
        }
    }
}
