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
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * LoginPage - Handles the Youth OTP-based login flow on MYBharat.
 *
 * Flow: Home → Close popup → Click Sign In → Enter email (from Excel)
 *       → Consent → Click Login (Send OTP) → Fetch OTP from Yopmail → Enter OTP → Verify
 *
 * Uses the same Yopmail approach as registration for OTP retrieval.
 */
public class LoginPage extends BasePage {

    private static final Logger log = LogManager.getLogger(LoginPage.class);

    private final ConfigReader config = new ConfigReader();
    private static final int LONG_WAIT = 30;

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
    @CacheLookup
    private WebElement mobileEmailInput;

    @FindBy(css = "#consentCheck1")
    @CacheLookup
    private WebElement iConsentToTermsOfUse;

    @FindBy(css = "button[class='btn btn-outline-primary rounded-pill float-end w-100 login_otp_header firebase-user-sentOtp-btn mb-3']")
    @CacheLookup
    private WebElement loginBtn;

    @FindBy(css = "#otp-field-3")
    @CacheLookup
    private WebElement enterOTPField;

    @FindBy(xpath = "//button[@id='btn-otp-verify-header']")
    @CacheLookup
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
    public void fetchOTPFromYopmail() throws InterruptedException {
        log.info("Fetching OTP from Yopmail for: {}", loginEmail);

        // Wait for OTP email to arrive before opening Yopmail
        safeSleep(5000);

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
        log.info("OTP extracted: {}", otp);

        // Close Yopmail tab and switch back to main tab
        driver.switchTo().defaultContent();
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(1)).close();
        driver.switchTo().window(tabs.get(0));
        safeSleep(300);

        // Enter OTP in the login form
        WebElement otpInput = longWait.until(ExpectedConditions.visibilityOf(enterOTPField));
        otpInput.clear();
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
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(7));
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
     * Uses UserDetails_beta.xlsx or UserDetails_prod.xlsx based on -Denv property.
     * Sheet: "UserData", Column 0 = email.
     */
    private String readLastEmailFromExcel() {
        String env = System.getProperty("env", "beta");
        String filePath = System.getProperty("user.dir") + File.separator
                + "resources" + File.separator + "UserDetails_" + env + ".xlsx";

        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("UserDetails_" + env + ".xlsx not found at: " + filePath
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
            throw new RuntimeException("Failed to read UserDetails_" + env + ".xlsx: " + e.getMessage(), e);
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
