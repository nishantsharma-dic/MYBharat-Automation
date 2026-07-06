package com.mybharat.pages.youth;

import java.io.File;
import java.io.FileInputStream;
import java.time.Duration;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * LoginPage - Page Object for the Youth OTP-based login flow on MYBharat.
 *
 * Purpose: Handles the complete login flow using OTP verification. Reads the most
 *          recently registered email from an environment-specific Excel file, sends
 *          an OTP, retrieves it from Maildrop API (disposable email service), and verifies it.
 *
 * Flow:
 *   1. navigateToHomePage()    — opens the MYBharat home page
 *   2. closePopupIfPresent()   — dismisses quiz/announcement popup
 *   3. clickSignIn()           — clicks the "Sign In" link
 *   4. enterEmailForOTPLogin() — reads email from Excel, enters in login form
 *   5. clickConsentCheckbox()  — checks the terms consent checkbox
 *   6. clickLoginToSendOTP()   — clicks Login button to trigger OTP delivery
 *   7. fetchOTPFromYopmail()   — opens Maildrop API in new tab, extracts OTP, enters it
 *   8. clickVerifyOTP()        — submits OTP for verification
 *   9. isLoginSuccessful()     — validates login by checking post-login UI elements
 *
 * Data Source: Youth_beta.xlsx or Youth_prod.xlsx (last row = most recent registration)
 * OTP Source: Maildrop API.com (disposable email inbox)
 *
 * Key Methods:
 *   - performLogin()          — convenience method that runs the full login flow
 *   - getLastRegisteredEmail() — returns the email used for login
 *   - isLoginSuccessful()     — multi-strategy login verification (menu button, dropdown, URL)
 *
 * Environment:
 *   Beta: https://yuva-beta.mybharats.in
 *   Prod: https://mybharat.gov.in
 *
 * Dependencies: BasePage, ConfigReader, Apache POI (Excel), Maildrop API
 * Developer: Nishant Sharma (QA Team)
 *
 * @see RegistrationPage
 * @see LoginTest
 */
public class LoginPage extends BasePage {

    private static final Logger log = LogManager.getLogger(LoginPage.class);

    private final ConfigReader config = new ConfigReader();
    private static final int LONG_WAIT = Boolean.parseBoolean(System.getProperty("ciMode", "false")) ? 90 : 30;

    private String loginEmail;

    // -------------------------------------------------------------------------
    // Elements - Landing & Sign In
    // -------------------------------------------------------------------------

    @FindBy(xpath = "//i[@class='fa fa-times']")
    private WebElement closePopupBtn;

    @FindBy(xpath = "//span[normalize-space()='Sign In']")
    private WebElement signInLink;

    // -------------------------------------------------------------------------
    // Elements - OTP Login Form
    // -------------------------------------------------------------------------

    @FindBy(xpath = "//input[@id='otp_login_header']")
    private WebElement mobileEmailInput;

    @FindBy(css = "#consentCheck1")
    private WebElement iConsentToTermsOfUse;

    @FindBy(css = "button[class='btn btn-outline-primary rounded-pill float-end w-100 login_otp_header firebase-user-sentOtp-btn mb-3']")
    private WebElement loginBtn;

    @FindBy(css = "#otp-field-3")
    private WebElement enterOTPField;

    @FindBy(xpath = "//button[@id='btn-otp-verify-header']")
    private WebElement verifyOTPBtn;

    // -------------------------------------------------------------------------
    // Elements - Maildrop API (for OTP retrieval)
    // -------------------------------------------------------------------------

    @FindBy(xpath = "//input[@id='login']")
    private WebElement yopmailInbox;

    @FindBy(css = ".material-icons-outlined.f36")
    private WebElement yopmailGoBtn;

    @FindBy(xpath = "//button[@id='refresh']")
    private WebElement yopmailRefresh;

    // Post-login elements for verification
    @FindBy(xpath = "//a[contains(@class,'dropdown-toggle') and contains(@class,'nav-link')]")
    private WebElement profileDropdown;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    // -------------------------------------------------------------------------
    // Public methods
    // -------------------------------------------------------------------------

    /**
     * Navigate to the MYBharat home page and wait for it to load.
     */
    public void navigateToHomePage() {
        String url = config.getUrl();
        log.info("Navigating to: {}", url);
        driver.get(url);
        waitForPageLoad();
        safeSleep(2000);
        // Verify page actually loaded (not blank/timeout) — retry once if needed
        try {
            String title = driver.getTitle();
            if (title == null || title.isEmpty() || title.contains("ERR_")) {
                log.warn("Page may not have loaded (title: {}), retrying...", title);
                driver.get(url);
                waitForPageLoad();
                safeSleep(3000);
            }
        } catch (Exception e) { /* skip check */ }
    }

