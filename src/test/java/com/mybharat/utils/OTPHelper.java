package com.mybharat.utils;

import java.time.Duration;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * OTPHelper - Centralized OTP fetching with Maildrop (primary) + Yopmail (fallback).
 *
 * Strategy:
 *   1. Try Maildrop API — fast, no browser tab, polls for 30s (8 attempts x 4s)
 *   2. If Maildrop fails → return null (caller handles Yopmail fallback)
 *
 * Yopmail fallback (called by the test when Maildrop returns null):
 *   - Opens Yopmail in new tab
 *   - Enters mailbox name
 *   - Reads OTP from iframe
 *   - Closes tab and returns OTP
 *
 * Usage:
 *   String otp = OTPHelper.fetchOTPFromMaildrop(mailbox);
 *   if (otp == null) {
 *       otp = OTPHelper.fetchOTPFromYopmail(driver, yopmailEmail);
 *   }
 */
public class OTPHelper {

    private static final Logger log = LogManager.getLogger(OTPHelper.class);

    // =========================================================================
    // MAILDROP API (Primary — fast, 30s timeout)
    // =========================================================================

    /**
     * Fetch OTP from Maildrop API. Quick poll — 8 attempts x 4s = ~32s max.
     * Returns null if OTP not received (caller should fallback to Yopmail).
     */
    public static String fetchOTPFromMaildrop(String mailbox) {
        return fetchOTPFromMaildrop(mailbox, 0);
    }

    /**
     * Fetch OTP from Maildrop API with prevCount logic.
     * Waits for a NEW message (inbox count > prevCount).
     * Returns null if OTP not received within timeout.
     */
    public static String fetchOTPFromMaildrop(String mailbox, int prevCount) {
        log.info("Trying Maildrop API for: {}@maildrop.cc (prevCount={})", mailbox, prevCount);

        try { Thread.sleep(3000); } catch (InterruptedException e) { /* skip */ }

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                org.apache.hc.client5.http.impl.classic.CloseableHttpClient client =
                        org.apache.hc.client5.http.impl.classic.HttpClients.createDefault();
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

                // Get inbox
                org.apache.hc.client5.http.classic.methods.HttpPost listReq =
                        new org.apache.hc.client5.http.classic.methods.HttpPost("https://api.maildrop.cc/graphql");
                listReq.setHeader("Content-Type", "application/json");
                listReq.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(
                        "{\"query\":\"{ inbox(mailbox:\\\"" + mailbox + "\\\") { id } }\"}"));
                String listResp = org.apache.hc.core5.http.io.entity.EntityUtils.toString(
                        client.execute(listReq).getEntity());

                com.fasterxml.jackson.databind.JsonNode inbox = mapper.readTree(listResp).path("data").path("inbox");

                if (inbox.size() <= prevCount) {
                    log.info("  Maildrop attempt {}/3 — no new email (count={})", attempt, inbox.size());
                    client.close();
                    Thread.sleep(4000);
                    continue;
                }

