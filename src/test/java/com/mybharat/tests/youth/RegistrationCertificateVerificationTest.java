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
 * Runs on the SAME browser session after YouthProfileTest.
 * The user is already logged in and on the profile page.
 * The certificate is in the "My Certifications" section on the About tab.
 * 
 * Flow: Scroll to certifications → Click card → Modal opens → Download PNG → Close modal
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
