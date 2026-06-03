package com.mybharat.pages.org;

import java.io.File;
import java.time.Duration;
import java.util.List;

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
import com.mybharat.utils.ConfigReader;

/**
 * CreateYouthClubPage — Youth Club Organization creation (Ionic Angular).
 * All locators verified from DOM inspection.
 */
public class CreateYouthClubPage extends BasePage {

    private static final Logger log = LogManager.getLogger(CreateYouthClubPage.class);
    private final ConfigReader config = new ConfigReader();
    private final JavascriptExecutor js;

    // =========================================================================
    // VERIFIED LOCATORS — from DOM inspection
    // =========================================================================
    // ion-select dropdowns
    private static final String CATEGORY_SEL       = "//ion-label[contains(.,'Category')]/following::ion-select[1]";
    private static final String SUBCATEGORY_SEL    = "//ion-label[contains(.,'SubCategory') or contains(.,'Sub Category')]/following::ion-select[1]";
    private static final String NODAL_DESIG_SEL    = "//ion-label[contains(.,'Nodal Designation')]/following::ion-select[1]";
    private static final String STATE_SEL          = "//ion-label[contains(.,'State')]/following::ion-select[1]";
    private static final String DISTRICT_SEL       = "//ion-label[contains(.,'District')]/following::ion-select[1]";
    private static final String LOCAL_BODY_SEL     = "//ion-label[contains(.,'Local Body')]/following::ion-select[1]";
    private static final String ACTIVITY_SEL       = "//ion-label[contains(.,'Activity')]/following::ion-select[1]";
    private static final String SUB_ACTIVITY_SEL   = "//ion-label[contains(.,'Sub Activity')]/following::ion-select[1]";
    // Affiliation is ion-radio (NOT ion-select) — from screenshot confirmed
    private static final String AFFILIATION_SEL    = "RADIO_NOT_SELECT"; // placeholder — handled by custom method
    private static final String ROLE_SEL           = "//ion-label[contains(.,'Role')]/following::ion-select[1]";

    // ion-input fields — use PLACEHOLDER (no formcontrolname in DOM)
    private static final String NAME_INPUT         = "//input[contains(@placeholder,'organization name') or contains(@placeholder,'enter organization')]";
    private static final String ABBREVIATION_INPUT = "//input[contains(@placeholder,'abbreviation') or contains(@placeholder,'abbrevation')]";
    private static final String ADDRESS1_INPUT     = "//input[contains(@placeholder,'Building number')]";
    private static final String ADDRESS2_INPUT     = "//input[contains(@placeholder,'Area details')]";
    private static final String PINCODE_INPUT      = "//input[contains(@placeholder,'Pincode')]";
    private static final String EMAIL_INPUT        = "//input[contains(@placeholder,'MY Bharat ID')]";

    // ion-radio buttons
    private static final String URBAN_RADIO        = "//ion-radio[contains(.,'Urban')]";
    private static final String PHYSICAL_OFFICE_NO = "//ion-label[contains(.,'physical office')]/following::ion-radio-group[1]//ion-radio[contains(.,'No')]";
    private static final String FIN_ASSIST_GROUP   = "//ion-label[contains(.,'Financial Assistance')]/following::ion-radio-group[1]";
    private static final String BANK_ACCOUNT_GROUP = "//ion-label[contains(.,'Own bank account')]/following::ion-radio-group[1]";
    private static final String REGISTERED_GROUP   = "//ion-label[contains(.,'Youth Club Registered')]/following::ion-radio-group[1]";
    private static final String MOA_GROUP          = "//ion-label[contains(.,'Memorandum')]/following::ion-radio-group[1]";

    // ion-checkbox — declaration
    private static final String AGREE_CHECKBOX     = "//ion-checkbox[@formcontrolname='declarationAccepted'] | //ion-checkbox[@formcontrolname='agree_to_affiliate'] | //ion-checkbox[contains(.,'agree') or contains(.,'declare')]";

    // Buttons — search is the orange button next to the email input
    private static final String SEARCH_BTN         = "//button[contains(@class,'search') or contains(@class,'btn')]//ion-icon | //button[.//ion-icon[@name='search']]";

    public CreateYouthClubPage(WebDriver driver) {
        super(driver);
        this.js = (JavascriptExecutor) driver;
    }

    // =========================================================================
    // NAVIGATION
    // =========================================================================

