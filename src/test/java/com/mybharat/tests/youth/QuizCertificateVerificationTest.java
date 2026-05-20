package com.mybharat.tests.youth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mybharat.base.BaseTest;
import com.mybharat.listeners.Retry;
import com.mybharat.listeners.TestListeners;
import com.mybharat.pages.youth.QuizAttemptPage;

/**
 * QuizCertificateVerificationTest - Downloads quiz certificate after quiz completion.
 * 
 * Runs on the SAME browser session AFTER QuizAttemptTest.
 * Flow: Download certificate → Close certificate modal
 */
@Listeners(TestListeners.class)
public class QuizCertificateVerificationTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(QuizCertificateVerificationTest.class);

    private QuizAttemptPage quizPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        quizPage = new QuizAttemptPage(driver, "English");
    }

    @Test(priority = 1, groups = {"regression", "quiz"}, retryAnalyzer = Retry.class,
          description = "Download quiz play certificate and close the certificate modal")
    public void verifyQuizCertificateDownload() throws Exception {
        log.info("Starting: Quiz Certificate Verification");

        quizPage.downloadQuizCertificateAndClose();

        log.info("✅ Quiz certificate downloaded and modal closed");
    }
}
