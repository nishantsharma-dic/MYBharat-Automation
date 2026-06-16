package com.mybharat.tests.youth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.Retry;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.youth.RegistrationCertificateVerificationPage;

/**
 * RegistrationCertificateVerificationTest - Verifies registration certificate download.
 *
 * Purpose: After profile completion, verifies that the user can download their
 *          registration certificate from the "My Certifications" section on the
 *          React profile page.
 *
 * Prerequisites: Runs on the SAME browser session after YouthProfileTest.
 *                User is logged in and on (or navigatable to) the profile page.
 *
 * Flow:
 *   1. Navigate to profile page → scroll to My Certifications section
 *   2. Click the "Registration Certificate" card → modal opens
 *   3. Click "Download PNG" → wait for file in downloads/ folder
 *   4. Assert file was downloaded successfully
 *   5. Close modal
 *
 * Key Methods:
 *   - verifyRegistrationCertificateDownload() — full download and verification flow
 *
 * Dependencies: BaseTest, RegistrationCertificateVerificationPage, TestListeners
 * Developer: Nishant Sharma (QA Team)
 *
 * @see RegistrationCertificateVerificationPage
 * @see YouthProfileTest
 */
@Listeners(TestListeners.class)
public class RegistrationCertificateVerificationTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(RegistrationCertificateVerificationTest.class);

    private RegistrationCertificateVerificationPage certPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        certPage = new RegistrationCertificateVerificationPage(driver);
    }

    @Test(priority = 1, groups = {"smoke", "certificate"}, retryAnalyzer = Retry.class,
          description = "Verify registration certificate: Scroll to My Certifications → Click card → Download PNG → Verify file exists in Downloads")
    public void verifyRegistrationCertificateDownload() throws Exception {
        log.info("Starting: Registration Certificate Verification");

        boolean downloaded = certPage.downloadCertificate();

        Assert.assertTrue(downloaded, "Certificate download should be triggered successfully");
        log.info("✅ Registration certificate download verified");
    }
}
