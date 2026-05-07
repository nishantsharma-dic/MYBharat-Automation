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
 * QuizAttemptTest - Attempts the competitive quiz after registration.
 * 
 * Runs on the SAME browser session after RegistrationCertificateVerificationTest.
 * The user is already logged in.
 */
@Listeners(TestListeners.class)
public class QuizAttemptTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(QuizAttemptTest.class);

    private QuizAttemptPage quizPage;

    @BeforeClass(alwaysRun = true)
    public void initPages() {
        quizPage = new QuizAttemptPage(driver, "English");
    }

    @Test(priority = 1, groups = {"regression", "quiz"})
    public void attemptCompetitiveQuiz() throws Exception {
        log.info("Starting: Quiz Attempt");

        quizPage.startQuiz();
        quizPage.attemptAllQuestionsAndSubmit();

        log.info("✅ Quiz completed successfully");
    }
}
