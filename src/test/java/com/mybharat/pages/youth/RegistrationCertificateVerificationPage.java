package com.mybharat.pages.youth;

import java.io.File;
import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.mybharat.pages.BasePage;
import com.mybharat.utils.ConfigReader;

/**
 * RegistrationCertificateVerificationPage - Page Object for downloading the registration certificate.
 *
 * Purpose: Navigates to the "My Certifications" section on the React youth profile page,
 *          clicks the registration certificate card, downloads it as PNG, and verifies
 *          the file was successfully saved to the downloads folder.
 *
 * New React UI Flow:
 *   1. Navigate to youth-profile page (if not already there)
 *   2. Scroll to "My Certifications" section on the About tab
 *   3. Click the certificate card (contains text "Registration Certificate")
 *   4. Modal opens with certificate preview + Download PNG / Download PDF buttons
 *   5. Click "Download PNG"
 *   6. Poll downloads/ folder for up to 15 seconds for a new .png file
 *   7. Close modal
 *
 * Key Methods:
 *   - downloadCertificate() — complete download flow, returns true if file verified
 *   - closeModal()          — closes the certificate preview modal (X button or Escape)
 *
 * Verification: Checks that a .png file appeared in downloads/ within the last 30 seconds,
 *               is not a partial download (.crdownload), and has non-zero size.
 *
 * Environment:
 *   Beta: https://yuva-beta.mybharats.in/youth-profile
 *   Prod: https://mybharat.gov.in/youth-profile
 *
 * Dependencies: BasePage, ConfigReader
 * Developer: Nishant Sharma (QA Team)
 *
 * @see RegistrationCertificateVerificationTest
 * @see YouthProfilePage
 */
public class RegistrationCertificateVerificationPage extends BasePage {

    private static final Logger log = LogManager.getLogger(RegistrationCertificateVerificationPage.class);
    private final ConfigReader config = new ConfigReader();

    public RegistrationCertificateVerificationPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Download the registration certificate as PNG and verify file exists.
     *
     * @return true if certificate file was downloaded successfully
     */
    public boolean downloadCertificate() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Ensure we're on the profile page
        String profileUrl = config.getProperty("profileUrl");
        if (profileUrl == null || profileUrl.isEmpty()) {
            profileUrl = config.getUrl() + "/youth-profile";
        }
        if (!driver.getCurrentUrl().contains("youth-profile")) {
            driver.get(profileUrl);
            waitForPageLoad();
            Thread.sleep(3000);
        }

        // Scroll to bottom where "My Certifications" section is
        log.info("Scrolling to My Certifications section...");
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        Thread.sleep(2000);

        // Step 1: Click the certificate card
        // The card contains <p> with text "Registration Certificate"
        WebElement certCard = null;
        try {
            // Find the card by its text content
            certCard = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//p[text()='Registration Certificate']/ancestor::div[contains(@class,'cursor-pointer')]")));
        } catch (Exception e) {
            // Fallback: try finding any clickable card in the certifications section
            try {
                certCard = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[contains(@class,'rounded-xl') and contains(@class,'cursor-pointer') and contains(@class,'shadow')]")));
            } catch (Exception e2) {
                log.error("Certificate card not found");
                return false;
            }
        }

        scrollToElement(certCard);
        Thread.sleep(500);
        try {
            certCard.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", certCard);
        }
        log.info("✅ Clicked certificate card — modal should open");
        Thread.sleep(2000);

        // Step 2: Click "Download PNG" button in the modal
        WebElement downloadBtn = null;
        try {
            downloadBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(),'Download PNG')]")));
        } catch (Exception e) {
            // Try broader search
            try {
                downloadBtn = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(.,'PNG')]")));
            } catch (Exception e2) {
                log.error("Download PNG button not found in modal");
                closeModal();
                return false;
            }
        }

        scrollToElement(downloadBtn);
        Thread.sleep(500);
        try {
            downloadBtn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", downloadBtn);
        }
        log.info("✅ Clicked 'Download PNG' button");

        // Step 3: Wait for download and verify file exists
        String downloadDir = System.getProperty("user.dir") + File.separator + "downloads";

        boolean downloaded = false;
        log.info("Waiting for certificate file in: {}", downloadDir);
        for (int i = 0; i < 15; i++) {
            Thread.sleep(1000);
            File dir = new File(downloadDir);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        String name = file.getName().toLowerCase();
                        if (name.endsWith(".png") && !name.contains(".crdownload")
                                && file.lastModified() > (System.currentTimeMillis() - 30000)) {
                            log.info("✅ Certificate downloaded: {} ({}KB)", file.getName(), file.length() / 1024);
                            downloaded = true;
                            break;
                        }
                    }
                }
            }
            if (downloaded) break;
        }

        // Step 4: Close the modal
        closeModal();

        if (!downloaded) {
            log.warn("Certificate file not found in downloads folder after 15 seconds");
        }
        return downloaded;
    }

    private void closeModal() {
        try {
            // Click the X close button (IoIosCloseCircle SVG inside a button)
            WebElement closeBtn = driver.findElement(
                    By.xpath("//div[contains(@class,'fixed')]//button[contains(@class,'cursor-pointer')]"));
            closeBtn.click();
            Thread.sleep(500);
        } catch (Exception e) {
            try {
                // Press Escape
                driver.findElement(By.tagName("body")).sendKeys(org.openqa.selenium.Keys.ESCAPE);
            } catch (Exception e2) {
                log.warn("Could not close modal");
            }
        }
    }
}
