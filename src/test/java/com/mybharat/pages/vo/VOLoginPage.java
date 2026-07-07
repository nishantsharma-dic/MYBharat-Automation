package com.mybharat.pages.vo;

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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * VOLoginPage - Handles OTP-based login for VO Partner/Organization users.
 *
 * Picks a random email from:
 *   - resources/VO_beta.xlsx  (when -Denv=beta)
 *   - resources/VO_prod.xlsx  (when -Denv=prod)
 *
 * Flow: Navigate → Sign In → Enter email → Consent → Send OTP
 *       → Fetch OTP from Yopmail → Enter OTP → Verify → Dismiss popup
 */
public class VOLoginPage extends BasePage {

    private static final Logger log = LogManager.getLogger(VOLoginPage.class);
    private final ConfigReader config = new ConfigReader();
    private final Random random = new Random();

    private String loginEmail;

    public VOLoginPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Returns the email used for login (shared with subsequent VO tests).
     */
    public String getLoginEmail() {
        return loginEmail;
    }

    /**
     * Full login flow: navigate → sign in → OTP → verify.
     * Uses -DvoEmail=xxx if provided, otherwise picks random from Excel.
     */
    public void loginWithRandomVOUser() throws InterruptedException {
        String overrideEmail = System.getProperty("voEmail");
        if (overrideEmail != null && !overrideEmail.trim().isEmpty()) {
            loginEmail = overrideEmail.trim();
            log.info("Using override VO email from -DvoEmail: {}", loginEmail);
        } else {
            loginEmail = pickRandomEmailFromExcel();
        }
        log.info("VO Login with email: {}", loginEmail);

        navigateToHome();
        closePopupIfPresent();
        clickSignIn();
        enterEmail(loginEmail);
        clickConsent();
        clickLoginToSendOTP();
        safeSleep(2000); // Wait for OTP email to arrive
        fetchOTPFromMaildrop(loginEmail);
        clickVerifyOTP();

        // Handle "Confirm your participation in youth club" popup if present
        dismissYouthClubInvitationPopup();

        log.info("✅ VO Login successful for: {}", loginEmail);
    }

    // -------------------------------------------------------------------------
    // Private steps
    // -------------------------------------------------------------------------

    private void navigateToHome() {
        String url = config.getUrl();
        log.info("Navigating to: {}", url);
        driver.get(url);
        // Don't use waitForPageLoad() — prod site has stuck AJAX that blocks readyState
        // Instead wait for a key element to appear (Sign In button)
        try {
            new WebDriverWait(driver, Duration.ofSeconds(30)).until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//span[normalize-space()='Sign In'] | //a[contains(text(),'Sign In')]")));
        } catch (Exception e) {
            log.warn("Sign In not found after 30s, continuing anyway...");
        }
        safeSleep(1000);
    }

    private void closePopupIfPresent() {
        try {
            WebElement popup = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//i[@class='fa fa-times']")));
            popup.click();
            safeSleep(300);
        } catch (Exception e) {
            // No popup — continue
        }
    }

    /**
     * Dismiss "Confirm your participation in youth club as a member" popup.
     * Clicks "Accept" if present; if not found, ignores silently.
     * This popup appears intermittently after login for some users.
     */
    private void dismissYouthClubInvitationPopup() {
        try {
            WebElement acceptBtn = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[normalize-space()='Accept']"
                                    + " | //button[contains(text(),'Accept')]")));
            acceptBtn.click();
            log.info("✅ Youth Club invitation popup accepted");
            safeSleep(1000);
        } catch (Exception e) {
            // Popup not present — continue normally
            log.info("No Youth Club invitation popup — continuing");
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
            WebElement btn = driver.findElement(
                    By.cssSelector("button[class*='login_otp_header']"));
            jsClick(btn);
        }
        log.info("OTP sent to: {}", loginEmail);
    }

    private void fetchOTPFromMaildrop(String email) throws InterruptedException {
        log.info("Fetching OTP from Maildrop API for: {}", email);
        String mailbox = email.split("@")[0];
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
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("<strong>(\\d{4,6})</strong>").matcher(html);
                if (m.find()) {
                    otp = m.group(1);
                    break;
                }
                // Fallback pattern
                java.util.regex.Matcher m2 = java.util.regex.Pattern.compile("is\\s+(\\d{4,6})").matcher(html);
                if (m2.find()) {
                    otp = m2.group(1);
                    break;
                }
            }
            client.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch OTP from Maildrop API for: " + email, e);
        }

        if (otp == null) {
            throw new RuntimeException("OTP not received for: " + email);
        }

        log.info("OTP extracted: {}", otp);

        // Enter OTP in the OTP field
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
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

        // Dismiss any submit popup that appears after login
        dismissSubmitPopupIfPresent();
    }

    private void dismissSubmitPopupIfPresent() {
        safeSleep(3000); // Wait for popup to fully appear

        // Try multiple approaches to dismiss the popup
        try {
            // Approach 1: Click Submit button directly
            WebElement submitBtn = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[normalize-space()='Submit']")));
            jsClick(submitBtn);
            log.info("✅ Dismissed popup (Submit button)");
            safeSleep(2000);
            return;
        } catch (Exception ignored) {}

        try {
            // Approach 2: Click X/close button on the modal
            WebElement closeBtn = driver.findElement(
                    By.xpath("//button[@class='close' or @aria-label='Close']"
                            + " | //span[text()='×']/parent::*"
                            + " | //i[@class='fa fa-times']/parent::*"));
            jsClick(closeBtn);
            log.info("✅ Dismissed popup (X button)");
            safeSleep(1000);
            return;
        } catch (Exception ignored) {}

        try {
            // Approach 3: Click outside the modal to dismiss
            ((JavascriptExecutor) driver).executeScript(
                    "var modal = document.querySelector('.modal.show, .modal[style*=\"display: block\"]');" +
                    "if(modal) { $(modal).modal('hide'); }");
            log.info("✅ Dismissed popup (modal hide)");
            safeSleep(1000);
            return;
        } catch (Exception ignored) {}

        try {
            // Approach 4: Press Escape key
            driver.findElement(By.tagName("body")).sendKeys(org.openqa.selenium.Keys.ESCAPE);
            log.info("✅ Dismissed popup (Escape key)");
            safeSleep(1000);
        } catch (Exception ignored) {
            log.info("No popup present — continuing");
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
     * Picks a random email from VO_beta.xlsx or VO_prod.xlsx (based on env).
     * Same pattern as ELP which reads from Partner_beta.xlsx / Partner_prod.xlsx.
     */
    private String pickRandomEmailFromExcel() {
        String env = config.getEnv();
        String prefix = System.getProperty("voExcelPrefix", "VO");
        String fileName = prefix + "_" + env + ".xlsx";
        String path = System.getProperty("user.dir") + File.separator
                + "resources" + File.separator + fileName;

        log.info("Reading VO users from: {}", path);

        try (FileInputStream fis = new FileInputStream(path);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet("VO_Users");
            if (sheet == null) sheet = workbook.getSheetAt(0);

            List<String> emails = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && row.getCell(0) != null) {
                    String email = row.getCell(0).getStringCellValue().trim();
                    if (!email.isEmpty()) emails.add(email);
                }
            }

            if (emails.isEmpty()) {
                throw new RuntimeException("No emails found in " + fileName);
            }

            String selected = emails.get(random.nextInt(emails.size()));
            log.info("Randomly picked VO email: {}", selected);
            return selected;

        } catch (Exception e) {
            throw new RuntimeException("Failed to read " + fileName + ": " + e.getMessage(), e);
        }
    }

    private void safeSleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
