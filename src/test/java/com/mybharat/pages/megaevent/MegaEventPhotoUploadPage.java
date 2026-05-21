package com.mybharat.pages.megaevent;

import java.io.File;
import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * MegaEventPhotoUploadPage - Handles the Youth photo upload flow for Mega Events.
 *
 * Flow:
 *   1. Navigate to profile → Click MY Bharat logo → Homepage
 *   2. Hover "Events & Program" → Click "Mega Events"
 *   3. Search for event by name in "Ongoing" tab
 *   4. Click event card → Event detail page
 *   5. Scroll to "Mega Event Gallery" → Click "Upload Media"
 *   6. Modal: Click "Upload Images" → sendKeys file → "Add More Images" → repeat
 *   7. Click Submit
 */
public class MegaEventPhotoUploadPage extends BasePage {

    private static final Logger log = LogManager.getLogger(MegaEventPhotoUploadPage.class);
    private final ConfigReader config = new ConfigReader();
    private final JavascriptExecutor js;
    private final WebDriverWait wait;

    public MegaEventPhotoUploadPage(WebDriver driver) {
        super(driver);
        this.js = (JavascriptExecutor) driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(25));
    }

    // =========================================================================
    // STEP 1: Click MY Bharat Logo to go to homepage
    // =========================================================================

    public void clickMyBharatLogo() {
        log.info("Clicking MY Bharat logo to navigate to homepage");
        // Navigate directly to the main homepage (more reliable than clicking logo)
        driver.get(config.getUrl());
        waitForPageLoad();
        safeSleep(3000);
        // Close any popup
        try {
            WebElement popup = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                ExpectedConditions.elementToBeClickable(By.xpath("//i[@class='fa fa-times']")));
            popup.click();
            safeSleep(500);
        } catch (Exception e) { /* no popup */ }
        log.info("Navigated to homepage: {}", driver.getCurrentUrl());
    }

    // =========================================================================
    // STEP 2: Hover "Events & Program" and click "Mega Events"
    // =========================================================================

    public void hoverEventsAndClickMegaEvents() {
        log.info("Hovering on 'Events & Program' menu");

        // The nav menu text from screenshot: "Events & Program" with chevron
        // May render as "Events & Program" or use &amp; in HTML
        WebElement eventsMenu = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(),'Events') and contains(text(),'Program')]")));

        // Hover to open dropdown
        Actions actions = new Actions(driver);
        actions.moveToElement(eventsMenu).perform();
        safeSleep(1500); // Wait for dropdown to render

        // Click "Mega Events" from the dropdown
        WebElement megaEventsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[normalize-space()='Mega Events'] | //span[normalize-space()='Mega Events'] | //*[contains(text(),'Mega Events')]")));
        safeClick(megaEventsLink);
        waitForPageLoad();
        safeSleep(3000);
        log.info("Navigated to Mega Events public page");
    }

    // =========================================================================
    // STEP 3: Search for event on Mega Events public page
    // =========================================================================

    public void searchEvent(String eventName) {
        log.info("[searchEvent] START — Searching for: {}", eventName);

        // Wait for page to fully load
        safeSleep(8000);

        // STEP 1: Click "All" tab if available
        log.info("[searchEvent] BEFORE clicking 'All' tab");
        try {
            WebElement allTab = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[normalize-space()='All'][self::a or self::span or self::button or self::h5 or self::ion-segment-button or ancestor::ion-segment-button]")));
            safeClick(allTab);
            safeSleep(3000);
            log.info("[searchEvent] ✅ 'All' tab clicked");
        } catch (Exception e) {
            // Try "Ongoing" as fallback
            log.warn("[searchEvent] 'All' tab not found, trying 'Ongoing'...");
            try {
                WebElement tab = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[normalize-space()='Ongoing']")));
                safeClick(tab);
                safeSleep(2000);
                log.info("[searchEvent] ✅ 'Ongoing' tab clicked as fallback");
            } catch (Exception e2) {
                log.warn("[searchEvent] No tab found, continuing...");
            }
        }

        // STEP 2: Select State = "All" from dropdown
        safeSleep(3000);
        log.info("[searchEvent] BEFORE selecting State filter 'All'");
        try {
            WebElement stateSelect = new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//select[@name='filter-state']")));
            org.openqa.selenium.support.ui.Select dropdown = new org.openqa.selenium.support.ui.Select(stateSelect);
            dropdown.selectByVisibleText("All");
            safeSleep(2000);
            log.info("[searchEvent] ✅ State 'All' selected");
        } catch (Exception e) {
            log.warn("[searchEvent] State dropdown not found: {}", e.getMessage());
        }

        // STEP 3: Type event name in search input
        safeSleep(1000);
        log.info("[searchEvent] BEFORE entering search text: '{}'", eventName);
        List<WebElement> allInputs = driver.findElements(By.cssSelector("input[type='text'], input:not([type])"));
        WebElement eventInput = null;
        for (int i = allInputs.size() - 1; i >= 0; i--) {
            try {
                if (allInputs.get(i).isDisplayed()) {
                    eventInput = allInputs.get(i);
                    break;
                }
            } catch (Exception e) { }
        }

        if (eventInput != null) {
            scrollToElement(eventInput);
            eventInput.clear();
            eventInput.sendKeys(eventName);
            safeSleep(3000);
            log.info("[searchEvent] ✅ Typed event name");

            // STEP 4: Select suggestion if appears
            try {
                WebElement suggestion = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + eventName.toLowerCase() + "')]" +
                            "[not(self::ion-text)][not(ancestor::*[contains(@class,'filteroverlay')])]")));
                safeClick(suggestion);
                safeSleep(1000);
                log.info("[searchEvent] ✅ Selected suggestion");
            } catch (Exception e) {
                log.warn("[searchEvent] No suggestion found, continuing...");
            }

            // STEP 5: Click Search icon
            log.info("[searchEvent] BEFORE clicking search icon");
            try {
                WebElement searchIcon = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(
                        By.xpath("//img[@alt='search icon'] | //ion-icon[@name='search']")));
                safeClick(searchIcon);
                log.info("[searchEvent] ✅ Search icon clicked");
            } catch (Exception e) {
                log.warn("[searchEvent] Search icon not found, pressing Enter");
                eventInput.sendKeys(org.openqa.selenium.Keys.ENTER);
            }
            safeSleep(3000);
        } else {
            throw new RuntimeException("Event Name search input not found");
        }

        log.info("[searchEvent] END — Search completed for: '{}'", eventName);
    }

    // =========================================================================
    // STEP 4: Find event in tabs (Upcoming → Ongoing) and click card
    // =========================================================================

    public void clickEventCard(String eventName) {
        log.info("Clicking event card: {}", eventName);
        safeSleep(3000);

        WebElement card = new WebDriverWait(driver, Duration.ofSeconds(20)).until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@id='event-cards1']//img[@class='img-fluid']")));
        scrollToElement(card);
        safeClick(card);
        waitForPageLoad();
        safeSleep(3000);
        log.info("✅ Event detail page opened for: {}", eventName);
    }

    /**
     * Select a value from the filter dropdowns (State, District) on the Mega Events page.
     * These are native <select> elements.
     */
    private void selectFilterDropdown(String label, String value) {
        log.info("Selecting '{}' in '{}' filter", value, label);
        try {
            // Find select element near the label
            WebElement select = driver.findElement(
                By.xpath("//*[contains(text(),'" + label + "')]/ancestor::div[contains(@class,'filter') or contains(@class,'col')]//select" +
                    " | //select[contains(@id,'" + label.toLowerCase() + "') or contains(@name,'" + label.toLowerCase() + "')]" +
                    " | //label[contains(text(),'" + label + "')]/following::select[1]"));
            scrollToElement(select);
            new org.openqa.selenium.support.ui.Select(select).selectByVisibleText(value);
        } catch (Exception e) {
            // Try as ion-select or custom dropdown
            log.warn("Native select not found for '{}', trying click approach", label);
            try {
                WebElement dropdown = driver.findElement(
                    By.xpath("//*[contains(text(),'" + label + "')]/ancestor::div//select | //*[contains(text(),'" + label + "')]/following::select[1]"));
                new org.openqa.selenium.support.ui.Select(dropdown).selectByVisibleText(value);
            } catch (Exception e2) {
                log.warn("Could not select '{}' for '{}' filter", value, label);
            }
        }
    }

    // =========================================================================
    // STEP 5: Scroll to Gallery and click "Upload Media"
    // =========================================================================

    public void clickUploadMedia() {
        log.info("Scrolling to Mega Event Gallery and clicking Upload Media");

        // Scroll aggressively to find "Upload Media" button
        WebElement uploadMediaBtn = null;
        for (int i = 0; i < 30; i++) {
            try {
                uploadMediaBtn = driver.findElement(
                    By.xpath("//button[contains(text(),'Upload Media') or contains(normalize-space(),'Upload Media')]" +
                        " | //ion-button[contains(text(),'Upload Media')]" +
                        " | //*[contains(@class,'btn') and contains(text(),'Upload Media')]"));
                if (uploadMediaBtn.isDisplayed()) {
                    scrollToElement(uploadMediaBtn);
                    break;
                }
            } catch (Exception e) {
                js.executeScript("window.scrollBy(0, 500);");
                safeSleep(500);
            }
        }

        if (uploadMediaBtn == null) {
            throw new RuntimeException("Upload Media button not found after scrolling");
        }

        safeSleep(500);
        safeClick(uploadMediaBtn);
        safeSleep(2000);
        log.info("Upload Media modal opened");
    }

    // =========================================================================
    // STEP 6: Upload images in the modal
    // =========================================================================

    /**
     * Upload exactly 2 images in the "Add Media to Event Gallery" modal.
     * Flow: Upload Image 1 → Validate → Add More Image → Upload Image 2 → Validate
     */
    public void uploadImages(int count) {
        log.info("Uploading {} images", count);

        // Use files from src/test/resources/testdata/ or TestData folder
        String testDataDir = System.getProperty("user.dir") + File.separator
                + "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "testdata";
        File dir = new File(testDataDir);

        // Fallback to TestData folder
        if (!dir.exists() || dir.listFiles((d, name) -> name.toLowerCase().matches(".*\\.(jpg|jpeg|png)")) == null) {
            testDataDir = "D:\\project\\ProjectM\\TestData";
            dir = new File(testDataDir);
        }

        File[] imageFiles = dir.listFiles((d, name) -> name.toLowerCase().matches(".*\\.(jpg|jpeg|png)"));

        if (imageFiles == null || imageFiles.length < 2) {
            throw new RuntimeException("Need at least 2 images in: " + testDataDir);
        }

        log.info("Using images from: {}", testDataDir);

        // Wait for modal content
        safeSleep(2000);
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Add Media') or contains(text(),'Upload Images') or contains(text(),'Upload Videos')]")));
        } catch (Exception e) {
            safeSleep(3000);
        }

        // Upload Image 1
        String image1Path = imageFiles[0].getAbsolutePath();
        log.info("Uploading image 1: {}", imageFiles[0].getName());
        uploadSingleImage(image1Path);
        safeSleep(3000); // Wait for upload to process and preview to appear
        log.info("✅ Image 1 uploaded");

        // Click "Add More Images"
        clickAddMoreImages();

        // Upload Image 2
        String image2Path = imageFiles[1].getAbsolutePath();
        log.info("Uploading image 2: {}", imageFiles[1].getName());
        uploadSingleImage(image2Path);
        safeSleep(3000); // Wait for upload to process and preview to appear
        log.info("✅ Image 2 uploaded");

        log.info("Both images uploaded successfully");
    }

    private void uploadSingleImage(String filePath) {
        // Find the file input for images (may be hidden) — NEVER click upload area as it opens OS file dialog
        List<WebElement> fileInputs = driver.findElements(By.cssSelector("input[type='file'][accept*='image']"));

        if (fileInputs.isEmpty()) {
            fileInputs = driver.findElements(By.cssSelector("input[type='file']"));
        }

        if (!fileInputs.isEmpty()) {
            // Get the last file input (most recently added)
            WebElement fileInput = fileInputs.get(fileInputs.size() - 1);
            // 1. Make visible so sendKeys works
            // 2. Remove onclick/onfocus handlers that could re-open OS dialog
            js.executeScript(
                "arguments[0].style.display='block';" +
                "arguments[0].style.opacity='1';" +
                "arguments[0].style.position='relative';" +
                "arguments[0].style.height='auto';" +
                "arguments[0].style.width='auto';" +
                "arguments[0].onclick=null;" +
                "arguments[0].onfocus=null;", fileInput);
            safeSleep(300);
            // sendKeys directly sets the file — does NOT open OS dialog
            fileInput.sendKeys(filePath);
            safeSleep(1500);
            log.info("Image sent to file input: {}", new File(filePath).getName());
        } else {
            log.error("No file input found for image upload — cannot proceed without triggering OS dialog");
            throw new RuntimeException("No file input found for image upload");
        }
    }

    private void clickAddMoreImages() {
        try {
            // Find "Add More Images" link — this reveals a new file input in the DOM
            // We click the link (not a file input) so it does NOT open OS dialog
            WebElement addMore = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'Add More Image')] | //span[contains(text(),'Add More Image')] | //*[contains(text(),'Add More')]")));
            // Use JS click to avoid any browser-level file dialog side effects
            js.executeScript("arguments[0].click();", addMore);
            safeSleep(1000);
            log.info("Clicked 'Add More Images'");
        } catch (Exception e) {
            log.warn("'Add More Images' link not found, trying to find new file input directly");
        }
    }

    // =========================================================================
    // STEP 6b: Upload Video
    // =========================================================================

    /**
     * Upload a video file via "Upload Videos" button in the modal.
     */
    public void uploadVideo() {
        // Try testdata folder first, then TestData
        String videoPath = System.getProperty("user.dir") + File.separator
                + "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "testdata" + File.separator + "video mega event.mp4";
        if (!new File(videoPath).exists()) {
            videoPath = System.getProperty("user.dir") + File.separator
                    + "src" + File.separator + "test" + File.separator + "resources"
                    + File.separator + "testdata" + File.separator + "test_video.mp4";
        }
        if (!new File(videoPath).exists()) {
            videoPath = "D:\\project\\ProjectM\\TestData\\video mega event.mp4";
        }
        log.info("Uploading video: {}", videoPath);

        // Find video file input directly — do NOT click the button as it opens OS file dialog
        // Make the hidden input visible via JS and use sendKeys directly
        List<WebElement> fileInputs = driver.findElements(By.cssSelector("input[type='file'][accept*='video']"));
        if (fileInputs.isEmpty()) {
            fileInputs = driver.findElements(By.cssSelector("input[type='file']"));
        }

        if (!fileInputs.isEmpty()) {
            WebElement videoInput = fileInputs.get(fileInputs.size() - 1);
            // Make visible + remove onclick/onfocus handlers that could open OS dialog
            js.executeScript(
                "arguments[0].style.display='block';" +
                "arguments[0].style.opacity='1';" +
                "arguments[0].style.position='relative';" +
                "arguments[0].onclick=null;" +
                "arguments[0].onfocus=null;", videoInput);
            safeSleep(300);
            videoInput.sendKeys(videoPath);
            safeSleep(3000);
            log.info("✅ Video uploaded: test_video.mp4");
        } else {
            log.error("Video file input not found");
        }
    }

    // =========================================================================
    // STEP 7: Click Submit
    // =========================================================================

    public void clickSubmit() {
        log.info("Clicking Submit button");
        safeSleep(2000);

        // Submit button: class='btn btn-danger firebase-megaevent-upload-btn'
        WebElement submitBtn = new WebDriverWait(driver, Duration.ofSeconds(15)).until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("button.firebase-megaevent-upload-btn")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", submitBtn);
        safeSleep(300);
        safeClick(submitBtn);
        log.info("✅ Submit button clicked");

        // Wait for confirmation popup and click "Okay"
        safeSleep(3000);
        WebElement okayBtn = new WebDriverWait(driver, Duration.ofSeconds(15)).until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@id='page_reload']")));
        safeClick(okayBtn);
        log.info("✅ Okay button clicked — upload confirmed");
        safeSleep(2000);
    }

    public boolean isUploadSuccessful() {
        safeSleep(3000);
        // Check for success toast/message or modal close
        try {
            // If modal closed, upload was successful
            List<WebElement> modals = driver.findElements(
                By.xpath("//*[contains(text(),'Add Media to Event Gallery')]"));
            if (modals.isEmpty()) {
                log.info("Modal closed — upload successful");
                return true;
            }
        } catch (Exception e) { /* continue */ }

        // Check for success text
        String pageSource = driver.getPageSource().toLowerCase();
        if (pageSource.contains("success") || pageSource.contains("uploaded")) {
            log.info("Success indicator found in page");
            return true;
        }

        log.warn("Could not confirm upload success");
        return false;
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private void safeSleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
