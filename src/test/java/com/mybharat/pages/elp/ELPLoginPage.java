package com.mybharat.pages.elp;

import java.io.File;
import java.io.FileInputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * ELPLoginPage - Handles OTP-based login for ELP admin users.
 * Picks a random email from resources/Partner_beta.xlsx or Partner_prod.xlsx and logs in via OTP.
 */
public class ELPLoginPage extends BasePage {

    private static final Logger log = LogManager.getLogger(ELPLoginPage.class);
    private final ConfigReader config = new ConfigReader();
    private final Random random = new Random();

    private String loginEmail;

    public ELPLoginPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Get the email used for login (for use in subsequent tests).
     */
    public String getLoginEmail() {
        return loginEmail;
    }

    /**
     * Full login flow: navigate → sign in → OTP → verify.
     * Picks a random email from Partner_beta.xlsx or Partner_prod.xlsx.
     */
    public void loginWithRandomELPUser() throws InterruptedException {
        loginEmail = pickRandomEmailFromExcel();
        log.info("ELP Login with email: {}", loginEmail);

        navigateToHome();
        closePopupIfPresent();
        clickSignIn();
        enterEmail(loginEmail);
        clickConsent();
        clickLoginToSendOTP();
        Thread.sleep(5000); // Wait for OTP email to arrive
        fetchOTPFromYopmail(loginEmail);
        clickVerifyOTP();

        log.info("✅ ELP Login successful for: {}", loginEmail);
    }

    // -------------------------------------------------------------------------
    // Private steps
    // -------------------------------------------------------------------------

    private void navigateToHome() {
        String url = config.getUrl();
        log.info("Navigating to: {}", url);
        driver.get(url);
        waitForPageLoad();
        safeSleep(1000);
    }

    private void closePopupIfPresent() {
        try {
            WebElement popup = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//i[@class='fa fa-times']")));
            popup.click();
            safeSleep(300);
        } catch (Exception e) {
            // No popup
        }
    }

