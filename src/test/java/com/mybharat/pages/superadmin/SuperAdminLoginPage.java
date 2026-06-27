package com.mybharat.pages.superadmin;

import java.io.File;
import java.io.FileInputStream;
import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * SuperAdminLoginPage — Handles SuperAdmin password-based login on MYBharat.
 *
 * Flow: Navigate → Sign In → Click "Login with Password"
 *       → Enter mobile + password → Consent → Click Login
 *
 * Reads credentials from: resources/superadmin_<env>.xlsx → "Credentials" sheet
 *   Row 1: Username (mobile number) | Password
 */
public class SuperAdminLoginPage extends BasePage {

    private static final Logger log = LogManager.getLogger(SuperAdminLoginPage.class);
    private final ConfigReader config = new ConfigReader();
    private static final int WAIT = 20;

    // =========================================================================
    // LOCATORS
    // =========================================================================

    // Step 1: Sign In link on landing page
    private static final By SIGN_IN_LINK = By.xpath("//span[normalize-space()='Sign In']");

    // Step 2: "Login with Password" link (on OTP modal)
    private static final By LOGIN_WITH_PASSWORD_LINK = By.xpath(
            "//a[contains(text(),'Login with Password')] | //span[contains(text(),'Login with Password')]");

    // Step 3: Mobile/Username input (password modal)
    private static final By USERNAME_INPUT = By.xpath(
            "//input[@id='otp_login_header'] | //input[contains(@placeholder,'Enter here')]");

    // Step 4: Password input
    private static final By PASSWORD_INPUT = By.xpath(
            "//input[@type='password'] | //input[contains(@placeholder,'Enter password')]");

    // Step 5: Consent checkbox
    private static final By CONSENT_CHECKBOX = By.cssSelector("#consentCheck1");

    // Step 6: Login button
    private static final By LOGIN_BUTTON = By.xpath(
            "//button[normalize-space()='Login'] | //button[contains(@class,'firebase-user-login-btn')]");

    // Post-login verification
    private static final By USER_MENU = By.xpath(
            "//button[@class='flex items-center rounded-full cursor-pointer']");

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    public SuperAdminLoginPage(WebDriver driver) {
        super(driver);
    }

    // =========================================================================
    // PUBLIC METHODS
    // =========================================================================

    /**
     * Full SuperAdmin login flow.
     * Reads credentials from superadmin_<env>.xlsx automatically.
     */
    public void loginAsSuperAdmin() throws InterruptedException {
        String[] credentials = readCredentialsFromExcel();
        String username = credentials[0];
        String password = credentials[1];

        log.info("SuperAdmin login with username: {}", username);

        // Navigate to home page
        driver.get(config.getUrl());
        waitForPageLoad();
        safeSleep(2000);

        // Close popup if present
        closePopupIfPresent();

        // Click Sign In
        clickSignIn();

        // Click "Login with Password" link
        clickLoginWithPassword();

        // Enter username
        enterUsername(username);

        // Enter password
        enterPassword(password);

        // Check consent
        clickConsent();

        // Click Login
        clickLoginButton();

        // Wait for login to complete
        waitForPageLoad();
        safeSleep(2000);

        log.info("✅ SuperAdmin login completed");
    }

    /**
     * Verify login was successful.
     */
    public boolean isLoginSuccessful() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.presenceOfElementLocated(USER_MENU));
            log.info("Login verified — user menu found");
            return true;
        } catch (Exception e) {
            String url = driver.getCurrentUrl();
            if (url.contains("dashboard") || url.contains("profile") || url.contains("home")) {
                log.info("Login verified — URL: {}", url);
                return true;
            }
            log.warn("Login verification failed");
            return false;
        }
    }

    // =========================================================================
    // PRIVATE STEPS
    // =========================================================================

    private void closePopupIfPresent() {
        try {
            WebElement popup = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//i[@class='fa fa-times']")));
            popup.click();
            safeSleep(300);
        } catch (Exception e) { /* no popup */ }
    }

    private void clickSignIn() {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(WAIT));
        try {
            WebElement signIn = longWait.until(ExpectedConditions.elementToBeClickable(SIGN_IN_LINK));
            signIn.click();
        } catch (Exception e) {
            jsClick(driver.findElement(SIGN_IN_LINK));
        }
        log.info("Clicked Sign In");
        safeSleep(500);
    }

    private void clickLoginWithPassword() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT));
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(LOGIN_WITH_PASSWORD_LINK));
        jsClick(link);
        log.info("Clicked 'Login with Password'");
        safeSleep(1000);
    }

    private void enterUsername(String username) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT));
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));
        input.clear();
        input.sendKeys(username);
        log.info("Username entered");
    }

    private void enterPassword(String password) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT));
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(PASSWORD_INPUT));
        input.clear();
        input.sendKeys(password);
        log.info("Password entered");
    }

    private void clickConsent() {
        try {
            // Password modal uses consentCheck2 (not consentCheck1 which is OTP modal)
            WebElement consent = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//input[@id='consentCheck2' and @type='checkbox']")));
            if (!consent.isSelected()) {
                jsClick(consent);
                safeSleep(500);
                log.info("Consent checked (consentCheck2)");
            }
        } catch (Exception e) {
            log.warn("Consent checkbox not found — continuing");
        }
    }

    private void clickLoginButton() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT));
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON));
        jsClick(btn);
        log.info("Clicked Login button");
        safeSleep(500);
    }

    // =========================================================================
    // EXCEL HELPER
    // =========================================================================

    /**
     * Read SuperAdmin credentials from superadmin_<env>.xlsx → "Credentials" sheet.
     * Returns [username, password].
     */
    private String[] readCredentialsFromExcel() {
        String env = config.getEnv();
        String filePath = System.getProperty("user.dir") + File.separator
                + "resources" + File.separator + "superadmin_" + env + ".xlsx";

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheet("Credentials");
            if (sheet == null) sheet = wb.getSheetAt(0);
            Row row = sheet.getRow(1); // Row 1 = data (Row 0 = header)
            String username = row.getCell(0).getStringCellValue().trim();
            String password = row.getCell(1).getStringCellValue().trim();
            log.info("Read SuperAdmin credentials for env: {}", env);
            return new String[]{username, password};
        } catch (Exception e) {
            throw new RuntimeException("Failed to read superadmin_" + env + ".xlsx: " + e.getMessage(), e);
        }
    }

    private void safeSleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
