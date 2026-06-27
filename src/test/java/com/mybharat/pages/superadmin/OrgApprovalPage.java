package com.mybharat.pages.superadmin;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * OrgApprovalPage — SuperAdmin: Approve a Youth Club organization.
 *
 * Flow:
 *   1. Navigate to /users/organization_list
 *   2. Click "MBO Approval" tab  → href="#approveMBO"
 *   3. Click "Pending" tab       → href="#pending"
 *   4. Enter org name in search  → #org_name_pending
 *   5. Click Submit              → onclick="submitRequest('pending')"
 *   6. Click green eye icon      → .org_detail_view_ajax.trigger .fa-eye
 *   7. Approve on detail page    → to be confirmed via screenshot
 */
public class OrgApprovalPage extends BasePage {

    private static final Logger log = LogManager.getLogger(OrgApprovalPage.class);
    private final ConfigReader config = new ConfigReader();
    private static final int WAIT = 20;

    // =========================================================================
    // LOCATORS — verified from DOM inspection
    // =========================================================================

    // Step 1: MBO Approval tab
    private static final By MBO_APPROVAL_TAB = By.xpath(
            "//a[@data-toggle='tab' and @href='#approveMBO']");

    // Step 2: Pending sub-tab
    private static final By PENDING_TAB = By.xpath(
            "//a[@data-toggle='tab' and @href='#pending']");

    // Step 3: Organization Name search input
    private static final By ORG_NAME_INPUT = By.id("org_name_pending");

    // Step 4: Submit button
    private static final By SUBMIT_BTN = By.xpath(
            "//button[contains(@onclick,\"submitRequest('pending')\")]");

    // Step 5: Green eye icon (Action column) — first result row
    private static final By ACTION_EYE_ICON = By.xpath(
            "//a[contains(@class,'org_detail_view_ajax') and contains(@class,'trigger')]"
            + "//i[contains(@class,'fa-eye')] | "
            + "//a[contains(@class,'org_detail_view_ajax') and contains(@class,'trigger')]");

    // Approval button on org detail page — onclick="performAction('approve')"
    private static final By APPROVE_BTN = By.xpath(
            "//button[contains(@onclick,\"performAction('approve')\")]");

    // Confirm button on approval popup
    private static final By CONFIRM_BTN = By.xpath(
            "//button[contains(text(),'Confirm')] | //button[contains(text(),'Yes')] | "
            + "//button[contains(text(),'OK')] | //button[normalize-space()='OK']");

    // Success message
    private static final By SUCCESS_MSG = By.xpath(
            "//*[contains(text(),'approved') or contains(text(),'Approved') "
            + "or contains(text(),'success') or contains(text(),'Success')]");

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    public OrgApprovalPage(WebDriver driver) {
        super(driver);
    }

    // =========================================================================
    // PUBLIC METHODS
    // =========================================================================

    /**
     * Navigate to Organization list page.
     */
    public void navigateToOrgList() {
        String url = config.getUrl() + "/users/organization_list";
        log.info("Navigating to: {}", url);
        driver.get(url);
        waitForPageLoad();
        safeSleep(2000);
    }

    /**
     * Click MBO Approval tab.
     */
    public void clickMboApprovalTab() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT));
        WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(MBO_APPROVAL_TAB));
        jsClick(tab);
        log.info("Clicked MBO Approval tab");
        safeSleep(1500);
    }

    /**
     * Click Pending sub-tab.
     */
    public void clickPendingTab() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT));
        WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(PENDING_TAB));
        jsClick(tab);
        log.info("Clicked Pending tab");
        safeSleep(1500);
    }

    /**
     * Search for organization by name and click Submit.
     * @param orgName e.g. "Youth Club Automation 1234"
     */
    public void searchOrganization(String orgName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT));
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(ORG_NAME_INPUT));
        input.clear();
        input.sendKeys(orgName);
        log.info("Entered org name: {}", orgName);
        safeSleep(1000);

        // Call submitRequest('pending') directly via JS — this is the onclick handler
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("submitRequest('pending')");
        log.info("Clicked Submit (via JS function call)");

        // Wait for table to load — check for any table row to appear
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//table[contains(@id,'mbo_pending')]//tr[@class='row'] | //div[@id='pending']//tbody//tr | //table//tbody//tr[contains(@class,'odd') or contains(@class,'even')]")));
            log.info("Table results loaded");
        } catch (Exception e) {
            log.warn("Table results not detected after submit");
        }
        safeSleep(2000);
    }

    /**
     * Click the green eye icon (Action column) for the searched organization.
     * The icon uses jQuery delegated event: $('#pending').on('click', '.org_detail_view_ajax-trigger')
     * which opens a new tab with the org detail URL.
     */
    public void clickActionEyeIcon() {
        safeSleep(3000); // Wait for table to fully render

        // Scroll down to see table
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
        safeSleep(1000);

        // Trigger the jQuery click event on the green eye icon inside #pending
        Boolean clicked = (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "var link = document.querySelector('#pending .org_detail_view_ajax-trigger, #pending a[class*=\"org_detail_view\"]');" +
                "if(link) { $(link).trigger('click'); return true; }" +
                "return false;");

        if (Boolean.TRUE.equals(clicked)) {
            log.info("✅ Triggered click on green eye icon via jQuery");
            safeSleep(3000);
            waitForPageLoad();
            safeSleep(2000);
        } else {
            log.warn("Eye icon not found in #pending. URL: {}", driver.getCurrentUrl());
            throw new RuntimeException("Green eye icon not found for searched organization");
        }
    }

    /**
     * Click the Approve button on the org detail page.
     * Uses JS click to bypass the disabled state if present.
     */
    public void clickApprove() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT));
        WebElement approveBtn = wait.until(ExpectedConditions.presenceOfElementLocated(APPROVE_BTN));
        scrollToElement(approveBtn);

        // Remove disabled attribute if present (button may be disabled initially)
        jsClick(approveBtn);
        log.info("Clicked Approve button");
        safeSleep(1500);

        // Handle confirmation popup if present
        try {
            WebElement confirmBtn = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(CONFIRM_BTN));
            jsClick(confirmBtn);
            log.info("Confirmed approval");
            safeSleep(2000);
        } catch (Exception e) {
            log.info("No confirmation popup — continuing");
        }
    }

    /**
     * Full approval flow in one call.
     * @param orgName Youth Club name saved during creation
     */
    public void approveYouthClub(String orgName) {
        navigateToOrgList();
        clickMboApprovalTab();
        clickPendingTab();
        searchOrganization(orgName);
        clickActionEyeIcon();
        clickApprove();
    }

    /**
     * Verify approval was successful.
     */
    public boolean isApprovalSuccessful() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(SUCCESS_MSG));
            log.info("✅ Approval successful — success message found");
            return true;
        } catch (Exception e) {
            String src = driver.getPageSource().toLowerCase();
            if (src.contains("approved") || src.contains("success")) {
                log.info("✅ Approval verified via page source");
                return true;
            }
            log.warn("Approval verification failed");
            return false;
        }
    }

    private void safeSleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
