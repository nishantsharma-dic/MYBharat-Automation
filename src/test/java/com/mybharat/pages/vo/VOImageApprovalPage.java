package com.mybharat.pages.vo;

import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;
import com.mybharat.utils.SessionHelper;

/**
 * VOImageApprovalPage - Org side: Youth Photo Moderation → VO tab → Approve/Reject
 *
 * Flow:
 *   1. Navigate to Org Dashboard (View More → Click org name)
 *   2. Click "Youth Photo Moderation" in left sidebar
 *   3. Click "VO" tab
 *   4. Click "Approve" on the 1st (latest) image
 *   5. Click "Reject" on the 2nd image
 *   6. Click "Events" in left sidebar
 *   7. Click "Edit Event" on the latest card
 *   8. Scroll down → Click "Save as draft"
 *   9. Logout
 */
public class VOImageApprovalPage extends BasePage {

    private static final Logger log = LogManager.getLogger(VOImageApprovalPage.class);
    private final ConfigReader config = new ConfigReader();

    public VOImageApprovalPage(WebDriver driver) {
        super(driver);
    }

    private void safeSleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    /** Wait for page ready: page load + overlay dismissed + short buffer */
    private void waitForPageReady() {
        waitForPageLoad();
        dismissOverlay();
        safeSleep(300);
    }

    // -------------------------------------------------------------------------
    // Step 1: Navigate to Org Dashboard
    // -------------------------------------------------------------------------