    /**
     * Close the quiz/announcement popup if it appears.
     * Silently continues if no popup is present.
     */
    public void closePopupIfPresent() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement popup = shortWait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//i[@class='fa fa-times']")));
            popup.click();
            log.info("Popup closed");
            safeSleep(300);
        } catch (Exception e) {
            log.info("No popup present — continuing");
        }
    }

    /**
     * Click the "Sign In" link on the landing page.
     */
    public void clickSignIn() {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(LONG_WAIT));
        try {
            WebElement signIn = longWait.until(ExpectedConditions.elementToBeClickable(signInLink));
            signIn.click();
        } catch (Exception e) {
            // Retry: refresh page and try again
            log.warn("Sign In not found, refreshing page and retrying...");
            driver.navigate().refresh();
            waitForPageLoad();
            safeSleep(3000);
            try {
                WebElement signIn = new WebDriverWait(driver, Duration.ofSeconds(30))
                        .until(ExpectedConditions.elementToBeClickable(signInLink));
                signIn.click();
            } catch (Exception e2) {
                // Final fallback: navigate directly to login page
                try {
                    jsClick(signInLink);
                } catch (Exception e3) {
                    log.warn("Sign In still not found after refresh, navigating to /login");
                    driver.get(config.getUrl() + "/login");
                    waitForPageLoad();
                    safeSleep(2000);
                }
            }
        }
        log.info("Clicked Sign In");
        safeSleep(300);
    }

    /**
     * Enter the email from Excel into the OTP login email field.
     */
    public void enterEmailForOTPLogin() {
        loginEmail = readLastEmailFromExcel();
        log.info("Using email for OTP login: {}", loginEmail);

        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(LONG_WAIT));
        WebElement input = longWait.until(ExpectedConditions.visibilityOf(mobileEmailInput));
        input.clear();
        input.sendKeys(loginEmail);
        log.info("Email entered in OTP login field");
    }

    /**
     * Click the consent/terms checkbox.
     */
    public void clickConsentCheckbox() {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(LONG_WAIT));
        try {
            WebElement consent = longWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("#consentCheck1")));
            if (!consent.isSelected()) {
                try {
                    consent.click();
                } catch (Exception e) {
                    jsClick(consent);
                }
                log.info("Consent checkbox checked");
            } else {
                log.info("Consent checkbox already checked");
            }
        } catch (Exception e) {
            log.warn("Consent checkbox not found — trying JS click on element");
            jsClick(iConsentToTermsOfUse);
        }
    }

    /**
     * Click the Login button to send OTP.
     */
    public void clickLoginToSendOTP() {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(LONG_WAIT));
        try {
            WebElement btn = longWait.until(ExpectedConditions.elementToBeClickable(loginBtn));
            btn.click();
        } catch (Exception e) {
            log.warn("Normal click on Login button failed, using JS click");
            jsClick(loginBtn);
        }
        log.info("Clicked Login button — OTP sent");
        safeSleep(300); // Wait for OTP to be sent
    }

    /**
     * Open Maildrop API in a new tab, fetch the login OTP, switch back and enter it.
     */
    /**
     * Fetch OTP from Maildrop API in new tab, extract it, and enter in login form.
     */
    public void fetchOTPFromYopmail() throws InterruptedException {
        log.info("Fetching OTP from Maildrop API for: {}", loginEmail);

        // Fetch OTP via Maildrop GraphQL API (no browser tab needed)
        String mailbox = loginEmail.split("@")[0];
        String otp = null;

        try {
            org.apache.hc.client5.http.impl.classic.CloseableHttpClient client =
                    org.apache.hc.client5.http.impl.classic.HttpClients.createDefault();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

            // Poll for new email (max 60 seconds)
            for (int attempt = 1; attempt <= 15; attempt++) {
                Thread.sleep(4000);

                org.apache.hc.client5.http.classic.methods.HttpPost listReq =
                        new org.apache.hc.client5.http.classic.methods.HttpPost("https://api.maildrop.cc/graphql");
                listReq.setHeader("Content-Type", "application/json");
                listReq.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(
                        "{\"query\":\"{ inbox(mailbox:\\\"" + mailbox + "\\\") { id } }\"}"));
                String listResp = org.apache.hc.core5.http.io.entity.EntityUtils.toString(
                        client.execute(listReq).getEntity());

                com.fasterxml.jackson.databind.JsonNode inbox = mapper.readTree(listResp).path("data").path("inbox");
                if (inbox.size() == 0) continue;

                // Get the newest message
                String msgId = inbox.get(0).get("id").asText();

                org.apache.hc.client5.http.classic.methods.HttpPost msgReq =
                        new org.apache.hc.client5.http.classic.methods.HttpPost("https://api.maildrop.cc/graphql");
                msgReq.setHeader("Content-Type", "application/json");
                msgReq.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(
                        "{\"query\":\"{ message(mailbox:\\\"" + mailbox + "\\\", id:\\\"" + msgId + "\\\") { id html } }\"}"));
                String msgResp = org.apache.hc.core5.http.io.entity.EntityUtils.toString(
                        client.execute(msgReq).getEntity());

                String html = mapper.readTree(msgResp).path("data").path("message").path("html").asText();

                // Extract OTP from <strong>XXXXXX</strong>
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("<strong>(\\d{6})</strong>").matcher(html);
                if (m.find()) {
                    otp = m.group(1);
                    break;
                }
                // Fallback pattern
                java.util.regex.Matcher m2 = java.util.regex.Pattern.compile("is\\s+(\\d{6})").matcher(html);
                if (m2.find()) {
                    otp = m2.group(1);
                    break;
                }
            }
            client.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch OTP from Maildrop API for: " + loginEmail, e);
        }

        if (otp == null) {
            throw new RuntimeException("OTP not received for: " + loginEmail);
        }

        log.info("Extracted OTP from Maildrop API: {}", otp);
        log.info("OTP extracted: {}", otp);

        // Enter OTP in the login form
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(LONG_WAIT));
        WebElement otpInput;
        try {
            otpInput = longWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#otp-field-3")));
        } catch (Exception e) {
            otpInput = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[contains(@id,'otp')]")));
        }
        otpInput.sendKeys(otp);
        log.info("OTP entered in login form");
    }

    /**
     * Click the Verify OTP button to complete login.
     */
    public void clickVerifyOTP() {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(LONG_WAIT));
        try {
            WebElement btn = longWait.until(ExpectedConditions.elementToBeClickable(verifyOTPBtn));
            btn.click();
        } catch (Exception e) {
            log.warn("Normal click on Verify OTP failed, using JS click");
            jsClick(verifyOTPBtn);
        }
        log.info("Clicked Verify OTP");
        waitForPageLoad();
        safeSleep(1500); // Wait for login to complete
    }

    /**
     * Verify that login was successful by checking for post-login elements.
     */
    public boolean isLoginSuccessful() {
        // Check 1 (priority): Look for user menu button (new React UI) — most common
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(
                    Boolean.parseBoolean(System.getProperty("ciMode", "false")) ? 30 : 7));
            shortWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//button[@class='flex items-center rounded-full cursor-pointer']")));
            log.info("Login verified — user menu button found (new UI)");
            return true;
        } catch (Exception e1) {
            // Check 2: Profile dropdown (old UI)
            try {
                WebDriverWait shortWait2 = new WebDriverWait(driver, Duration.ofSeconds(5));
                shortWait2.until(ExpectedConditions.visibilityOf(profileDropdown));
                log.info("Login verified — profile dropdown visible");
                return true;
            } catch (Exception e2) {
                // Check 3: URL contains dashboard or profile
                String currentUrl = driver.getCurrentUrl();
                if (currentUrl.contains("dashboard") || currentUrl.contains("profile")
                        || currentUrl.contains("home")) {
                    log.info("Login verified — URL indicates logged-in state: {}", currentUrl);
                    return true;
                }
            }
        }

        log.warn("Login verification failed — no logged-in indicators found");
        return false;
    }

    /**
     * Perform the complete OTP-based login flow in one call.
     * Reads email from Excel, fetches OTP from Maildrop API.
     */
    public void performLogin() throws InterruptedException {
        navigateToHomePage();
        closePopupIfPresent();
        clickSignIn();
        enterEmailForOTPLogin();
        clickConsentCheckbox();
        clickLoginToSendOTP();
        fetchOTPFromYopmail();
        clickVerifyOTP();
    }

    /**
     * Unified login — OTP based (password not supported on this branch).
     */
    public void login(String email, String password) throws InterruptedException {
        navigateToHomePage();
        closePopupIfPresent();
        if (isLoginSuccessful()) {
            log.info("Already logged in — skipping");
            return;
        }
        clickSignIn();
        enterEmailForOTPLogin(email);
        clickConsentCheckbox();
        clickLoginToSendOTP();
        fetchOTPFromYopmail();
        clickVerifyOTP();
    }

    /**
     * Login with password — delegates to OTP login for compatibility.
     * NCSNavigationTest uses this method.
     */
    public void performLoginWithPassword(String email) throws InterruptedException {
        login(email, null);
    }

    /**
     * Enter a specific email into the OTP login field.
     */
    public void enterEmailForOTPLogin(String email) {
        loginEmail = email;
        log.info("Using email for OTP login: {}", loginEmail);
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(LONG_WAIT));
        WebElement input = longWait.until(ExpectedConditions.visibilityOf(mobileEmailInput));
        input.clear();
        input.sendKeys(loginEmail);
        log.info("Email entered in OTP login field");
    }

    /**
     * Get the last registered email from Excel (for external use/logging).
     */
    public String getLastRegisteredEmail() {
        return readLastEmailFromExcel();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Extract OTP from the Maildrop API email content.
     * Looks for OTP pattern in the email body.
     */
    private String extractOTPFromEmail() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Try to find OTP text in the email — same pattern as registration
        try {
            WebElement otpElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//p[contains(text(),'OTP') or contains(text(),'otp') or contains(text(),'one-time password')]")));
            String otpText = otpElement.getText();
            // Extract numeric OTP (typically 4-6 digits)
            String otp = otpText.replaceAll("[^0-9]", "");
            // If multiple numbers, take the first 4-6 digit sequence
            if (otp.length() > 6) {
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d{4,6}").matcher(otpText);
                if (matcher.find()) {
                    otp = matcher.group();
                }
            }
            log.info("Extracted OTP from email: {}", otp);
            return otp;
        } catch (Exception e1) {
            // Fallback: try the registration OTP pattern
            try {
                WebElement otpElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//p[contains(text(),'Your one-time password')]")));
                String otpText = otpElement.getText();
                String otp = otpText.split("\\. This")[0].trim().split(" is ")[1].trim();
                log.info("Extracted OTP (registration pattern): {}", otp);
                return otp;
            } catch (Exception e2) {
                // Last fallback: look for any element with digits that looks like OTP
                try {
                    WebElement body = driver.findElement(By.tagName("body"));
                    String bodyText = body.getText();
                    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\b(\\d{4,6})\\b").matcher(bodyText);
                    if (matcher.find()) {
                        String otp = matcher.group(1);
                        log.info("Extracted OTP (body scan): {}", otp);
                        return otp;
                    }
                } catch (Exception e3) {
                    log.error("Failed to extract OTP from email");
                }
            }
        }
        throw new RuntimeException("Could not extract OTP from Maildrop API email");
    }

    /**
     * Read the last entry (most recent registration) from environment-specific Excel.
     * Uses Youth_beta.xlsx or Youth_prod.xlsx based on -Denv property.
     * Sheet: "UserData", Column 0 = email.
     */
    private String readLastEmailFromExcel() {
        String env = System.getProperty("env", "beta");
        String filePath = System.getProperty("user.dir") + File.separator
                + "resources" + File.separator + "Youth_" + env + ".xlsx";

        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("Youth_" + env + ".xlsx not found at: " + filePath
                    + ". Please run registration on " + env + " first.");
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet("UserData");
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
                log.warn("Sheet 'UserData' not found, using first sheet: {}", sheet.getSheetName());
            }

            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum < 1) {
                throw new RuntimeException("No user data found in Excel. Please run registration first.");
            }

            // Scan from last row upward to find latest @maildrop.cc email (skip stale @yopmail.com)
            String email = null;
            for (int i = lastRowNum; i >= 1; i--) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null) continue;
                Cell cell = row.getCell(0);
                String val = cell.getCellType() == CellType.STRING ? cell.getStringCellValue().trim() : cell.toString().trim();
                if (!val.isEmpty() && val.contains("@maildrop.cc")) {
                    email = val;
                    log.info("Read email from Excel (row {}): {}", i, email);
                    break;
                }
            }
            // Fallback: use last row regardless of domain
            if (email == null) {
                Row lastRow = sheet.getRow(lastRowNum);
                if (lastRow != null && lastRow.getCell(0) != null) {
                    Cell emailCell = lastRow.getCell(0);
                    email = emailCell.getCellType() == CellType.STRING ? emailCell.getStringCellValue().trim() : emailCell.toString().trim();
                    log.warn("No @maildrop.cc email found, using last row: {}", email);
                } else {
                    throw new RuntimeException("Could not find email in Excel.");
                }
            }
            return email;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Youth_" + env + ".xlsx: " + e.getMessage(), e);
        }
    }

    /**
     * Safe sleep that doesn't throw checked exception.
     */
    private void safeSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