                // New message — fetch it
                String msgId = inbox.get(0).get("id").asText();
                org.apache.hc.client5.http.classic.methods.HttpPost msgReq =
                        new org.apache.hc.client5.http.classic.methods.HttpPost("https://api.maildrop.cc/graphql");
                msgReq.setHeader("Content-Type", "application/json");
                msgReq.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(
                        "{\"query\":\"{ message(mailbox:\\\"" + mailbox + "\\\", id:\\\"" + msgId + "\\\") { id html } }\"}"));
                String msgResp = org.apache.hc.core5.http.io.entity.EntityUtils.toString(
                        client.execute(msgReq).getEntity());
                client.close();

                String html = mapper.readTree(msgResp).path("data").path("message").path("html").asText();

                // Extract OTP
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("<strong>(\\d{6})</strong>").matcher(html);
                if (m.find()) {
                    log.info("  ✅ Maildrop OTP: {}", m.group(1));
                    return m.group(1);
                }
                java.util.regex.Matcher m2 = java.util.regex.Pattern.compile("(\\d{6})").matcher(html);
                if (m2.find()) {
                    log.info("  ✅ Maildrop OTP (fallback pattern): {}", m2.group(1));
                    return m2.group(1);
                }

                log.warn("  OTP pattern not found in email HTML");
                Thread.sleep(4000);
            } catch (Exception e) {
                log.warn("  Maildrop API error (attempt {}/3): {}", attempt, e.getMessage());
                try { Thread.sleep(4000); } catch (InterruptedException ie) { /* skip */ }
            }
        }

        log.warn("  Maildrop failed after 3 attempts — caller should use Yopmail fallback");
        return null;
    }

    // =========================================================================
    // YOPMAIL BROWSER (Fallback — opens new tab, reads from inbox)
    // =========================================================================

    /**
     * Fetch OTP from Yopmail by opening a new browser tab.
     * Uses the yopmail.com web interface to read the inbox.
     *
     * @param driver    WebDriver instance
     * @param mailbox   The email prefix (without @yopmail.com)
     * @return OTP string, or null if not found
     */
    public static String fetchOTPFromYopmail(WebDriver driver, String mailbox) {
        log.info("Fallback: Fetching OTP from Yopmail browser for: {}", mailbox);

        String originalWindow = driver.getWindowHandle();
        try {
            // Open new tab for Yopmail
            driver.switchTo().newWindow(WindowType.TAB);
            driver.get("https://yopmail.com/en/");
            Thread.sleep(2000);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            // Enter email prefix
            WebElement inbox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@id='login']")));
            inbox.clear();
            inbox.sendKeys(mailbox);

            // Click Go
            driver.findElement(By.cssSelector(".material-icons-outlined.f36")).click();
            Thread.sleep(3000);

            // Poll for OTP (refresh + read from iframe)
            String otp = null;
            for (int attempt = 1; attempt <= 10; attempt++) {
                try {
                    driver.findElement(By.xpath("//button[@id='refresh']")).click();
                    Thread.sleep(3000);

                    // Switch to mail iframe
                    driver.switchTo().frame("ifmail");
                    otp = extractOTPFromPage(driver);
                    driver.switchTo().defaultContent();

                    if (otp != null) {
                        log.info("  ✅ Yopmail OTP: {}", otp);
                        break;
                    }
                } catch (Exception e) {
                    driver.switchTo().defaultContent();
                    log.info("  Yopmail attempt {}/10 — not found yet", attempt);
                    Thread.sleep(3000);
                }
            }

            // Close Yopmail tab
            driver.close();
            driver.switchTo().window(originalWindow);
            Thread.sleep(500);

            return otp;
        } catch (Exception e) {
            log.error("Yopmail fallback failed: {}", e.getMessage());
            // Make sure we're back on original window
            try {
                if (driver.getWindowHandles().size() > 1) {
                    driver.close();
                }
                driver.switchTo().window(originalWindow);
            } catch (Exception ex) { /* best effort */ }
            return null;
        }
    }

    /**
     * Extract OTP from the current page/iframe content.
     */
    private static String extractOTPFromPage(WebDriver driver) {
        try {
            // Try finding OTP paragraph
            WebElement otpElement = driver.findElement(By.xpath(
                    "//p[contains(text(),'OTP') or contains(text(),'otp') or contains(text(),'one-time')]"));
            String text = otpElement.getText();

            // Pattern: "Your OTP is XXXXXX. This..."
            if (text.contains(" is ") && text.contains(".")) {
                String[] parts = text.split("\\. ")[0].split(" is ");
                if (parts.length > 1) {
                    String candidate = parts[1].trim();
                    if (candidate.matches("\\d{6}")) return candidate;
                }
            }
            // Generic 6-digit extraction
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d{6}").matcher(text);
            if (m.find()) return m.group();
        } catch (Exception e) { /* not found via paragraph */ }

        // Broader search: any 6-digit number in body
        try {
            String bodyText = driver.findElement(By.tagName("body")).getText();
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\b(\\d{6})\\b").matcher(bodyText);
            if (m.find()) return m.group(1);
        } catch (Exception e) { /* skip */ }

        return null;
    }

    // =========================================================================
    // COMBINED STRATEGY — Maildrop first, Yopmail fallback
    // =========================================================================

    /**
     * Complete OTP fetch with auto-fallback.
     * 1. Try Maildrop API (30s)
     * 2. If fails → re-enter yopmail email on form → fetch from Yopmail browser
     *
     * @param driver        WebDriver
     * @param emailInput    The email input element on the form
     * @param getOtpBtn     The "Get OTP" button element
     * @param maildropEmail The @maildrop.cc email already entered
     * @return Object[] {otp, finalEmail} — the OTP and which email was used
     */
    public static Object[] fetchOTPWithFallback(WebDriver driver, WebElement emailInput,
                                                 WebElement getOtpBtn, String maildropEmail) {
        String mailbox = maildropEmail.split("@")[0];

        // Try Maildrop first
        String otp = fetchOTPFromMaildrop(mailbox);
        if (otp != null) {
            return new Object[]{otp, maildropEmail};
        }

        // Maildrop failed — fallback to Yopmail
        log.warn("Maildrop failed, switching to Yopmail fallback...");
        String yopmailEmail = mailbox + "@yopmail.com";

        try {
            // Clear and re-enter with yopmail domain
            emailInput.clear();
            emailInput.sendKeys(yopmailEmail);
            Thread.sleep(500);

            // Click Get OTP again
            try {
                getOtpBtn.click();
            } catch (Exception e) {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", getOtpBtn);
            }
            Thread.sleep(2000);
            log.info("Re-requested OTP with Yopmail email: {}", yopmailEmail);

            // Fetch from Yopmail browser
            otp = fetchOTPFromYopmail(driver, mailbox);
            if (otp != null) {
                return new Object[]{otp, yopmailEmail};
            }
        } catch (Exception e) {
            log.error("Yopmail fallback failed: {}", e.getMessage());
        }

        return new Object[]{null, maildropEmail};
    }
}