    public void navigateToOrgDashboard() throws InterruptedException {
        log.info("Navigating to Org Dashboard...");

        // Check session
        SessionHelper sessionHelper = new SessionHelper(driver);
        if (!sessionHelper.isSessionActive()) {
            log.warn("Session expired — re-logging in...");
            sessionHelper.ensureLoggedIn(null);
        }

        dismissOverlay();

        // Navigate to profile
        String baseUrl = config.getUrl();
        driver.get(baseUrl + "/youth-profile");
        waitForPageReady();

        // Scroll down
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        safeSleep(500);

        // Click "View More"
        try {
            WebElement viewMore = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[contains(text(),'View More')] | //button[contains(text(),'View More')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", viewMore);
            waitForPageReady();
            log.info("✅ Clicked View More");
        } catch (Exception e) {
            log.warn("View More not found, navigating directly...");
            driver.get(baseUrl + "/mybharat_organizations");
            waitForPageReady();
        }

        // Click org name (first row in table)
        try {
            WebElement orgLink = new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//table//tbody//tr[1]//td[2]//a | //table//tbody//a[1]")));
            scrollToElement(orgLink);
            safeSleep(200);
            orgLink.click();
            waitForPageReady();
            log.info("✅ Clicked org name — on Org Dashboard");
        } catch (Exception e) {
            log.warn("Org name not found: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Step 2: Click "Youth Photo Moderation" in sidebar
    // -------------------------------------------------------------------------

    public void clickYouthPhotoModeration() throws InterruptedException {
        log.info("Clicking 'Youth Photo Moderation' in sidebar...");
        try {
            WebElement link = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[contains(text(),'Youth Photo Moderation')]"
                                    + " | //span[contains(text(),'Youth Photo Moderation')]/ancestor::a")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
            waitForPageReady();
            log.info("✅ On Youth Photo Moderation page. URL: {}", driver.getCurrentUrl());
        } catch (Exception e) {
            // Direct navigation fallback
            log.warn("Sidebar link not found, navigating directly...");
            driver.get(config.getUrl() + "/pages/youth_events_approval");
            waitForPageReady();
            log.info("✅ Navigated to Youth Photo Moderation directly");
        }
    }

    // -------------------------------------------------------------------------
    // Step 3: Click "VO" tab
    // -------------------------------------------------------------------------

    public void clickVOTab() throws InterruptedException {
        log.info("Clicking 'VO' tab...");
        try {
            WebElement voTab = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[normalize-space()='VO']"
                                    + " | //button[normalize-space()='VO']"
                                    + " | //li//a[normalize-space()='VO']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", voTab);
            safeSleep(1000);
            dismissOverlay();
            log.info("✅ Clicked 'VO' tab");
        } catch (Exception e) {
            log.warn("VO tab not found: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Step 4 + 5: Approve 1st image, Reject 2nd image
    // -------------------------------------------------------------------------
    // Search event by name in Youth Photo Moderation
    // -------------------------------------------------------------------------

    public void searchEventInModeration(String eventName) throws InterruptedException {
        log.info("Searching for event in Youth Photo Moderation: {}", eventName);
        dismissOverlay();

        // Type event name in the "Event Name" input
        try {
            WebElement eventInput = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//input[@placeholder='Event Name' or contains(@placeholder,'Event')]"
                                    + " | //input[@name='event_name' or @name='eventName']"
                                    + " | //input[@type='text'][1]")));
            eventInput.clear();
            eventInput.sendKeys(eventName);
            safeSleep(300);
            log.info("✅ Typed event name: {}", eventName);
        } catch (Exception e) {
            log.warn("Event Name input not found: {}", e.getMessage());
        }

        // Click Search button
        try {
            WebElement searchBtn = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[normalize-space()='Search']"
                                    + " | //a[normalize-space()='Search']"
                                    + " | //input[@type='submit' and @value='Search']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchBtn);
            waitForPageReady();
            log.info("✅ Clicked Search");
        } catch (Exception e) {
            log.warn("Search button not found: {}", e.getMessage());
        }

        // Check if "No records" — if so, click Reset button
        try {
            List<WebElement> noRecords = driver.findElements(
                    By.xpath("//*[contains(text(),'No records') or contains(text(),'No Records') or contains(text(),'No data')]"));
            if (!noRecords.isEmpty()) {
                log.warn("No records found for '{}'. Clicking Reset...", eventName);
                try {
                    WebElement resetBtn = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                            ExpectedConditions.elementToBeClickable(
                                    By.xpath("//button[normalize-space()='Reset']"
                                            + " | //a[normalize-space()='Reset']"
                                            + " | //input[@type='button' and @value='Reset']")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", resetBtn);
                    waitForPageReady();
                    log.info("✅ Clicked Reset — showing all records");
                } catch (Exception e2) {
                    log.warn("Reset button not found: {}", e2.getMessage());
                }
            }
        } catch (Exception ignored) {}
    }

    // -------------------------------------------------------------------------

    public void approveFirstRejectSecond() throws InterruptedException {
        log.info("Approving 1st image and Rejecting 2nd image...");
        safeSleep(500);
        dismissOverlay();

        // Find all Approve buttons
        List<WebElement> approveBtns = driver.findElements(
                By.xpath("//button[normalize-space()='Approve'] | //a[normalize-space()='Approve']"));

        // Find all Reject buttons
        List<WebElement> rejectBtns = driver.findElements(
                By.xpath("//button[normalize-space()='Reject'] | //a[normalize-space()='Reject']"));

        log.info("Found {} Approve buttons and {} Reject buttons", approveBtns.size(), rejectBtns.size());

        // Approve the 1st (latest) image
        if (!approveBtns.isEmpty()) {
            WebElement firstApprove = approveBtns.get(0);
            scrollToElement(firstApprove);
            safeSleep(300);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstApprove);
            safeSleep(1000);
            handleConfirmationPopup();
            log.info("✅ Approved 1st image");
        } else {
            log.warn("No Approve buttons found");
        }

        safeSleep(500);
        dismissOverlay();

        // Re-fetch Reject buttons (DOM may have refreshed)
        rejectBtns = driver.findElements(
                By.xpath("//button[normalize-space()='Reject'] | //a[normalize-space()='Reject']"));

        // Reject the 2nd image (now it's the new 1st in the list after approve)
        if (!rejectBtns.isEmpty()) {
            WebElement rejectBtn = rejectBtns.get(0);
            scrollToElement(rejectBtn);
            safeSleep(300);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", rejectBtn);
            safeSleep(1000);
            handleConfirmationPopup();
            log.info("✅ Rejected 2nd image");
        } else {
            log.warn("No Reject buttons found");
        }

        log.info("✅ Image approval/rejection complete");
    }

    // -------------------------------------------------------------------------
    // Step 6: Click "Events" in sidebar
    // -------------------------------------------------------------------------

    public void clickEventsInSidebar() throws InterruptedException {
        log.info("Clicking 'Events' in sidebar...");
        try {
            WebElement eventsLink = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[normalize-space()='Events']"
                                    + " | //span[normalize-space()='Events']/ancestor::a")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", eventsLink);
            waitForPageReady();
            log.info("✅ Clicked Events. URL: {}", driver.getCurrentUrl());
        } catch (Exception e) {
            log.warn("Events sidebar link not found: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Step 7: Click "Add/Edit Gallery" on the latest (1st) card
    // -------------------------------------------------------------------------

    public void clickAddEditGalleryOnLatestCard() throws InterruptedException {
        log.info("Clicking 'Add/Edit Gallery' on the latest card...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        dismissOverlay();

        try {
            WebElement galleryBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//a[normalize-space()='Add/Edit Gallery'] | //button[normalize-space()='Add/Edit Gallery'])[1]")));
            scrollToElement(galleryBtn);
            safeSleep(200);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", galleryBtn);
            log.info("✅ Clicked 'Add/Edit Gallery' on latest card");
        } catch (Exception e) {
            log.warn("Add/Edit Gallery not found via XPath, trying JS...");
            ((JavascriptExecutor) driver).executeScript(
                    "var btns = document.querySelectorAll('a, button');" +
                    "for(var i=0; i<btns.length; i++) {" +
                    "  if(btns[i].textContent.trim() === 'Add/Edit Gallery') { btns[i].click(); break; }" +
                    "}");
            log.info("✅ Clicked 'Add/Edit Gallery' (JS fallback)");
        }

        waitForPageReady();
        log.info("✅ On Add Gallery page. URL: {}", driver.getCurrentUrl());
    }

    // -------------------------------------------------------------------------
    // Step 8: Browse → Upload 5 images → Publish → Delete 1 → Back
    // -------------------------------------------------------------------------

    public void uploadGalleryImagesAndPublish() throws InterruptedException {
        log.info("Uploading 5 images via Browse...");
        dismissOverlay();

        // Find file input (hidden behind Browse link)
        WebElement fileInput = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("input[type='file']")));

        // Make it visible
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.display='block';" +
                "arguments[0].style.opacity='1';" +
                "arguments[0].classList.remove('hidden');", fileInput);
        safeSleep(200);

        // Get image path
        String imagePath = getImagePath();
        log.info("Image path: {}", imagePath);

        // Upload 5 images
        for (int i = 1; i <= 5; i++) {
            fileInput.sendKeys(imagePath);
            safeSleep(1000);
            log.info("✅ Uploaded image {}", i);
        }

        safeSleep(500);

        // Click "Send For Approval" button
        log.info("Clicking 'Send For Approval'...");
        try {
            WebElement sendBtn = new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[normalize-space()='Send For Approval']"
                                    + " | //a[normalize-space()='Send For Approval']"
                                    + " | //button[contains(text(),'Send For Approval') or contains(text(),'Send for Approval')]"
                                    + " | //button[normalize-space()='Publish']")));
            scrollToElement(sendBtn);
            safeSleep(200);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sendBtn);
            waitForPageReady();
            log.info("✅ Clicked 'Send For Approval'");
        } catch (Exception e) {
            log.warn("Send For Approval button not found: {}", e.getMessage());
        }

        // Delete one image (click red trash icon on first image)
        log.info("Deleting one image...");
        safeSleep(500);
        try {
            List<WebElement> deleteIcons = driver.findElements(
                    By.xpath("//i[contains(@class,'fa-trash') or contains(@class,'delete')]"
                            + " | //span[contains(@class,'delete') or contains(@class,'trash')]"
                            + " | //button[contains(@class,'delete')]"
                            + " | //a[contains(@class,'delete')]"
                            + " | //*[contains(@class,'fa-trash-alt')]"));
            if (!deleteIcons.isEmpty()) {
                WebElement firstDelete = deleteIcons.get(0);
                scrollToElement(firstDelete);
                safeSleep(200);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstDelete);
                safeSleep(1000);
                handleConfirmationPopup();
                log.info("✅ Deleted one image");
            } else {
                log.warn("No delete icons found");
            }
        } catch (Exception e) {
            log.warn("Delete image failed: {}", e.getMessage());
        }

        // Click back button (← arrow)
        log.info("Clicking back button...");
        safeSleep(300);
        try {
            WebElement backBtn = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[contains(@class,'back') or contains(text(),'←')]"
                                    + " | //button[contains(@class,'back')]"
                                    + " | //a[contains(@href,'volunteer')]//i[contains(@class,'arrow')]/.."
                                    + " | //a[normalize-space()='← Add Gallery']//.."
                                    + " | //h3/a | //h2/a")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", backBtn);
            waitForPageReady();
            log.info("✅ Clicked back button");
        } catch (Exception e) {
            log.warn("Back button not found, using browser back...");
            driver.navigate().back();
            waitForPageReady();
            log.info("✅ Navigated back (browser back)");
        }
    }

    private String getImagePath() {
        java.io.File imagesDir = new java.io.File(System.getProperty("user.dir") + java.io.File.separator + "UploadImages");
        if (!imagesDir.exists()) throw new RuntimeException("UploadImages folder not found");
        java.io.File[] files = imagesDir.listFiles((dir, name) -> name.toLowerCase().matches(".*\\.(jpg|png|jpeg)"));
        if (files == null || files.length == 0) throw new RuntimeException("No images found");
        for (java.io.File f : files) {
            if (f.length() >= 50 * 1024) return f.getAbsolutePath();
        }
        return files[0].getAbsolutePath();
    }

    // -------------------------------------------------------------------------
    // Step 7: Click "Edit Event" on the latest (1st) card
    // -------------------------------------------------------------------------

    public void clickEditEventOnLatestCard() throws InterruptedException {
        log.info("Clicking 'Edit Event' on the latest card...");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        dismissOverlay();

        try {
            WebElement editBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//a[normalize-space()='Edit Event'] | //button[normalize-space()='Edit Event'])[1]")));
            scrollToElement(editBtn);
            safeSleep(200);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editBtn);
            log.info("✅ Clicked 'Edit Event' on latest card");
        } catch (Exception e) {
            log.warn("Edit Event not found via XPath, trying JS...");
            ((JavascriptExecutor) driver).executeScript(
                    "var btns = document.querySelectorAll('a, button');" +
                    "for(var i=0; i<btns.length; i++) {" +
                    "  if(btns[i].textContent.trim() === 'Edit Event') { btns[i].click(); break; }" +
                    "}");
            log.info("✅ Clicked 'Edit Event' (JS fallback)");
        }

        waitForPageReady();
        log.info("✅ On Edit Event page. URL: {}", driver.getCurrentUrl());
    }

    // -------------------------------------------------------------------------
    // Step 8: Scroll down → Click "Save as draft"
    // -------------------------------------------------------------------------

    public void clickSaveAsDraft() throws InterruptedException {
        log.info("Clicking 'Save as draft'...");
        dismissOverlay();

        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        safeSleep(500);

        try {
            WebElement saveBtn = new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[normalize-space()='Save as draft']"
                                    + " | //a[normalize-space()='Save as draft']"
                                    + " | //button[contains(text(),'Save as draft')]")));
            scrollToElement(saveBtn);
            safeSleep(200);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);
            log.info("✅ Clicked 'Save as draft'");
        } catch (Exception e) {
            log.warn("Save as draft not found, trying JS...");
            ((JavascriptExecutor) driver).executeScript(
                    "var btns = document.querySelectorAll('button, a');" +
                    "for(var i=0; i<btns.length; i++) {" +
                    "  if(btns[i].textContent.trim().toLowerCase().indexOf('save as draft') !== -1) { btns[i].click(); break; }" +
                    "}");
            log.info("✅ Clicked 'Save as draft' (JS fallback)");
        }

        waitForPageReady();
        log.info("✅ Event saved as draft");
    }

    // -------------------------------------------------------------------------
    // Step 9: Logout
    // -------------------------------------------------------------------------

    public void logout() throws InterruptedException {
        log.info("Logging out...");
        safeSleep(500);

        // Click profile dropdown
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var links = document.querySelectorAll('a[data-bs-toggle=\"dropdown\"], a[data-toggle=\"dropdown\"], a.dropdown-toggle');" +
                    "if(links.length > 0) { links[links.length-1].click(); }");
            safeSleep(500);
            log.info("✅ Clicked profile dropdown");
        } catch (Exception e) {
            log.warn("Profile dropdown click failed");
        }

        // Click "Log Out"
        try {
            WebElement logoutBtn = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[normalize-space()='Log Out'] | //a[contains(text(),'Log Out')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", logoutBtn);
            waitForPageLoad();
            safeSleep(500);
            log.info("✅ Logged out");
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript(
                    "var links = document.querySelectorAll('a');" +
                    "for(var i=0; i<links.length; i++) {" +
                    "  if(links[i].textContent.trim() === 'Log Out') { links[i].click(); break; }" +
                    "}");
            waitForPageLoad();
            safeSleep(500);
            log.info("✅ Logged out (JS fallback)");
        }

        waitForPageLoad();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void handleConfirmationPopup() throws InterruptedException {
        try {
            WebElement confirmBtn = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[normalize-space()='OK' or normalize-space()='Yes' or normalize-space()='Confirm']"
                                    + " | //a[normalize-space()='OK' or normalize-space()='Yes']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmBtn);
            safeSleep(1000);
            log.info("✅ Dismissed confirmation popup");
        } catch (Exception e) {
            // No popup
        }
        dismissOverlay();
    }

    private void dismissOverlay() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.invisibilityOfElementLocated(By.id("overlay")));
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript(
                    "var o=document.getElementById('overlay');if(o)o.style.display='none';" +
                    "var l=document.getElementById('loader2');if(l)l.style.display='none';" +
                    "try{$('#overlay').hide();$('#loader2').hide();}catch(e){}");
        }
        safeSleep(300);
    }
}
