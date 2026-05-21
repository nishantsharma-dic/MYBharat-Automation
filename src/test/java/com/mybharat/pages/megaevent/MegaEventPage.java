package com.mybharat.pages.megaevent;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * MegaEventPage - Handles the complete Mega Event creation flow.
 *
 * This is a standard PHP/CakePHP form (NOT Ionic/Angular).
 * All elements are standard HTML — input, select, textarea, checkbox.
 * Form ID: megaEventAddForm
 * Form action: /orgeventmanagement/add_mega_event
 */
public class MegaEventPage extends BasePage {

    private static final Logger log = LogManager.getLogger(MegaEventPage.class);
    private final ConfigReader config = new ConfigReader();
    private final JavascriptExecutor js;

    // File paths for uploads
    private static final String BANNER_IMAGE = "UploadImages" + File.separator + "JPG1.jpg";
    private static final String LOGO_IMAGE = "UploadImages" + File.separator + "JPG2.jpg";

    public MegaEventPage(WebDriver driver) {
        super(driver);
        this.js = (JavascriptExecutor) driver;
    }

    // =========================================================================
    // LOGIN
    // =========================================================================

    public void loginWithOTP(String email) {
        log.info("Logging in with OTP for: {}", email);

        // Step 1: Navigate to home
        driver.get(config.getUrl());
        waitForPageLoad();

        // Step 2: Close popup
        closePopup();

        // Check if already logged in — skip OTP flow if Sign In button is not present
        if (isAlreadyLoggedIn()) {
            log.info("✅ Already logged in — skipping OTP login for: {}", email);
            return;
        }

        // Step 3: Click Sign In (exact locator from existing LoginPage)
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        WebElement signIn = longWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[normalize-space()='Sign In']")));
        try { signIn.click(); } catch (Exception e) { jsClick(signIn); }

        // Step 4: Enter email in OTP login field (id="otp_login_header")
        WebElement emailInput = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("otp_login_header")));
        emailInput.clear();
        emailInput.sendKeys(email);

        // Step 5: Click consent (#consentCheck1)
        WebElement consent = longWait.until(ExpectedConditions.presenceOfElementLocated(By.id("consentCheck1")));
        if (!consent.isSelected()) { try { consent.click(); } catch (Exception e) { jsClick(consent); } }

        // Step 6: Click Login button (class="login_otp_header")
        WebElement loginBtn = longWait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button.login_otp_header")));
        try { loginBtn.click(); } catch (Exception e) { jsClick(loginBtn); }

        // Step 7: Fetch OTP from Yopmail (open new tab, same as existing framework)
        safeSleep(5000); // Wait for OTP email to arrive
        String mainWindow = driver.getWindowHandle(); // capture BEFORE opening new tab
        driver.switchTo().newWindow(org.openqa.selenium.WindowType.TAB);
        driver.get(config.getProperty("dummyEmail"));

        WebElement inbox = longWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login")));
        inbox.clear();
        inbox.sendKeys(email.split("@")[0]);
        safeClick(driver.findElement(By.cssSelector(".material-icons-outlined.f36")));
        safeSleep(2000);
        safeClick(driver.findElement(By.id("refresh")));
        safeSleep(2000);

        // Extract OTP from email frame
        driver.switchTo().frame("ifmail");
        WebElement otpElement = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//p[contains(text(),'OTP') or contains(text(),'otp') or contains(text(),'one-time password')]")));
        String otpText = otpElement.getText();
        String otp;
        try {
            otp = otpText.split("\\. This")[0].trim().split(" is ")[1].trim();
        } catch (Exception e) {
            // Fallback: extract digits
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d{4,6}").matcher(otpText);
            otp = matcher.find() ? matcher.group() : otpText.replaceAll("[^0-9]", "").substring(0, 6);
        }
        log.info("OTP extracted: {}", otp);

        // Close Yopmail tab, switch back to main window by handle (not index)
        driver.switchTo().defaultContent();
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(mainWindow)) {
                driver.switchTo().window(handle).close();
            }
        }
        driver.switchTo().window(mainWindow);

        // Step 8: Enter OTP (#otp-field-3)
        WebElement otpField = longWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("otp-field-3")));
        otpField.clear();
        otpField.sendKeys(otp);

        // Step 9: Click Verify OTP (#btn-otp-verify-header)
        WebElement verifyBtn = longWait.until(ExpectedConditions.elementToBeClickable(By.id("btn-otp-verify-header")));
        try { verifyBtn.click(); } catch (Exception e) { jsClick(verifyBtn); }

        // Wait for login to complete
        waitForPageLoad();
        safeSleep(3000);
        closePopup();
        log.info("Login successful for: {}", email);
    }

    private boolean isAlreadyLoggedIn() {
        try {
            // If "Sign In" button is NOT present within 3 seconds, user is logged in
            new WebDriverWait(driver, Duration.ofSeconds(3)).until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//span[normalize-space()='Sign In']")));
            return false; // Sign In found → not logged in
        } catch (Exception e) {
            return true; // Sign In not found → already logged in
        }
    }

    private void safeSleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // =========================================================================
    // NAVIGATION
    // =========================================================================

    public void navigateToProfile() {
        log.info("On Profile page — handling popup");
        // After login, user is already on profile page. Don't refresh.
        // Just close any popup that appears
        closeAllPopups();
        waitForPageLoad();
    }

    public void clickViewOrganization() {
        log.info("Looking for organization on profile page");
        closePopup();
        for (int i = 0; i < 5; i++) {
            try {
                WebElement viewMore = driver.findElement(By.xpath(
                        "//button[contains(text(),'View More')] | //a[contains(text(),'View More')]"));
                if (viewMore.isDisplayed()) {
                    scrollToElement(viewMore);
                    safeClick(viewMore);
                    waitForPageLoad();
                    log.info("Clicked View More");
                    return;
                }
            } catch (Exception e) { scrollPage(300); }
        }
        log.info("View More not found — org may be directly visible");
    }

    public void selectOrganization(String orgName) {
        log.info("Selecting organization: {}", orgName != null ? orgName : "first available");
        WebDriverWait wait15 = new WebDriverWait(driver, Duration.ofSeconds(15));

        WebElement orgLink = null;

        // If orgName provided, try to find it by name first
        if (orgName != null && !orgName.isEmpty()) {
            String[] xpaths = {
                "//a[contains(text(),'" + orgName + "')]",
                "//td[contains(text(),'" + orgName + "')]/ancestor::tr//a",
                "//*[contains(text(),'" + orgName + "')]"
            };
            for (String xpath : xpaths) {
                try {
                    orgLink = wait15.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
                    if (orgLink != null) break;
                } catch (Exception e) { orgLink = null; }
            }
        }

        // Fallback: click first available org link
        if (orgLink == null) {
            log.warn("Org '{}' not found or not specified — clicking first available org", orgName);
            try {
                orgLink = wait15.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("(//table//a)[1] | (//div[contains(@class,'org')]//a)[1]")));
            } catch (Exception e) {
                log.error("No organization found to click");
                return;
            }
        }

        scrollToElement(orgLink);
        safeClick(orgLink);
        waitForPageLoad();
        log.info("✅ Organization selected");
    }

    public void clickMegaEventMenu() {
        log.info("Clicking Mega Event from sidebar");
        WebDriverWait wait20 = new WebDriverWait(driver, Duration.ofSeconds(20));
        waitForPageLoad();

        // Aggressively close ALL popups/modals/overlays
        closeAllPopups();
        safeSleep(1000);
        closeAllPopups();

        WebElement megaEventLink = wait20.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[@title='Mega Event']")));
        scrollToElement(megaEventLink);
        safeClick(megaEventLink);
        waitForPageLoad();
        log.info("Mega Event page loaded");
    }

    public void clickCreateMegaEvent() {
        log.info("Clicking Create a Mega Event");
        WebDriverWait wait10 = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement createBtn = wait10.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'Create a Mega Event')] | //button[contains(text(),'Create a Mega Event')]")));
        safeClick(createBtn);
        waitForPageLoad();
        log.info("Create Mega Event form loaded");
    }

    // =========================================================================
    // FORM FILLING
    // =========================================================================

    public void uploadBanner() {
        log.info("Uploading banner image");
        String filePath = System.getProperty("user.dir") + File.separator + BANNER_IMAGE;
        WebElement fileInput = driver.findElement(By.id("fileInput"));
        js.executeScript("arguments[0].style.display='block'", fileInput);
        fileInput.sendKeys(filePath);
        log.info("Banner uploaded");
    }

    public void uploadLogo() {
        log.info("Uploading logo image");
        String filePath = System.getProperty("user.dir") + File.separator + LOGO_IMAGE;
        WebElement fileInput = driver.findElement(By.id("fileInput1"));
        js.executeScript("arguments[0].style.display='block'", fileInput);
        fileInput.sendKeys(filePath);
        log.info("Logo uploaded");
    }

    public void enterEventName(String name) {
        log.info("Entering event name: {}", name);
        WebElement input = driver.findElement(By.id("editableText"));
        scrollToElement(input);
        input.clear();
        input.sendKeys(name);
    }

    public void enterAbout(String text) {
        log.info("Entering About text");
        WebElement textarea = driver.findElement(By.id("exampleFormControlTextarea1"));
        scrollToElement(textarea);
        textarea.clear();
        textarea.sendKeys(text);
    }

    public void fillEventDates(String startDate, String startTime, String endDate, String endTime) {
        log.info("Filling Event Dates");
        scrollToText("Event Dates");

        // Start Date (datepicker — set via JS to bypass readonly)
        setDateField("start_date", startDate);

        // Start Time
        WebElement startTimeInput = driver.findElement(By.xpath("//input[@name='event_start_time']"));
        startTimeInput.sendKeys(startTime);

        // End Date
        setDateField("end_date", endDate);

        // End Time
        WebElement endTimeInput = driver.findElement(By.xpath("//input[@name='event_end_time']"));
        endTimeInput.sendKeys(endTime);
    }

    public void fillInclusionDates(String startDate, String startTime, String endDate, String endTime) {
        log.info("Filling Inclusion Dates");
        scrollToText("Event Inclusion Dates");

        setDateField("inclusion_start_date", startDate);

        WebElement startTimeInput = driver.findElement(By.id("inclusion_start_time"));
        startTimeInput.sendKeys(startTime);

        setDateField("inclusion_end_date", endDate);

        WebElement endTimeInput = driver.findElement(By.id("inclusion_end_time"));
        endTimeInput.sendKeys(endTime);
    }

    public void checkVolunteerOpportunity() {
        log.info("Checking Volunteer Opportunity");
        WebElement checkbox = driver.findElement(By.xpath("//input[@name='event_type[]'][@value='1']"));
        scrollToElement(checkbox);
        if (!checkbox.isSelected()) checkbox.click();
    }

    public void checkExperientialLearning() {
        log.info("Checking Experiential Learning");
        WebElement checkbox = driver.findElement(By.xpath("//input[@name='event_type[]'][@value='2']"));
        scrollToElement(checkbox);
        if (!checkbox.isSelected()) checkbox.click();
    }

    public void selectSpecialization() {
        log.info("Selecting Specialization");
        // Multi-select with id="specialization" — click to open, select first option
        WebElement specSelect = driver.findElement(By.id("specialization"));
        scrollToElement(specSelect);
        // This is likely a multi-select plugin — try clicking and selecting first visible option
        safeClick(specSelect);
        WebDriverWait wait5 = new WebDriverWait(driver, Duration.ofSeconds(5));
        try {
            WebElement option = wait5.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//select[@id='specialization']/option[2] | //div[contains(@class,'option')][1]")));
            safeClick(option);
        } catch (Exception e) {
            // Fallback: use Select class
            Select select = new Select(specSelect);
            if (select.getOptions().size() > 1) select.selectByIndex(1);
        }
    }

    public void selectMedium() {
        log.info("Selecting Medium");
        WebElement mediumSelect = driver.findElement(By.xpath("//select[@name='medium']"));
        scrollToElement(mediumSelect);
        Select select = new Select(mediumSelect);
        if (select.getOptions().size() > 1) select.selectByIndex(1);
    }

    public void selectFunctionalCategory() {
        log.info("Selecting Functional Category");
        WebElement catSelect = driver.findElement(By.id("area_of_interest"));
        scrollToElement(catSelect);
        Select select = new Select(catSelect);
        if (select.getOptions().size() > 1) select.selectByIndex(1);
    }

    public void selectState() {
        log.info("Selecting State");
        WebElement stateSelect = driver.findElement(By.id("state_id"));
        scrollToElement(stateSelect);
        safeClick(stateSelect);
        // Multi-select — try selecting first option
        try {
            Select select = new Select(stateSelect);
            if (select.getOptions().size() > 0) select.selectByIndex(0);
        } catch (Exception e) {
            // Custom multi-select — click first option in dropdown
            WebDriverWait wait5 = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement option = wait5.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//select[@id='state_id']/option[1] | //div[contains(@class,'option')][1]")));
            safeClick(option);
        }
    }

    public void selectDistrict() {
        log.info("Selecting District");
        WebDriverWait wait10 = new WebDriverWait(driver, Duration.ofSeconds(10));
        // Wait for districts to load after state selection
        wait10.until(d -> {
            WebElement distSelect = d.findElement(By.id("district_id"));
            return distSelect.findElements(By.tagName("option")).size() > 0;
        });

        WebElement distSelect = driver.findElement(By.id("district_id"));
        scrollToElement(distSelect);
        try {
            Select select = new Select(distSelect);
            if (select.getOptions().size() > 0) select.selectByIndex(0);
        } catch (Exception e) {
            safeClick(distSelect);
            WebElement option = wait10.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//select[@id='district_id']/option[1]")));
            safeClick(option);
        }
    }

    public void clickSave() {
        log.info("Clicking Save");
        WebElement saveBtn = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(),'Save')] | //input[@type='submit'] | //button[@type='submit'] | //a[contains(@class,'save')]")));
        scrollToElement(saveBtn);
        safeClick(saveBtn);
        waitForPageLoad();
        log.info("Mega Event saved — navigated to preview page");
    }

    public void publishMegaEvent() {
        log.info("Publishing Mega Event — clicking green tick mark");
        WebDriverWait wait15 = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Click green tick icon (fa fa-check-circle)
        WebElement greenTick = wait15.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//i[@class='fa fa-check-circle']")));
        scrollToElement(greenTick);
        safeClick(greenTick);

        // Handle confirmation popup if appears
        try {
            WebDriverWait wait5 = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement confirmBtn = wait5.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(),'Yes')] | //button[contains(text(),'Confirm')] | //button[contains(text(),'OK')] | //button[contains(text(),'Publish')]")));
            safeClick(confirmBtn);
            log.info("Confirmed publish");
        } catch (Exception e) {
            log.info("No confirmation popup — published directly");
        }

        waitForPageLoad();
        log.info("Mega Event published — navigated to listing page");
    }

    public boolean isEventInActiveTab() {
        log.info("Verifying event in Active tab");
        WebDriverWait wait10 = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Click Active tab if not already selected
        try {
            WebElement activeTab = wait10.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(text(),'Active')] | //span[contains(text(),'Active')]")));
            safeClick(activeTab);
            waitForPageLoad();
        } catch (Exception e) {
            log.info("Active tab may already be selected");
        }

        // Verify event is listed (not "No record found")
        try {
            wait10.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'No record found')]")));
            log.info("Event found in Active tab");
            return true;
        } catch (Exception e) {
            // Check if any event card/row exists
            List<WebElement> events = driver.findElements(By.xpath(
                    "//div[contains(@class,'event')] | //tr[contains(@class,'event')] | //a[contains(@class,'event')]"));
            if (!events.isEmpty()) {
                log.info("Events found in Active tab: {}", events.size());
                return true;
            }
        }
        log.warn("No events found in Active tab");
        return false;
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private void setDateField(String fieldId, String dateValue) {
        // Datepicker fields are readonly — set value via JS
        js.executeScript("document.getElementById('" + fieldId + "').value = '" + dateValue + "'");
        // Trigger change event
        js.executeScript("$('#" + fieldId + "').trigger('change')");
    }

    private void closePopup() {
        try {
            WebElement popup = driver.findElement(By.xpath("//i[@class='fa fa-times']"));
            if (popup.isDisplayed()) popup.click();
        } catch (Exception e) { /* no popup */ }
    }

    private void closeAllPopups() {
        // Close fa-times popups
        try {
            List<WebElement> closeButtons = driver.findElements(By.xpath(
                    "//i[@class='fa fa-times'] | //button[@class='close'] | //button[contains(@class,'btn-close')] | " +
                    "//button[@data-dismiss='modal'] | //button[@data-bs-dismiss='modal'] | " +
                    "//span[@aria-hidden='true'][text()='×']/parent::button | //div[contains(@class,'modal')]//button[contains(.,'Close')] | " +
                    "//div[contains(@class,'modal')]//button[contains(.,'OK')] | //div[contains(@class,'modal')]//button[contains(.,'Submit')]"));
            for (WebElement btn : closeButtons) {
                try { if (btn.isDisplayed()) btn.click(); } catch (Exception e) { /* skip */ }
            }
        } catch (Exception e) { /* no popups */ }

        // Dismiss any JS alerts
        try { driver.switchTo().alert().accept(); } catch (Exception e) { /* no alert */ }

        // Click any modal backdrop to dismiss
        try {
            WebElement backdrop = driver.findElement(By.xpath("//div[contains(@class,'modal-backdrop')]"));
            if (backdrop.isDisplayed()) js.executeScript("arguments[0].click()", backdrop);
        } catch (Exception e) { /* no backdrop */ }

        // Force hide all modals via JS
        js.executeScript("try { $('.modal').modal('hide'); } catch(e) {}");
        js.executeScript("try { document.querySelectorAll('.modal-backdrop').forEach(e => e.remove()); } catch(e) {}");
    }

    private void scrollToText(String text) {
        try {
            WebElement el = driver.findElement(By.xpath("//*[contains(text(),'" + text + "')]"));
            scrollToElement(el);
        } catch (Exception e) { scrollPage(300); }
    }
}
