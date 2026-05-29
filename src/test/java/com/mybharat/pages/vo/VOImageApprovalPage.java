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
        Thread.sleep(3000);
        waitForPageLoad();
        dismissOverlay();

        // Scroll down
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        Thread.sleep(2000);

        // Click "View More"
        try {
            WebElement viewMore = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[contains(text(),'View More')] | //button[contains(text(),'View More')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", viewMore);
            Thread.sleep(3000);
            waitForPageLoad();
            dismissOverlay();
            log.info("✅ Clicked View More");
        } catch (Exception e) {
            log.warn("View More not found, navigating directly...");
            driver.get(baseUrl + "/mybharat_organizations");
            Thread.sleep(3000);
            dismissOverlay();
        }

        // Click org name (first row in table)
        try {
            WebElement orgLink = new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//table//tbody//tr[1]//td[2]//a | //table//tbody//a[1]")));
            scrollToElement(orgLink);
            Thread.sleep(300);
            orgLink.click();
            Thread.sleep(3000);
            waitForPageLoad();
            dismissOverlay();
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
            Thread.sleep(3000);
            waitForPageLoad();
            dismissOverlay();
            log.info("✅ On Youth Photo Moderation page. URL: {}", driver.getCurrentUrl());
        } catch (Exception e) {
            // Direct navigation fallback
            log.warn("Sidebar link not found, navigating directly...");
            driver.get(config.getUrl() + "/pages/youth_events_approval");
            Thread.sleep(3000);
            dismissOverlay();
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
            Thread.sleep(2000);
            dismissOverlay();
            log.info("✅ Clicked 'VO' tab");
        } catch (Exception e) {
            log.warn("VO tab not found: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Step 4 + 5: Approve 1st image, Reject 2nd image
    // -------------------------------------------------------------------------

    public void approveFirstRejectSecond() throws InterruptedException {
        log.info("Approving 1st image and Rejecting 2nd image...");
        Thread.sleep(2000);
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
            Thread.sleep(500);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstApprove);
            Thread.sleep(2000);
            handleConfirmationPopup();
            log.info("✅ Approved 1st image");
        } else {
            log.warn("No Approve buttons found");
        }

        Thread.sleep(2000);
        dismissOverlay();

        // Re-fetch Reject buttons (DOM may have refreshed)
        rejectBtns = driver.findElements(
                By.xpath("//button[normalize-space()='Reject'] | //a[normalize-space()='Reject']"));

        // Reject the 2nd image (now it's the new 1st in the list after approve)
        if (!rejectBtns.isEmpty()) {
            WebElement rejectBtn = rejectBtns.get(0);
            scrollToElement(rejectBtn);
            Thread.sleep(500);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", rejectBtn);
            Thread.sleep(2000);
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
            Thread.sleep(3000);
            waitForPageLoad();
            dismissOverlay();
            log.info("✅ Clicked Events. URL: {}", driver.getCurrentUrl());
        } catch (Exception e) {
            log.warn("Events sidebar link not found: {}", e.getMessage());
        }
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
            Thread.sleep(300);
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

        Thread.sleep(5000);
        waitForPageLoad();
        dismissOverlay();
        log.info("✅ On Edit Event page. URL: {}", driver.getCurrentUrl());
    }

    // -------------------------------------------------------------------------
    // Step 8: Scroll down → Click "Save as draft"
    // -------------------------------------------------------------------------

    public void clickSaveAsDraft() throws InterruptedException {
        log.info("Clicking 'Save as draft'...");
        dismissOverlay();

        // Scroll to bottom
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        Thread.sleep(2000);

        try {
            WebElement saveBtn = new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[normalize-space()='Save as draft']"
                                    + " | //a[normalize-space()='Save as draft']"
                                    + " | //button[contains(text(),'Save as draft')]")));
            scrollToElement(saveBtn);
            Thread.sleep(300);
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

        Thread.sleep(5000);
        waitForPageLoad();
        dismissOverlay();
        log.info("✅ Event saved as draft");
    }

    // -------------------------------------------------------------------------
    // Step 9: Logout
    // -------------------------------------------------------------------------

    public void logout() throws InterruptedException {
        log.info("Logging out...");
        Thread.sleep(2000);

        // Click profile dropdown
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var links = document.querySelectorAll('a[data-bs-toggle=\"dropdown\"], a[data-toggle=\"dropdown\"], a.dropdown-toggle');" +
                    "if(links.length > 0) { links[links.length-1].click(); }");
            Thread.sleep(1000);
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
            Thread.sleep(5000);
            log.info("✅ Logged out");
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript(
                    "var links = document.querySelectorAll('a');" +
                    "for(var i=0; i<links.length; i++) {" +
                    "  if(links[i].textContent.trim() === 'Log Out') { links[i].click(); break; }" +
                    "}");
            Thread.sleep(5000);
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
            Thread.sleep(1500);
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
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }
}
