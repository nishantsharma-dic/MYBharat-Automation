package com.mybharat.tests.youth;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.LandingPage;

/**
 * SpamEmailRestrictionTest - Verifies that registration is blocked for spam/dummy email domains.
 *
 * Purpose: Tests that the system properly rejects registration attempts using known
 *          spambot domains from the blocked domain list.
 *
 * Flow:
 *   1. Navigate to registration page (yuva_register)
 *   2. Enter email with spam domain (e.g., test@aminating.com)
 *   3. Click "Get OTP"
 *   4. Verify error/restriction message appears
 *
 * Run:
 *   mvn test -Denv=beta -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testSuites/testng-spam-email-restriction.xml
 *
 * Developer: QA Team
 */
@Listeners(TestListeners.class)
public class SpamEmailRestrictionTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(SpamEmailRestrictionTest.class);
    private LandingPage landingPage;
    private static final String SCREENSHOT_DIR = "/Volumes/For Mac/Projects/1.Reports/28 July/screenshots/";

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        landingPage = new LandingPage(driver);
        // Create screenshot directory
        new java.io.File(SCREENSHOT_DIR).mkdirs();
    }

    /**
     * DataProvider: returns a selection of spam domains to test (5 representative domains).
     */
    @DataProvider(name = "spamDomains")
    public Object[][] spamDomainsProvider() {
        return new Object[][] {
            { "aminating.com" },    // highest registrations (2806)
            { "adadad.uk" },        // 139 registrations
            { "any.pink" },         // 125 registrations
            { "aifotoeditor.com" }, // 124 registrations
            { "anowt.com" },        // 227 registrations
        };
    }

    @Test(priority = 1, dataProvider = "spamDomains", groups = {"smoke", "spam-restriction"},
          description = "Verify spam domain emails are blocked from registration on main page")
    public void testSpamEmailBlockedOnMainPage(String spamDomain) throws Exception {
        String testEmail = "testuser123@" + spamDomain;
        log.info("Testing spam email restriction for: {}", testEmail);

        // Step 1: Navigate to registration page
        driver.get("https://yuva-beta.mybharats.in/yuva_register");
        Thread.sleep(4000);

        // Close popup if present
        landingPage.closePopupIfPresent();
        Thread.sleep(1000);

        // Step 2: Click "Register" button to open the Sign Up modal
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        try {
            WebElement registerBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@class='btn btn_login lang_yuva_register_as_youth_btn fontchange']")));
            registerBtn.click();
            Thread.sleep(2000);
        } catch (Exception e) {
            log.info("Direct register button not found, trying Register Now → Register flow");
            try {
                WebElement registerNow = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//span[@class='fontchange' and contains(text(),'Register')]")));
                registerNow.click();
                Thread.sleep(1000);
                WebElement registerBtn2 = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(@class,'btn_login')]")));
                registerBtn2.click();
                Thread.sleep(2000);
            } catch (Exception e2) {
                log.info("Fallback: navigating directly and using JS to show modal");
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                        "document.getElementById('signUpModal') && (document.getElementById('signUpModal').classList.add('show'), document.getElementById('signUpModal').style.display='block');");
                Thread.sleep(2000);
            }
        }

        // Step 3: Wait for modal and enter spam domain email
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#signUpModal input#user_mobile, input#user_mobile")));
        emailInput.clear();
        emailInput.sendKeys(testEmail);

        // Step 4: Click Get OTP button
        WebElement getOtpBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button.generate_otp")));
        getOtpBtn.click();

        // Step 4: Wait for response and check for error/restriction message
        Thread.sleep(3000);

        // Take screenshot
        String screenshotName = "spam_restriction_main_" + spamDomain.replace(".", "_");
        takeScreenshotToReport(screenshotName);

        // Check for error/restriction indicators
        boolean isBlocked = checkIfEmailBlocked(wait);

        log.info("Domain: {} | Email: {} | Blocked: {}", spamDomain, testEmail, isBlocked);
        Assert.assertTrue(isBlocked,
                "Spam domain email " + testEmail + " should be BLOCKED from registration but was NOT restricted!");
    }

    /**
     * Checks if the email was blocked by looking for error messages or toast notifications.
     */
    private boolean checkIfEmailBlocked(WebDriverWait wait) {
        try {
            // Check for Toastify error toast
            List<WebElement> toasts = driver.findElements(By.cssSelector(".Toastify__toast--error, .Toastify__toast"));
            for (WebElement toast : toasts) {
                String text = toast.getText().toLowerCase();
                if (text.contains("block") || text.contains("restrict") || text.contains("not allowed")
                        || text.contains("invalid") || text.contains("spam") || text.contains("cannot")
                        || text.contains("denied") || text.contains("error") || text.contains("not valid")
                        || text.contains("disposable") || text.contains("temporary")) {
                    log.info("Found blocking toast: {}", toast.getText());
                    return true;
                }
            }

            // Check for any error/validation message on the page
            List<WebElement> errorMessages = driver.findElements(By.xpath(
                    "//*[contains(@class,'error') or contains(@class,'alert') or contains(@class,'invalid') " +
                    "or contains(@class,'danger') or contains(@class,'warning')]"));
            for (WebElement msg : errorMessages) {
                String text = msg.getText().toLowerCase();
                if (text.contains("block") || text.contains("restrict") || text.contains("not allowed")
                        || text.contains("invalid") || text.contains("spam") || text.contains("cannot")
                        || text.contains("denied") || text.contains("not valid")
                        || text.contains("disposable") || text.contains("temporary")) {
                    log.info("Found blocking message: {}", msg.getText());
                    return true;
                }
            }

            // Check if OTP field did NOT appear (meaning registration was blocked before OTP stage)
            List<WebElement> otpFields = driver.findElements(By.id("otp-field-1"));
            if (otpFields.isEmpty()) {
                // Also check: is there any visible error text anywhere?
                String pageSource = driver.getPageSource().toLowerCase();
                if (pageSource.contains("blocked") || pageSource.contains("not allowed")
                        || pageSource.contains("restricted") || pageSource.contains("invalid email")
                        || pageSource.contains("spam") || pageSource.contains("disposable")) {
                    log.info("Page source contains restriction keywords");
                    return true;
                }
            }

            // Check for any toast message (success toasts indicate OTP was sent = NOT blocked)
            List<WebElement> successToasts = driver.findElements(By.cssSelector(".Toastify__toast--success"));
            if (!successToasts.isEmpty()) {
                log.warn("OTP was sent successfully — email was NOT blocked");
                return false;
            }

            // If OTP field appeared, the email was NOT blocked
            if (!otpFields.isEmpty() && otpFields.get(0).isDisplayed()) {
                log.warn("OTP field appeared — email was NOT blocked");
                return false;
            }

            // If nothing happened (no toast, no OTP field), could indicate silent blocking
            // Wait a bit more and recheck
            Thread.sleep(2000);
            List<WebElement> anyToast = driver.findElements(By.cssSelector(".Toastify__toast"));
            if (!anyToast.isEmpty()) {
                String toastText = anyToast.get(0).getText();
                log.info("Toast message found: {}", toastText);
                // Any error-type response means blocked
                return !toastText.toLowerCase().contains("otp sent") && !toastText.toLowerCase().contains("success");
            }

            return false;

        } catch (Exception e) {
            log.error("Error checking if email blocked: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Takes a screenshot and saves to the report directory.
     */
    private void takeScreenshotToReport(String name) {
        try {
            java.io.File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            java.io.File dest = new java.io.File(SCREENSHOT_DIR + name + ".png");
            org.apache.commons.io.FileUtils.copyFile(source, dest);
            log.info("Screenshot saved: {}", dest.getAbsolutePath());
        } catch (Exception e) {
            log.warn("Failed to take screenshot: {}", e.getMessage());
        }
    }
}
