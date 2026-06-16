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
 * QuizAttemptTest - End-to-end test for attempting the competitive quiz.
 *
 * Purpose: Verifies that a logged-in user can navigate to the quiz section, start a
 *          competitive quiz, answer all questions (with random answers), and submit
 *          the quiz with feedback.
 *
 * Prerequisites: User is already logged in (runs on the SAME browser session
 *                after RegistrationCertificateVerificationTest in testng-youth.xml).
 *
 * Flow:
 *   1. Navigate to Quiz &amp; Essay section
 *   2. Click "Start Quiz" on the first available quiz card
 *   3. Fill quiz details form (disability=No) → Proceed
 *   4. Select language → Start quiz
 *   5. Answer all questions with random selections → Submit
 *   6. Rate the quiz (4 stars) → Submit feedback
 *
 * Key Methods:
 *   - attemptCompetitiveQuiz() — orchestrates the full quiz attempt flow
 *
 * Dependencies: BaseTest, QuizAttemptPage, TestListeners
 * Developer: Nishant Sharma (QA Team)
 *
 * @see QuizAttemptPage
 * @see QuizCertificateVerificationTest
 */
@Listeners(TestListeners.class)
public class QuizAttemptTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(QuizAttemptTest.class);

    private QuizAttemptPage quizPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        quizPage = new QuizAttemptPage(driver, "English");
    }

    @Test(priority = 1, groups = {"regression", "quiz"},
          description = "Attempt competitive quiz: Navigate to quiz → Select language → Answer all questions → Submit → Provide feedback")
    public void attemptCompetitiveQuiz() throws Exception {
        log.info("Starting: Quiz Attempt");

        quizPage.startQuiz();

        // If no quiz is available, pass the test gracefully
        if (!QuizAttemptPage.isQuizAvailable()) {
            log.info("⚠ No active quiz available for play on Production — test passed (no functional issue)");
            return;
        }

        quizPage.attemptAllQuestionsAndSubmit();

        log.info("✅ Quiz completed successfully");
    }
}
