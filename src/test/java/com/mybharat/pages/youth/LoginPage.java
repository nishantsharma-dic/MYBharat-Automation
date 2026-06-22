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
 *          an OTP, retrieves it from Yopmail (disposable email service), and verifies it.
 *
 * Flow:
 *   1. navigateToHomePage()    — opens the MYBharat home page
 *   2. closePopupIfPresent()   — dismisses quiz/announcement popup
 *   3. clickSignIn()           — clicks the "Sign In" link
 *   4. enterEmailForOTPLogin() — reads email from Excel, enters in login form
 *   5. clickConsentCheckbox()  — checks the terms consent checkbox
 *   6. clickLoginToSendOTP()   — clicks Login button to trigger OTP delivery
 *   7. fetchOTPFromYopmail()   — opens Yopmail in new tab, extracts OTP, enters it
 *   8. clickVerifyOTP()        — submits OTP for verification
 *   9. isLoginSuccessful()     — validates login by checking post-login UI elements
 *
 * Data Source: Youth_beta.xlsx or Youth_prod.xlsx (last row = most recent registration)
 * OTP Source: Yopmail.com (disposable email inbox)
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
 * Dependencies: BasePage, ConfigReader, Apache POI (Excel), Yopmail
 * Developer: Nishant Sharma (QA Team)
 *
 * @see RegistrationPage
 * @see LoginTest
 */
public class LoginPage extends BasePage {

    private static final Logger log = LogManager.getLogger(LoginPage.class);

