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
 * CertificateVerificationTest - Verifies registration certificate download.
 * 
 * Runs on the SAME browser session after YouthRegistrationTest.
 * The user is already logged in from registration.
 */
@Listeners(TestListeners.class)
public class RegistrationCertificateVerificationTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(RegistrationCertificateVerificationTest.class);

    private RegistrationCertificateVerificationPage certPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        certPage = new RegistrationCertificateVerificationPage(driver);
    }

    @Test(priority = 1, groups = {"smoke", "certificate"}, retryAnalyzer = Retry.class)
    public void verifyRegistrationCertificateDownload() throws Exception {
        log.info("Starting: Registration Certificate Verification for: {}", YouthRegistrationTest.registeredEmail);

        boolean downloaded = certPage.downloadCertificate();

        Assert.assertTrue(downloaded, "Certificate should be downloaded successfully");
        log.info("✅ Registration certificate verified and downloaded");
    }
}
