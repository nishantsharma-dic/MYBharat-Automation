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
 * QuizCertificateVerificationTest - Verifies quiz certificate download after quiz completion.
 *
 * Purpose: After the user successfully completes a quiz (in QuizAttemptTest), this test
 *          downloads the quiz play certificate and closes the certificate modal.
 *
 * Prerequisites: QuizAttemptTest must have completed successfully in the same browser
 *                session. The certificate modal should be visible after quiz feedback.
 *
 * Flow:
 *   1. Wait for certificate download button to appear
 *   2. Click "Download" button
 *   3. Close the certificate modal
 *
 * Key Methods:
 *   - verifyQuizCertificateDownload() — downloads certificate and closes modal
 *
 * Dependencies: BaseTest, QuizAttemptPage, TestListeners
 * Developer: Nishant Sharma (QA Team)
 *
 * @see QuizAttemptPage
 * @see QuizAttemptTest
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

        // If no quiz was available to play, skip certificate download gracefully
        if (!QuizAttemptPage.isQuizAvailable()) {
            log.info("⚠ No quiz was played (quiz not available) — skipping certificate verification");
            return;
        }

        quizPage.downloadQuizCertificateAndClose();

        log.info("✅ Quiz certificate downloaded and modal closed");
    }
}