    private final ConfigReader config = new ConfigReader();
    private static final int LONG_WAIT = Boolean.parseBoolean(System.getProperty("ciMode", "false")) ? 60 : 30;

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
    // Elements - Yopmail (for OTP retrieval)
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
        safeSleep(300);
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
            log.warn("Normal click on Sign In failed, using JS click");
            jsClick(signInLink);
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
     * Enter a specific email (not from Excel) into the OTP login email field.
     * Used when the caller wants to provide the email directly.
     *
     * @param email the email to enter
     */
    public void enterSpecificEmail(String email) {
        log.info("Using specific email for login: {}", email);

        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(LONG_WAIT));
        WebElement input = longWait.until(ExpectedConditions.visibilityOf(mobileEmailInput));
        input.clear();
        input.sendKeys(email);
        log.info("Email entered in login field: {}", email);
    }

    /**
     * Switch to "Login with Password" mode from OTP mode.
     * Clicks the password tab/link on the login form.
     */
    private void switchToPasswordLogin() {
        log.info("Switching to password login mode...");
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(LONG_WAIT));
        try {
            // Try finding "Login with Password" link/tab
            WebElement passwordTab = longWait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(text(),'Password')] | " +
                             "//button[contains(text(),'Password')] | " +
                             "//span[contains(text(),'Password')]/ancestor::a | " +
                             "//a[contains(text(),'password')] | " +
                             "//*[contains(text(),'Login with Password')] | " +
                             "//*[contains(text(),'Sign in with Password')]")));
            passwordTab.click();
            log.info("Clicked 'Login with Password' tab");
        } catch (Exception e) {
            // Try alternate: tab with id or class
            try {
                WebElement tab = driver.findElement(By.xpath(
                        "//li[contains(@class,'password')] | " +
                        "//a[@href='#password'] | " +
                        "//button[@data-tab='password'] | " +
                        "//*[@id='password-tab']"));
                tab.click();
                log.info("Clicked password tab via alternate locator");
            } catch (Exception e2) {
                log.info("No password tab found — form may already be in password mode");
            }
        }
        safeSleep(500);
    }

    /**
     * Enter password into the password input field.
     */
    private void enterPassword(String password) {
        log.info("Entering password...");
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(LONG_WAIT));
        try {
            WebElement passwordInput = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@type='password'] | " +
                             "//input[@id='password'] | " +
                             "//input[@name='password'] | " +
                             "//input[@placeholder='Password'] | " +
                             "//input[@placeholder='Enter Password'] | " +
                             "//input[@placeholder='Enter your password']")));
            passwordInput.clear();
            passwordInput.sendKeys(password);
            log.info("Password entered");
        } catch (Exception e) {
            log.error("Could not find password input field: {}", e.getMessage());
            throw new RuntimeException("Password input field not found on login form");
        }
    }

    /**
     * Click the login/submit button after entering password.
     */
    private void clickPasswordLoginButton() {
        log.info("Clicking login button...");
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(LONG_WAIT));
        try {
            WebElement loginButton = longWait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(),'Login')] | " +
                             "//button[contains(text(),'Sign In')] | " +
                             "//button[contains(text(),'Submit')] | " +
                             "//button[@type='submit'] | " +
                             "//input[@type='submit']")));
            loginButton.click();
            log.info("Login button clicked");
        } catch (Exception e) {
            // Fallback: try the existing loginBtn
            try {
                jsClick(loginBtn);
                log.info("Login button clicked via JS fallback");
            } catch (Exception e2) {
                log.error("Could not click login button: {}", e2.getMessage());
                throw new RuntimeException("Login button not found or not clickable");
            }
        }
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
     * Open Yopmail in a new tab, fetch the login OTP, switch back and enter it.
     */
    /**
     * Fetch OTP from Yopmail in new tab, extract it, and enter in login form.
     */
    public void fetchOTPFromYopmail() throws InterruptedException {
        log.info("Fetching OTP for: {}", loginEmail);

        // Wait for OTP email to arrive before opening email service
        safeSleep(5000);

        // Determine which email service to use based on email domain
        if (loginEmail != null && loginEmail.contains("@sharklasers.com")) {
            fetchOTPFromGuerrillaMail();
            return;
        }

        // Default: use Yopmail
        fetchOTPFromYopmailService();
    }

    /**
     * Fetch OTP from Yopmail (for @yopmail.com addresses).
     */
    private void fetchOTPFromYopmailService() throws InterruptedException {
        log.info("Using Yopmail for OTP retrieval: {}", loginEmail);

        // Open new tab for Yopmail
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get(config.getDummyEmailUrl());
        safeSleep(300);

        // Enter email prefix in Yopmail
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(LONG_WAIT));
        WebElement inbox = longWait.until(ExpectedConditions.visibilityOf(yopmailInbox));
        inbox.clear();
        String emailPrefix = loginEmail.split("@")[0];
        inbox.sendKeys(emailPrefix);
        safeClick(yopmailGoBtn);
        safeSleep(2000);

        // Refresh to get latest email
        safeClick(yopmailRefresh);
        safeSleep(2000);

        // Switch to mail iframe and extract OTP
        driver.switchTo().frame("ifmail");
        String otp = extractOTPFromEmail();
        log.info("OTP extracted from Yopmail: {}", otp);

        // Close Yopmail tab and switch back to main tab
        driver.switchTo().defaultContent();
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(1)).close();
        driver.switchTo().window(tabs.get(0));
        safeSleep(1000);

        // Enter OTP in the login form
        enterOTPInField(otp, longWait);
    }

    /**
     * Fetch OTP from Guerrilla Mail (for @sharklasers.com addresses).
     * Guerrilla Mail domains: sharklasers.com, guerrillamail.com, grr.la, etc.
     */
    private void fetchOTPFromGuerrillaMail() throws InterruptedException {
        log.info("Using Guerrilla Mail for OTP retrieval: {}", loginEmail);

        // Open new tab for Guerrilla Mail
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get("https://www.guerrillamail.com/");
        safeSleep(2000);

        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(LONG_WAIT));

        // Set the email address in Guerrilla Mail
        try {
            // Click the "Set" button to set a custom address
            WebElement editBtn = longWait.until(ExpectedConditions.elementToBeClickable(
                    By.id("inbox-id")));
            editBtn.click();
            safeSleep(500);

            // Clear and enter the email prefix
            WebElement emailInput = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("inbox-id")));
            emailInput.clear();
            String emailPrefix = loginEmail.split("@")[0];
            emailInput.sendKeys(emailPrefix);

            // Click "Set" to confirm
            WebElement setBtn = driver.findElement(By.cssSelector("button.save"));
            setBtn.click();
            safeSleep(2000);
        } catch (Exception e) {
            log.warn("Could not set custom address in Guerrilla Mail, trying inbox directly...");
        }

        // Wait for OTP email and refresh inbox
        safeSleep(5000);
        try {
            driver.navigate().refresh();
            safeSleep(3000);
        } catch (Exception ignored) {}

        // Click on the latest email in inbox
        String otp = null;
        try {
            WebElement latestEmail = longWait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("#email_list tr:first-child")));
            latestEmail.click();
            safeSleep(2000);

            // Extract OTP from email body
            WebElement emailBody = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id("email_body")));
            String bodyText = emailBody.getText();
            log.info("Guerrilla Mail email body: {}", bodyText);

            // Extract OTP (4-6 digit number)
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\b(\\d{4,6})\\b").matcher(bodyText);
            if (matcher.find()) {
                otp = matcher.group(1);
                log.info("OTP extracted from Guerrilla Mail: {}", otp);
            }
        } catch (Exception e) {
            log.error("Failed to extract OTP from Guerrilla Mail: {}", e.getMessage());
        }

        if (otp == null) {
            throw new RuntimeException("Could not extract OTP from Guerrilla Mail for: " + loginEmail);
        }

        // Close Guerrilla Mail tab and switch back to main tab
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(1)).close();
        driver.switchTo().window(tabs.get(0));
        safeSleep(1000);

        // Enter OTP in the login form
        enterOTPInField(otp, longWait);
    }

    /**
     * Enter OTP value into the OTP input field on the login form.
     */
    private void enterOTPInField(String otp, WebDriverWait longWait) {
        WebElement otpInput;
        try {
            otpInput = longWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#otp-field-3")));
        } catch (Exception e) {
            otpInput = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[contains(@id,'otp-field')] | //input[contains(@class,'otp')]")));
        }
        otpInput.clear();
        otpInput.sendKeys(otp);
        log.info("OTP entered in login form: {}", otp);
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
     * Reads email from Excel, fetches OTP from Yopmail.
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
     * Perform the complete OTP-based login flow with a specific email.
     * Does NOT read from Excel — uses the provided email directly.
     *
     * @param email the email address to use for OTP login
     */
    public void performLoginWithEmail(String email) throws InterruptedException {
        loginEmail = email;
        navigateToHomePage();
        closePopupIfPresent();
        clickSignIn();
        enterSpecificEmail(email);
        clickConsentCheckbox();
        clickLoginToSendOTP();
        fetchOTPFromYopmail();
        clickVerifyOTP();
    }

    /**
     * Perform login using email + password (not OTP).
     * Uses the password from config-prod.properties (password=25@March).
     *
     * Flow:
     *   1. Navigate to home page
     *   2. Close popup if present
     *   3. Click Sign In
     *   4. Enter email
     *   5. Click "Login with Password" tab/link
     *   6. Enter password
     *   7. Click Login button
     *
     * @param email the email address to use for login
     */
    public void performLoginWithPassword(String email) throws InterruptedException {
        loginEmail = email;
        String password = config.getProperty("password");
        log.info("Performing password-based login for: {} with password from config", email);

        navigateToHomePage();
        closePopupIfPresent();
        clickSignIn();
        safeSleep(1000);

        // Enter email
        enterSpecificEmail(email);
        safeSleep(500);

        // Switch to password login mode (click "Login with Password" tab/link)
        switchToPasswordLogin();
        safeSleep(500);

        // Enter password
        enterPassword(password);
        safeSleep(300);

        // Click consent checkbox
        clickConsentCheckbox();
        safeSleep(300);

        // Click login button
        clickPasswordLoginButton();
        safeSleep(3000); // Wait for login to complete

        waitForPageLoad();
        log.info("Password-based login submitted for: {}", email);
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
     * Extract OTP from the Yopmail email content.
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
        throw new RuntimeException("Could not extract OTP from Yopmail email");
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

            // Read last row, first column (email)
            Row lastRow = sheet.getRow(lastRowNum);
            if (lastRow == null) {
                for (int i = lastRowNum; i >= 1; i--) {
                    lastRow = sheet.getRow(i);
                    if (lastRow != null && lastRow.getCell(0) != null) {
                        break;
                    }
                }
            }

            if (lastRow == null || lastRow.getCell(0) == null) {
                throw new RuntimeException("Could not find email in Excel. Last row is empty.");
            }

            Cell emailCell = lastRow.getCell(0);
            String email;

            if (emailCell.getCellType() == CellType.STRING) {
                email = emailCell.getStringCellValue().trim();
            } else if (emailCell.getCellType() == CellType.NUMERIC) {
                email = String.valueOf((long) emailCell.getNumericCellValue());
            } else {
                email = emailCell.toString().trim();
            }

            if (email.isEmpty()) {
                throw new RuntimeException("Email cell is empty in Excel.");
            }

            log.info("Read email from Excel (row {}): {}", lastRowNum, email);
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