    public void navigateToCreateOrg() {
        log.info("Navigating to Create Organization");
        driver.get(config.getUrl() + "/reports/public_profile");
        waitForPageLoad();
        closePopup();

        WebDriverWait wait30 = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait30.until(d -> d.getCurrentUrl().contains("public_profile") || d.getCurrentUrl().contains("mybharat"));

        // Scroll to find and click "Create Organization" button
        WebElement btn = null;
        for (int i = 0; i < 15; i++) {
            try {
                List<WebElement> btns = driver.findElements(By.xpath(
                        "//button[contains(text(),'Create Organization')] | //ion-button[contains(text(),'Create Organization')]"));
                if (!btns.isEmpty() && btns.get(0).isDisplayed()) {
                    btn = btns.get(0);
                    scrollToElement(btn);
                    break;
                }
            } catch (Exception e) { /* scroll */ }
            scrollPage(300);
            safeSleep(500);
        }
        if (btn == null) throw new RuntimeException("Create Organization button not found");

        jsClick(btn);

        // Handle new tab if opened
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> d.getCurrentUrl().contains("create-org"));
        } catch (Exception e) {
            if (driver.getWindowHandles().size() > 1) {
                java.util.ArrayList<String> tabs = new java.util.ArrayList<>(driver.getWindowHandles());
                driver.switchTo().window(tabs.get(tabs.size() - 1));
            }
        }

        wait30.until(d -> d.getCurrentUrl().contains("create-org"));
        waitForPageLoad();
        wait30.until(d -> !d.findElements(By.xpath("//ckeditor | //div[contains(@class,'ck-editor')]")).isEmpty());
        log.info("✅ Create Org page loaded: {}", driver.getCurrentUrl());
    }

    public boolean isPageLoaded() {
        return driver.getCurrentUrl().contains("create-org");
    }

    // =========================================================================
    // ABOUT SECTION — Banner, Logo, About, Next
    // =========================================================================

    public void uploadBanner() {
        log.info("Uploading banner");
        safeSleep(2000);
        injectImageAndUpdateForm(getTestImagePath(), "banner");
    }

    public void uploadLogo() {
        log.info("Uploading logo");
        safeSleep(1000);
        injectImageAndUpdateForm(getTestImagePath(), "logo");
    }

    public void enterAboutText(String text) {
        log.info("Entering About text");
        try {
            WebElement editor = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[contains(@class,'ck-editor__editable')] | //div[contains(@class,'ck-content')]")));
            scrollToElement(editor);
            safeClick(editor);
            editor.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            editor.sendKeys(Keys.DELETE);
            editor.sendKeys(text);
        } catch (Exception e) { log.warn("CKEditor not found: {}", e.getMessage()); }
    }

    public void clickNext() {
        log.info("Clicking Next");
        WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//ion-button[normalize-space()='Next'] | //ion-button[contains(.,'Next')]")));
        scrollToElement(btn);
        jsClick(btn);
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(),'Details of Organization')]")));
        log.info("✅ Basic Info section visible");
    }

    // =========================================================================
    // BASIC INFO — All ion-select and ion-input fields
    // =========================================================================

    public void selectCategory(String value) {
        log.info("Selecting Category: {}", value);
        selectIonDropdown(CATEGORY_SEL, value);
        safeSleep(5000); // Wait for SubCategory options to load via API
    }

    public void selectSubCategory(String value) {
        log.info("Selecting SubCategory: {}", value);
        // Wait for SubCategory ion-select to exist and have options (API loaded)
        WebDriverWait wait10 = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait10.until(d -> {
            try {
                WebElement sel = d.findElement(By.xpath(SUBCATEGORY_SEL));
                return sel != null;
            } catch (Exception e) { return false; }
        });
        selectIonDropdown(SUBCATEGORY_SEL, value);
        safeSleep(2000);
    }

    public void selectNodalDesignation(String value) {
        log.info("Selecting Nodal Designation: {}", value);
        safeSleep(1000);
        selectIonDropdown(NODAL_DESIG_SEL, value);
    }

    public void selectState(String value) {
        log.info("Selecting State: {}", value);
        selectIonDropdown(STATE_SEL, value);
        safeSleep(2000); // Wait for District to load
    }

    public void selectDistrict() {
        log.info("Selecting first District");
        safeSleep(2000);
        selectFirstIonDropdown(DISTRICT_SEL);
        safeSleep(1500); // Wait for Local Body to load
    }

    public void selectLocalBody() {
        log.info("Selecting first Local Body");
        safeSleep(2000);
        selectFirstIonDropdown(LOCAL_BODY_SEL);
        safeSleep(1000);
    }

    public void selectAffiliation(String value) {
        log.info("Selecting Affiliation: {}", value);
        // Affiliation with MY Bharat — ion-radio-group with Yes/No
        // Use index: first radio = Yes, second radio = No
        try {
            WebElement group = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//ion-label[contains(.,'Affiliation with MY Bharat')]/following::ion-radio-group[1]")));
            scrollToElement(group);
            List<WebElement> radios = group.findElements(By.xpath(".//ion-radio"));
            if (value.equals("No") && radios.size() >= 2) {
                jsClick(radios.get(1)); // Second = No
            } else if (radios.size() >= 1) {
                jsClick(radios.get(0)); // First = Yes
            }
            safeSleep(1000);
            log.info("  ✅ Affiliation = {}", value);
        } catch (Exception e) {
            log.warn("Affiliation radio-group not found: {}", e.getMessage());
        }
    }

    public void enterName(String name) { typeInIonInput(NAME_INPUT, name); }
    public void enterAbbreviation(String abbr) { typeInIonInput(ABBREVIATION_INPUT, abbr); }
    public void enterAddress1(String addr) { typeInIonInput(ADDRESS1_INPUT, addr); }
    public void enterAddress2(String addr) { typeInIonInput(ADDRESS2_INPUT, addr); }
    public void enterPincode(String pin) { typeInIonInput(PINCODE_INPUT, pin); }

    public void selectAreaUrban() {
        log.info("Selecting Urban");
        WebElement radio = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(URBAN_RADIO)));
        scrollToElement(radio);
        jsClick(radio);
        safeSleep(1000);
    }

    // =========================================================================
    // INFRASTRUCTURE + FINANCIAL — Radio groups
    // =========================================================================

    public void selectPhysicalOfficeNo() {
        log.info("Physical Office: No");
        // Scroll to Infrastructure section first
        try {
            WebElement section = driver.findElement(By.xpath("//*[contains(text(),'Infrastructure Information')]"));
            scrollToElement(section);
            safeSleep(1000);
        } catch (Exception e) { scrollPage(400); safeSleep(500); }

        // Click Yes radio — scroll to Infrastructure section first
        try {
            WebElement section = driver.findElement(By.xpath("//*[contains(text(),'Infrastructure Information')]"));
            scrollToElement(section);
            safeSleep(500);
        } catch (Exception e) { scrollPage(400); safeSleep(500); }

        WebElement radio = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//ion-label[contains(.,'physical office')]/following::ion-radio[2]")));
        scrollToElement(radio);
        safeSleep(300);
        jsClick(radio);
        safeSleep(1500);

        // Select "Rent-Free" from Type of Office dropdown
        try {
            WebElement option = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//*[normalize-space()='Rent-Free']")));
            // First open the dropdown
            WebElement typeDropdown = driver.findElement(By.xpath(
                    "//ion-label[contains(.,'Type of Office')]/following::ion-select[1]"));
            scrollToElement(typeDropdown);
            js.executeScript("arguments[0].click()", typeDropdown);
            safeSleep(1000);
            // Click Rent-Free option
            WebElement rentFree = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//ion-item[contains(@class,'select-interface-option')][contains(.,'Rent-Free')]")));
            rentFree.click();
            safeSleep(300);
            clickOkIfPresent();
            dismissPopover();
            safeSleep(500);
            log.info("  Type of Office = Rent-Free");
        } catch (Exception e) {
            log.warn("Type of Office selection failed: {}", e.getMessage());
        }
        log.info("  ✅ Physical Office = No");
    }

    public void selectFinancialAssistance(String value) {
        log.info("Financial Assistance: {}", value);
        // From screenshot: "Has the Club received any Financial Assistance*" is an ion-select popover
        selectIonDropdown("//ion-label[contains(.,'Financial Assistance')]/following::ion-select[1]", value);
    }

    public void selectBankAccount(String value) {
        log.info("Bank Account: {}", value);
        // "Does the Club have their Own bank account?" — ion-radio-group with Yes/No
        try {
            WebElement group = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//ion-label[contains(.,'bank account') or contains(.,'Own bank')]/following::ion-radio-group[1]")));
            scrollToElement(group);
            // Click the radio by finding it within the group — text is in shadow DOM
            // Use JS to find and click the correct radio
            List<WebElement> radios = group.findElements(By.xpath(".//ion-radio"));
            if (value.equals("No") && radios.size() >= 2) {
                jsClick(radios.get(1)); // Second radio = No
            } else if (radios.size() >= 1) {
                jsClick(radios.get(0)); // First radio = Yes
            }
            safeSleep(500);
        } catch (Exception e) {
            log.warn("Bank account radio not found: {}", e.getMessage());
        }
    }

    // =========================================================================
    // ACTIVITIES — Multi-select ion-select
    // =========================================================================

    public void selectActivities(String... activities) {
        log.info("Selecting Activities");
        WebElement sel = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(ACTIVITY_SEL)));
        scrollToElement(sel);
        js.executeScript("arguments[0].click()", sel);
        safeSleep(1500);
        for (String act : activities) {
            try {
                WebElement opt = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                        ExpectedConditions.elementToBeClickable(
                                By.xpath("//ion-item[contains(@class,'select-interface-option')][contains(.,'" + act + "')]")));
                opt.click();
                safeSleep(300);
            } catch (Exception e) { log.warn("Activity '{}' not found", act); }
        }
        // Close the multi-select popover
        clickOkIfPresent();
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        safeSleep(1000);
        dismissPopover();
    }

    public void selectSubActivities(String... subActivities) {
        log.info("Selecting Sub Activities");
        safeSleep(2000); // Wait for Sub Activity to be enabled after Activity selection
        WebElement sel = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(SUB_ACTIVITY_SEL)));
        scrollToElement(sel);
        js.executeScript("arguments[0].click()", sel);
        safeSleep(1500);
        for (String sub : subActivities) {
            try {
                WebElement opt = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                        ExpectedConditions.elementToBeClickable(
                                By.xpath("//ion-item[contains(@class,'select-interface-option')][contains(.,'" + sub + "')]")));
                opt.click();
                safeSleep(300);
            } catch (Exception e) { log.warn("Sub Activity '{}' not found", sub); }
        }
        // Close the multi-select popover
        clickOkIfPresent();
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        safeSleep(1000);
        dismissPopover();
    }

    // =========================================================================
    // ESTABLISHMENT — Radio groups
    // =========================================================================

    public void selectRegistered(String value) {
        log.info("Youth Club Registered: {}", value);
        // This is an ion-select dropdown (from screenshot: shows Yes/No in popover)
        selectIonDropdown("//ion-label[contains(.,'Youth Club Registered') or contains(.,'Registered')]/following::ion-select[1]", value);
    }

    public void selectMoA(String value) {
        log.info("MoA/Bylaws: {}", value);
        // Locator: (//ion-radio-group)[5]//ion-radio[2] for No
        try {
            WebElement radio = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("(//ion-radio-group)[5]//ion-radio[2]")));
            scrollToElement(radio);
            jsClick(radio);
            safeSleep(500);
            log.info("  MoA = No");
        } catch (Exception e) {
            log.warn("MoA radio not found: {}", e.getMessage());
        }
    }

    public void setDateOfEstablishment() {
        log.info("Setting Date of Establishment");
        try {
            WebElement dateInput = driver.findElement(By.xpath(
                    "//input[@type='date'] | //*[contains(text(),'Date of Establishment')]/following::input[1]"));
            scrollToElement(dateInput);
            safeSleep(500);
            // Use JS to set value (avoids click intercept from overlapping native-wrapper)
            String pastDate = java.time.LocalDate.now().minusYears(2).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            js.executeScript(
                    "var el = arguments[0];" +
                    "var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                    "nativeSetter.call(el, arguments[1]);" +
                    "el.dispatchEvent(new Event('input', {bubbles: true}));" +
                    "el.dispatchEvent(new Event('change', {bubbles: true}));",
                    dateInput, pastDate);
            safeSleep(500);
            log.info("  Date = {}", pastDate);
        } catch (Exception e) {
            log.warn("Date of Establishment not found: {}", e.getMessage());
        }
    }

    // =========================================================================
    // AFFILIATION + DECLARATION CHECKBOX
    // =========================================================================

    public void clickAgreeCheckbox() {
        log.info("Clicking 'We agree to affiliate' checkbox");
        try {
            WebElement cb = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//ion-checkbox[@formcontrolname='agree_to_affiliate']")));
            scrollToElement(cb);
            safeSleep(300);
            // Only click if NOT already checked
            String checked = cb.getAttribute("aria-checked");
            if (!"true".equals(checked)) {
                jsClick(cb);
                safeSleep(500);
                log.info("  ✅ Agree checkbox checked");
            } else {
                log.info("  Agree checkbox already checked — skipping");
            }
        } catch (Exception e) {
            log.warn("  Agree checkbox not found: {}", e.getMessage());
        }
    }

    public void clickDeclarationCheckbox() {
        log.info("Clicking declaration checkbox");
        js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
        safeSleep(1000);
        // Locator: //ion-checkbox[@formcontrolname='declarationAccepted']
        try {
            WebElement cb = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//ion-checkbox[@formcontrolname='declarationAccepted']")));
            scrollToElement(cb);
            safeSleep(300);
            // Only click if NOT already checked
            String checked = cb.getAttribute("aria-checked");
            if (!"true".equals(checked)) {
                jsClick(cb);
                safeSleep(500);
                log.info("  ✅ Declaration checkbox checked");
            } else {
                log.info("  Declaration checkbox already checked — skipping");
            }
        } catch (Exception e) {
            log.warn("  Declaration checkbox not found: {}", e.getMessage());
        }
    }
    // =========================================================================
    // MEMBERSHIP — Search + Add 6 members
    // =========================================================================

    public void addMembers(String[] emails) {
        log.info("Adding {} members", emails.length);
        // Roles: Do NOT use the Nodal Designation role (President is taken by creator)
        // Only assign: General Secretary(1), Treasurer(1), Member(rest)
        String[] roles = {"General Secretary", "Treasurer", "Member", "Member", "Member", "Member"};

        // Scroll to Membership section
        try {
            WebElement section = driver.findElement(By.xpath("//*[contains(text(),'Membership Details')]"));
            scrollToElement(section);
            safeSleep(1000);
        } catch (Exception e) { scrollPage(500); }

        int addedCount = 0;
        int roleIndex = 0;
        for (int i = 0; i < emails.length && addedCount < 6; i++) {
            String email = emails[i];
            String role = roleIndex < roles.length ? roles[roleIndex] : "Member";
            log.info("  Trying member {}: {} ({})", addedCount + 1, email, role);

            // Enter email in the search input
            WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(EMAIL_INPUT)));
            scrollToElement(input);
            safeSleep(300);
            input.clear();
            input.sendKeys(email);
            input.sendKeys(Keys.TAB);
            safeSleep(1000);

            // Click the orange search button
            safeSleep(500);
            try {
                WebElement searchBtn = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                        ExpectedConditions.elementToBeClickable(
                                By.xpath("//ion-button[@color='myb']")));
                scrollToElement(searchBtn);
                jsClick(searchBtn);
            } catch (Exception searchEx) {
                // Fallback: JS click
                js.executeScript(
                        "var btns = document.querySelectorAll('ion-button[color=myb]');" +
                        "if(btns.length > 0) btns[btns.length-1].click();");
            }
            log.info("  Search button clicked");
            safeSleep(2000);

            // Check for alert: "User has already been invited..."
            try {
                driver.switchTo().alert().accept();
                log.warn("  Member {} already invited — skipping email: {}", i + 1, email);
                safeSleep(1000);
                continue; // Skip to next email
            } catch (Exception noAlert) {
                // No alert — member was added successfully
            }
            safeSleep(2000); // Wait for member row to appear

            // For all 6 members: Send OTP → Get from Yopmail → Enter → Verify
            if (addedCount < 6) {
                try {
                    // Find "Send OTP" element first (even if not visible)
                    WebElement sendOtp = null;
                    // Try different locators for Send OTP
                    String[] sendOtpXpaths = {
                        "(//ion-button[contains(.,'Send OTP')])[last()]",
                        "(//ion-text[contains(.,'Send OTP')])[last()]",
                        "(//span[contains(.,'Send OTP')])[last()]",
                        "(//*[normalize-space()='Send OTP'])[last()]",
                        "(//*[contains(text(),'Send OTP')])[last()]"
                    };
                    for (String xpath : sendOtpXpaths) {
                        try {
                            sendOtp = new WebDriverWait(driver, Duration.ofSeconds(5))
                                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
                            log.info("  Send OTP found with xpath: {}", xpath);
                            break;
                        } catch (Exception ex) { /* try next */ }
                    }
                    if (sendOtp == null) {
                        // Debug: log all buttons/text on page to identify the element
                        java.util.List<WebElement> allBtns = driver.findElements(By.xpath("//ion-button | //ion-text | //span[contains(.,'OTP')]"));
                        log.warn("  Send OTP not found! Visible OTP-related elements ({}):", allBtns.size());
                        for (WebElement btn : allBtns) {
                            try {
                                if (btn.isDisplayed()) log.warn("    Tag:{} Text:'{}' Class:'{}'",
                                        btn.getTagName(), btn.getText().trim(), btn.getAttribute("class"));
                            } catch (Exception ignored) {}
                        }
                        throw new RuntimeException("Send OTP button not found for member " + (i + 1));
                    }
                    // Scroll specifically to the Send OTP element
                    js.executeScript("arguments[0].scrollIntoView({block:'center',behavior:'instant'});", sendOtp);
                    safeSleep(500);

                    // BEFORE clicking Send OTP — capture current inbox count
                    String mailbox = email.split("@")[0];
                    int prevCount = 0;
                    try {
                        org.apache.hc.client5.http.impl.classic.CloseableHttpClient preClient =
                                org.apache.hc.client5.http.impl.classic.HttpClients.createDefault();
                        org.apache.hc.client5.http.classic.methods.HttpPost preReq =
                                new org.apache.hc.client5.http.classic.methods.HttpPost("https://api.maildrop.cc/graphql");
                        preReq.setHeader("Content-Type", "application/json");
                        preReq.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(
                                "{\"query\":\"{ inbox(mailbox:\\\"" + mailbox + "\\\") { id } }\"}"));
                        String preResp = org.apache.hc.core5.http.io.entity.EntityUtils.toString(
                                preClient.execute(preReq).getEntity());
                        prevCount = new com.fasterxml.jackson.databind.ObjectMapper()
                                .readTree(preResp).path("data").path("inbox").size();
                        preClient.close();
                    } catch (Exception preEx) { /* ignore */ }
                    log.info("  prevCount={} for {} — NOW clicking Send OTP", prevCount, mailbox);

                    // NOW click Send OTP
                    jsClick(sendOtp);
                    log.info("  Send OTP clicked for member {}", i + 1);
                    safeSleep(8000); // Wait for OTP email to arrive

                    String otp = "";
                    for (int otpAttempt = 1; otpAttempt <= 8; otpAttempt++) {
                        try {
                            org.apache.hc.client5.http.impl.classic.CloseableHttpClient httpClient =
                                    org.apache.hc.client5.http.impl.classic.HttpClients.createDefault();

                            // Get inbox
                            org.apache.hc.client5.http.classic.methods.HttpPost listReq =
                                    new org.apache.hc.client5.http.classic.methods.HttpPost("https://api.maildrop.cc/graphql");
                            listReq.setHeader("Content-Type", "application/json");
                            listReq.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(
                                    "{\"query\":\"{ inbox(mailbox:\\\"" + mailbox + "\\\") { id subject date } }\"}"));
                            String listResp = org.apache.hc.core5.http.io.entity.EntityUtils.toString(
                                    httpClient.execute(listReq).getEntity());

                            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            com.fasterxml.jackson.databind.JsonNode inbox = mapper.readTree(listResp).path("data").path("inbox");

                            if (inbox.size() <= prevCount) {
                                log.info("  No NEW email yet for {} (attempt {}/8, count={})", mailbox, otpAttempt, inbox.size());
                                safeSleep(3000);
                                continue;
                            }

                            // Get the NEWEST message — try index 0 first (Maildrop may return newest first)
                            String msgId = inbox.get(0).get("id").asText();
                            org.apache.hc.client5.http.classic.methods.HttpPost msgReq =
                                    new org.apache.hc.client5.http.classic.methods.HttpPost("https://api.maildrop.cc/graphql");
                            msgReq.setHeader("Content-Type", "application/json");
                            msgReq.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(
                                    "{\"query\":\"{ message(mailbox:\\\"" + mailbox + "\\\", id:\\\"" + msgId + "\\\") { id html data } }\"}"));
                            String msgResp = org.apache.hc.core5.http.io.entity.EntityUtils.toString(
                                    httpClient.execute(msgReq).getEntity());

                            com.fasterxml.jackson.databind.JsonNode msg = mapper.readTree(msgResp).path("data").path("message");
                            String body = msg.has("html") && !msg.get("html").isNull()
                                    ? msg.get("html").asText() : msg.has("data") ? msg.get("data").asText() : "";

                            httpClient.close();

                            if (!body.isEmpty()) {
                                // Extract OTP — try multiple patterns
                                // Pattern 1: "Your OTP: XXXXXX" (member verification format)
                                java.util.regex.Matcher mYourOtp = java.util.regex.Pattern.compile("Your OTP:\\s*(\\d{6})").matcher(body);
                                if (mYourOtp.find()) { otp = mYourOtp.group(1); break; }
                                // Pattern 2: "<strong>XXXXXX</strong>" (registration format)
                                java.util.regex.Matcher mStrong = java.util.regex.Pattern.compile("<strong>(\\d{6})</strong>").matcher(body);
                                if (mStrong.find()) { otp = mStrong.group(1); break; }
                                // Pattern 3: "OTP is XXXXXX" or "is XXXXXX"
                                java.util.regex.Matcher mIs = java.util.regex.Pattern.compile("(?:OTP|is)\\s+(?:<[^>]+>)*(\\d{6})").matcher(body);
                                if (mIs.find()) { otp = mIs.group(1); break; }
                            }
                            safeSleep(3000);
                        } catch (Exception apiEx) {
                            log.warn("  Maildrop API error (attempt {}/8): {}", otpAttempt, apiEx.getMessage());
                            safeSleep(3000);
                        }
                    }
                    log.info("  OTP extracted via Maildrop API: {}", otp);

                    // Enter OTP in the OTP field (last one)
                    if (!otp.isEmpty()) {
                        List<WebElement> otpFields = driver.findElements(By.xpath(
                                "//ion-input[@formcontrolname='myBharat_otp']//input"));
                        if (!otpFields.isEmpty()) {
                            WebElement otpInput = otpFields.get(otpFields.size() - 1);
                            scrollToElement(otpInput);
                            otpInput.clear();
                            otpInput.sendKeys(otp);
                            safeSleep(500);

                            // Click "Verify OTP" button — handles ion-button, ion-text, span
                            WebElement verifyOtp = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                                    ExpectedConditions.elementToBeClickable(
                                            By.xpath("(//ion-button[contains(.,'Verify OTP')] | //ion-text[contains(.,'Verify OTP')] | //span[contains(.,'Verify OTP')] | //*[normalize-space()='Verify OTP'])[last()]")));
                            jsClick(verifyOtp);
                            safeSleep(2000);
                            // Wait for "OTP Verified" status
                            try {
                                new WebDriverWait(driver, Duration.ofSeconds(8)).until(
                                        d -> d.getPageSource().contains("OTP Verified") || d.getPageSource().contains("otp_verified"));
                                log.info("  ✅ OTP verified for member {}", i + 1);
                            } catch (Exception ev) {
                                log.warn("  OTP verified click done but status not confirmed for member {}", i + 1);
                            }
                        }
                    }
                } catch (Exception otpEx) {
                    log.warn("  OTP verification failed for member {}: {}", i + 1, otpEx.getMessage());
                }
            }

            // Select Role from the LAST Role dropdown (most recently added member)
            try {
                safeSleep(1500); // Wait for role dropdown to render
                // Find the last "-- Select Role --" dropdown and click it
                List<WebElement> roleSelects = driver.findElements(By.xpath(
                        "//ion-select[contains(.,'Select Role') or ancestor::*[contains(.,'Role')]]"));
                if (!roleSelects.isEmpty()) {
                    WebElement lastRole = roleSelects.get(roleSelects.size() - 1);
                    scrollToElement(lastRole);
                    js.executeScript("arguments[0].click()", lastRole);
                    safeSleep(1500);

                    // Click the role option
                    WebElement opt = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                            ExpectedConditions.elementToBeClickable(
                                    By.xpath("//ion-item[contains(@class,'select-interface-option')][contains(.,'" + role + "')]")));
                    opt.click();
                    safeSleep(300);
                    clickOkIfPresent();
                    dismissPopover();
                    safeSleep(500);
                    log.info("  Role selected: {}", role);
                }
            } catch (Exception e) {
                log.warn("  Role selection failed: {}", e.getMessage());
            }
            safeSleep(500);
            log.info("  ✅ Member {} added", addedCount + 1);
            addedCount++;
            roleIndex++;
        }
        log.info("✅ {} members added", addedCount);
    }

    // =========================================================================
    // PREVIEW + SUBMIT
    // =========================================================================

    public void clickPreview() {
        log.info("Clicking Preview");
        js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
        safeSleep(500);
        WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//ion-button[normalize-space()='Preview'] | //ion-button[contains(.,'Preview')]")));
        scrollToElement(btn);
        jsClick(btn);
        new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[contains(text(),'SUBMIT')] | //ion-checkbox")));
        log.info("✅ Preview loaded");

        // On preview page: click agree checkbox and download
        safeSleep(2000);
        try {
            WebElement agreeCb = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//ion-checkbox[contains(.,'agree') or contains(.,'I agree') or @formcontrolname='declarationAccepted']")));
            scrollToElement(agreeCb);
            String checked = agreeCb.getAttribute("aria-checked");
            if (!"true".equals(checked)) {
                jsClick(agreeCb);
                safeSleep(500);
                log.info("  Preview agree checkbox checked");
            }
        } catch (Exception e) {
            log.warn("  Preview agree checkbox not found: {}", e.getMessage());
        }

        // Click Download if available
        try {
            WebElement downloadBtn = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//ion-button[contains(.,'Download') or contains(.,'DOWNLOAD')]")));
            scrollToElement(downloadBtn);
            jsClick(downloadBtn);
            safeSleep(3000);
            log.info("  Download clicked");
        } catch (Exception e) {
            log.info("  Download button not found - skipping");
        }
    }

    public boolean isPreviewLoaded() {
        return !driver.findElements(By.xpath("//*[contains(text(),'SUBMIT')] | //ion-checkbox")).isEmpty();
    }

    public void finalSubmit() {
        log.info("Clicking SUBMIT");
        js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
        safeSleep(1000);
        // Try multiple locators for Submit button
        WebElement btn = null;
        String[] submitXpaths = {
            "//ion-button[contains(.,'SUBMIT')]",
            "//ion-button[contains(.,'Submit')]",
            "//ion-button[contains(.,'submit')]",
            "//button[contains(.,'SUBMIT')]",
            "//button[contains(.,'Submit')]"
        };
        for (String xpath : submitXpaths) {
            try {
                btn = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                        ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
                if (btn != null) break;
            } catch (Exception e) { /* try next */ }
        }
        if (btn == null) {
            // JS fallback
            js.executeScript(
                    "var btns = document.querySelectorAll('ion-button, button');" +
                    "for(var i=0; i<btns.length; i++) {" +
                    "  var t = (btns[i].textContent||'').trim().toLowerCase();" +
                    "  if(t.indexOf('submit') >= 0) { btns[i].click(); return; }" +
                    "}");
        } else {
            scrollToElement(btn);
            jsClick(btn);
        }
        safeSleep(3000);
        waitForPageLoad();
        log.info("✅ Submitted");
    }

    public boolean isSubmissionSuccessful() {
        String src = driver.getPageSource().toLowerCase();
        return src.contains("success") || !driver.getCurrentUrl().contains("create-org");
    }

    public void clickGoToProfile() {
        log.info("Clicking GO TO MY BHARAT PROFILE");
        safeSleep(5000);
        // Use shadow DOM approach (proven from join-org flow)
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(d ->
                    ((JavascriptExecutor) d).executeScript(
                            "var btn = document.querySelector('ion-button.button-block');" +
                            "return btn && btn.shadowRoot && " +
                            "btn.shadowRoot.querySelector('button.button-native') !== null;"
                    ).equals(true));
            WebElement nativeButton = (WebElement) js.executeScript(
                    "return document.querySelector('ion-button.button-block')" +
                    ".shadowRoot.querySelector('button.button-native')");
            js.executeScript("arguments[0].click();", nativeButton);
            safeSleep(3000);
            waitForPageLoad();
            log.info("  ✅ Navigated to Profile");
        } catch (Exception e) {
            try {
                WebElement btn = driver.findElement(
                        By.xpath("//ion-button[contains(., 'GO TO MY BHARAT PROFILE')]"));
                js.executeScript("arguments[0].click();", btn);
                safeSleep(3000);
                waitForPageLoad();
            } catch (Exception e2) {
                log.warn("GO TO PROFILE not found: {}", e2.getMessage());
            }
        }
    }

    // =========================================================================
    // CORE HELPERS
    // =========================================================================

    private void selectIonDropdown(String xpath, String optionText) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                WebElement sel = new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                        ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
                scrollToElement(sel);
                safeSleep(300);
                js.executeScript("arguments[0].click()", sel);
                safeSleep(1500);

                // Debug: log all visible options
                List<WebElement> allOpts = driver.findElements(
                        By.xpath("//ion-item[contains(@class,'select-interface-option')]"));
                log.info("  Popover options ({}): {}", allOpts.size(),
                        allOpts.stream().map(o -> o.getText().trim()).toList());

                WebElement opt = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                        ExpectedConditions.elementToBeClickable(
                                By.xpath("//ion-item[contains(@class,'select-interface-option')][contains(.,'" + optionText + "')]")));
                opt.click();
                safeSleep(300);
                clickOkIfPresent();
                dismissPopover();
                safeSleep(500);
                log.info("  ✅ Selected: {}", optionText);
                return;
            } catch (Exception e) {
                log.warn("  Attempt {}/3 failed for '{}': {}", attempt, optionText, e.getMessage());
                dismissPopover();
                safeSleep(2000);
                if (attempt == 3) throw new RuntimeException("Failed to select '" + optionText + "' from " + xpath, e);
            }
        }
    }

    private void selectFirstIonDropdown(String xpath) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                WebElement sel = new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                        ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
                scrollToElement(sel);
                safeSleep(300);
                js.executeScript("arguments[0].click()", sel);
                safeSleep(1500);

                WebElement opt = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                        ExpectedConditions.elementToBeClickable(
                                By.xpath("(//ion-item[contains(@class,'select-interface-option')])[1]")));
                opt.click();
                safeSleep(300);
                clickOkIfPresent();
                dismissPopover();
                safeSleep(500);
                log.info("  First option selected");
                return;
            } catch (Exception e) {
                log.warn("  Attempt {}/3 failed for first option: {}", attempt, e.getMessage());
                dismissPopover();
                safeSleep(1000);
                if (attempt == 3) throw new RuntimeException("Failed to select first option from " + xpath, e);
            }
        }
    }

    private void typeInIonInput(String xpath, String text) {
        WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        scrollToElement(input);
        js.executeScript("arguments[0].focus(); arguments[0].click();", input);
        safeSleep(200);
        input.clear();
        input.sendKeys(text);
        safeSleep(300);
    }

    private void waitForEnabled(String xpath) {
        safeSleep(2000); // Wait for Angular to render after previous selection
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(d -> {
            try {
                List<WebElement> els = d.findElements(By.xpath(xpath));
                if (els.isEmpty()) return false;
                WebElement el = els.get(0);
                if (!el.isDisplayed()) return false;
                String disabled = el.getAttribute("disabled");
                // Element is enabled if disabled is null or not "true"
                return disabled == null || (!disabled.equals("true") && !disabled.equals(""));
            } catch (Exception e) { return false; }
        });
        safeSleep(500);
    }

    private void clickOkIfPresent() {
        try {
            js.executeScript(
                    "var btns = document.querySelectorAll('.alert-button-group button');" +
                    "for(var i=0; i<btns.length; i++) {" +
                    "  var t = (btns[i].textContent || '').trim();" +
                    "  if(t === 'OK' || t === 'Ok') { btns[i].click(); return; }" +
                    "}");
        } catch (Exception e) { /* no OK */ }
    }

    private void dismissPopover() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2)).until(
                    ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("ion-popover")));
        } catch (Exception e) {
            try { driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE); } catch (Exception e2) { /* ok */ }
        }
    }

    private void closePopup() {
        try {
            WebElement popup = driver.findElement(By.xpath("//i[@class='fa fa-times']"));
            if (popup.isDisplayed()) popup.click();
        } catch (Exception e) { /* no popup */ }
    }

    private void safeSleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private void injectImageAndUpdateForm(String filePath, String imageType) {
        String inputId = "selenium_img_" + imageType;
        js.executeScript(
            "var existing = document.getElementById('" + inputId + "'); if(existing) existing.remove();" +
            "var inp = document.createElement('input'); inp.type='file'; inp.id='" + inputId + "';" +
            "inp.accept='image/*'; inp.style.cssText='position:fixed;top:0;left:0;z-index:99999;display:block;width:300px;height:30px;opacity:1;';" +
            "document.body.appendChild(inp);");
        WebElement fileInput = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                ExpectedConditions.presenceOfElementLocated(By.id(inputId)));
        fileInput.sendKeys(filePath);
        js.executeScript(
            "var inp = document.getElementById('" + inputId + "'); var type = '" + imageType + "';" +
            "if(inp.files && inp.files[0]) { var reader = new FileReader(); reader.onload = function(e) {" +
            "  var base64 = e.target.result; var format = inp.files[0].type.split('/')[1] || 'jpeg';" +
            "  window['__sel_' + type + '_base64'] = base64; window['__sel_' + type + '_format'] = format;" +
            "  window['__sel_' + type] = 'done'; }; reader.readAsDataURL(inp.files[0]); }");
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                "done".equals(((JavascriptExecutor) d).executeScript("return window['__sel_" + imageType + "']")));
        js.executeScript(
            "var type='" + imageType + "'; var base64=window['__sel_'+type+'_base64']; var format=window['__sel_'+type+'_format']||'jpeg';" +
            "if(!base64)return; var allEls=document.querySelectorAll('ion-content,app-org-create,ion-app,[ng-version]');" +
            "for(var i=0;i<allEls.length;i++){var ctx=allEls[i].__ngContext__;if(!ctx)continue;" +
            "for(var j=0;j<ctx.length;j++){if(ctx[j]&&typeof ctx[j]==='object'&&ctx[j].dynamicCreateForm){" +
            "var ctrl=ctx[j].dynamicCreateForm.get(type);if(ctrl){ctrl.setValue({url:base64,format:format});" +
            "ctrl.markAsDirty();ctrl.updateValueAndValidity();" +
            "if(type==='banner'){ctx[j].imageUrl=base64;ctx[j].imgUpload=true;}" +
            "if(type==='logo'){ctx[j].logoImg=base64;}" +
            "if(ctx[j].cdr)ctx[j].cdr.detectChanges();}}}}");
        js.executeScript("var el=document.getElementById('" + inputId + "');if(el)el.remove();");
        log.info("✅ {} uploaded", imageType);
    }

    private String getTestImagePath() {
        String dir = System.getProperty("user.dir") + File.separator + "src" + File.separator
                + "test" + File.separator + "resources" + File.separator + "testdata";
        File[] imgs = new File(dir).listFiles((d, n) -> n.toLowerCase().matches(".*\\.(jpg|jpeg|png)"));
        if (imgs != null && imgs.length > 0) return imgs[0].getAbsolutePath();
        String uploadDir = System.getProperty("user.dir") + File.separator + "UploadImages";
        File[] uploads = new File(uploadDir).listFiles((d, n) -> n.toLowerCase().matches(".*\\.(jpg|jpeg|png)"));
        if (uploads != null && uploads.length > 0) return uploads[0].getAbsolutePath();
        throw new RuntimeException("No test images found");
    }
}