    private void clickSignIn() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//span[normalize-space()='Sign In']")));
            signIn.click();
        } catch (Exception e) {
            jsClick(driver.findElement(By.xpath("//span[normalize-space()='Sign In']")));
        }
        safeSleep(500);
    }

    private void enterEmail(String email) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='otp_login_header']")));
        input.clear();
        input.sendKeys(email);
    }

    private void clickConsent() {
        try {
            WebElement consent = driver.findElement(By.cssSelector("#consentCheck1"));
            if (!consent.isSelected()) {
                jsClick(consent);
            }
        } catch (Exception e) {
            // Consent not present or already checked
        }
    }

    private void clickLoginToSendOTP() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button.login_otp_header")));
            btn.click();
        } catch (Exception e) {
            // Fallback: find by class substring
            WebElement btn = driver.findElement(
                    By.cssSelector("button[class*='login_otp_header']"));
            jsClick(btn);
        }
        log.info("OTP sent");
    }

    private void fetchOTPFromYopmail(String email) throws InterruptedException {
        // Open Yopmail in new tab
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get(config.getDummyEmailUrl());
        safeSleep(1000);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Enter email prefix
        WebElement inbox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='login']")));
        inbox.clear();
        inbox.sendKeys(email.split("@")[0]);

        // Click Go
        driver.findElement(By.cssSelector(".material-icons-outlined.f36")).click();
        safeSleep(2000);

        // Refresh
        driver.findElement(By.xpath("//button[@id='refresh']")).click();
        safeSleep(2000);

        // Extract OTP from iframe
        driver.switchTo().frame("ifmail");
        String otp = extractOTP();
        log.info("OTP extracted: {}", otp);

        // Close Yopmail tab and switch back
        driver.switchTo().defaultContent();
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(1)).close();
        driver.switchTo().window(tabs.get(0));
        safeSleep(500);

        // Enter OTP
        WebElement otpField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#otp-field-3")));
        otpField.clear();
        otpField.sendKeys(otp);
    }

    private void clickVerifyOTP() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@id='btn-otp-verify-header']")));
            btn.click();
        } catch (Exception e) {
            jsClick(driver.findElement(By.xpath("//button[@id='btn-otp-verify-header']")));
        }
        waitForPageLoad();
        safeSleep(2000);

        // Handle submit popup if it appears after login
        dismissSubmitPopupIfPresent();
    }

    /**
     * Dismiss the submit popup that may appear after login.
     * Same popup that appears after registration.
     */
    private void dismissSubmitPopupIfPresent() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));

            // Try CSS locator (same as registration popup)
            WebElement popup = shortWait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("body > div:nth-child(1) > div:nth-child(1) > main:nth-child(2) > div:nth-child(1) > div:nth-child(1) > div:nth-child(2) > main:nth-child(2) > div:nth-child(2) > div:nth-child(1) > div:nth-child(1) > div:nth-child(3) > div:nth-child(3) > button:nth-child(1)")));
            jsClick(popup);
            log.info("✅ Dismissed submit popup (CSS)");
        } catch (Exception e1) {
            try {
                // Try XPath alternative
                WebElement popup = new WebDriverWait(driver, Duration.ofSeconds(3)).until(
                        ExpectedConditions.elementToBeClickable(
                                By.xpath("(//button[contains(@class,'bg-[#bc4717]') and contains(@class,'text-white')])[1]")));
                jsClick(popup);
                log.info("✅ Dismissed submit popup (XPath)");
            } catch (Exception e2) {
                try {
                    // Try additionalDetails button
                    WebElement popup = new WebDriverWait(driver, Duration.ofSeconds(2)).until(
                            ExpectedConditions.elementToBeClickable(
                                    By.xpath("//button[@id='btnAdditionalDetails']")));
                    jsClick(popup);
                    log.info("✅ Dismissed submit popup (additionalDetails)");
                } catch (Exception e3) {
                    log.info("No submit popup present — continuing");
                }
            }
        }

        // Handle any browser alert
        safeSleep(1000);
        try {
            driver.switchTo().alert().accept();
        } catch (Exception e) {
            // No alert
        }
    }

    private String extractOTP() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            WebElement otpElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//p[contains(text(),'OTP') or contains(text(),'otp') or contains(text(),'one-time password')]")));
            String otpText = otpElement.getText();
            if (otpText.contains(" is ") && otpText.contains(". This")) {
                return otpText.split("\\. This")[0].trim().split(" is ")[1].trim();
            }
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d{4,6}").matcher(otpText);
            if (matcher.find()) return matcher.group();
        } catch (Exception e) {
            WebElement body = driver.findElement(By.tagName("body"));
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\b(\\d{4,6})\\b").matcher(body.getText());
            if (matcher.find()) return matcher.group(1);
        }
        throw new RuntimeException("Could not extract OTP from Yopmail");
    }

    /**
     * Pick a random email from resources/Partner_beta.xlsx or Partner_prod.xlsx
     */
    private String pickRandomEmailFromExcel() {
        String env = System.getProperty("env", "beta");
        String path = System.getProperty("user.dir") + File.separator
                + "resources" + File.separator + "Partner_" + env + ".xlsx";

        try (FileInputStream fis = new FileInputStream(path);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet("ELP_Users");
            if (sheet == null) sheet = workbook.getSheetAt(0);

            List<String> emails = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && row.getCell(0) != null) {
                    String email = row.getCell(0).getStringCellValue().trim();
                    if (!email.isEmpty()) emails.add(email);
                }
            }

            if (emails.isEmpty()) throw new RuntimeException("No emails found in Partner_" + env + ".xlsx");

            String selected = emails.get(random.nextInt(emails.size()));
            log.info("Randomly picked ELP email: {}", selected);
            return selected;

        } catch (Exception e) {
            throw new RuntimeException("Failed to read Partner_" + env + ".xlsx: " + e.getMessage(), e);
        }
    }

    private void safeSleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
